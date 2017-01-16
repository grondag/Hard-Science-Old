package grondag.adversity.feature.volcano.lava;

import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

import com.google.common.collect.ComparisonChain;


public class ConnectionMap
{
  
//    private final ConcurrentHashMap<CellConnectionPos, LavaCellConnection> map = new ConcurrentHashMap<CellConnectionPos, LavaCellConnection>();
    
    private int size = 0;

    private final ConcurrentSkipListMap<CellConnectionPos, LavaCellConnection> map = new ConcurrentSkipListMap<CellConnectionPos, LavaCellConnection>(
            new Comparator<CellConnectionPos>() {
                @Override
                public int compare(CellConnectionPos o1, CellConnectionPos o2)
                {
                    return ComparisonChain.start()
                    //vertical first
                    .compare(o1.isVertical(), o2.isVertical())
                    //top down
                    .compare(o2.lowerPos.getY(), o1.lowerPos.getY())
                    //tie breaker -> don't favor any horizontal direction
                    .compare(o1.hashCode(), o2.hashCode())
                    //remaining ensure unique match
                    .compare(o1.lowerPos.getX(), o2.lowerPos.getX())
                    .compare(o1.lowerPos.getZ(), o2.lowerPos.getZ())
                    .compare(o1.axis, o2.axis)
                    .result();
                  
                }});

            
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
   
    public LavaCellConnection get(CellConnectionPos pos)
    {
        return map.get(pos);
    }
    
    public void createConnectionIfNotPresent(LavaSimulator sim, CellConnectionPos pos)
    {
        synchronized(this)
        {
            if(!map.containsKey(pos))
            {
                LavaCell cell1 = sim.getCell(pos.lowerPos, false);
                LavaCell cell2 = sim.getCell(pos.upperPos, false);
                LavaCellConnection connection = new LavaCellConnection(cell1, cell2);
                map.put(pos, connection);
                size++;
            }
        }
    }
    
    public void remove(CellConnectionPos pos)
    {
        synchronized(this)
        {
            LavaCellConnection connection = this.map.get(pos);
            if(connection != null)
            {
                connection.releaseCells();
                map.remove(pos);
                size--;
            }
        }
    }
    
    public Collection<LavaCellConnection> getSortedValues()
    {
        return this.map.values();
    }
    
}
