package grondag.adversity.feature.volcano.lava;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import grondag.adversity.library.PackedBlockPos;
import net.minecraft.util.EnumFacing;


public class ConnectionMap
{
  
//    private final ConcurrentHashMap<CellConnectionPos, LavaCellConnection> map = new ConcurrentHashMap<CellConnectionPos, LavaCellConnection>();
    
    private int size = 0;

    private final ConcurrentHashMap<Long, LavaCellConnection> map = new ConcurrentHashMap<Long, LavaCellConnection>();
    
//    private final ConcurrentSkipListMap<CellConnectionPos, LavaCellConnection> map = new ConcurrentSkipListMap<CellConnectionPos, LavaCellConnection>(
//            new Comparator<CellConnectionPos>() {
//                @Override
//                public int compare(CellConnectionPos o1, CellConnectionPos o2)
//                {
//                    return ComparisonChain.start()
//                    //vertical first
//                    .compare(o1.isVertical(), o2.isVertical())
//                    //top down
//                    .compare(o2.lowerPos.getY(), o1.lowerPos.getY())
//                    //tie breaker -> don't favor any horizontal direction
//                    .compare(o1.hashCode(), o2.hashCode())
//                    //remaining ensure unique match
//                    .compare(o1.lowerPos.getX(), o2.lowerPos.getX())
//                    .compare(o1.lowerPos.getZ(), o2.lowerPos.getZ())
//                    .compare(o1.axis, o2.axis)
//                    .result();
//                  
//                }});

            
    public void clear()
    {
        synchronized(this)
        {
            map.clear();
            this.size = 0;
        }
    }
    
    public int size()
    {
        synchronized(this)
        {
            return this.size;
        }
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
                LavaCellConnection connection = LavaCellConnection.create(cell1, cell2, packedConnectionPos);
                map.put(packedConnectionPos, connection);
                size++;
            }
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
    
    public void remove(long packedConnectionPos)
    {
        synchronized(this)
        {
            LavaCellConnection connection = this.map.get(packedConnectionPos);
            if(connection != null)
            {
                connection.releaseCells();
                map.remove(packedConnectionPos);
                size--;
            }
        }
    }
    
    public Collection<LavaCellConnection> values()
    {
        return this.map.values();
    }
    
}
