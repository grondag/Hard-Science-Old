package grondag.hard_science.machines.base;

import grondag.hard_science.init.ModSuperModelBlocks;
import grondag.hard_science.machines.support.MachineControlState;
import grondag.hard_science.machines.support.MachinePowerSupply;
import grondag.hard_science.machines.support.MachineStatusState;
import grondag.hard_science.machines.support.MaterialBufferManager;
import grondag.hard_science.network.server_to_client.PacketMachineStatusUpdateListener;
import grondag.hard_science.superblock.block.SuperModelBlock;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.placement.PlacementItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Holds client-side state for machine TE
 *
 */
public class MachineClientState
{
    /**
     * For use by TESR - cached items stack based on status info.
     */
    private ItemStack statusStack;
    
    /**
     * For use by TESR - last time player looked at this machine within the machine rendering distance
     */
    @SideOnly(Side.CLIENT)
    public long lastInViewMillis;
    
    /** 
     * Caches {@link AbstractMachine#hasOnOff() on client}
     */
    public boolean hasOnOff;
    
    /** 
     * Caches {@link AbstractMachine#hasRedstoneControl() on client}
     */
    public boolean hasRedstoneControl;
    
    /**
     * Caches {@link AbstractMachine#maxPowerConsumptionWatts()}
     */
    public float maxPowerConsumptionWatts; 
    
    /**
     * Used for client rendering - will only be updated as needed.
     */
    public MachineControlState controlState = new MachineControlState();
    
    /**
     * Used for client rendering - will only be updated as needed.
     */
    public MachineStatusState statusState = new MachineStatusState();
    
    /**
     * Used for client rendering - will only be updated as needed.
     */
    public MaterialBufferManager bufferManager;
    
    /**
     * Used for client rendering - will only be updated as needed.
     */
    public MachinePowerSupply powerSupply;
    
    public String machineName = "???";
    
    public MachineClientState(MachineTileEntity mte)
    {
        AbstractMachine tempMachine = mte.createNewMachine();
        this.bufferManager = tempMachine.getBufferManager();
        this.powerSupply = tempMachine.getPowerSupply();
        this.hasOnOff = tempMachine.hasOnOff();
        this.hasRedstoneControl = tempMachine.hasRedstoneControl();
        this.maxPowerConsumptionWatts = tempMachine.maxPowerConsumptionWatts();
    }
    
    /**
     * Handles client status updates received from server.
     */
    
    public void handleMachineStatusUpdate(PacketMachineStatusUpdateListener packet)
    {
        this.controlState = packet.controlState;
        this.statusState = packet.statusState;
        this.statusStack = null;
        this.machineName = packet.machineName;
        
        if(this.bufferManager != null)
        {
            this.bufferManager.deserializeFromArray(packet.materialBufferData);
        }
        if(this.powerSupply != null)
        {
            this.powerSupply.fromBytes(packet.powerProviderData);
        }
    }
    
    public boolean isOn()
    {
        if(!this.hasOnOff) return false;
        if(this.controlState == null || this.statusState == null) return false;
        return AbstractMachine.computeIsOn(this.controlState, this.statusState);
    }
    
    public boolean isRedstoneControlEnabled()
    {
        return this.hasRedstoneControl
                && this.controlState != null
                && this.controlState.getControlMode().isRedstoneControlEnabled;
    }
    
    /**
     * For use by TESR - cached items stack based on status info.
     * Assumes that the target block is a superModel block.
     */
    @SideOnly(Side.CLIENT)
    public ItemStack getStatusStack()
    {
        ItemStack result = this.statusStack;
        MachineControlState controlState = this.controlState;
        
        if(result == null && controlState.hasModelState())
        {
            ModelState modelState = controlState.getModelState();
            if(modelState == null) return null;
            
            SuperModelBlock newBlock = ModSuperModelBlocks.findAppropriateSuperModelBlock(controlState.getSubstance(), controlState.getModelState());
            result = newBlock.getSubItems().get(0);
            PlacementItem.setStackLightValue(result, controlState.getLightValue());
            PlacementItem.setStackSubstance(result, controlState.getSubstance());
            PlacementItem.setStackModelState(result, controlState.getModelState());
            this.statusStack = result;
        }
        return result;
    }
}