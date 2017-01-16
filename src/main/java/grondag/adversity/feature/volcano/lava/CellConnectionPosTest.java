package grondag.adversity.feature.volcano.lava;

import static org.junit.Assert.*;

import java.util.Random;
import java.util.TreeSet;

import org.junit.Test;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class CellConnectionPosTest
{

    @Test
    public void test()
    {
        BlockPos pos1, pos2;
        CellConnectionPos cellPos1, cellPos2;
        Random rand = new Random();

        int x = (int) (rand.nextGaussian() * Integer.MAX_VALUE);
        int y = (int) (rand.nextGaussian() * Integer.MAX_VALUE);
        int z = (int) (rand.nextGaussian() * Integer.MAX_VALUE);
        
        pos1 = new BlockPos(x, y, z);
        pos2 = new BlockPos(x + 1,y,z);
                
        cellPos1 = new CellConnectionPos(pos1, pos2);
        assert(cellPos1.lowerPos.equals(pos1));
        assert(cellPos1.upperPos.equals(pos2));
        assert(cellPos1.getAxis() == EnumFacing.Axis.X);
        
        cellPos2 = new CellConnectionPos(pos2, pos1);
        assert(cellPos2.lowerPos.equals(pos1));
        assert(cellPos2.upperPos.equals(pos2));
        assert(cellPos2.getAxis() == EnumFacing.Axis.X);
        
        assert(cellPos1 != cellPos2);
        
        assert(cellPos1.equals(cellPos2));
        assert(cellPos1.hashCode() == (cellPos2).hashCode());
        
        TreeSet<CellConnectionPos> set = new TreeSet<CellConnectionPos>();
        set.add(cellPos1);
        set.add(cellPos2);
        assert(set.size() == 1);
        
        
        x = (int) (rand.nextGaussian() * Integer.MAX_VALUE);
        y = (int) (rand.nextGaussian() * Integer.MAX_VALUE);
        z = (int) (rand.nextGaussian() * Integer.MAX_VALUE);
        
        pos1 = new BlockPos(x, y, z);
        pos2 = new BlockPos(x,y+1,z);
                
        cellPos1 = new CellConnectionPos(pos1, pos2);
        assert(cellPos1.lowerPos.equals(pos1));
        assert(cellPos1.upperPos.equals(pos2));
        assert(cellPos1.getAxis() == EnumFacing.Axis.Y);
        
        cellPos2 = new CellConnectionPos(pos2, pos1);
        assert(cellPos2.lowerPos.equals(pos1));
        assert(cellPos2.upperPos.equals(pos2));
        assert(cellPos2.getAxis() == EnumFacing.Axis.Y);
        
        assert(cellPos1 != cellPos2);
        
        assert(cellPos1.equals(cellPos2));
        assert(cellPos1.hashCode() == (cellPos2).hashCode());
        
        set.add(cellPos1);
        set.add(cellPos2);
        assert(set.size() == 2);
        
        
        
        x = (int) (rand.nextGaussian() * Integer.MAX_VALUE);
        y = (int) (rand.nextGaussian() * Integer.MAX_VALUE);
        z = (int) (rand.nextGaussian() * Integer.MAX_VALUE);
        
        pos1 = new BlockPos(x, y, z);
        pos2 = new BlockPos(x,y,z+1);
                
        cellPos1 = new CellConnectionPos(pos1, pos2);
        assert(cellPos1.lowerPos.equals(pos1));
        assert(cellPos1.upperPos.equals(pos2));
        assert(cellPos1.getAxis() == EnumFacing.Axis.Z);
        
        cellPos2 = new CellConnectionPos(pos2, pos1);
        assert(cellPos2.lowerPos.equals(pos1));
        assert(cellPos2.upperPos.equals(pos2));
        assert(cellPos2.getAxis() == EnumFacing.Axis.Z);
        
        assert(cellPos1 != cellPos2);
        
        assert(cellPos1.equals(cellPos2));
        assert(cellPos1.hashCode() == (cellPos2).hashCode());
        
        set.add(cellPos1);
        set.add(cellPos2);
        assert(set.size() == 3);
        
    }

}
