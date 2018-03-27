package grondag.hard_science;

import grondag.exotic_matter.placement.IPlacementItem;
import grondag.exotic_matter.simulator.Simulator;
import grondag.exotic_matter.simulator.WorldTaskManager;
import grondag.exotic_matter.simulator.domain.DomainManager;
import grondag.exotic_matter.simulator.domain.IDomain;
import grondag.hard_science.simulator.jobs.JobManager;
import grondag.hard_science.simulator.jobs.TaskType;
import grondag.hard_science.simulator.jobs.tasks.ExcavationTask;
import grondag.hard_science.superblock.placement.spec.PlacementHandler;
import grondag.hard_science.superblock.placement.spec.PlacementResult;
import grondag.hard_science.superblock.virtual.ExcavationRenderTracker;
import grondag.hard_science.superblock.virtual.VirtualBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

@Mod.EventBusSubscriber
public class CommonEventHandler 
{
    
    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) 
    {
        VirtualBlock.registerVirtualBlocks(event);
    }
    
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) 
    {
        grondag.exotic_matter.CommonEventHandler.handleRegisterItems(HardScience.MODID, event);
    }
    
    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) 
    {
        if (event.getModID().equals(HardScience.MODID))
        {
            ConfigManager.sync(HardScience.MODID, Type.INSTANCE);
            Configurator.recalcDerived();
        }
    }
    
    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event)
    {
        EntityPlayer player = event.getEntityPlayer();
        
        if(player == null) return;
        
        ItemStack stackIn = event.getItemStack();
        if (stackIn == null || stackIn.isEmpty() || !(stackIn.getItem() instanceof IPlacementItem)) return;

        PlacementResult result = PlacementHandler.doLeftClickBlock(player, event.getPos(), event.getFace(), event.getHitVec(), stackIn);
        
        if(!result.shouldInputEventsContinue())
        {
            result.apply(stackIn, player);
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent event) 
    {
        if(event.phase == Phase.START) 
        {
            // noop
        }
        else
        {
            WorldTaskManager.doServerTick();
            
            // thought it might be more determinism if simulator runs after block/entity ticks
            Simulator.instance().onServerTick(event);
            
            // TODO: remove
            // Temporary drone service
            for(IDomain domain : DomainManager.instance().getAllDomains())
            {
                JobManager jm = domain.getCapability(JobManager.class);
                
                try
                {
                    for(int i = 0; i < 1; i++)
                    {
                        ExcavationTask task = (ExcavationTask) jm.claimReadyWork(TaskType.EXCAVATION, null).get();
                        if(task == null) break;
                        
                        World world = task.job().world();
                        if(world != null) world.setBlockToAir(task.pos());
                        
                        task.complete();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
    
    
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerChangedDimensionEvent event)
    {
        if(event.player instanceof EntityPlayerMP)
        {
            ExcavationRenderTracker.INSTANCE.updatePlayerTracking((EntityPlayerMP) event.player);
        }
    }
    
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerRespawnEvent event)
    {
        if(event.player instanceof EntityPlayerMP)
        {
            ExcavationRenderTracker.INSTANCE.updatePlayerTracking((EntityPlayerMP) event.player);
        }
    }
    
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerLoggedInEvent event)
    {
        if(event.player instanceof EntityPlayerMP)
        {
            ExcavationRenderTracker.INSTANCE.updatePlayerTracking((EntityPlayerMP) event.player);
        }
    }
    
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerLoggedOutEvent event)
    {
        if(event.player instanceof EntityPlayerMP)
        {
            ExcavationRenderTracker.INSTANCE.stopPlayerTracking((EntityPlayerMP) event.player);
        }
    }
    
//	@SubscribeEvent
//	public void onReplaceBiomeBlocks(ReplaceBiomeBlocks.ReplaceBiomeBlocks event) {
//		if (event.getWorld().provider.getDimension() == 0) {
//			Drylands.replaceBiomeBlocks(event);
//			event.setResult(Result.DENY);
//		}
//	}
//
}
