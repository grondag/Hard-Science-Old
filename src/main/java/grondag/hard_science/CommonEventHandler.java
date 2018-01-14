package grondag.hard_science;

import java.util.concurrent.ThreadLocalRandom;

import com.google.gson.Gson;

import grondag.hard_science.init.ModBlocks;
import grondag.hard_science.simulator.Simulator;
import grondag.hard_science.simulator.domain.Domain;
import grondag.hard_science.simulator.domain.DomainManager;
import grondag.hard_science.simulator.jobs.TaskType;
import grondag.hard_science.simulator.jobs.WorldTaskManager;
import grondag.hard_science.simulator.jobs.tasks.ExcavationTask;
import grondag.hard_science.superblock.placement.PlacementHandler;
import grondag.hard_science.superblock.placement.PlacementItem;
import grondag.hard_science.superblock.placement.PlacementResult;
import grondag.hard_science.superblock.virtual.ExcavationRenderTracker;
import grondag.hard_science.volcano.lava.LavaBlock;
import grondag.hard_science.volcano.lava.simulator.LavaSimulator;
import jline.internal.Log;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

@SuppressWarnings({ "deprecation"})
@Mod.EventBusSubscriber
public class CommonEventHandler 
{
    private static final String[] DENIALS;
    
    static
    {
        String[] denials = {"DENIED"};
        try
        {
            Gson g = new Gson();
            String json = I18n.translateToLocal("misc.denials");
            denials = g.fromJson(json, String[].class);
        }
        catch(Exception e)
        {
            Log.warn("Unable to parse localized denial messages. Using default.");
        }
        DENIALS = denials;
    }
    
    /**
     * Troll user if they attempt to put volcanic lava in a bucket.
     */
    @SubscribeEvent(priority = EventPriority.HIGH) 
    public static void onFillBucket(FillBucketEvent event)
    {
        if(event.getEntityPlayer() != null && !event.getWorld().isRemote)
        {
            RayTraceResult target = event.getTarget();
            if(target != null && target.typeOfHit == RayTraceResult.Type.BLOCK)
            {
                if(target.getBlockPos() != null)
                {
                    IBlockState state = event.getWorld().getBlockState(target.getBlockPos());
                    if(state.getBlock() instanceof LavaBlock)
                    {
                        event.getEntityPlayer().sendMessage(new TextComponentString(DENIALS[ThreadLocalRandom.current().nextInt(DENIALS.length)]));
                        event.setCanceled(true);
                    }
                }
            }
        }
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
    public static void onBlockBreak(BlockEvent.BreakEvent event) 
    {
        // Lava blocks have their own handling
        if(!event.getWorld().isRemote && !(event.getState().getBlock() instanceof LavaBlock))
        {
            LavaSimulator sim = Simulator.instance().lavaSimulator();
            if(sim != null) sim.notifyBlockChange(event.getWorld(), event.getPos());
        }
    }
    
    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event)
    {
        EntityPlayer player = event.getEntityPlayer();
        
        if(player == null) return;
        
        ItemStack stackIn = event.getItemStack();
        if (stackIn == null || stackIn.isEmpty() || !(stackIn.getItem() instanceof PlacementItem)) return;

        PlacementResult result = PlacementHandler.doLeftClickBlock(player, event.getPos(), event.getFace(), event.getHitVec(), stackIn);
        
        if(!result.shouldInputEventsContinue())
        {
            result.apply(stackIn, player);
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.PlaceEvent event)
    {
        // Lava blocks have their own handling
        if(!event.getWorld().isRemote && !(event.getState().getBlock() instanceof LavaBlock))
        {
            LavaSimulator sim = Simulator.instance().lavaSimulator();
            if(sim != null) sim.notifyBlockChange(event.getWorld(), event.getPos());
        }
    }
    
    @SubscribeEvent
    public static void onBlockMultiPlace(BlockEvent.MultiPlaceEvent event)
    {
        if(event.getWorld().isRemote) return;
        
        LavaSimulator sim = Simulator.instance().lavaSimulator();
        if(sim != null)
        {
            for(BlockSnapshot snap : event.getReplacedBlockSnapshots())
            {
                if(!(snap.getCurrentBlock() instanceof LavaBlock))
                {
                    sim.notifyBlockChange(event.getWorld(), snap.getPos());
                }
            }
        }
    }
    
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent event) 
    {
        if(event.phase == Phase.START) 
        {
            CommonProxy.updateCurrentTime();
            CommonProxy.refreshWorldInfos();
        }
        else
        {
            WorldTaskManager.doServerTick();
            
            // thought it might be more determinism if simulator runs after block/entity ticks
            Simulator.instance().onServerTick(event);
            
            // TODO: remove
            // Temporary drone service
            for(Domain domain : DomainManager.instance().getAllDomains())
            {
                try
                {
                    for(int i = 0; i < 1; i++)
                    {
                        ExcavationTask task = (ExcavationTask) domain.jobManager.claimReadyWork(TaskType.EXCAVATION, null).get();
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
    
    private static String lastTroubleMaker = null;
    private static BlockPos lastAttemptLocation = null;
    private static long lastAttemptTimeMillis = -1;
    private static int attemptsAtTrouble = 0;
    @SubscribeEvent
    public static void onAskingForIt(ServerChatEvent event)
    {
        if(!Configurator.VOLCANO.enableVolcano) return;
        
        EntityPlayerMP player = event.getPlayer();
        
        if(player.getHeldItemMainhand().getItem() == Items.LAVA_BUCKET && event.getMessage().toLowerCase().contains("volcanos are awesome"))
        {
            long time = CommonProxy.currentTimeMillis();

            if(event.getUsername() == lastTroubleMaker
                    && player.getPosition().equals(lastAttemptLocation)
                    && time - lastAttemptTimeMillis < 30000)
            {
                //FIXME" check for volcano nearby
                
                attemptsAtTrouble++;
                
                //FIXME: localize
                if(attemptsAtTrouble == 1)
                {
                    player.sendMessage(new TextComponentString(String.format("This is a really bad idea, %s", player.getDisplayNameString())));
                }
                else if(attemptsAtTrouble == 2)
                {
                    player.sendMessage(new TextComponentString(String.format("I hope there isn't anything nearby you want to keep.", player.getDisplayNameString())));
                }
                else if(attemptsAtTrouble == 3)
                {
                    player.sendMessage(new TextComponentString(String.format("Now would be a good time to run away.", player.getDisplayNameString())));
                    player.world.setBlockState(new BlockPos(lastAttemptLocation.getX(), 0, lastAttemptLocation.getZ()), ModBlocks.volcano_block.getDefaultState());
                }
            }
            else
            {
                attemptsAtTrouble = 0;
            }
            lastTroubleMaker = event.getUsername();
            lastAttemptLocation = player.getPosition();
            lastAttemptTimeMillis = time;
            
            Log.warn("player is asking for it at " + event.getPlayer().posX + " " + event.getPlayer().posZ);
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
