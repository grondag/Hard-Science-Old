package grondag.adversity;

import grondag.adversity.Configurator.Render.PreviewMode;
import grondag.adversity.init.ModKeys;
import grondag.adversity.library.render.QuadCache;
import grondag.adversity.library.varia.Useful;
import grondag.adversity.network.AdversityMessages;
import grondag.adversity.network.PacketReplaceHeldItem;
import grondag.adversity.superblock.placement.PlacementItem;
import grondag.adversity.superblock.placement.PlacementRenderer;
import grondag.adversity.superblock.varia.BlockHighlighter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
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
    
    private static int clientStatCounter = Configurator.RENDER.quadCacheStatReportingInterval * 20;
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) 
    {
        if (event.phase == TickEvent.Phase.END
                && Configurator.RENDER.enableQuadCacheStatistics
                && --clientStatCounter == 0) 
        {
            clientStatCounter = Configurator.RENDER.quadCacheStatReportingInterval * 20;
            Log.info("QuadCache stats = " + QuadCache.INSTANCE.cache.stats().toString());
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
                AdversityMessages.INSTANCE.sendToServer(new PacketReplaceHeldItem(stack));
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
                AdversityMessages.INSTANCE.sendToServer(new PacketReplaceHeldItem(stack));
                String message = I18n.translateToLocalFormatted("placement.message.mode",  I18n.translateToLocal("placement.mode." + ((PlacementItem)stack.getItem()).getMode(stack).toString().toLowerCase()));
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message));
            }
        }
        else if(ModKeys.PLACEMENT_PREVIEW.isPressed())
        {
            PreviewMode newMode = Useful.nextEnumValue(Configurator.RENDER.previewSetting);
            Configurator.RENDER.previewSetting = newMode;
            ConfigManager.sync(Adversity.MODID, Type.INSTANCE);
            String message = I18n.translateToLocalFormatted("placement.message.preview_set",  I18n.translateToLocal("placement.preview." + newMode.toString().toLowerCase()));
            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message));
        }
        else if(ModKeys.PLACEMENT_ROTATION.isPressed())
        {
            ItemStack stack = Minecraft.getMinecraft().player.getHeldItem(EnumHand.MAIN_HAND);
            if(stack.getItem() instanceof PlacementItem)
            {
                ((PlacementItem)stack.getItem()).cycleRotation(stack);
                AdversityMessages.INSTANCE.sendToServer(new PacketReplaceHeldItem(stack));
                String message = I18n.translateToLocalFormatted("placement.message.rotation",  I18n.translateToLocal("placement.rotation." + ((PlacementItem)stack.getItem()).getRotation(stack).toString().toLowerCase()));
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message));
            }
        }
    }
    
}
