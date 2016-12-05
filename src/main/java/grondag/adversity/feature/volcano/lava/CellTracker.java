package grondag.adversity.feature.volcano.lava;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import grondag.adversity.Adversity;
import grondag.adversity.feature.volcano.LavaManager;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CellTracker
{
    private final BlockPos origin;
    private final int radius;
    private final int xOffset;
    private final int zOffset;
    private final int arrayLength;
    private final World world;
    
    private final ArrayList<LavaCell>[][] cells;
    
    private int nextCellID = 0;
    
    @SuppressWarnings("unchecked")
    public CellTracker(BlockPos origin, int radius, World world)
    {
        this.origin = origin;
        this.radius = radius;
        this.arrayLength = radius * 2 + 1;
        this.xOffset = radius - origin.getX();
        this.zOffset = radius - origin.getZ();
        this.world = world;
        this.cells = new ArrayList[arrayLength][arrayLength];
    }
    
    //TODO: make incremental
    public void initializeSpaces()
    {
        for(int x = -radius; x <= radius; x++)
        {
            int worldX = this.origin.getX() + x;
            for(int z = -radius; z <= radius; z++)
            {
                int worldZ = this.origin.getZ() + z;
                for(int y = 0; y < 256; y++)
                {
                    IBlockState state 
                        = world.getBlockState(new BlockPos(worldX, y, worldZ));
                    if(LavaManager.canDisplace(state))
                    {
                        addOrConfirmSpace(worldX, worldZ, y, y+1);
                    }
                }
            }
        }
    }
    
    public int getNextCellID()
    {
        return ++this.nextCellID;
    }
    
    public void addCell(LavaCell cell)
    {
        int arrayX = cell.x + xOffset;
        int arrayZ = cell.z + zOffset;
        
        //Should never happen. 
        if(arrayX >= arrayLength || arrayX < 0 || arrayZ >= arrayLength || arrayZ < 0)
        {
            Adversity.log.debug("Attempt to add lava cell outside radius ignored. Origin=" + origin.toString()
                    + " x=" + cell.x + " z=" + cell.z);
            return;
        }
        
        ArrayList<LavaCell> list = cells[arrayX][arrayZ];
        if(list == null)
        {
            list = new ArrayList<LavaCell>();
            cells[arrayX][arrayZ] = list;
        }
        if(!list.isEmpty())
        {
            for(LavaCell existing : list)
            {
                if(existing.intersectsWith(cell))
                {
                    //should not normally occur
                    Adversity.log.debug("Overlapping lava cell added @x=" + cell.x + " z=" + cell.z);
                }
            }
        }
        list.add(cell);
    }
    
    public void removeCell(LavaCell cell)
    {
        int arrayX = cell.x + xOffset;
        int arrayZ = cell.z + zOffset;
        
        //Should never happen. 
        if(arrayX >= arrayLength || arrayX < 0 || arrayZ >= arrayLength || arrayZ < 0)
        {
            Adversity.log.debug("Attempt to remove lava cell outside radius ignored. Origin=" + origin.toString()
                    + " x=" + cell.x + " z=" + cell.z);
            return;
        }
        
        ArrayList<LavaCell> list = cells[arrayX][arrayZ];

        if(list == null || list.isEmpty() || !list.remove(cell))
        {
            //should not normally occur
            Adversity.log.debug("Attempted to remove non-existant lava cell @x=" + cell.x + " z=" + cell.z);
            return;
        }
        
        //remove list from array if now empty
        if(list.isEmpty())
        {
            cells[arrayX][arrayZ] = null;
        }
     }
    
    public void replaceCell(LavaCell oldCell, LavaCell newCell)
    {
        this.removeCell(oldCell);
        this.addCell(newCell);
    }
    
    /**
     * Returns list of all cells within the x, z column.
     */
    public List<LavaCell> getCells(int x, int z)
    {
        int arrayX = x + xOffset;
        int arrayZ = z + zOffset;
        
        //Will get called by boundary lookups - 
        //just ignore these, effective creates a barrier at boundary
        if(arrayX >= arrayLength || arrayX < 0 || arrayZ >= arrayLength || arrayZ < 0)
        {
            return Collections.emptyList();
        }
        
        return cells[arrayX][arrayZ] == null ? Collections.emptyList() : cells[arrayX][arrayZ];
    }
    
    public void addOrConfirmSpace(int x, int z, float floor, float ceiling)
    {
        List<LavaCell> existing = getCells(x, z);
        LavaCell firstFound = null;
        LavaCell secondFound = null;
        for(LavaCell cell : existing)
        {
            if(cell.isVerticallyAdjacentTo(floor, ceiling))
            {
                if(firstFound == null)
                {
                    firstFound = cell;
                }
                else if(secondFound == null)
                {
                    secondFound = cell;
                }
                else
                {
                    //should never happen
                    Adversity.log.debug("Added space is vertically adjacent to more than two cells. Origin=" + origin.toString()
                    + " x=" + x + " z=" + z);
                }
            }
        }
        
        if(firstFound == null)
        {
            this.addCell(new LavaCell(x, z, floor, ceiling, this));
        }
        else if(secondFound == null)
        {
            firstFound.addOrConfirmSpace(floor, ceiling);
        }
        else
        {
            existing.remove(firstFound);
            existing.remove(secondFound);
            existing.add(CellMerger.MergeWithVerticalSpace(firstFound, secondFound));
        }
           
    }
    
    public void removeSpace(BlockPos pos)
    {
        
    }
    
    /**
     * Returns horizontally neighboring cells that overlap with the 
     * volume defined by floor and ceiling by at least minimumWindow.
     * @param x
     * @param z
     * @param floor
     * @param ceiling
     * @param minimumWindow
     */
    public List<LavaCell> getAdjacentCells(int x, int z, float floor, float ceiling, float minimumWindow)
    {
        LinkedList<LavaCell> result = new LinkedList<LavaCell>();
        result.addAll(getIntersectingCells(x+1, z, floor, ceiling, minimumWindow));
        result.addAll(getIntersectingCells(x-1, z, floor, ceiling, minimumWindow));
        result.addAll(getIntersectingCells(x, z+1, floor, ceiling, minimumWindow));
        result.addAll(getIntersectingCells(x, z-1, floor, ceiling, minimumWindow));
        return result;
    }
    
    /**
     * Returns cells in the given column that overlap with the 
     * volume defined by floor and ceiling by at least minimumWindow.
     * @param x
     * @param z
     * @param floor
     * @param ceiling
     * @param minimumWindow
     */
    public List<LavaCell> getIntersectingCells(int x, int z, float floor, float ceiling, float minimumWindow)
    {
        LinkedList<LavaCell> result = new LinkedList<LavaCell>();
        
        for(LavaCell cell : getCells(x, z))
        {
            if(cell.intersectsWith(floor, ceiling)
                && Math.min(ceiling, cell.getCeiling()) - Math.max(floor, cell.getFloor()) >= minimumWindow)
            {
                result.add(cell);
            }
        }
        return result;
    }
    
    public LavaCell getCellForBlockPos(BlockPos pos)
    {
        List<LavaCell> candidates = getIntersectingCells(pos.getX(), pos.getZ(), pos.getY(), pos.getY() + 1, 0.5F);
        if(candidates.isEmpty())
        {
            return null;
        }
        else
        {
            return candidates.get(0);
        }
    }
}
