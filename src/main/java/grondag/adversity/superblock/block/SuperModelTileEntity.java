package grondag.adversity.superblock.block;

import grondag.adversity.superblock.varia.BlockSubstance;
import grondag.adversity.superblock.varia.SuperBlockNBTHelper;
import grondag.adversity.superblock.varia.SuperBlockNBTHelper.SuperModelNBTReadHandler;
import net.minecraft.nbt.NBTTagCompound;

public class SuperModelTileEntity extends SuperTileEntity implements SuperModelNBTReadHandler
{
    /** non-zero if block emits light */
    private byte lightValue = 0;

    private BlockSubstance substance = BlockSubstance.FLEXSTONE;
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        return SuperBlockNBTHelper.writeToNBT(super.writeToNBT(compound), this.lightValue, this.substance);
    }

    @Override
    public void handleNBTRead(byte lightValue, BlockSubstance substance)
    {
        this.lightValue = lightValue;
        this.substance = substance;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        // The description packet often arrives after render state is first cached on client
        // so we need to refresh render state once we have the server-side info.
        int oldLight = this.lightValue;
        
        super.handleUpdateTag(tag);
        
        if(oldLight != this.lightValue)
        {
            this.world.checkLight(this.pos);
        }
    }
    
    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        SuperBlockNBTHelper.superModelReadFromNBT(compound, this);
        isLoaded = true;
    }

    public byte getLightValue()
    {
        return this.lightValue;
    }

    public void setLightValue(byte lightValue)
    {
        if(this.lightValue != lightValue)
        {
            this.lightValue = lightValue;
            if(this.world.isRemote)
                this.world.checkLight(this.pos);
            else
                this.markDirty();
            
        }
    }
    
    public BlockSubstance getSubstance()
    {
        return this.substance;
    }
    
    public void setSubstance(BlockSubstance substance)
    {
        if(this.substance != substance)
        {
            this.substance = substance;
            if(!this.world.isRemote) this.markDirty();
        }
    }
}
