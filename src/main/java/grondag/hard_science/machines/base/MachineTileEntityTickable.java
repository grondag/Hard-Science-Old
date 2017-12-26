package grondag.hard_science.machines.base;

import grondag.hard_science.CommonProxy;
import grondag.hard_science.Configurator;
import grondag.hard_science.Log;
import grondag.hard_science.library.varia.SimpleUnorderedArraySet;
import grondag.hard_science.machines.support.MachineControlState;
import grondag.hard_science.network.ModMessages;
import grondag.hard_science.network.server_to_client.PacketMachineStatusUpdateListener;
import grondag.hard_science.superblock.varia.KeyedTuple;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ITickable;

public abstract class MachineTileEntityTickable extends MachineTileEntity implements ITickable
{
    /** players who are looking at this machine and need updates sent to client */
    private SimpleUnorderedArraySet<PlayerListener> listeningPlayers;
    
    /**
     * True if information has changed and players should receive an update.
     * Difference from {@link #playerUpdateTicks} is that {@link #playerUpdateTicks} controls
     * <i>when</i> update occurs. {@link #isPlayerUpdateNeeded} contols <i>if</i> update occurs.
     */
    private boolean isPlayerUpdateNeeded = false;
    
    private class PlayerListener extends KeyedTuple<EntityPlayerMP>
    {
        /** Listener is valid until this time*/
        private long goodUntilMillis;
        
        private PlayerListener(EntityPlayerMP key)
        {
            super(key);
            this.goodUntilMillis = CommonProxy.currentTimeMillis() + Configurator.Machines.machineKeepAlivePlusLatency;
        }
        
        /**
         * Returns true if this listener should be removed because it has timed out or player has disconnected.
         */
        private boolean checkForRemoval(long currentTime)
        {
            return this.key.hasDisconnected()
                    || (currentTime > this.goodUntilMillis && !isOpenContainerForPlayer(this.key));
        }
    }
   
    @Override
    public void addPlayerListener(EntityPlayerMP player)
    {
        if(world.isRemote) return;
        
        if(Configurator.logMachineActivity) 
            Log.info("MachineTileEntityTickable.addPlayerListener: %s got keepalive packet", this.machine().machineName());
        PlayerListener listener = new PlayerListener(player);
        
        if(this.listeningPlayers == null)
        {
            this.listeningPlayers = new SimpleUnorderedArraySet<PlayerListener>();
            this.listeningPlayers.put(listener);
            
            if(Configurator.logMachineActivity) 
                Log.info("MachineTileEntityTickable.addPlayerListener: %s added new listener", this.machine().machineName());
            
            // send immediate refresh for any new listener
            if(this.machine() == null) return;
            
            ModMessages.INSTANCE.sendTo(this.createMachineStatusUpdate(), player);
            
        }
        else
        {
            PlayerListener previous = this.listeningPlayers.put(listener);
            
            if(previous == null)
            {
                if(Configurator.logMachineActivity) 
                    Log.info("MachineTileEntityTickable.addPlayerListener: %s added new listener", this.machine().machineName());

                if(this.machine() == null) return;
                
                // send immediate refresh for any new listener
                ModMessages.INSTANCE.sendTo(this.createMachineStatusUpdate(), player);
            }

        }
    }
    
    // not used - just waits for them to time out
//    public void removePlayerListener(EntityPlayerMP player)
//    {
//        if(world.isRemote || this.listeningPlayers == null) return;
//
//        PlayerListener removed = this.listeningPlayers.removeIfPresent(new PlayerListener(player, false));
//        if( removed != null && removed.isRequired) this.requiredListenerCount--;
//    }

    @Override
    public void markPlayerUpdateDirty(boolean isUrgent)
    {
        if(this.listeningPlayers == null || this.listeningPlayers.isEmpty()) return;
        
        this.isPlayerUpdateNeeded = true;
        if(isUrgent)
        {
            this.nextPlayerUpdateMilliseconds = 0;
        }
    }
    
    @Override
    public void update()
    {
        if(world.isRemote) 
        {
            // estimate progress for any jobs in flight
            // provides smoother user feedback on client
            
            MachineControlState controlState = this.clientState.controlState;
            
            if(controlState != null && controlState.hasJobTicks() && controlState.getJobRemainingTicks() > 0)
                controlState.progressJob((short) 1);
            return;
        }
        else if(this.isPlayerUpdateNeeded && this.listeningPlayers != null && !this.listeningPlayers.isEmpty())
        {
            long time = CommonProxy.currentTimeMillis();
        
            if(time >= this.nextPlayerUpdateMilliseconds)
            {
                PacketMachineStatusUpdateListener packet = this.machine() == null
                        ? null : this.createMachineStatusUpdate();
                
                int i = 0;
                while(i < listeningPlayers.size())
                {
                    PlayerListener listener = listeningPlayers.get(i);
                    
                    if(listener.checkForRemoval(time))
                    {
                        this.listeningPlayers.remove(i);
                        if(Configurator.logMachineActivity) 
                            Log.info("MachineTileEntityTickable.update: %s Removed timed out listener", this.machine().machineName());
                    }
                    else
                    {
                        if(Configurator.logMachineActivity) 
                            Log.info("MachineTileEntityTickable.update: %s Sending update packet due to change", this.machine().machineName());
                        if(packet != null) ModMessages.INSTANCE.sendTo(packet, listener.key);
                        i++;
                    }
                }
                
                this.nextPlayerUpdateMilliseconds = time + Configurator.MACHINES.machineUpdateIntervalMilliseconds;
                this.isPlayerUpdateNeeded = false;
            }
        }
    }
}
