package grondag.hard_science.library.world;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableSet;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;

/**
 * Future version could in theory contain non-cubic shapes.
 */
public class BlockRegion extends IntegerAABB implements IBlockRegion
{
    private final boolean isHollow;
    
    private HashSet<BlockPos> exclusions;
    
    /**
     * Created region includes from from and to positions.
     */
    public BlockRegion(BlockPos fromPos, BlockPos toPos, boolean isHollow)
    {
        super(fromPos, toPos);
        this.isHollow = isHollow;
    }
    
    public final boolean isHollow()
    {
        return this.isHollow;
    }
    
    public final Set<BlockPos> exclusions()
    {
        return this.exclusions == null ? Collections.emptySet() : Collections.unmodifiableSet(exclusions);
    }
    
    public void exclude(BlockPos pos)
    {
        if(this.exclusions == null)
        {
            this.exclusions = new HashSet<BlockPos>();
        }
        this.exclusions.add(pos);
    }
    
    public void exclude(Collection<BlockPos> positions)
    {
        if(this.exclusions == null)
        {
            this.exclusions = new HashSet<BlockPos>();
        }
        this.exclusions.addAll(positions);
    }
    
    public boolean isExcluded(BlockPos pos)
    {
        if(this.exclusions == null) return false;
        return !this.exclusions.contains(pos);
    }
    
    public void clearExclusions()
    {
        if(this.exclusions != null) this.exclusions.clear();
    }
    
    /** All positions contained in the region, including interior positions if it is hollow */
    public Iterable<MutableBlockPos> allPositions()
    {
        return BlockPos.getAllInBoxMutable(this.minX, this.minY, this.minZ, this.maxX - 1, this.maxY - 1, this.maxZ - 1);
    }
    
    /** All positions on the surface of the region. Will be same as {@link #allPositions()} if region is not at least 3x3x3 */
    public Iterable<MutableBlockPos> surfacePositions()
    {
        return getAllOnBoxSurfaceMutable(this.minX, this.minY, this.minZ, this.maxX - 1, this.maxY - 1, this.maxZ - 1);
    }
    
    /** Positions that belong the region, excluding interior positions if hollow, but not excluding any excluded positions. */
    public Iterable<MutableBlockPos> positions()
    {
        return isHollow ? surfacePositions() : allPositions();
    }
    
    /** All positions on the surface of the region. Will be same as {@link #allPositions()} if region is not at least 3x3x3 */
    public Iterable<MutableBlockPos> adjacentPositions()
    {
        return getAllOnBoxSurfaceMutable(this.minX - 1, this.minY - 1, this.minZ - 1, this.maxX, this.maxY, this.maxZ);
    }
    
    /**
     * All positions included in the region. Excludes interior positions if hollow, and excludes any excluded positions.
     */
    public Iterable<MutableBlockPos> includedPositions()
    {
        return new Iterable<BlockPos.MutableBlockPos>()
        {
            public Iterator<BlockPos.MutableBlockPos> iterator()
            {
                return new AbstractIterator<BlockPos.MutableBlockPos>()
                {
                    Iterator<BlockPos.MutableBlockPos> wrapped = positions().iterator();
                    
                    protected BlockPos.MutableBlockPos computeNext()
                    {
                        while(wrapped.hasNext())
                        {
                            BlockPos.MutableBlockPos result = wrapped.next();
                            if(exclusions == null || !exclusions.contains(result)) return result;
                        }
                        return (BlockPos.MutableBlockPos)this.endOfData();
                    }
                };
            }
        };
        
    }
    
    /** convenience method - returns set of all block positions in AABB defined by inputs, inclusive */
    public static Set<BlockPos> positionsInRegion(BlockPos from, BlockPos to)
    {
        BlockRegion temp = new BlockRegion(from, to, false);
        ImmutableSet.Builder<BlockPos> builder = ImmutableSet.builder();
        
        for(BlockPos.MutableBlockPos pos : temp.allPositions())
        {
            builder.add(pos.toImmutable());
        }
        return builder.build();
    }
    
    /**
     * Like the BlockPos method, but only returns Block positions on the surface of the AABB
     */
    public static Iterable<BlockPos.MutableBlockPos> getAllOnBoxSurfaceMutable(final int x1, final int y1, final int z1, final int x2, final int y2, final int z2)
    {
        // has to be at least 3x3x3 or logic will get stuck and is also inefficient
        if(x2 - x1 < 2 || y2 - y1 < 2 || z2 - z1 < 2) return BlockPos.getAllInBoxMutable(x1, y1, z1, x2, y2, z2);
        
        return new Iterable<BlockPos.MutableBlockPos>()
        {
            public Iterator<BlockPos.MutableBlockPos> iterator()
            {
                return new AbstractIterator<BlockPos.MutableBlockPos>()
                {
                    private BlockPos.MutableBlockPos pos;
                    private int x = x1, y = y1, z = z1;
                    protected BlockPos.MutableBlockPos computeNext()
                    {
                        if (this.pos == null)
                        {
                            // at beginning
                            this.pos = new BlockPos.MutableBlockPos(x1, y1, z1);
                            return this.pos;
                        }
                        else if (this.x == x2 && this.y == y2 && this.z == z2)
                        {
                            // at end
                            return (BlockPos.MutableBlockPos)this.endOfData();
                        }
                        else
                        {
                            // if at either end of Z, normal behavior
                            if(this.z == z1 || this.z == z2)
                            {
                                if (this.x < x2)
                                {
                                    ++this.x;
                                }
                                else if (this.y < y2)
                                {
                                    this.x = x1;
                                    ++y;
                                }
                                else if (this.z < z2)
                                {
                                    this.x = x1;
                                    this.y = y1;
                                    ++this.z;
                                }
                            }
                            else
                            {
                                // in middle section, only do exterior points for x and y
                                if(this.y == y1)
                                {
                                    // on ends of Y, iterate X
                                    if(this.x < x2)
                                    {
                                        ++this.x;
                                    }
                                    else
                                    {
                                        this.x = x1;
                                        ++this.y;
                                    }
                                }
                                else if(this.y == y2)
                                {
                                    // on ends of Y, iterate X
                                    if(this.x < x2)
                                    {
                                        ++this.x;
                                    }
                                    else
                                    {
                                        this.x = x1;
                                        this.y = y1;
                                        ++this.z;
                                    }   
                                    
                                }
                                else
                                {
                                    // between Y ends, only x values are minX and maxX
                                    if(this.x == x1)
                                    {
                                        this.x = x2;
                                    }
                                    else
                                    {
                                        this.x = x1;
                                        ++this.y;
                                    }
                                }
                            }
                            this.pos.setPos(x, y, z);
                            return this.pos;
                        }
                    }
                };
            }
        };
    }
}
