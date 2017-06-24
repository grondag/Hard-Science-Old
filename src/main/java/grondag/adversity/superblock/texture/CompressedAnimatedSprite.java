package grondag.adversity.superblock.texture;

import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import org.apache.commons.io.IOUtils;
import grondag.adversity.Configurator;
import grondag.adversity.Log;
import grondag.adversity.library.concurrency.PerformanceCollector;
import grondag.adversity.library.concurrency.PerformanceCounter;
import grondag.adversity.library.varia.Useful;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

public class CompressedAnimatedSprite extends TextureAtlasSprite
{
    private static ExecutorService animationThread = Configurator.RENDER.enableAnimatedTextures ? Executors.newSingleThreadExecutor() : null;
    
    private Future<int[][]> nextFrame;
    
    /** set to false if error in encountered to stop future processing */
    private boolean isValid = true;
    
    public static final PerformanceCollector perfCollectorOnTick = new PerformanceCollector("Texture Animation On tick");
    public static final PerformanceCollector perfCollectorOffTick = new PerformanceCollector("Texture Animation  Off tick");
    private static final PerformanceCounter perfUpload = PerformanceCounter.create(Configurator.RENDER.enableAnimationStatistics, "Texture Upload", perfCollectorOnTick);
    private static final PerformanceCounter perfDecode = PerformanceCounter.create(Configurator.RENDER.enableAnimationStatistics, "Decode JPG", perfCollectorOffTick);
    private static final PerformanceCounter perfArray = PerformanceCounter.create(Configurator.RENDER.enableAnimationStatistics, "Populate Array", perfCollectorOffTick);
    private static final PerformanceCounter perfAlpha = PerformanceCounter.create(Configurator.RENDER.enableAnimationStatistics, "Construct Alpha Channel", perfCollectorOffTick);

    private static final String[] RESOLUTION_SUFFIX = {"a", "b", "c", "d", "e"};
    
    private static final ImageReader IMAGE_READER;
    
    static
    {
        Iterator<ImageReader> readers = ImageIO.getImageReadersBySuffix("jpg");
        IMAGE_READER = readers.hasNext() ? readers.next() : null;
    }

    /** dimensions are frame, mipmap level */
//    private ByteArrayInputStream[][] inputStreams;
    private byte[][][] inputBytes;
    
    private final int mipmapLevels;
    private final int ticksPerFrame;
    private int frameCount;
    
    
    private static int compressedBytes = 0;
    private static int vanillaBytes = 0;

    /** number of ticks waiting for frames that should have been ready for upload */
    private static int frameMissCount = 0;
    
    
    /** Frame execution logic */
    Callable<int[][]> animator = new Callable<int[][]>()
    {
        @Override
        public int[][] call() throws Exception
        {
            return getCurrentFrame();
        }    
     };
     
    public CompressedAnimatedSprite(ResourceLocation loc, int ticksPerFrame)
    {
        super(loc.toString());
        this.mipmapLevels = Minecraft.getMinecraft().gameSettings.mipmapLevels;
        this.ticksPerFrame = ticksPerFrame;
    }
 
    @Override
    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location)
    {
        return true;
    }

    @Override
    public boolean load(IResourceManager manager, ResourceLocation location)
    {
        try
        {
            // by default resource manager will give us a .png extension
            // we use .jpg for larger textures, so strip this off and go case by case
            String baseName = location.getResourcePath().substring(0, location.getResourcePath().length() - 4);
            
          
            
            this.setFramesTextureData(new ArrayList<int[][]>(1));
            // allows set(0) to work
            this.framesTextureData.add(null);
            
            this.frameCounter = 0;
            this.tickCounter = 0;

//            ArrayList<ByteArrayInputStream[]> inputs = new ArrayList<ByteArrayInputStream[]>(200);
            ArrayList<byte[][]> allFrameBytes = new ArrayList<byte[][]>(200);
            
            
            boolean keepGoing = true;
            int i = 0;
            while(keepGoing)
            {
//                ByteArrayInputStream[] streams = new ByteArrayInputStream[this.mipmapLevels + 1];
                byte[][] frameBytes = new byte[this.mipmapLevels + 1][];
                
                for(int j = 0; j <= this.mipmapLevels; j++)
                {
                    try
                    {
                        String suffix = RESOLUTION_SUFFIX[j];
                        ResourceLocation frameLoc = new ResourceLocation(location.getResourceDomain(), baseName.concat("_" + suffix + "_" + i + ".jpg"));
                        IResource frameResouce = manager.getResource(frameLoc);
                        
//                        streams[j] = new ByteArrayInputStream(IOUtils.toByteArray(frameResouce.getInputStream()));
                        frameBytes[j] = IOUtils.toByteArray(frameResouce.getInputStream());
//                        compressedBytes += streams[j].available();
                        compressedBytes += frameBytes[j].length;
                        
                        // confirm first frame loads
                        if(i == 0)
                        {
//                            BufferedImage firstFrame = decoder.decode(new ByteSourceArray(frameBytes[j]));

//                            BufferedImage firstFrame = TextureUtil.readBufferedImage(streams[j]);
                            IMAGE_READER.reset();
                            IMAGE_READER.setInput(new InMemoryImageInputStream(frameBytes[j]));
                            Raster firstFrame = IMAGE_READER.readRaster(0, null);
                            
                            // capture dimensions
                            if(j == 0)
                            {
                                this.width = firstFrame.getWidth();
                                this.height = firstFrame.getHeight();
                            }
                        }
                    }
                    catch(FileNotFoundException e)
                    {
                        keepGoing = false;
                        break;
                    }
                }
                
//                if(keepGoing) inputs.add(streams);
                if(keepGoing) allFrameBytes.add(frameBytes);
                i++;
            }
            
            if(allFrameBytes.size() == 0)
            {
                Log.error(String.format("Unable to load animated texture %s because textures files not found.", this.getIconName()));
                this.isValid = false;
                return true;
            }
            this.frameCount = allFrameBytes.size();
//            this.inputStreams = inputs2.toArray(new ByteArrayInputStream[this.frameCount][this.mipmapLevels + 1]);
            this.inputBytes = allFrameBytes.toArray(new byte[1][1][1]);
            
            vanillaBytes += (this.frameCount * this.width * this.height * 4 * 4 / 3);

            
            // Comments on super appear to be incorrect.
            // False causes us to be included in map,
            // which is what we want.
            return false;
        }
        catch (IOException e)
        {
            Log.error(String.format("Unable to load animated texture %s due to error.", this.getIconName()), e);
            this.isValid = false;
            return true;
        }
    }
    
    private int[][] getCurrentFrame()
    {
        perfArray.startRun();
        int[][] aint = new int[this.mipmapLevels + 1][];
        perfArray.endRun();
        
        try
        {
            for(int i =  0; i <= this.mipmapLevels; i++)
            {
                perfDecode.startRun();
//                BufferedImage bufferedimage = TextureUtil.readBufferedImage(this.inputStreams[this.frameCounter][i]);
//                BufferedImage bufferedimage = decoder.decode(new ByteSourceArray(this.inputBytes[this.frameCounter][i]));
                IMAGE_READER.reset();
                IMAGE_READER.setInput(new InMemoryImageInputStream(this.inputBytes[this.frameCounter][i]));
//                ImageTypeSpecifier spec = IMAGE_READER.getRawImageType(0);
                Raster raster = IMAGE_READER.readRaster(0, null);
                perfDecode.endRun();
                perfDecode.addCount(1);
                
//                BufferedImage testImmage = ImageIO.read(new InMemoryImageInputStream(this.inputBytes[this.frameCounter][i]));
//               BufferedImage testImmage = TextureUtil.readBufferedImage(new ByteArrayInputStream(this.inputBytes[this.frameCounter][i]));
                
                if(raster == null)
                {
                    Log.warn(String.format("Unable to load frame for animated texture %s. Texture will not animate.", this.getIconName()));
                    this.isValid = false;
                    return null;
                }
                else
                {
                    perfArray.startRun();
//                    aint[i] = new int[bufferedimage.getWidth() * bufferedimage.getHeight()];
                    final int size = raster.getWidth() * raster.getHeight();
                    int pixels[] = new int[size];
//                    bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), aint[i], 0, bufferedimage.getWidth());
                    perfArray.endRun();
                    perfArray.addCount(1);
                    
                    // restore alpha
                    perfAlpha.startRun();
                    int sourcePosition = 0;
                    int targetPosition = 0;
                    DataBuffer buff = raster.getDataBuffer();
                    while(targetPosition < size)
                    {

                        // very important that JPGs used for textures be encoded with RGB color space
                        // this avoids a color conversion step
                        int r = buff.getElem(sourcePosition++);
                        int g = buff.getElem(sourcePosition++);
                        int b = buff.getElem(sourcePosition++);
                        int alpha = Useful.max(r, g, b);
                        pixels[targetPosition++] = (alpha << 24) | (r << 16) | (g << 8) | b;
                    }
//                    for(int j = 0; j < aint[i].length; j++)
//                    {
//                        int p = aint[i][j];
//                        int alpha = Math.max(Math.max(p & 0xFF, (p >> 8) & 0xFF), (p >> 16) & 0xFF);
//                        aint[i][j] = alpha << 24 | (p & 0xFFFFFF);
//                    }
                    aint[i] = pixels;
                    perfAlpha.endRun();
                    perfAlpha.addCount(1);
                    
                }
            }
            return aint;
        }
        catch (IOException e)
        {
            Log.error(String.format("Unable to load frame for animated texture %s. Texture will not animate.", this.getIconName()), e);
            this.isValid = false;
            return null;
        }
    }
    
    @Override
    public void loadSpriteFrames(IResource resource, int mipmaplevels) throws IOException
    {
        //NOOP
    }

    @Override
    public int getFrameCount()
    {
        return frameCount;
    }

    @Override
    public void generateMipmaps(int level)
    {
        this.framesTextureData.set(0, this.getCurrentFrame());
        this.frameCounter++;
        if(Configurator.RENDER.enableAnimatedTextures)
        {
            this.nextFrame = animationThread.submit(this.animator);
        }
    }

    @Override
    public boolean hasAnimationMetadata()
    {
       return Configurator.RENDER.enableAnimatedTextures;
    }

    @Override
    public void updateAnimation()
    {
        if(this.isValid && animationThread != null)
        {
            perfUpload.startRun();
            ++this.tickCounter;
            if (this.tickCounter >= this.ticksPerFrame)
            {
                if(this.nextFrame.isDone())
                {
                    try
                    {
                        int[][] frameData = this.nextFrame.get();
                        if(frameData != null)
                        {
                            this.framesTextureData.set(0, frameData);
                            TextureUtil.uploadTextureMipmap(frameData, this.width, this.height, this.originX, this.originY, false, false);
                            perfUpload.addCount(1);
                        }

                        this.frameCounter = (this.frameCounter + 1) % frameCount;
                        this.tickCounter = 0;
                        this.nextFrame = animationThread.submit(this.animator);
                    }
                    catch(Exception e)
                    {
                        Log.error(String.format("Unable to load frame for animated texture %s. Texture will not animate.", this.getIconName()), e);
                        this.isValid = false;
                    }
                }
                else
                {
                    frameMissCount++;
                }
            }
            perfUpload.endRun();
        }
    }
    
    public static void reportMemoryUsage()
    {
        //don't output on first pass when we are empty
        if(vanillaBytes != 0)
        {
            Log.info("Animated texture memory consumption is " + (compressedBytes >> 20) + "MB");
            Log.info("Vanilla memory consumption for animated textures would be " + (vanillaBytes >> 20) + "MB");
            Log.info("In-memory compression for animated textures gives a " + 100L * (vanillaBytes - compressedBytes) / vanillaBytes + "% reduction in memory consumption.");
        }
    }

    public static int getAndClearFrameMissCount()
    {
        int result = frameMissCount;
        frameMissCount = 0;
        return result;
    }
  
}
