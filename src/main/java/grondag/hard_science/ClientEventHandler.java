package grondag.hard_science;

import grondag.hard_science.Configurator.Render.PreviewMode;
import grondag.hard_science.init.ModItems;
import grondag.hard_science.init.ModKeys;
import grondag.hard_science.library.render.QuadCache;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.machines.base.MachineTileEntity;
import grondag.hard_science.network.ModMessages;
import grondag.hard_science.network.client_to_server.PacketReplaceHeldItem;
import grondag.hard_science.network.client_to_server.PacketUpdatePlacementKey;
import grondag.hard_science.player.ModPlayerCaps;
import grondag.hard_science.simulator.wip.OpenContainerStorageProxy;
import grondag.hard_science.superblock.block.SuperTileEntity;
import grondag.hard_science.superblock.placement.PlacementItem;
import grondag.hard_science.superblock.placement.PlacementRenderer;
import grondag.hard_science.superblock.texture.CompressedAnimatedSprite;
import grondag.hard_science.superblock.varia.BlockHighlighter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(Side.CLIENT)
@SideOnly(Side.CLIENT)
public class ClientEventHandler
{
    @SubscribeEvent()
    public static void renderWorldLastEvent(RenderWorldLastEvent event) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
        if (heldItem !=null && !heldItem.isEmpty() && (heldItem.getItem() instanceof PlacementItem)) 
        {
            PlacementItem placer = (PlacementItem) heldItem.getItem();
            PlacementRenderer.renderOverlay(event, player, heldItem, placer);
        }
    }

    /**
     * Check for blocks that need a custom block highlight and draw if found.
     * Adapted from the vanilla highlight code.
     */
    @SubscribeEvent
    public static void onDrawBlockHighlightEvent(DrawBlockHighlightEvent event) 
    {
        BlockHighlighter.handleDrawBlockHighlightEvent(event);
    }
    
    
    /** used to detect key down/up for modifier key */
    private static boolean isPlacementModifierPressed=false;
    
    private static int clientStatCounter = Configurator.RENDER.clientStatReportingInterval * 20;
    
    private static int cooldown = 0;
    
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) 
    {
        if(event.phase == Phase.START) 
        {
            CommonProxy.updateCurrentTime();
            
            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayerSP player = mc.player;
            
            if(player != null && player.world != null)
            {
                RayTraceResult mouseOver = ForgeHooks.rayTraceEyes(player, Configurator.MACHINES.machineMaxRenderDistance + 5);
                if(mouseOver != null && mouseOver.typeOfHit == RayTraceResult.Type.BLOCK)
                {
                    TileEntity te = mc.player.world.getTileEntity(mouseOver.getBlockPos());
                    if(te != null && te instanceof MachineTileEntity) ((MachineTileEntity)te).notifyInView();
                }
            }
           
            // render virtual blocks only if player is holding item that enables it
            boolean renderVirtual = Configurator.BLOCKS.alwaysRenderVirtualBlocks;
            if(!renderVirtual)
            {
                if(player != null)
                {
                    ItemStack stack = player.getHeldItemMainhand();
                    if(stack != null && stack.getItem() == ModItems.virtual_block) renderVirtual = true;
                    if(!renderVirtual)
                    {
                        stack = player.getHeldItemOffhand();
                        if(stack != null && stack.getItem() == ModItems.virtual_block) renderVirtual = true;
                    }
                }
            }
            if(renderVirtual != ClientProxy.isVirtualBlockRenderingEnabled())
            {
                ClientProxy.setVirtualBlockRenderingEnabled(renderVirtual);
            }
            
            boolean newDown;
            
            switch(Configurator.BLOCKS.placementModifier)
            {
            case ALT:
                newDown = GuiScreen.isAltKeyDown();
                break;
                
            case CONTROL:
                newDown =  GuiScreen.isCtrlKeyDown();
                break;
                
            case SHIFT:
            default:
                newDown = GuiScreen.isShiftKeyDown();
                break;
            }
            
            
            if(newDown != isPlacementModifierPressed)
            {
                isPlacementModifierPressed = newDown;
                ModPlayerCaps.setPlacementModifierOn(Minecraft.getMinecraft().player, newDown);
                ModMessages.INSTANCE.sendToServer(new PacketUpdatePlacementKey(newDown));
            }
            
            //FIXME: remove or cleanup
            if(cooldown == 0)
            {
                cooldown = OpenContainerStorageProxy.ITEM_PROXY.refreshListIfNeeded() ? 10 : 0;
            }
            else
            {
                cooldown--;
            }

        }
        else
        {
            if ((Configurator.RENDER.enableQuadCacheStatistics || Configurator.RENDER.enableAnimationStatistics)
                    && --clientStatCounter == 0) 
            {
                clientStatCounter = Configurator.RENDER.clientStatReportingInterval * 20;
                
                if(Configurator.RENDER.enableQuadCacheStatistics)
                {
                    Log.info("QuadCache stats = " + QuadCache.INSTANCE.cache.stats().toString());
                }
    
                if(Configurator.RENDER.enableAnimatedTextures && Configurator.RENDER.enableAnimationStatistics)
                {
                    CompressedAnimatedSprite.perfCollectorUpdate.outputStats();
                    CompressedAnimatedSprite.perfCollectorUpdate.clearStats();
                }
            }

        }
   
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event)
    {
        if (ModKeys.PLACEMENT_FACE.isPressed())
        {
            ItemStack stack = Minecraft.getMinecraft().player.getHeldItem(EnumHand.MAIN_HAND);
            if(stack.getItem() instanceof PlacementItem)
            {
                ((PlacementItem)stack.getItem()).cycleFace(stack);
                ModMessages.INSTANCE.sendToServer(new PacketReplaceHeldItem(stack));
                String message = I18n.translateToLocalFormatted("placement.message.orientation_set",  I18n.translateToLocal("placement.orientation." + ((PlacementItem)stack.getItem()).getFace(stack).toString().toLowerCase()));
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message));
            }
        } 
        else if (ModKeys.PLACEMENT_MODE.isPressed())
        {
            ItemStack stack = Minecraft.getMinecraft().player.getHeldItem(EnumHand.MAIN_HAND);
            if(stack.getItem() instanceof PlacementItem)
            {
                ((PlacementItem)stack.getItem()).cycleMode(stack);
                ModMessages.INSTANCE.sendToServer(new PacketReplaceHeldItem(stack));
                String message = I18n.translateToLocalFormatted("placement.message.mode",  I18n.translateToLocal("placement.mode." + ((PlacementItem)stack.getItem()).getMode(stack).toString().toLowerCase()));
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message));
            }
        }
        else if(ModKeys.PLACEMENT_PREVIEW.isPressed())
        {
            PreviewMode newMode = Useful.nextEnumValue(Configurator.RENDER.previewSetting);
            Configurator.RENDER.previewSetting = newMode;
            ConfigManager.sync(HardScience.MODID, Type.INSTANCE);
            String message = I18n.translateToLocalFormatted("placement.message.preview_set",  I18n.translateToLocal("placement.preview." + newMode.toString().toLowerCase()));
            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message));
        }
        else if(ModKeys.PLACEMENT_ROTATION.isPressed())
        {
            ItemStack stack = Minecraft.getMinecraft().player.getHeldItem(EnumHand.MAIN_HAND);
            if(stack.getItem() instanceof PlacementItem)
            {
                ((PlacementItem)stack.getItem()).cycleRotation(stack);
                ModMessages.INSTANCE.sendToServer(new PacketReplaceHeldItem(stack));
                String message = I18n.translateToLocalFormatted("placement.message.rotation",  I18n.translateToLocal("placement.rotation." + ((PlacementItem)stack.getItem()).getRotation(stack).toString().toLowerCase()));
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message));
            }
        }
    }
    
    @SubscribeEvent
    public static void onActionPerformed(ActionPerformedEvent.Pre event)
    {
        if(event.getGui() != null && event.getGui() instanceof GuiOptions )
        {
            SuperTileEntity.updateRenderDistance();
        }
    }
    
}
