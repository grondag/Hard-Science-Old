package grondag.hard_science.superblock.placement;

import java.util.ArrayList;

import javax.annotation.Nonnull;

import grondag.hard_science.superblock.placement.AbstractPlacementSpec.PlacementSpecEntry;
import jline.internal.Log;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

/**
 * Contains list of indexes to placement operations
 * that affect a position.  Once compiled,
 * also has the ItemStack that should be placed
 * at the position.
 * <p>
 * Not persisted, because is derived dynamically from 
 * the placements.
 */
public class BuildEntry
{
    /**
     * 
     */
    private final BuildChunk buildChunk;

    private ArrayList<PlacementSpecEntry> specEntries = new ArrayList<PlacementSpecEntry>();
    
    /**
     * True if needs compiled and has been
     * added to the compile queue.
     * Reset to false at start of compile.
     * Set true when specs are added or removed,
     * or world state has changed.
     */
    private boolean isCompileDirty = true;
    
    /**
     * True if world state has not been obtained
     * from world or if world state may be out of date.
     * Tracked separately from compilation so 
     * that all world access can happen on game thread.
     */
    private boolean isWorldStateDirty = true;
    
    public final BlockPos pos;
    
    /**
     * Result of the last successful compile operation.
     * Null if not compiled OR if no change should be made to world.
     */
    private ItemStack compiledStack = null;
    
    protected BuildEntry(BuildChunk buildChunk, BlockPos pos)
    {
        this.buildChunk = buildChunk;
        this.pos = pos;
    }
    
    private boolean isEmpty()
    {
        return this.specEntries.isEmpty();
    }
    
    public void addSpecEntry(PlacementSpecEntry specEntry)
    {
        this.specEntries.add(specEntry);
        this.setDirty();
    }

    private void removeSpecEntry(PlacementSpecEntry specEntry)
    {
        for(int i = this.specEntries.size() - 1; i >= 0; i--)
        {
            if(this.specEntries.get(i) == specEntry)
            {
                this.specEntries.remove(i);
                this.setDirty();
                break;
            }
        }
    }

    /**
     * TODO: Call when world state changes.
     * Marked for compilation (if not already marked)
     * and add to the compilation queue.
     */
    public void setDirty()
    {
        boolean needsAdd = false;
        synchronized(this)
        {
            if(!this.isCompileDirty)
            {
                this.isCompileDirty = true;
                needsAdd = true;
            }
        }
        if(needsAdd)
        {
            synchronized(this.buildChunk.compileQueue)
            {
                this.buildChunk.compileQueue.add(this);
            }
        }
    }
    
    /**
     * Resets dirty flag but does NOT remove this
     * entry from the compile queue. Is assumed
     * caller already did that. <p>
     * 
     * If chunk at this position is not loaded, compilation
     * will fail, compilation dirty flag will be set true,
     * and will return false. 
     */
    public boolean compile(@Nonnull ItemStack worldStack)
    {
        this.isCompileDirty = false;
        
        if(this.specEntries.isEmpty())
        {
            this.compiledStack = null;
            Log.warn("BuildEntry compile encountered empty spec list. This is a bug.");
            return true;
        }
        
        ItemStack result = worldStack.copy();
        
        // using an array to allow concurrent modification of list while iterating
        // if modification does occur, isCompileDirty should be true 
        // because we cleared it at the very beginning and we'll compile again.
        for(PlacementSpecEntry specEntry : this.specEntries.toArray(new PlacementSpecEntry[this.specEntries.size()]))
        {
            specEntry.applyToWorld(result);
        }
        
        this.compiledStack = result;
        return true;
    }
}