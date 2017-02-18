package grondag.adversity.feature.volcano.lava.cell;

import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import grondag.adversity.Adversity;
import grondag.adversity.feature.volcano.lava.LavaSimulatorNew;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.nbt.NBTTagCompound;

public class LavaCells
{
    private final Long2ObjectOpenHashMap<LavaCell2> cellLocator = new Long2ObjectOpenHashMap<LavaCell2>();
    
    private LavaCell2 cells[] = new LavaCell2[0xFFFF];
    
    /** first index that has not been used */
    private AtomicInteger firstUnusedIndex = new AtomicInteger(0);
    
    /** list of indexes below firstUnused that are empty */
    private ConcurrentLinkedQueue<Integer> unusedIndexes = new ConcurrentLinkedQueue<Integer>();
    
    private AtomicInteger size = new AtomicInteger(0);
    
    /**
     * Retrieves cell at the given block position, creating it if it does not exist.
     * Returns null if the given location cannot contain lava.
     */
    private LavaCell2 getCellIfExists(LavaSimulatorNew sim, int x, int y, int z)
    {
        LavaCell2 locator = cellLocator.get(LavaCell2.computeKey(x, z));
        return locator == null ? null : locator.getCellIfExists(sim, y);
    }
    
    LavaCell2 newCell(int x, int z)
    {
        LavaCell2 cell = new LavaCell2(x, z);
        this.addCellToArray(cell);
        return cell;
    }
    
    /** 
     * Adds cell to the storage array. 
     * Does not add to locator list.
     * Thread-safe.
     */
    private void addCellToArray(LavaCell2 cell)
    {
        int i = size.getAndIncrement();
        cell.index = i;
        cells[i] = cell;
//        Integer index = unusedIndexes.poll();
//        if(index == null)
//        {
//            i = firstUnusedIndex.getAndIncrement();
//        }
//        else
//        {
//            i = index;
//        }
//        cell.index = i;
//        cells[i] = cell;
    }
    
    /** 
     * Removes cell from the storage array. 
     * Does not remove from locator.
     * Call after already cell has been unlinked from other 
     * cells in column and removed (and if necessary replaced) in locator.
     * Thread-safe.
     */
    private void removeCellFromArray(LavaCell2 cell)
    {
        //TODO - don't think this is thread-safe as-is
        int i = cell.index;
        cells[i] = cells[size.decrementAndGet()];
        cells[i].index = i;
        
//        cells[cell.index] = null;
//        unusedIndexes.add(cell.index);
//        cell.index = -1;
//        size.decrementAndGet();
    }
    
    public void writeNBT(NBTTagCompound nbt)
    {
        Adversity.log.info("Saving " + size.get() + " lava cells.");
        int[] saveData = new int[size.get() * LavaCell2.LAVA_CELL_NBT_WIDTH];
        int i = 0;

        for(int j = 0; j < this.firstUnusedIndex.get(); j++)
        {
            if(cells[j] != null) cells[j].writeNBT(saveData, i);
            
            // Java parameters are always pass by value, so have to advance index here
            i += LavaCell2.LAVA_CELL_NBT_WIDTH;
        }         
        nbt.setIntArray(LavaCell2.LAVA_CELL_NBT_TAG, saveData);
    }
    
    public void readNBT(LavaSimulatorNew sim, NBTTagCompound nbt)
    {
        Arrays.fill(this.cells, null);
        this.cellLocator.clear();
        this.size.set(0);

        // LOAD LAVA CELLS
        int[] saveData = nbt.getIntArray(LavaCell2.LAVA_CELL_NBT_TAG);

        //confirm correct size
        if(saveData == null || saveData.length % LavaCell2.LAVA_CELL_NBT_WIDTH != 0)
        {
            Adversity.log.warn("Invalid save data loading lava simulator. Lava blocks may not be updated properly.");
        }
        else
        {
            int i = 0;
            while(i < saveData.length)
            {
                LavaCell2 cell = new LavaCell2(saveData, i);
                
             // Java parameters are always pass by value, so have to advance index here
                i += LavaCell2.LAVA_CELL_NBT_WIDTH;
                        
                cell.clearBlockUpdate(sim);
                
                this.addCellToArray(cell);
                
                LavaCell2 startingCell = cellLocator.get(cell.locationKey());
                
                if(startingCell == null)
                {
                    cellLocator.put(cell.locationKey(), cell);
                }
                else
                {
                    startingCell.addCellToColumn(cell);
                }
                
            }
         
            Adversity.log.info("Loaded " + this.size.get() + " lava cells.");
        }
    }
    
    
}
