package grondag.hard_science.superblock.placement;

import java.util.ArrayDeque;

import grondag.hard_science.Log;
import grondag.hard_science.library.world.ChunkBlockMap;
import grondag.hard_science.superblock.placement.AbstractPlacementSpec.PlacementSpecEntry;
import net.minecraft.util.math.BlockPos;

class BuildChunk extends ChunkBlockMap<BuildEntry>
{
    protected final Build build;

    /**
     * Queue of entries in this chunk that need compilation, in order of addition.
     * Each entry tracks dirty status, and will not add self to queue
     * again if already dirty.  All modifications should be synchronized.
     */
    ArrayDeque<BuildEntry> compileQueue = new ArrayDeque<BuildEntry>();
    
    protected BuildChunk(Build build, BlockPos pos)
    {
        super(pos);
        this.build = build;
    }
    
    private synchronized BuildEntry getOrCreateEntry(BlockPos pos)
    {
        BuildEntry buildEntry = this.get(pos);
        if(buildEntry == null)
        {
            buildEntry = new BuildEntry(this, pos);
            this.put(pos, buildEntry);
        }
        return buildEntry;
    }
    
    /**
     * Assumes the entry is in this chunk.
     */
    protected void addSpec(PlacementSpecEntry specEntry, AbstractPlacementSpec spec)
    {
        BuildEntry buildEntry = this.getOrCreateEntry(specEntry.pos());
        if(buildEntry == null)
        {
            Log.warn("Unable to add build entry to build chunk for unknown reason. This is a bug.");
            return;
        }
//        buildEntry.addSpec(spec);
    }
}