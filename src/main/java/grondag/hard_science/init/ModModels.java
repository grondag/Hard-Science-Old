package grondag.hard_science.init;

import grondag.artbox.ArtBoxTextures;
import grondag.hard_science.gui.control.machine.BinaryReference;
import grondag.hard_science.machines.impl.building.BlockFabricatorTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
@SuppressWarnings("null")
public class ModModels
{
    
    public static TextureAtlasSprite SPRITE_REDSTONE_TORCH_LIT;
    public static TextureAtlasSprite SPRITE_REDSTONE_TORCH_UNLIT;
    
    public static BinaryReference<TextureAtlasSprite> TEX_MACHINE_ON_OFF;
    
    public static final int COLOR_POWER = 0xFFFFBF;
    public static final int COLOR_BATTERY = 0x00B1FF;
    public static final int COLOR_BATTERY_DRAIN = 0xff4e00;
    public static final int COLOR_FUEL_CELL = 0xFC8D59;
    public static final int COLOR_FAILURE = 0xFFFF20;
    public static final int COLOR_NO = 0xB30000;
    
    @SubscribeEvent
    public static void stitcherEventPost(TextureStitchEvent.Post event)
    {
        
        TEX_MACHINE_ON_OFF = new BinaryReference<TextureAtlasSprite>(
                ArtBoxTextures.MACHINE_POWER_ON.getSampleSprite(),
                ArtBoxTextures.MACHINE_POWER_OFF.getSampleSprite());
        
//        TEX_LINEAR_GAUGE_LEVEL = loadNonBlockTexture("hard_science:textures/blocks/linear_level_128.png");
//        TEX_LINEAR_GAUGE_MARKS = loadNonBlockTexture("hard_science:textures/blocks/linear_marks_128.png");
//        TEX_LINEAR_POWER_LEVEL = loadNonBlockTexture("hard_science:textures/blocks/linear_power_128.png");
        
        SPRITE_REDSTONE_TORCH_LIT   = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/redstone_torch_on");
        SPRITE_REDSTONE_TORCH_UNLIT = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/redstone_torch_off");
         
        BlockFabricatorTileEntity.initRenderSpecs();

    }
    
    

    
}
