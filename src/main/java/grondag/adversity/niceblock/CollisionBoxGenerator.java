package grondag.adversity.niceblock;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.adversity.Adversity;
import grondag.adversity.library.model.quadfactory.QuadFactory;
import grondag.adversity.library.model.quadfactory.RawQuad;
import grondag.adversity.niceblock.VoxelBitField.VoxelBox;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public class CollisionBoxGenerator
{

    private final static int STOP_UP = 1 << EnumFacing.UP.ordinal();
    private final static int STOP_DOWN = 1 << EnumFacing.DOWN.ordinal();
    private final static int STOP_EAST = 1 << EnumFacing.EAST.ordinal();
    private final static int STOP_WEST = 1 << EnumFacing.WEST.ordinal();
    private final static int STOP_NORTH = 1 << EnumFacing.NORTH.ordinal();
    private final static int STOP_SOUTH = 1 << EnumFacing.SOUTH.ordinal();

    public static List<AxisAlignedBB> makeCollisionBox(List<RawQuad> quads)
    {
        if(quads.isEmpty())
        {
            return new ImmutableList.Builder<AxisAlignedBB>().add(new AxisAlignedBB(0, 0, 0, 1, 1, 1)).build();
        }

        // voxel method
        List<AxisAlignedBB> retVal = makeBoxVoxelMethod(quads);

        List<AxisAlignedBB> simple = makeBoxSimpleMethod(quads);

        if(retVal.isEmpty() || getListVolume(simple) <= getListVolume(retVal))
        {
            retVal = simple;
        }

        return retVal;
    }

    private static double getListVolume(List<AxisAlignedBB> list)
    {
        double retVal = 0;
        for(AxisAlignedBB box : list)
        {
            retVal += (box.maxX - box.minX) * (box.maxY - box.minY) * (box.maxZ - box.minZ);
        }
        return retVal;
    }

    private static List<AxisAlignedBB> makeBoxVoxelMethod(List<RawQuad> quads)
    {

        ImmutableList.Builder<AxisAlignedBB> retVal = new ImmutableList.Builder<AxisAlignedBB>();
        Vec3d ray = new Vec3d(5525, 13123, 7435);

        VoxelBitField voxels = new VoxelBitField(3);

        for(int x = 0; x < 8; x++)
        {
            for(int y = 0; y < 8; y++)
            {
                for(int z = 0; z < 8; z++)
                {
                    Vec3d point = new Vec3d((x+0.5)/8.0, (y+0.5)/8.0, (z+0.5)/8.0);
                    int intersectionCount = 0;
                    for(RawQuad quad : quads)
                    {                  
                        if(quad.intersectsWithRay(point, ray)) intersectionCount++;
                    }          
                    if((intersectionCount & 0x1) == 1)
                    {
                        voxels.setFilled(x, y, z, true);
                    }
                }
            }
        }

        // use bigger voxels if doesn't inflate the volume too much
        VoxelBitField simplified =  voxels.simplify();
        //     Adversity.log.info("v:" + simplified.getFilledRatio() + " d:" + simplified.getDiscardedVolume() + " i:" + simplified.getInflatedVolume());
        if(simplified.getFilledRatio() > 0 && (simplified.getDiscardedVolume() / simplified.getFilledRatio()) < 0.1 && simplified.getInflatedVolume() < 0.05) 
        {
            voxels = simplified;
        }

        VoxelBitField boxFodder = voxels.clone();
        boxFodder.setOrigin(voxels.getBitsPerAxis() / 2, 0, voxels.getBitsPerAxis() / 2);
        while(!boxFodder.areAllBitsConsumed())
        {
            VoxelBox box = boxFodder.getNearestVoxelBox();

            //should never happen, but need to stop loop if it somehow does
            if(box == null) break; 

            int stoppedFlags = 0;

            while(stoppedFlags < (1 << 6) - 1)
            {
                if((stoppedFlags & STOP_EAST) == 0 && !box.expandIfPossible(EnumFacing.EAST)) stoppedFlags |= STOP_EAST;
                if((stoppedFlags & STOP_SOUTH) == 0 && !box.expandIfPossible(EnumFacing.SOUTH)) stoppedFlags |= STOP_SOUTH;
                if((stoppedFlags & STOP_UP) == 0 && !box.expandIfPossible(EnumFacing.UP)) stoppedFlags |= STOP_UP;
                if((stoppedFlags & STOP_WEST) == 0 && !box.expandIfPossible(EnumFacing.WEST)) stoppedFlags |= STOP_WEST;
                if((stoppedFlags & STOP_NORTH) == 0 && !box.expandIfPossible(EnumFacing.NORTH)) stoppedFlags |= STOP_NORTH;
                if((stoppedFlags & STOP_DOWN) == 0 && !box.expandIfPossible(EnumFacing.DOWN)) stoppedFlags |= STOP_DOWN;
            }

            // double to force floating point values in call below - otherwise lower bound is always at 0.
            double bpa = voxels.getBitsPerAxis();

            retVal.add(new AxisAlignedBB(box.getMinX()/bpa, box.getMinY()/bpa, box.getMinZ()/bpa,
                    (box.getMaxX()+1.0)/bpa, (box.getMaxY()+1.0)/bpa, (box.getMaxZ()+1.0)/bpa));
        }

        return retVal.build();
    }

    private static List<AxisAlignedBB> makeBoxSimpleMethod(List<RawQuad> quads)
    {

        ImmutableList.Builder<AxisAlignedBB> retVal = new ImmutableList.Builder<AxisAlignedBB>();

        AxisAlignedBB simpleBox = null;
        for(RawQuad quad : quads)
        {
            if(simpleBox == null)
            {
                simpleBox = quad.getAABB();
            }
            else
            {
                simpleBox = simpleBox.union(quad.getAABB());
            }
        }

        retVal.add(simpleBox);
        return retVal.build();
    }
}
