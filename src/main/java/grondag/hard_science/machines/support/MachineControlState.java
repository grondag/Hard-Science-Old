package grondag.hard_science.machines.support;

import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.varia.BitPacker;
import grondag.hard_science.library.varia.BitPacker.BitElement.BooleanElement;
import grondag.hard_science.library.varia.BitPacker.BitElement.EnumElement;
import net.minecraft.nbt.NBTTagCompound;



public class MachineControlState implements IReadWriteNBT
{
    public static enum ControlMode
    {
        ON,
        OFF,
        ON_WITH_REDSTONE,
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

    private static final long DEFAULT_BITS;
    
    static
    {
        long bits = 0;
        bits = PACKED_CONTROL_MODE.setValue(ControlMode.ON, bits);
        bits = PACKED_RENDER_LEVEL.setValue(RenderLevel.EXTENDED_WHEN_VISIBLE, bits);
        DEFAULT_BITS = bits;
    }
    
    private long bits = DEFAULT_BITS;
    
    
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
    // Serialization stuff
    //////////////////////////////////////////////////////////////////////


    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        this.bits = (tag.hasKey("HSMCS")) ? tag.getLong("HSMCS") : DEFAULT_BITS;
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        tag.setLong("HSMCS", this.bits);
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
