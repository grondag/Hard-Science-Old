package grondag.hard_science.superblock.block;

import grondag.hard_science.init.ModNBTTag;
import grondag.hard_science.init.ModSubstances;
import grondag.hard_science.movetogether.BlockSubstance;
import net.minecraft.nbt.NBTTagCompound;

public class SuperModelTileEntity extends SuperTileEntity
{
    ////////////////////////////////////////////////////////////////////////
    //  STATIC MEMBERS
    ////////////////////////////////////////////////////////////////////////

    
    ////////////////////////////////////////////////////////////////////////
    //  INSTANCE MEMBERS
    ////////////////////////////////////////////////////////////////////////
    
    /** non-zero if block emits light */
    private byte lightValue = 0;

    private BlockSubstance substance = ModSubstances.FLEXSTONE;
    
    @Override
    public void writeModNBT(NBTTagCompound compound)
    {
        super.writeModNBT(compound);
        if(this.substance != null) this.substance.serializeNBT(compound);
        compound.setByte(ModNBTTag.SUPER_MODEL_LIGHT_VALUE, (byte)this.lightValue);
    }

    @Override
    public void readModNBT(NBTTagCompound compound)
    {
        super.readModNBT(compound);
        this.substance = BlockSubstance.deserializeNBT(compound);
        this.lightValue = compound == null ? 0 : compound.getByte(ModNBTTag.SUPER_MODEL_LIGHT_VALUE);
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
            if(this.world != null && this.world.isRemote)
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
            if(!(this.world == null || this.world.isRemote)) this.markDirty();
        }
    }
}
