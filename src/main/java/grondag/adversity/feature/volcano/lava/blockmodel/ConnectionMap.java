package grondag.adversity.feature.volcano.lava.blockmodel;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

import grondag.adversity.feature.volcano.lava.AbstractLavaSimulator;
import grondag.adversity.library.PackedBlockPos;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.EnumFacing;


public class ConnectionMap
{
  
//    private final ConcurrentHashMap<CellConnectionPos, LavaCellConnection> map = new ConcurrentHashMap<CellConnectionPos, LavaCellConnection>();

//    private final ConcurrentHashMap<Long, LavaCellConnection> map = new ConcurrentHashMap<Long, LavaCellConnection>();
    
//    private final ConcurrentSkipListMap<Long, LavaCellConnection> map = new ConcurrentSkipListMap<Long, LavaCellConnection>(
//            new Comparator<Long>() {
//                @Override
//                public int compare(Long o1, Long o2)
//                {
//                    return ComparisonChain.start()
//                    //vertical first
//                    .compare(PackedBlockPos.getExtra(o1) == EnumFacing.Axis.Y.ordinal(), PackedBlockPos.getExtra(o2) == EnumFacing.Axis.Y.ordinal())
//                    //top down
//                    .compare(PackedBlockPos.getY(o2), PackedBlockPos.getY(o2))
//                    //tie breaker -> don't favor any horizontal direction
//                    .compare(Useful.longHash(o1), Useful.longHash(o2))
//                    //remaining ensure unique match
//                    .compare(o1, o2)
//                    .result();
//                  
//                }});

    private LavaCellConnection sortedArray[] = null;
    
    private final Long2ObjectOpenHashMap<LavaCellConnection> map = new Long2ObjectOpenHashMap<LavaCellConnection>();
    
//    private final TreeSet<LavaCellConnection>sorted = new TreeSet<LavaCellConnection>(
//                        new Comparator<LavaCellConnection>() {
//              @Override
//              public int compare(LavaCellConnection o1, LavaCellConnection o2)
//              {
//                  return Long.compare(o1.getSortKey(), o2.getSortKey());
//              }});
            
    public void clear()
    {
        synchronized(this)
        {
            map.clear();
            sortedArray = null;
//            sorted.clear();
        }
    }
    
    public int size()
    {
        return map.size();
    }
   
    public LavaCellConnection get(long packedConnectionPos)
    {
        return map.get(packedConnectionPos);
    }
    
//    public LavaCellConnection get(long lowerPackedBlockPos, EnumFacing.Axis axis)
//    {
//        return map.get(getPackedConnectionPos(lowerPackedBlockPos, axis));
//    }
    
    public void createConnectionIfNotPresent(LavaSimulator sim, long lowerPackedBlockPos, EnumFacing.Axis axis)
    {
        createConnectionIfNotPresent(sim, getPackedConnectionPos(lowerPackedBlockPos, axis));
    }
    
    
    public void createConnectionIfNotPresent(LavaSimulator sim, long packedConnectionPos)
    {
        synchronized(this)
        {
            if(!map.containsKey(packedConnectionPos))
            {
                LavaCell cell1 = sim.getCell(lowerCellPackedBlockPos(packedConnectionPos), false);
                LavaCell cell2 = sim.getCell(upperCellPackedBlockPos(packedConnectionPos), false);
                LavaCellConnection connection = LavaCellConnection.create(sim, cell1, cell2, packedConnectionPos);
                map.put(packedConnectionPos, connection);
                sortedArray = null;
//                sorted.add(connection);
            }
//            if(map.size() != sorted.size())
//                Adversity.log.info("connection tracking error");
        }
    }
    
    public static long getPackedConnectionPos(long lowerPackedBlockPos, EnumFacing.Axis axis)
    {
        return PackedBlockPos.setExtra(lowerPackedBlockPos, axis.ordinal());
    }
    
    public static long lowerCellPackedBlockPos(long packedCellPos)
    {
        return PackedBlockPos.getPosition(packedCellPos);
    }
    
    public static EnumFacing.Axis getAxisFromPackedCellPos(long packedCellPos)
    {
        return EnumFacing.Axis.values()[PackedBlockPos.getExtra(packedCellPos)];
    }
    
    public static long upperCellPackedBlockPos(long packedCellPos)
    {
        return upperCellPackedBlockPos(lowerCellPackedBlockPos(packedCellPos), getAxisFromPackedCellPos(packedCellPos));
    }
    /**
     * Gives the packed block pos of the upper-valued cell in this connection.
     */
    public static long upperCellPackedBlockPos(long lowerPackedBlockPos, EnumFacing.Axis axis)
    {
        switch(axis)
        {
        case X:
            return PackedBlockPos.east(lowerPackedBlockPos);
            
        case Y:
            return PackedBlockPos.up(lowerPackedBlockPos);
            
        case Z:
            return PackedBlockPos.south(lowerPackedBlockPos);
            
        default:
            // not real
            return lowerPackedBlockPos;
        }
    }
    
    public static long getUpConnectionFromPackedBlockPos(long fromPackedBlockPos)
    {
        return PackedBlockPos.setExtra(fromPackedBlockPos, EnumFacing.Axis.Y.ordinal());
    }
    
    public static long getDownConnectionFromPackedBlockPos(long fromPackedBlockPos)
    {
        return PackedBlockPos.setExtra(PackedBlockPos.down(fromPackedBlockPos), EnumFacing.Axis.Y.ordinal());
    }
    
    public static long getEastConnectionFromPackedBlockPos(long fromPackedBlockPos)
    {
        return PackedBlockPos.setExtra(fromPackedBlockPos, EnumFacing.Axis.X.ordinal());
    }
    
    public static long getWestConnectionFromPackedBlockPos(long fromPackedBlockPos)
    {
        return PackedBlockPos.setExtra(PackedBlockPos.west(fromPackedBlockPos), EnumFacing.Axis.X.ordinal());
    }
    
    public static long getSouthConnectionFromPackedBlockPos(long fromPackedBlockPos)
    {
        return PackedBlockPos.setExtra(fromPackedBlockPos, EnumFacing.Axis.Z.ordinal());
    }
    
    public static long getNorthConnectionFromPackedBlockPos(long fromPackedBlockPos)
    {
        return PackedBlockPos.setExtra(PackedBlockPos.north(fromPackedBlockPos), EnumFacing.Axis.Z.ordinal());
    }
    
    public void removeIfInvalid(AbstractLavaSimulator sim, long packedConnectionPos)
    {
        synchronized(this)
        {
            LavaCellConnection connection = this.map.get(packedConnectionPos);
            if(connection != null && 
                    (
                        connection.firstCell.isBarrier() 
                        || connection.secondCell.isBarrier()
                        || (connection.firstCell.getFluidAmount() == 0 && connection.secondCell.getFluidAmount() == 0)
                    )
            )
            {
                connection.releaseCells(sim);
                map.remove(packedConnectionPos);
                sortedArray = null;
//                sorted.remove(connection);
            }
//            if(map.size() != sorted.size())
//                Adversity.log.info("connection tracking error");
        }
    }
    
    private static final LavaCellConnection[] ARRAY_TEMPLATE = new LavaCellConnection[0];
    public LavaCellConnection[] values()
    {
//        if(map.size() > 2)
//            Adversity.log.info("boop");
        
        if(sortedArray == null)
        {
//            sortedArray = sorted.toArray(ARRAY_TEMPLATE);
            map.values().parallelStream().forEach(p -> p.updateSortKey());
            
            sortedArray = map.values().toArray(ARRAY_TEMPLATE);
        
            Arrays.parallelSort(sortedArray, 
                    new Comparator<LavaCellConnection>() {
                      @Override
                      public int compare(LavaCellConnection o1, LavaCellConnection o2)
                      {
                          return Long.compare(o1.getSortKey(), o2.getSortKey());
                      }});
        }
        
        return sortedArray;
        
    }
    
    public void validateConnections(AbstractLavaSimulator sim)
    {
        Iterator<LavaCellConnection> it = map.values().iterator();
        while(it.hasNext())
        {
            LavaCellConnection test = it.next();
            if(test.firstCell.isBarrier() 
                    || test.secondCell.isBarrier()
                    || (test.firstCell.getFluidAmount() == 0 && test.secondCell.getFluidAmount() == 0))
            {
                test.releaseCells(sim);
                it.remove();
            }
        }
    }
    
}
