package grondag.adversity.superblock.texture;

import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;
import com.sun.imageio.plugins.jpeg.JPEGImageReaderSpi;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GLContext;

import grondag.adversity.Configurator;
import grondag.adversity.Log;
//import grondag.adversity.library.concurrency.ConcurrentPerformanceCounter;
import grondag.adversity.library.concurrency.PerformanceCollector;
import grondag.adversity.library.concurrency.PerformanceCounter;
import grondag.adversity.library.render.TextureHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CompressedAnimatedSprite extends TextureAtlasSprite
{
    /** DO NOT ACCESS DIRECTLY.  Use {@link #getLoaderPool()} */
    private static ThreadPoolExecutor loaderThreadPool;
    
    private static ThreadPoolExecutor getLoaderPool()
    {
        if(loaderThreadPool == null)
        {
            loaderThreadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        }
        return loaderThreadPool;
    }
    
    /** DO NOT ACCESS DIRECTLY!    */
    private static JpegHelper jpegHelper;
    
    private static JpegHelper getJpegHelper()
    {
        if(jpegHelper == null)
        {
            jpegHelper = new JpegHelper();
        }
        return jpegHelper;
    }
    
    /**
     * Releases resources no longer needed after texture bake.
     */
    public static void tearDown()
    {
        jpegHelper = null;
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

    /**
     * Enables fast color conversion from JPEG to RBG by
     * taking a more efficient code path than native JPEG reader.
     */
    private static class JpegHelper
    {
        
        private final JPEGImageReaderSpi JPEG_IMAGE_READER_PROVIDER = new JPEGImageReaderSpi();
        
        private final byte[] YCbB_OFFSETS = new byte[0xFF];
        private final byte[] YCbG_OFFSETS = new byte[0xFF];
        private final byte[] YCrG_OFFSETS = new byte[0xFF];
        private final byte[] YCrR_OFFSETS = new byte[0xFF];
        
        private JpegHelper()
        {
            for(int i = 0; i < 0xFF; i++)
            {
                YCbB_OFFSETS[i] = (byte) Math.round(1.402 * (i-128));
                YCbG_OFFSETS[i] = (byte) Math.round(-0.34416 * (i-128));
                YCrG_OFFSETS[i] = (byte) Math.round(-0.71414136 * (i-128));
                YCrR_OFFSETS[i] = (byte) Math.round(1.772* (i-128));
            }
        }
    }
    
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
    public boolean load(IResourceManager manager, ResourceLocation location)
    {
        perfLoadRead.startRun();
        
        ExecutorCompletionService<Pair<Integer, int[][]>> runner = new ExecutorCompletionService<Pair<Integer, int[][]>>(getLoaderPool());
        
        JpegHelper jpeg = getJpegHelper();
        
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
                    ImageReader reader = jpeg.JPEG_IMAGE_READER_PROVIDER.createReaderInstance();
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
                Pair<Integer, int[][]> frameResult = runner.poll(200, TimeUnit.MILLISECONDS).get();

//                long start = perfLoadTransfer.startRun();
                if(frameResult == null)
                {
                    if(loaderThreadPool.getActiveCount() == 0)
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
                
                JpegHelper jpeg = getJpegHelper();
                
                byte[] frameData = IOUtils.toByteArray(frameResouce.getInputStream());
                ImageReader reader = jpeg.JPEG_IMAGE_READER_PROVIDER.createReaderInstance();
                reader.setInput(new InMemoryImageInputStream(frameData));
                Raster raster = reader.readRaster(0, null);
//                perfLoadJpeg.endRun(start);
                
                if(raster == null)
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
                    int sourceIndex = 0;
                    
                    
                    
                    byte[] rawBuffer = ((DataBufferByte)raster.getDataBuffer()).getData();
                    for(int destIndex = 0; destIndex < size; destIndex++)
                    {
                        // convert from JPEG YCbCr representation to RGB

                        final int y = rawBuffer[sourceIndex++] & 0xFF;
                        final int cb = rawBuffer[sourceIndex++] & 0xFF;
                        final int cr = rawBuffer[sourceIndex++] & 0xFF;
                        
                        final int r = MathHelper.clamp(jpeg.YCrR_OFFSETS[cr] + y, 0, 0xFF);
                        final int g = MathHelper.clamp(jpeg.YCrG_OFFSETS[cr] + jpeg.YCbG_OFFSETS[cb] + y, 0, 0xFF);
                        final int b = MathHelper.clamp(jpeg.YCbB_OFFSETS[cb] + y, 0, 0xFF);
                        
                        // restore alpha
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
