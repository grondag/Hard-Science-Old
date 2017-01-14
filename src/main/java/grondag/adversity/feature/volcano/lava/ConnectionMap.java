package grondag.adversity.feature.volcano.lava;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;

import com.google.common.collect.ComparisonChain;

import grondag.adversity.Adversity;


public class ConnectionMap
{
  
    private final HashMap<CellConnectionPos, LavaCellConnection> map = new HashMap<CellConnectionPos, LavaCellConnection>();

    private final TreeSet<LavaCellConnection> set = new TreeSet<LavaCellConnection>(
            new Comparator<LavaCellConnection>() {
                @Override
                public int compare(LavaCellConnection o1, LavaCellConnection o2)
                {
                    return ComparisonChain.start()
                    //vertical first
                    .compare(o1.isVertical, o2.isVertical)
                    //top down
                    .compare(o2.firstCell.pos.getY(), o1.firstCell.pos.getY())
                    //largest drop first
                    .compare(o2.getSortDrop(), o1.getSortDrop())
                    //random tie breaker -> don't favor any horizontal direction
                    .compare(o1.rand, o2.rand)
                    //ensure match equals behavior in rare case rand doesn't break tie
                    .compare(o1.id, o2.id)
                    .result();
                  
                }});

            
    public void clear()
    {
        map.clear();
        set.clear();
    }
    
    public int size()
    {
        return map.size();
    }
   
    public LavaCellConnection get(CellConnectionPos pos)
    {
        return map.get(pos);
    }
    
    public void createConnectionIfNotPresent(LavaSimulator sim, CellConnectionPos pos)
    {
        if(!map.containsKey(pos))
        {
            LavaCell cell1 = sim.getCell(pos.getLowerPos(), false);
            LavaCell cell2 = sim.getCell(pos.getUpperPos(), false);
            LavaCellConnection connection = new LavaCellConnection(cell1, cell2);
            map.put(pos, connection);
            set.add(connection);
            
            //TODO: remove for release
            if(map.size() != set.size())
            {
                Adversity.log.warn("Connection tracking error: set size does not match map size.");
            }
        }
    }
    
    public void remove(CellConnectionPos pos)
    {
        LavaCellConnection connection = this.map.get(pos);
        if(connection != null)
        {
            connection.releaseCells();
            set.remove(connection);
            map.remove(pos);
            
            //TODO: remove for release
            if(map.size() != set.size())
            {
                Adversity.log.warn("Connection tracking error: set size does not match map size.");
            }
        }
    }
    
    public Collection<LavaCellConnection> getSortedValues()
    {
        return this.set;
    }
    
}
