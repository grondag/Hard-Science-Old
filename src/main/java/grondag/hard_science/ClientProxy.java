package grondag.hard_science;

import grondag.hard_science.gui.ModGuiHandler;
import grondag.hard_science.init.ModKeys;
import grondag.hard_science.init.ModModels;
import grondag.hard_science.superblock.block.SuperTileEntity;
import grondag.hard_science.superblock.color.BlockColorMapProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.client.model.animation.Animation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{

    private static boolean isVirtualBlockRenderingEnabled = false;

    /**
     * Nulled out at start of each render and then initialized if needed.
     * Allows reuse whereever needed
     */
    private static ICamera camera;
    private static double cameraX;
    private static double cameraY;
    private static double cameraZ;

    private static void refreshCamera()
    {
        Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
        if(entity == null) return;

        float partialTicks = Animation.getPartialTickTime();
        
        ICamera newCam = new Frustum();
        double d0 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
        double d1 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
        double d2 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;
        newCam.setPosition(d0, d1, d2);
        cameraX = d0;
        cameraY = d1;
        cameraZ = d2;
        camera = newCam;
    }

    public static void updateCamera()
    {
        camera = null;
    }

    public static ICamera camera()
    {
        if(camera == null) refreshCamera();
        return camera;
    }

    public static double cameraX()
    {
        if(camera == null) refreshCamera();
        return cameraX;
    }
    
    public static double cameraY()
    {
        if(camera == null) refreshCamera();
        return cameraY;
    }
    
    public static double cameraZ()
    {
        if(camera == null) refreshCamera();
        return cameraZ;
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) 
    {
        super.preInit(event);
        ModModels.preInit(event);

        if(Configurator.RENDER.debugOutputColorAtlas)
        {
            BlockColorMapProvider.writeColorAtlas(event.getModConfigurationDirectory());
        }
    }

    @Override
    public void init(FMLInitializationEvent event) 
    {
        super.init(event);
        ModKeys.init(event);
        ModModels.init(event);
        NetworkRegistry.INSTANCE.registerGuiHandler(HardScience.INSTANCE, new ModGuiHandler());
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) 
    {
        super.postInit(event);
        SuperTileEntity.updateRenderDistance();
    }

    @Override
    public boolean allowCollisionWithVirtualBlocks(World world)
    {
        // uses client proxy when running local so still have to check world for side
        if(world == null)
        {
            return isVirtualBlockRenderingEnabled && FMLCommonHandler.instance().getEffectiveSide() ==  Side.CLIENT;
        }
        else
        {
            return world.isRemote && isVirtualBlockRenderingEnabled;
        }
    }

    public static void setVirtualBlockRenderingEnabled(boolean isEnabled)
    {
        isVirtualBlockRenderingEnabled = isEnabled;
    }

    public static boolean isVirtualBlockRenderingEnabled()
    {
        return isVirtualBlockRenderingEnabled;
    }
}
