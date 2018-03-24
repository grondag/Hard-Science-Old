package grondag.hard_science;

import java.io.IOException;
import java.util.Map;

import grondag.exotic_matter.ConfigXM;
import grondag.exotic_matter.ConfigXM.Render.PreviewMode;
import grondag.exotic_matter.block.SuperBlock;
import grondag.exotic_matter.block.SuperBlockStackHelper;
import grondag.exotic_matter.block.SuperDispatcher;
import grondag.exotic_matter.block.SuperModelBlock;
import grondag.exotic_matter.block.SuperModelTileEntity;
import grondag.exotic_matter.model.varia.CraftingItem;
import grondag.exotic_matter.network.PacketHandler;
import grondag.exotic_matter.render.CompressedAnimatedSprite;
import grondag.exotic_matter.render.QuadCache;
import grondag.exotic_matter.varia.Useful;
import grondag.hard_science.init.ModKeys;
import grondag.hard_science.machines.base.MachineTESR;
import grondag.hard_science.machines.base.MachineTileEntity;
import grondag.hard_science.machines.base.MachineTileEntityTickable;
import grondag.hard_science.machines.impl.building.BlockFabricatorTESR;
import grondag.hard_science.machines.impl.building.BlockFabricatorTileEntity;
import grondag.hard_science.machines.impl.logistics.ChemicalBatteryTESR;
import grondag.hard_science.machines.impl.logistics.ChemicalBatteryTileEntity;
import grondag.hard_science.machines.impl.processing.DigesterTESR;
import grondag.hard_science.machines.impl.processing.DigesterTileEntity;
import grondag.hard_science.machines.impl.processing.MicronizerTESR;
import grondag.hard_science.machines.impl.processing.MicronizerTileEntity;
import grondag.hard_science.machines.support.OpenContainerStorageProxy;
import grondag.hard_science.matter.BulkItem;
import grondag.hard_science.matter.MatterCube;
import grondag.hard_science.matter.MatterCubeItemModel;
import grondag.hard_science.matter.MatterCubeItemModel1;
import grondag.hard_science.network.client_to_server.PacketConfigurePlacementItem;
import grondag.hard_science.network.client_to_server.PacketSimpleAction;
import grondag.hard_science.network.client_to_server.PacketUpdateModifierKeys;
import grondag.hard_science.player.ModPlayerCaps;
import grondag.hard_science.superblock.block.SuperItemBlock;
import grondag.hard_science.superblock.blockmovetest.PlacementHandler;
import grondag.hard_science.superblock.blockmovetest.PlacementItem;
import grondag.hard_science.superblock.blockmovetest.PlacementResult;
import grondag.hard_science.superblock.virtual.ExcavationRenderManager;
import grondag.hard_science.superblock.virtual.VirtualItemBlock;
import grondag.hard_science.superblock.virtual.VirtualTESR;
import grondag.hard_science.superblock.virtual.VirtualTileEntityTESR;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(Side.CLIENT)
@SideOnly(Side.CLIENT)
public class ClientEventHandler
{
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
        
        ExcavationRenderManager.render(event.getContext(), event.getPartialTicks());
    }

    /** used to detect key down/up for modifier keys */
    private static int modifierKeyFlags = 0;
    
    private static int clientStatCounter = ConfigXM.RENDER.clientStatReportingInterval * 20;
    
    private static int cooldown = 0;
    
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) 
    {
        if(event.phase == Phase.START) 
        {
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
           
       
            int keyFlags = (GuiScreen.isCtrlKeyDown() ? ModPlayerCaps.ModifierKey.CTRL_KEY.flag : 0) 
                     | (GuiScreen.isAltKeyDown() ? ModPlayerCaps.ModifierKey.ALT_KEY.flag : 0);
                    
            if(keyFlags != modifierKeyFlags)
            {
                modifierKeyFlags = keyFlags;
                ModPlayerCaps.setPlacementModifierFlags(Minecraft.getMinecraft().player, keyFlags);
                PacketHandler.CHANNEL.sendToServer(new PacketUpdateModifierKeys(keyFlags));
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
            if ((ConfigXM.RENDER.enableQuadCacheStatistics || ConfigXM.RENDER.enableAnimationStatistics)
                    && --clientStatCounter == 0) 
            {
                clientStatCounter = ConfigXM.RENDER.clientStatReportingInterval * 20;
                
                if(ConfigXM.RENDER.enableQuadCacheStatistics)
                {
                    Log.info("QuadCache stats = " + QuadCache.INSTANCE.cache.stats().toString());
                }
    
                if(ConfigXM.RENDER.enableAnimatedTextures && ConfigXM.RENDER.enableAnimationStatistics)
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
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack stack = PlacementItem.getHeldPlacementItem(mc.player);
        
        if(stack != null)
        {
            PlacementItem item = (PlacementItem)stack.getItem();
            
            // If holding a virtual block and click pick block, 
            // change appearance of held block to match picked block
            if(item instanceof VirtualItemBlock && mc.gameSettings.keyBindPickBlock.isPressed())
            {
                
                if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK)
                {
                    IBlockState blockState = mc.player.world.getBlockState(mc.objectMouseOver.getBlockPos());
                    if(blockState.getBlock() instanceof SuperModelBlock)
                    {
                        SuperModelTileEntity smte = (SuperModelTileEntity)mc.player.world.getTileEntity(mc.objectMouseOver.getBlockPos());
                        if(smte != null)
                        {
                            SuperBlockStackHelper.setStackModelState(stack, smte.getModelState());
                            SuperBlockStackHelper.setStackLightValue(stack, smte.getLightValue());
                            SuperBlockStackHelper.setStackSubstance(stack, smte.getSubstance());
                            PacketHandler.CHANNEL.sendToServer(new PacketConfigurePlacementItem(stack));
                            
                            // prevent vanilla pick block 
                            KeyBinding.unPressAllKeys();
                        }
                    }
                }
            }

            
            if(ModKeys.PLACEMENT_CYCLE_SELECTION_TARGET.isPressed() && item.cycleSelectionTargetRange(stack, false))
            {
                PacketHandler.CHANNEL.sendToServer(new PacketConfigurePlacementItem(stack));
                
                String message = item.isFloatingSelectionEnabled(stack)
                        ? I18n.translateToLocalFormatted("placement.message.range_floating",  item.getFloatingSelectionRange(stack))
                        : I18n.translateToLocal("placement.message.range_normal");
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message));
                return;
            }
            
            else if(ModKeys.PLACEMENT_CYCLE_REGION_ORIENTATION.isPressed() && item.cycleRegionOrientation(stack, false))
            {
                PacketHandler.CHANNEL.sendToServer(new PacketConfigurePlacementItem(stack));
                String message = I18n.translateToLocalFormatted("placement.message.orientation_region",  item.getRegionOrientation(stack).localizedName());
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message));
                return;
            }
                
            else if(ModKeys.PLACEMENT_CYCLE_BLOCK_ORIENTATION.isPressed() && item.cycleBlockOrientation(stack, false))
            {
                PacketHandler.CHANNEL.sendToServer(new PacketConfigurePlacementItem(stack));
                String message = I18n.translateToLocalFormatted("placement.message.orientation_block",  item.blockOrientationLocalizedName(stack));
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message));
                return;
            }
            
            else if (ModKeys.PLACEMENT_HISTORY_FORWARD.isPressed())
            {
                // TODO
                return;
            }
            
            else if (ModKeys.PLACEMENT_HISTORY_BACK.isPressed())
            {
                // TODO
                return;
            }

            else if(ModKeys.PLACEMENT_DISPLAY_GUI.isPressed() && item.isGuiSupported(stack))
            {
                item.displayGui(Minecraft.getMinecraft().player);
                return;
            }
            
            else if (ModKeys.PLACEMENT_CYCLE_FILTER_MODE.isPressed() && item.cycleFilterMode(stack, false))
            {
                PacketHandler.CHANNEL.sendToServer(new PacketConfigurePlacementItem(stack));
                String message = I18n.translateToLocalFormatted("placement.message.filter_mode",  item.getFilterMode(stack).localizedName());
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message));
                return;
            }
            
            else if(ModKeys.PLACEMENT_CYCLE_SPECIES_HANDLING.isPressed() && item.cycleSpeciesMode(stack, false))
            {
                PacketHandler.CHANNEL.sendToServer(new PacketConfigurePlacementItem(stack));
                String message = I18n.translateToLocalFormatted("placement.message.species_mode",  item.getSpeciesMode(stack).localizedName());
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message));
                return;
            }
            
            else if(ModKeys.PLACEMENT_CYCLE_TARGET_MODE.isPressed() && item.cycleTargetMode(stack, false))
            {
                PacketHandler.CHANNEL.sendToServer(new PacketConfigurePlacementItem(stack));
                String message = I18n.translateToLocalFormatted("placement.message.target_mode",  item.getTargetMode(stack).localizedName());
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message));
                return;
            }
            
            else if(ModKeys.PLACEMENT_PREVIEW.isPressed())
            {
                PreviewMode newMode = Useful.nextEnumValue(ConfigXM.RENDER.previewSetting);
                ConfigXM.RENDER.previewSetting = newMode;
                ConfigManager.sync(HardScience.MODID, Type.INSTANCE);
                String message = I18n.translateToLocalFormatted("placement.message.preview_set",  I18n.translateToLocal("placement.preview." + newMode.toString().toLowerCase()));
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message));
                return;
            }
            
            else if(ModKeys.PLACEMENT_UNDO.isPressed())
            {
               //TODO
                return;
            }
            
            else if(ModKeys.PLACEMENT_DECREASE_DEPTH.isPressed() && item.changeRegionSize(stack, 0, 0, -1))
            {
                PacketHandler.CHANNEL.sendToServer(new PacketConfigurePlacementItem(stack));
                BlockPos pos = item.getRegionSize(stack, false);
                String message  = I18n.translateToLocalFormatted("placement.message.region_box", pos.getX(), pos.getY(), pos.getZ());
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message));
                return;
            }
            
            else if(ModKeys.PLACEMENT_DECREASE_HEIGHT.isPressed() && item.changeRegionSize(stack, 0, -1, 0))
            {
                PacketHandler.CHANNEL.sendToServer(new PacketConfigurePlacementItem(stack));
                BlockPos pos = item.getRegionSize(stack, false);
                String message  = I18n.translateToLocalFormatted("placement.message.region_box", pos.getX(), pos.getY(), pos.getZ());
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message));
                return;
            }
            
            else if(ModKeys.PLACEMENT_DECREASE_WIDTH.isPressed() && item.changeRegionSize(stack, -1, 0, 0))
            {
                PacketHandler.CHANNEL.sendToServer(new PacketConfigurePlacementItem(stack));
                BlockPos pos = item.getRegionSize(stack, false);
                String message  = I18n.translateToLocalFormatted("placement.message.region_box", pos.getX(), pos.getY(), pos.getZ());
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message));
                return;
            }
            
            else if(ModKeys.PLACEMENT_INCREASE_DEPTH.isPressed() && item.changeRegionSize(stack, 0, 0, 1))
            {
                PacketHandler.CHANNEL.sendToServer(new PacketConfigurePlacementItem(stack));
                BlockPos pos = item.getRegionSize(stack, false);
                String message  = I18n.translateToLocalFormatted("placement.message.region_box", pos.getX(), pos.getY(), pos.getZ());
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message));
                return;
            }
            
            else if(ModKeys.PLACEMENT_INCREASE_HEIGHT.isPressed() && item.changeRegionSize(stack, 0, 1, 0))
            {
                PacketHandler.CHANNEL.sendToServer(new PacketConfigurePlacementItem(stack));
                BlockPos pos = item.getRegionSize(stack, false);
                String message  = I18n.translateToLocalFormatted("placement.message.region_box", pos.getX(), pos.getY(), pos.getZ());
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message));
                return;
            }
            
            else if(ModKeys.PLACEMENT_INCREASE_WIDTH.isPressed() && item.changeRegionSize(stack, 1, 0, 0))
            {
                PacketHandler.CHANNEL.sendToServer(new PacketConfigurePlacementItem(stack));
                BlockPos pos = item.getRegionSize(stack, false);
                String message  = I18n.translateToLocalFormatted("placement.message.region_box", pos.getX(), pos.getY(), pos.getZ());
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message));
                return;
            }
        }
        
        if(ModKeys.PLACEMENT_LAUNCH_BUILD.isPressed())
        {
            PacketHandler.CHANNEL.sendToServer(new PacketSimpleAction(PacketSimpleAction.ActionType.LAUNCH_CURRENT_BUILD));
            String message  = I18n.translateToLocal("placement.message.launch_build");
            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message));
            return;
        }
    }
    
    @SubscribeEvent
    public static void modelRegistryEvent(ModelRegistryEvent event)
    {
          
        IForgeRegistry<Item> itemReg = GameRegistry.findRegistry(Item.class);
        
        for(Map.Entry<ResourceLocation, Item> entry: itemReg.getEntries())
        {
            if(entry.getKey().getResourceDomain().equals(HardScience.MODID))
            {
                
                //TODO: move to library mod
                Item item = entry.getValue();
                if(item instanceof SuperItemBlock)
                {
                    
                    SuperBlock block = (SuperBlock)((ItemBlock)item).getBlock();
                    for (ItemStack stack : block.getSubItems())
                    {
                        String variantName = SuperDispatcher.INSTANCE.getDelegate(block).getModelResourceString() + "." + stack.getMetadata();
                        ModelBakery.registerItemVariants(item, new ResourceLocation(variantName));
                        ModelLoader.setCustomModelResourceLocation(item, stack.getMetadata(), new ModelResourceLocation(variantName, "inventory"));     
                    }
                }
                else if(item instanceof MatterCube)
                {
                    ModelBakery.registerItemVariants(item, item.getRegistryName());
                    ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));     
                }
                else if(item instanceof BulkItem)
                {
                    ModelBakery.registerItemVariants(item, item.getRegistryName());
                    ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));     
                }
                else if(item instanceof CraftingItem)
                {
                    ModelBakery.registerItemVariants(item, item.getRegistryName());
                    ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));     
                }
                else
                {
                    ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
                }
            }
        }
        
        // Bind TESR to tile entity
        ClientRegistry.bindTileEntitySpecialRenderer(VirtualTileEntityTESR.class, VirtualTESR.INSTANCE);
        ClientRegistry.bindTileEntitySpecialRenderer(BlockFabricatorTileEntity.class, BlockFabricatorTESR.INSTANCE);
        ClientRegistry.bindTileEntitySpecialRenderer(MachineTileEntity.class, MachineTESR.INSTANCE);
        ClientRegistry.bindTileEntitySpecialRenderer(MachineTileEntityTickable.class, MachineTESR.INSTANCE);
        ClientRegistry.bindTileEntitySpecialRenderer(ChemicalBatteryTileEntity.class, ChemicalBatteryTESR.INSTANCE);
        ClientRegistry.bindTileEntitySpecialRenderer(MicronizerTileEntity.class, MicronizerTESR.INSTANCE);
        ClientRegistry.bindTileEntitySpecialRenderer(DigesterTileEntity.class, DigesterTESR.INSTANCE);
    }
    
    @SubscribeEvent()
    public static void onModelBakeEvent(ModelBakeEvent event) throws IOException
    {
        
        IForgeRegistry<Item> itemReg = GameRegistry.findRegistry(Item.class);
        
        for(Map.Entry<ResourceLocation, Item> entry: itemReg.getEntries())
        {
            if(entry.getKey().getResourceDomain().equals(HardScience.MODID))
            {
                // TODO: move to library mod
                Item item = entry.getValue();
                if(item instanceof SuperItemBlock)
                {
                    SuperBlock block = (SuperBlock)((ItemBlock)item).getBlock();
                    for (ItemStack stack : block.getSubItems())
                    {
                        event.getModelRegistry().putObject(new ModelResourceLocation(item.getRegistryName() + "." + stack.getMetadata(), "inventory"),
                                SuperDispatcher.INSTANCE.getDelegate(block));
                    }
                }
                else if(item instanceof MatterCube)
                {
                    event.getModelRegistry().putObject(new ModelResourceLocation(item.getRegistryName(), "inventory"),
                            new MatterCubeItemModel((MatterCube) item));
                }
                else if(item instanceof BulkItem)
                {
                    event.getModelRegistry().putObject(new ModelResourceLocation(item.getRegistryName(), "inventory"),
                            new MatterCubeItemModel1((BulkItem) item));
                }
                else if(item instanceof CraftingItem)
                {
                    event.getModelRegistry().putObject(new ModelResourceLocation(item.getRegistryName(), "inventory"),
                            SuperDispatcher.INSTANCE.getItemDelegate());
                }
                else
                {
                    // Not needed - will look for json files for normal items;
                }
            }
        }
    }
}
