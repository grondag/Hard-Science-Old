package grondag.hard_science.superblock.placement.spec;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.world.Location;
import grondag.hard_science.library.world.Location.ILocated;
import grondag.hard_science.superblock.placement.FilterMode;
import grondag.hard_science.superblock.placement.PlacementSpecType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public abstract class AbstractPlacementSpec implements ILocated, IReadWriteNBT
{
    /**
     * @see {@link #getLocation()}
     */
    private Location location;

    private String playerName;
    
    /**
     * Make true if setting all positions to air.
     */
    private boolean isExcavation;
    
    /**
     * True if only places/removes virtual blocks. 
     * Derived from the placement item that created this spec.
     */
    private boolean isVirtual;
    
    /**
     * Will be adjusted to a value that makes sense if we are excavating.
     */
    private FilterMode filterMode;
    
    protected AbstractPlacementSpec() {};
            
    protected AbstractPlacementSpec(PlacementSpecBuilder builder)
    {
        this.isExcavation = builder.isExcavation;
        this.isVirtual = builder.isVirtual;
        this.filterMode = builder.effectiveFilterMode;
        this.location = new Location(builder.placementPosition().inPos, builder.player().world);
        this.playerName = builder.player().getName();
    }
    
    /**
     * Used for serialization, factory methods
     */
    public abstract PlacementSpecType specType();
    
    /**
     * List of excavations and block placements in this spec.
     * Should only be called after {@link #worldTask(EntityPlayer)} has run.
     * Entries are in order they should be executed.
     * Excavations and placements for same position must be 
     * different entries.
     */
    public abstract ImmutableList<PlacementSpecEntry> entries();
    
    /**
     * Position where the user clicked to activate the placement.
     * Also identifies the dimension in which placement occurs.
     */
    @Override
    public Location getLocation()
    {
        return this.location;
    }

    @Override
    public void setLocation(Location loc)
    {
        this.location = loc;
    }
    
    public String playerName()
    {
        return this.playerName;
    }
    
    public boolean isExcavation()
    {
        return this.isExcavation;
    }
    
    public boolean isVirtual()
    {
        return this.isVirtual;
    }
    
    public FilterMode filterMode()
    {
        return this.filterMode;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        this.deserializeLocation(tag);
        this.playerName = tag.getString(ModNBTTag.PLACEMENT_PLAYER_NAME);
        this.isExcavation = tag.getBoolean(ModNBTTag.PLACEMENT_DELETE_ENABLED);
        this.isVirtual = tag.getBoolean(ModNBTTag.PLACEMENT_IS_VIRTUAL);
        this.filterMode = FilterMode.FILL_REPLACEABLE.deserializeNBT(tag);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        this.serializeLocation(tag);
        tag.setString(ModNBTTag.PLACEMENT_PLAYER_NAME, this.playerName);
        tag.setBoolean(ModNBTTag.PLACEMENT_DELETE_ENABLED, this.isExcavation);
        tag.setBoolean(ModNBTTag.PLACEMENT_IS_VIRTUAL, this.isVirtual);
        this.filterMode.serializeNBT(tag);
    }


}
