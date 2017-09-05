package grondag.hard_science.superblock.block;

import grondag.hard_science.library.serialization.ByteSerializer;
import grondag.hard_science.library.serialization.EnumSerializer;
import grondag.hard_science.superblock.varia.BlockSubstance;
import net.minecraft.nbt.NBTTagCompound;

public class SuperModelTileEntity extends SuperTileEntity
{
    ////////////////////////////////////////////////////////////////////////
    //  STATIC MEMBERS
    ////////////////////////////////////////////////////////////////////////
    public static final EnumSerializer<SuperModelTileEntity, BlockSubstance> SERIALIZER_SUBSTANCE 
            = new EnumSerializer<SuperModelTileEntity, BlockSubstance>(false, "HSSUB", BlockSubstance.class)
    {
        @Override
        public BlockSubstance getValue(SuperModelTileEntity target)
        {
            return target.getSubstance();
        }

        @Override
        public void setValue(SuperModelTileEntity target, BlockSubstance value)
        {
            target.setSubstance(value);
        } 
    };
    
    public static final ByteSerializer<SuperModelTileEntity> SERIALIZER_LIGHT_VALUE = new ByteSerializer<SuperModelTileEntity>(false, "HSLV")
    {
        @Override
        public byte getValue(SuperModelTileEntity target)
        {
            return (byte) target.getLightValue();
        }

        @Override
        public void setValue(SuperModelTileEntity target, byte value)
        {
            target.setLightValue(value);;
        } 
    };
    
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
        SERIALIZER_SUBSTANCE.serializeNBT(this, compound);
        SERIALIZER_LIGHT_VALUE.serializeNBT(this, compound);
    }

    @Override
    public void readModNBT(NBTTagCompound compound)
    {
        super.readModNBT(compound);
        SERIALIZER_SUBSTANCE.deserializeNBT(this, compound);
        SERIALIZER_LIGHT_VALUE.deserializeNBT(this, compound);
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
