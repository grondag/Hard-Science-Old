package grondag.hard_science.superblock.texture;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;
import com.sun.imageio.plugins.jpeg.JPEGImageReaderSpi;

import grondag.hard_science.Configurator;
import grondag.hard_science.Log;
import grondag.hard_science.library.concurrency.PerformanceCollector;
import grondag.hard_science.library.concurrency.PerformanceCounter;
import grondag.hard_science.library.render.TextureHelper;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GLContext;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CompressedAnimatedSprite extends TextureAtlasSprite
{
    /** DO NOT ACCESS DIRECTLY.  Use {@link #getLoaderPool()} */
    private static volatile ThreadPoolExecutor loaderThreadPool;
    
    private static synchronized ThreadPoolExecutor getLoaderPool()
    {
        if(loaderThreadPool == null)
        {
            loaderThreadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        }
        return loaderThreadPool;
    }
    
    /** DO NOT ACCESS DIRECTLY!    */
    private static volatile JPEGImageReaderSpi jpegReader;
    
    private static synchronized JPEGImageReaderSpi getJpegReader()
    {
        if(jpegReader == null)
        {
            jpegReader = new JPEGImageReaderSpi();
        }
        return jpegReader;
    }
    
    /**
     * Releases resources no longer needed after texture bake.
     */
    public static void tearDown()
    {
        jpegReader = null;
        if(loaderThreadPool != null)
        {
            loaderThreadPool.shutdownNow();
            loaderThreadPool = null;
        }
    }
    
    public static final PerformanceCollector perfCollectorUpdate = new PerformanceCollector("Texture Animation Update");
    public static final PerformanceCollector perfCollectorLoad = new PerformanceCollector("Texture Animation Loading: Total Time");
    private static final PerformanceCounter perfUpdate = PerformanceCounter.create(Configurator.RENDER.enableAnimationStatistics, "Texture Animation Update", perfCollectorUpdate);
    public static final PerformanceCounter perfLoadRead = PerformanceCounter.create(Configurator.RENDER.enableAnimationStatistics, "Read Texture Files", perfCollectorLoad);
    public static final PerformanceCounter perfLoadProcessing = PerformanceCounter.create(Configurator.RENDER.enableAnimationStatistics, "Decode and Buffer Textures", perfCollectorLoad);
//    public static final ConcurrentPerformanceCounter perfLoadJpeg = new ConcurrentPerformanceCounter();
//    public static final ConcurrentPerformanceCounter perfLoadAlpha = new ConcurrentPerformanceCounter();
//    public static final ConcurrentPerformanceCounter perfLoadMipMap = new ConcurrentPerformanceCounter();
//    public static final ConcurrentPerformanceCounter perfLoadTransfer = new ConcurrentPerformanceCounter();
    
    private static int vanillaBytes = 0;

    
    private final int mipmapLevels;
    private final int ticksPerFrame;
    private int frameCount;
    
    /** 
     * Used when texture compression is disabled.
     * Dimensions are frame, mipmap level 
     */
    private IntBuffer[][] rawImageData;
    
    /** handles to compressed textures if texture compression is enabled */
    private int glCompressedTextureID[];

    /** set to false if error in encountered to stop future processing */
    private boolean isValid = true;

    /**
     * True if texture compression is available and enabled.
     */
    private final boolean isCompressed;
     
    public CompressedAnimatedSprite(ResourceLocation loc, int ticksPerFrame)
    {
        super(loc.toString());
        this.mipmapLevels = Minecraft.getMinecraft().gameSettings.mipmapLevels;
        this.ticksPerFrame = ticksPerFrame;
        this.isCompressed = Configurator.RENDER.enableAnimatedTextures 
                && Configurator.RENDER.enableAnimatedTextureCompression
                && GLContext.getCapabilities().GL_EXT_texture_compression_s3tc;
    }
 
    @Override
    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location)
    {
        return true;
    }

    @Override
    public boolean load(IResourceManager manager, ResourceLocation location, Function<ResourceLocation, TextureAtlasSprite> textureGetter)
    {
        perfLoadRead.startRun();
        
        ThreadPoolExecutor loaderPool = getLoaderPool();
        
        ExecutorCompletionService<Pair<Integer, int[][]>> runner = new ExecutorCompletionService<Pair<Integer, int[][]>>(loaderPool);
        
        JPEGImageReaderSpi jpeg = getJpegReader();
        
        // by default resource manager will give us a .png extension
        // we use .jpg for larger textures, so strip this off and go case by case
        String baseName = location.getResourcePath().substring(0, location.getResourcePath().length() - 4);
        
        this.setFramesTextureData(new ArrayList<int[][]>(1));
        // allows set(0) to work
        this.framesTextureData.add(null);
        
        this.frameCounter = 0;
        this.tickCounter = 0;
        
        int frameIndex = 0;
        boolean keepGoing = true;
        while(keepGoing)
        {
            try
            {
                ResourceLocation frameLoc = new ResourceLocation(location.getResourceDomain(), baseName.concat("_" + frameIndex + ".jpg"));

                // confirm first frame loads
                if(frameIndex == 0)
                {
                    ImageReader reader = jpeg.createReaderInstance();
                    reader.setInput(new MemoryCacheImageInputStream(manager.getResource(frameLoc).getInputStream()));
                    this.width = reader.getWidth(0);
                    this.height = reader.getHeight(0);
                    
                    if(this.width != this.height || this.width == 0)
                    {
                        Log.error(String.format("Unable to load animated texture %s because textures file did not contain a square image.", this.getIconName()));
                        this.isValid = false;
                        perfLoadRead.endRun();
                        return true;
                    }
                }
                
                // will throw IO Exception and we will abort if not found
                IResource frameResouce = manager.getResource(frameLoc);
                runner.submit(new FrameReader(frameResouce, frameIndex++));
                
                // only load the first texture if animation is disabled
                if(frameIndex == 1 && !Configurator.RENDER.enableAnimatedTextures)
                {
                    keepGoing = false;
                }
            }
            catch(Exception e)
            {
                keepGoing = false;
                break;
            }
        }
        
        perfLoadRead.endRun();
        perfLoadRead.addCount(1);
            
        if(frameIndex == 0)
        {
            Log.error(String.format("Unable to load animated texture %s because textures files not found.", this.getIconName()));
            this.isValid = false;
            return true;
        }
            
        perfLoadProcessing.startRun();
        try
        {
            
            this.frameCount = frameIndex;
            if(Configurator.RENDER.enableAnimatedTextureCompression)
            {
                this.glCompressedTextureID = new int[frameIndex];
            }
            else
            {
                this.rawImageData = new IntBuffer[frameIndex][];            
            }
            
            int completedFrameCount = 0;
            while(completedFrameCount < this.frameCount)
            {
                Future<Pair<Integer, int[][]>> result = runner.poll(200, TimeUnit.MILLISECONDS);
                
                Pair<Integer, int[][]> frameResult = result == null ? null : result.get();

//                long start = perfLoadTransfer.startRun();
                if(result == null || frameResult == null)
                {
                    if(loaderPool.getActiveCount() == 0)
                    {
                        Log.error(String.format("Unable to load animated texture due to unknown error.", this.getIconName()));
                        this.isValid = false;
                        perfLoadProcessing.endRun();
//                        perfLoadTransfer.endRun(start);
                        return true;
                    }
                }
                else
                {
                    if(frameResult.getLeft().intValue() == 0)
                    {
                        this.framesTextureData.set(0, frameResult.getRight());
                    }
                    
                    if(this.isCompressed)
                    {
                        this.glCompressedTextureID[frameResult.getLeft()] = TextureHelper.getCompressedTexture(frameResult.getRight(), this.width);
                    }
                    else
                    {
                        this.rawImageData[frameResult.getLeft()] = TextureHelper.getBufferedTexture(frameResult.getRight());
                    }
                    completedFrameCount++;
                }
//                perfLoadTransfer.endRun(start);
            }
            
            vanillaBytes += (this.frameCount * this.width * this.height * 4 * 4 / 3);

            perfLoadProcessing.endRun();
            perfLoadProcessing.addCount(1);
            
            // Comments on super appear to be incorrect.
            // False causes us to be included in map,
            // which is what we want.
            return false;
        }
        catch (Exception e)
        {
            Log.error(String.format("Unable to load animated texture %s due to error.", this.getIconName()), e);
            this.isValid = false;
            perfLoadProcessing.endRun();
            return true;
        }
    }
    
    private class FrameReader implements Callable<Pair<Integer, int[][]>>
    {
        private final IResource frameResouce;
        private final int frameIndex;

        private FrameReader(IResource frameResource, int frameIndex)
        {
            this.frameResouce = frameResource;
            this.frameIndex = frameIndex;
        }
        
        @Override
        public Pair<Integer, int[][]> call() throws Exception
        {
            try
            {
//                long start = perfLoadJpeg.startRun();
                
                JPEGImageReaderSpi jpeg = getJpegReader();
                
                byte[] frameData = IOUtils.toByteArray(frameResouce.getInputStream());
                ImageReader reader = jpeg.createReaderInstance();
                reader.setInput(new InMemoryImageInputStream(frameData));
                BufferedImage image = reader.read(0);
//                perfLoadJpeg.endRun(start);
                
                if(image == null)
                {
                    Log.warn(String.format("Unable to load frame for animated texture %s. Texture will not animate.", CompressedAnimatedSprite.this.getIconName()));
                    CompressedAnimatedSprite.this.isValid = false;
                    return null;
                }
                else
                {
//                    start = perfLoadAlpha.startRun();
                    final int size = CompressedAnimatedSprite.this.width * CompressedAnimatedSprite.this.height;
                    int pixels[] = new int[size];
                    
                    image.getRGB(0, 0, CompressedAnimatedSprite.this.width, CompressedAnimatedSprite.this.height, pixels, 0, CompressedAnimatedSprite.this.width);
                    
                    // restore alpha
                    for(int destIndex = 0; destIndex < size; destIndex++)
                    {
                        final int r = (pixels[destIndex] >> 16) & 0xFF;
                        final int g = (pixels[destIndex] >> 8) & 0xFF;
                        final int b = pixels[destIndex] & 0xFF;
                        
                        int alpha = Math.max(Math.max(r, g), b);
                        pixels[destIndex] = alpha << 24 | (r << 16) | (g << 8) | b;
                    }
//                    perfLoadAlpha.endRun(start);
                    
//                    start = perfLoadMipMap.startRun();
                    // generate mip maps
                    int[][] template = new int[CompressedAnimatedSprite.this.mipmapLevels + 1][];
                    template[0] = pixels;
                    int[][] result = TextureUtil.generateMipmapData(CompressedAnimatedSprite.this.mipmapLevels, CompressedAnimatedSprite.this.width, template);
//                    perfLoadMipMap.endRun(start);
                    
                    return Pair.of(this.frameIndex, result);
                }
            }
            catch (Exception e)
            {
                Log.error(String.format("Unable to load frame for animated texture %s. Texture will not animate.", CompressedAnimatedSprite.this.getIconName()), e);
                CompressedAnimatedSprite.this.isValid = false;
                return null;
            }
        }
    }
   
    
    @Override
    public void loadSpriteFrames(IResource resource, int mipmaplevels) throws IOException
    {
        //NOOP -  all handled during load
    }

    @Override
    public int getFrameCount()
    {
        return frameCount;
    }

    @Override
    public void generateMipmaps(int level)
    {
        // NOOP - all handled during load
    }

    @Override
    public boolean hasAnimationMetadata()
    {
       return Configurator.RENDER.enableAnimatedTextures;
    }

    @Override
    public void updateAnimation()
    {
        if(this.isValid && Configurator.RENDER.enableAnimatedTextures)
        {
            perfUpdate.startRun();
            ++this.tickCounter;
            if (this.tickCounter >= this.ticksPerFrame)
            {
                try
                {
                    this.frameCounter = (this.frameCounter + 1) % frameCount;
                    this.tickCounter = 0;

                    if(this.isCompressed)
                    {
                        TextureHelper.loadCompressedTextureFrame(this.glCompressedTextureID[frameCounter], this.mipmapLevels + 1, this.originX, this.originY, this.width);
                    }
                    else
                    {
                        TextureHelper.uploadTextureMipmap(this.rawImageData[frameCounter], this.width, this.height, this.originX, this.originY, false, false);
                    }
//                    this.currentFrame = this.rawImageData[this.frameCounter];
//                    this.framesTextureData.set(0, this.currentFrame);
//                    RenderThingy.uploadTextureMipmap(this.currentFrame, this.width, this.height, this.originX, this.originY, false, false);
                    perfUpdate.addCount(1);

                }
                catch(Exception e)
                {
                    Log.error(String.format("Unable to load frame for animated texture %s. Texture will not animate.", this.getIconName()), e);
                    this.isValid = false;
                }
            }
            perfUpdate.endRun();
        }
    }
    
    public static void reportMemoryUsage()
    {
        //don't output on first pass when we are empty
        if(Configurator.RENDER.enableAnimatedTextures)
        {
            if(vanillaBytes != 0)
            {
                if(Configurator.RENDER.enableAnimatedTextureCompression)
                {
                    Log.info("Animated texture memory consumption is " + (vanillaBytes >> 22) + "MB. (Compression enabled.)");
                }
                else
                {
                    Log.info("Animated texture memory consumption is " + (vanillaBytes >> 20) + "MB. (Compression disabled.)");
                }
            }
        }
        else
        {
            Log.info("Animated textures are disabled.");
        }
    }



  
}
