package grondag.hard_science.machines.support;

import javax.annotation.Nonnull;

import grondag.exotic_matter.serialization.IMessagePlus;
import grondag.exotic_matter.serialization.IReadWriteNBT;
import grondag.exotic_matter.varia.BitPacker;
import grondag.exotic_matter.varia.PackedBlockPos;
import grondag.exotic_matter.varia.BitPacker.BitElement.BooleanElement;
import grondag.exotic_matter.varia.BitPacker.BitElement.EnumElement;
import grondag.exotic_matter.varia.BitPacker.BitElement.IntElement;
import grondag.hard_science.crafting.base.GenericRecipe;
import grondag.hard_science.init.ModNBTTag;
import grondag.hard_science.superblock.model.state.ModelState;
import grondag.hard_science.superblock.varia.BlockSubstance;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;



public class MachineControlState implements IReadWriteNBT, IMessagePlus
{
    public static enum ControlMode
    {
        ON(false),
        OFF(false),
        ON_WITH_REDSTONE(true),
        OFF_WITH_REDSTONE(true);
        
        public final boolean isRedstoneControlEnabled;
        
        private ControlMode(boolean isRedstoneControlEnabled)
        {
            this.isRedstoneControlEnabled = isRedstoneControlEnabled;
        }
    }
    
    public static enum RenderLevel
    {
        NONE,
        MINIMAL,
        EXTENDED_WHEN_LOOKING,
        EXTENDED_WHEN_VISIBLE;
    }
    
    /**
     * Not all machines use all states,
     * nor are all transitions the same, but 
     * wanted a shared vocabulary.
     */
    public static enum MachineState
    {
        /**
         * Machine is not doing anything, because it is off, not powered, has no work, or is just designed to sit there.
         * This is the default state.
         */
        IDLE,
        
        /**
         * The machine is thinking about fixing to get ready to get started on doing something... maybe.
         * Might also be searching for work.
         */
        THINKING,
        
        /**
         * The machine is pulling resources into internal buffers.
         * This state may not be reported much, because usually happens simultaneously with something more interesting.
         */
        SUPPLYING,
        
        /**
         * The machine is making something.
         */
        FABRICATING,
        
        /**
         * Machine is moving or delivering something.
         */
        TRANSPORTING,
        
        // Reserved to pad enum serializer so don't break world saves if later add more substances.
        RESERVED01,
        RESERVED02,
        RESERVED03,
        RESERVED04,
        RESERVED05,
        RESERVED06,
        RESERVED07,
        RESERVED08,
        RESERVED09,
        RESERVED10,
        RESERVED11,
        RESERVED12,
        RESERVED13,
        RESERVED14,
        RESERVED15,
        RESERVED16,
        RESERVED17,
        RESERVED18,
        RESERVED19,
        RESERVED20,
        RESERVED21,
        RESERVED22,
        RESERVED23,
        RESERVED24,
        RESERVED25,
        RESERVED26,
        RESERVED27;
    }
    
    private static BitPacker PACKER = new BitPacker();
    
    private static EnumElement<ControlMode> PACKED_CONTROL_MODE = PACKER.createEnumElement(ControlMode.class);
    private static EnumElement<RenderLevel> PACKED_RENDER_LEVEL = PACKER.createEnumElement(RenderLevel.class);
    private static BooleanElement PACKED_HAS_MODELSTATE = PACKER.createBooleanElement();
    private static IntElement PACKED_META = PACKER.createIntElement(16);
    private static IntElement PACKED_LIGHT_VALUE = PACKER.createIntElement(16);
    private static EnumElement<BlockSubstance> PACKED_SUBSTANCE = PACKER.createEnumElement(BlockSubstance.class);
    private static EnumElement<MachineState> PACKED_MACHINE_STATAE = PACKER.createEnumElement(MachineState.class);
    private static BooleanElement PACKED_HAS_JOB_TICKS = PACKER.createBooleanElement();
    private static BooleanElement PACKED_HAS_TARGET_POS = PACKER.createBooleanElement();
    private static BooleanElement PACKED_HAS_MATERIAL_BUFFER = PACKER.createBooleanElement();
    private static BooleanElement PACKED_HAS_POWER_SUPPLY= PACKER.createBooleanElement();
    private static BooleanElement PACKED_HAS_RECIPE= PACKER.createBooleanElement();

    private static final long DEFAULT_BITS;
    
    static
    {
        long bits = 0;
        bits = PACKED_CONTROL_MODE.setValue(ControlMode.ON, bits);
        bits = PACKED_RENDER_LEVEL.setValue(RenderLevel.EXTENDED_WHEN_VISIBLE, bits);
        DEFAULT_BITS = bits;
    }
    
    private long bits = DEFAULT_BITS;
    private ModelState modelState;
    private short jobDurationTicks = 0;
    private short jobRemainingTicks = 0;
    private BlockPos targetPos = null;
    private GenericRecipe currentRecipe = null;
    
    //////////////////////////////////////////////////////////////////////
    // ACCESS METHODS
    //////////////////////////////////////////////////////////////////////
    
    public ControlMode getControlMode() { return PACKED_CONTROL_MODE.getValue(bits); }
    public void setControlMode(@Nonnull ControlMode value) { bits = PACKED_CONTROL_MODE.setValue(value, bits); }
    
    public RenderLevel getRenderLevel() { return PACKED_RENDER_LEVEL.getValue(bits); }
    public void setRenderLevel(@Nonnull RenderLevel value) { bits = PACKED_RENDER_LEVEL.setValue(value, bits); }
    
    /**
     * If true, then modelState should be populated.
     * Used by block fabricators
     */
    public boolean hasModelState() { return PACKED_HAS_MODELSTATE.getValue(bits); }
    private void updateModelStateStatus() { bits = PACKED_HAS_MODELSTATE.setValue(this.modelState != null, bits); }
    
    public ModelState getModelState() { return this.modelState; }
    public void setModelState( ModelState value)
    {
        this.modelState = value; 
        this.updateModelStateStatus();
    }
    
    /**
     * If true, then recipe should be populated.
     */
    public boolean hasRecipe() { return PACKED_HAS_RECIPE.getValue(bits); }
    private void updateRecipeStatus() { bits = PACKED_HAS_RECIPE.setValue(this.currentRecipe != null, bits); }
    
    public GenericRecipe getRecipe() { return this.currentRecipe; }
    public void setRecipe( GenericRecipe value)
    {
        this.currentRecipe = value; 
        this.updateRecipeStatus();
    }
    
    public boolean hasTargetPos() { return PACKED_HAS_TARGET_POS.getValue(bits); }
    private void updateTargetPosStatus() { bits = PACKED_HAS_TARGET_POS.setValue(this.targetPos != null, bits); }
    
    public BlockPos getTargetPos() { return this.targetPos; }
    public void setTargetPos( BlockPos value)
    {
        this.targetPos = value; 
        this.updateTargetPosStatus();
    }
    
    /** 
     * Intended for block fabricators, but usage determined by machine. 
     * While values are always non-null, they are not always valid.  
     * Check that a modelState or other related attribute also exists
     */
    public @Nonnull BlockSubstance getSubstance() { return PACKED_SUBSTANCE.getValue(bits); }
    public void setSubstance(@Nonnull BlockSubstance value) { bits = PACKED_SUBSTANCE.setValue(value, bits); }
    
    /** intended for block fabricators, but usage determined by machine. */
    public int getLightValue() { return PACKED_LIGHT_VALUE.getValue(bits); }
    public void setLightValue(int value) { bits = PACKED_LIGHT_VALUE.setValue(value, bits); }
    
    /** intended for block fabricators, but usage determined by machine. */
    public int getMeta() { return PACKED_META.getValue(bits); }
    public void setMeta(int value) { bits = PACKED_META.setValue(value, bits); }
    
    public MachineState getMachineState() { return PACKED_MACHINE_STATAE.getValue(bits); }
    public void setMachineState(@Nonnull MachineState value) { bits = PACKED_MACHINE_STATAE.setValue(value, bits); }
    
    public boolean hasJobTicks() { return PACKED_HAS_JOB_TICKS.getValue(this.bits); }
    
    private void updateJobTicksStatus()
    {
        this.bits = PACKED_HAS_JOB_TICKS.setValue(this.jobDurationTicks >0 || this.jobRemainingTicks >0, this.bits);
    }
    
    public short getJobDurationTicks() { return this.jobDurationTicks; }
    
    /** This will NOT set JobRemainingTicks to the same value. Use {@link #startJobTicks(short)} for that. */
    public void setJobDurationTicks(short ticks)
    {
        this.jobDurationTicks = ticks;
        this.updateJobTicksStatus();
    }
    
    /** reduces remaining job ticks by given amount and returns true if job is complete */
    public boolean progressJob(short howManyTicks)
    {
        int current = this.getJobRemainingTicks() - howManyTicks;
        if(current < 0) current = 0;
        this.setJobRemainingTicks((short) current);
        return current == 0;
    }
    
    public short getJobRemainingTicks() { return this.jobRemainingTicks; }
    public void setJobRemainingTicks(short ticks)
    {
        this.jobRemainingTicks = ticks;
        this.updateJobTicksStatus();
    }
    
    public void setJobTicks(short jobDurationTicks, short jobRemainingTicks)
    {
        this.jobDurationTicks = jobDurationTicks;
        this.jobRemainingTicks = jobRemainingTicks;
        this.updateJobTicksStatus();
    }
    
    /** sets both duration and remaining ticks to given value. */
    public void startJobTicks(short jobDurationTicks)
    {
        this.setJobTicks(jobDurationTicks, jobDurationTicks);
    }
    
    /**
     * Sets both job tick values to zero - saves space in packets if no job active.
     */
    public void clearJobTicks()
    {
        this.jobDurationTicks = 0;
        this.jobRemainingTicks = 0;
        this.updateJobTicksStatus();
    }
    
    public boolean hasMaterialBuffer() { return PACKED_HAS_MATERIAL_BUFFER.getValue(bits); }
    public void hasMaterialBuffer(boolean hasBuffer) { bits = PACKED_HAS_MATERIAL_BUFFER.setValue(hasBuffer, bits); }
    
    public boolean hasPowerSupply() { return PACKED_HAS_POWER_SUPPLY.getValue(bits); }
    public void hasPowerSupply(boolean hasProvider) { bits = PACKED_HAS_POWER_SUPPLY.setValue(hasProvider, bits); }
    
    //////////////////////////////////////////////////////////////////////
    // Serialization Stuff                                              //
    //////////////////////////////////////////////////////////////////////

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        if(tag.hasKey(ModNBTTag.MACHINE_CONTROL_STATE))
        {
            this.bits = tag.getLong(ModNBTTag.MACHINE_CONTROL_STATE);
            
            if(this.hasModelState())
            {
                if(this.modelState == null) this.modelState = new ModelState();
                this.modelState.deserializeNBT(tag, ModNBTTag.MACHINE_MODEL_STATE);
            }
            if(this.hasTargetPos())
            {
                this.targetPos = PackedBlockPos.unpack(tag.getLong(ModNBTTag.MACHINE_TARGET_BLOCKPOS));
            }
            if(this.hasJobTicks())
            {
                this.jobDurationTicks = tag.getShort(ModNBTTag.MACHINE_JOB_DURATION_TICKS);
                this.jobRemainingTicks = tag.getShort(ModNBTTag.MACHINE_JOB_REMAINING_TICKS);
            }
            if(this.hasRecipe())
            {
                this.currentRecipe = new GenericRecipe(tag);
            }
        }
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        tag.setLong(ModNBTTag.MACHINE_CONTROL_STATE, this.bits);
        if(this.hasModelState())
        {
            this.modelState.serializeNBT(tag, ModNBTTag.MACHINE_MODEL_STATE);
        }
        if(this.hasTargetPos())
        {
            tag.setLong(ModNBTTag.MACHINE_TARGET_BLOCKPOS, PackedBlockPos.pack(this.targetPos));
        }
        if(this.hasJobTicks())
        {
            tag.setShort(ModNBTTag.MACHINE_JOB_DURATION_TICKS, this.jobDurationTicks);
            tag.setShort(ModNBTTag.MACHINE_JOB_REMAINING_TICKS, this.jobRemainingTicks);
        }
        if(this.hasRecipe())
        {
            this.currentRecipe.serializeNBT(tag);
        }
    }   
    
    @Override
    public void fromBytes(PacketBuffer pBuff)
    {
        this.bits = pBuff.readLong();
        if(this.hasModelState())
        {
            if(this.modelState == null) this.modelState = new ModelState();
            this.modelState.fromBytes(pBuff);
        }
        if(this.hasTargetPos())
        {
            this.targetPos = PackedBlockPos.unpack(pBuff.readLong());
        }
        if(this.hasJobTicks())
        {
            this.jobDurationTicks = pBuff.readShort();
            this.jobRemainingTicks = pBuff.readShort();
        }
        
        if(this.hasRecipe())
        {
            this.currentRecipe = new GenericRecipe(pBuff);
        }
    }
    
    @Override
    public void toBytes(PacketBuffer pBuff)
    {
        pBuff.writeLong(this.bits);
        if(this.hasModelState())
        {
            this.modelState.toBytes(pBuff);
        }
        if(this.hasTargetPos())
        {
            pBuff.writeLong(PackedBlockPos.pack(this.targetPos));
        }
        if(this.hasJobTicks())
        {
            pBuff.writeShort(this.jobDurationTicks);
            pBuff.writeShort(this.jobRemainingTicks);
        }
        if(this.hasRecipe())
        {
            this.currentRecipe.toBytes(pBuff);
        }
    }
}
