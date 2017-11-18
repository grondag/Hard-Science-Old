package grondag.hard_science;

import grondag.hard_science.Configurator.Render.PreviewMode;
import grondag.hard_science.init.ModItems;
import grondag.hard_science.init.ModKeys;
import grondag.hard_science.library.render.QuadCache;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.machines.base.MachineTileEntity;
import grondag.hard_science.network.ModMessages;
import grondag.hard_science.network.client_to_server.ConfigurePlacementItem;
import grondag.hard_science.network.client_to_server.PacketUpdateModifierKeys;
import grondag.hard_science.player.ModPlayerCaps;
import grondag.hard_science.simulator.machine.OpenContainerStorageProxy;
import grondag.hard_science.superblock.block.SuperTileEntity;
import grondag.hard_science.superblock.placement.PlacementHandler;
import grondag.hard_science.superblock.placement.PlacementItem;
import grondag.hard_science.superblock.placement.PlacementResult;
import grondag.hard_science.superblock.texture.CompressedAnimatedSprite;
import grondag.hard_science.superblock.varia.BlockHighlighter;
import grondag.hard_science.virtualblock.ExcavationRenderTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
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
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(Side.CLIENT)
@SideOnly(Side.CLIENT)
public class ClientEventHandler
{
    @SubscribeEvent()
    public static void onRenderTick(TickEvent.RenderTickEvent event)
    {
        if(event.phase == Phase.START) ClientProxy.updateCamera();
    }
    
    @SubscribeEvent()
    public static void renderWorldLastEvent(RenderWorldLastEvent event)
    {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        ItemStack stack = PlacementItem.getHeldPlacementItem(player);
        
        if(stack != null)
        {
            PlacementItem placer = (PlacementItem) stack.getItem();
            PlacementResult result = PlacementHandler.predictPlacementResults(player, stack, placer);
            if(result.builder() != null) result.builder().renderPreview(event, player);
        }
        
        ExcavationRenderTracker.INSTANCE.render(event.getContext(), event.getPartialTicks());
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
    
    
    /** used to detect key down/up for modifier keys */
    private static int modifierKeyFlags = 0;
    
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
       
            int keyFlags = (GuiScreen.isCtrlKeyDown() ? ModPlayerCaps.ModifierKey.CTRL_KEY.flag : 0) 
                     | (GuiScreen.isAltKeyDown() ? ModPlayerCaps.ModifierKey.ALT_KEY.flag : 0);
                    
            if(keyFlags != modifierKeyFlags)
            {
                modifierKeyFlags = keyFlags;
                ModPlayerCaps.setPlacementModifierFlags(Minecraft.getMinecraft().player, keyFlags);
                ModMessages.INSTANCE.sendToServer(new PacketUpdateModifierKeys(keyFlags));
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
        
        ItemStack stack = PlacementItem.getHeldPlacementItem(Minecraft.getMinecraft().player);
        
        if(stack != null)
        {
            PlacementItem item = (PlacementItem)stack.getItem();
            
            if(ModKeys.PLACEMENT_CYCLE_SELECTION_TARGET.isPressed())
            {
                item.cycleSelectionTargetRange(stack, false);
                ModMessages.INSTANCE.sendToServer(new ConfigurePlacementItem(stack));
                
                String message = item.isFloatingSelectionEnabled(stack)
                        ? I18n.translateToLocalFormatted("placement.message.range_floating",  item.getFloatingSelectionRange(stack))
                        : I18n.translateToLocal("placement.message.range_normal");
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message));
            }
            
            else if(ModKeys.PLACEMENT_CYCLE_REGION_ORIENTATION.isPressed())
            {
                item.cycleRegionOrientation(stack, false);
                ModMessages.INSTANCE.sendToServer(new ConfigurePlacementItem(stack));
                String message = I18n.translateToLocalFormatted("placement.message.orientation_region",  item.getRegionOrientation(stack).localizedName());
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message));
            }
                
            else if(ModKeys.PLACEMENT_CYCLE_BLOCK_ORIENTATION.isPressed())
            {
                item.cycleBlockOrientation(stack, false);
                ModMessages.INSTANCE.sendToServer(new ConfigurePlacementItem(stack));
                String message = I18n.translateToLocalFormatted("placement.message.orientation_block",  item.blockOrientationLocalizedName(stack));
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message));
            }
            else if (ModKeys.PLACEMENT_HISTORY_FORWARD.isPressed())
            {
                // TODO
            }
            else if (ModKeys.PLACEMENT_HISTORY_BACK.isPressed())
            {
                // TODO
            }
//            else if(ModKeys.PLACEMENT_TOGGLE_EXCAVATION.isPressed())
//            {
//                item.toggleDeleteMode(stack);
//                ModMessages.INSTANCE.sendToServer(new ConfigurePlacementItem(stack));
//                String message  = I18n.translateToLocal(item.isDeleteModeEnabled(stack) ? "placement.message.delete_on" : "placement.message.delete_off");
//                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message));
//            }
            else if(ModKeys.PLACEMENT_DISPLAY_GUI.isPressed())
            {
                item.displayGui(Minecraft.getMinecraft().player);
            }
            else if (ModKeys.PLACEMENT_CYCLE_FILTER_MODE.isPressed())
            {
                item.cycleFilterMode(stack, false);
                ModMessages.INSTANCE.sendToServer(new ConfigurePlacementItem(stack));
                String message = I18n.translateToLocalFormatted("placement.message.filter_mode",  item.getFilterMode(stack).localizedName());
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message));
            }
            else if(ModKeys.PLACEMENT_CYCLE_SPECIES_HANDLING.isPressed())
            {
                item.cycleSpeciesMode(stack, false);
                ModMessages.INSTANCE.sendToServer(new ConfigurePlacementItem(stack));
                String message = I18n.translateToLocalFormatted("placement.message.species_mode",  item.getSpeciesMode(stack).localizedName());
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message));
            }
            else if(ModKeys.PLACEMENT_CYCLE_TARGET_MODE.isPressed())
            {
                item.cycleTargetMode(stack, false);
                ModMessages.INSTANCE.sendToServer(new ConfigurePlacementItem(stack));
                String message = I18n.translateToLocalFormatted("placement.message.target_mode",  item.getTargetMode(stack).localizedName());
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message));
                
            }
            else if(ModKeys.PLACEMENT_PREVIEW.isPressed())
            {
                PreviewMode newMode = Useful.nextEnumValue(Configurator.RENDER.previewSetting);
                Configurator.RENDER.previewSetting = newMode;
                ConfigManager.sync(HardScience.MODID, Type.INSTANCE);
                String message = I18n.translateToLocalFormatted("placement.message.preview_set",  I18n.translateToLocal("placement.preview." + newMode.toString().toLowerCase()));
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message));
            }
            else if(ModKeys.PLACEMENT_TOGGLE_FIXED_REGION.isPressed())
            {
                item.toggleFixedRegionEnabled(stack);
                ModMessages.INSTANCE.sendToServer(new ConfigurePlacementItem(stack));
                String message  = I18n.translateToLocal(item.isFixedRegionEnabled(stack) ? "placement.message.fixed_region_on" : "placement.message.fixed_region_off");
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
