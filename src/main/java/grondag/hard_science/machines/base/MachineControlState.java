package grondag.hard_science.machines.base;

import grondag.hard_science.library.serialization.IFlexibleSerializer;
import grondag.hard_science.library.varia.BitPacker;
import grondag.hard_science.library.varia.BitPacker.BitElement.BooleanElement;
import grondag.hard_science.library.varia.BitPacker.BitElement.EnumElement;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;



public class MachineControlState implements IFlexibleSerializer
{
    public static enum ControlMode
    {
        ON,
        OFF,
        ON_WITH_REDSTOWN,
        OFF_WITH_REDSTONE;
    }
    
    public static enum RenderLevel
    {
        NONE,
        MINIMAL,
        EXTENDED_WHEN_LOOKING,
        EXTENDED_WHEN_VISIBLE;
    }
    
    private static BitPacker PACKER = new BitPacker();
    
    private static EnumElement<ControlMode> PACKED_CONTROL_MODE = PACKER.createEnumElement(ControlMode.class);
    private static EnumElement<RenderLevel> PACKED_RENDER_LEVEL = PACKER.createEnumElement(RenderLevel.class);
    private static BooleanElement PACKED_REDSTONE_POWER = PACKER.createBooleanElement();

    private long bits;
    
    //////////////////////////////////////////////////////////////////////
    // ACCESS METHODS
    //////////////////////////////////////////////////////////////////////
    
    public ControlMode getControlMode() { return PACKED_CONTROL_MODE.getValue(bits); }
    public void setControlMode(ControlMode value) { bits = PACKED_CONTROL_MODE.setValue(value, bits); }
    
    public RenderLevel getRenderLevel() { return PACKED_RENDER_LEVEL.getValue(bits); }
    public void setRenderLevel(RenderLevel value) { bits = PACKED_RENDER_LEVEL.setValue(value, bits); }
    
    public boolean hasRedstonePower() { return PACKED_REDSTONE_POWER.getValue(bits); }
    public void setHasRestonePower(boolean value) { bits = PACKED_REDSTONE_POWER.setValue(value, bits); }
    
    //////////////////////////////////////////////////////////////////////
    // IFlexibleSerializer implementation stuff
    //////////////////////////////////////////////////////////////////////
    @Override
    public void fromBytes(PacketBuffer pBuff)
    {
        this.bits = pBuff.readVarLong();
    }

    @Override
    public void toBytes(PacketBuffer pBuff)
    {
        pBuff.writeVarLong(this.bits);
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        this.bits = tag.getLong("HSMCS");
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        tag.setLong("HSMCS", this.bits);
    }

    @Override
    public boolean fromBytesDetectChanges(PacketBuffer buf)
    {
        long oldBits = this.bits;
        this.fromBytes(buf);
        return oldBits != this.bits;
    }

    @Override
    public boolean deserializeNBTDetectChanges(NBTTagCompound tag)
    {
        long oldBits = this.bits;
        this.deserializeNBT(tag);
        return oldBits != this.bits;
    }
    
    /**
     * For packet updates
     */
    public long serializeToBits()
    {
        return this.bits;
    }
    
    /**
     * For packet updates
     */
    public void deserializeFromBits(long bits)
    {
        this.bits = bits;
    }
}
