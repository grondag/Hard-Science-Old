package grondag.hard_science.superblock.block;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.superblock.varia.BlockSubstance;
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

    private BlockSubstance substance = BlockSubstance.FLEXSTONE;
    
    @Override
    public void writeModNBT(NBTTagCompound compound)
    {
        super.writeModNBT(compound);
        Useful.saveEnumToTag(compound, ModNBTTag.SUPER_MODEL_SUBSTANCE, substance);
        compound.setByte(ModNBTTag.SUPER_MODEL_LIGHT_VALUE, (byte)this.lightValue);
    }

    @Override
    public void readModNBT(NBTTagCompound compound)
    {
        super.readModNBT(compound);
        this.substance = Useful.safeEnumFromTag(compound, ModNBTTag.SUPER_MODEL_SUBSTANCE,  BlockSubstance.FLEXSTONE);
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
