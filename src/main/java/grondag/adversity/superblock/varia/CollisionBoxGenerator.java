package grondag.adversity.superblock.varia;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.adversity.library.render.RawQuad;
import grondag.adversity.library.render.Vertex;
import grondag.adversity.library.varia.VoxelBitField;
import grondag.adversity.library.varia.VoxelBitField.VoxelBox;
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

    public static List<AxisAlignedBB> makeCollisionBoxList(List<RawQuad> quads)
    {
        if(quads.isEmpty())
        {
            return Collections.emptyList();
        }

        // voxel method
        List<AxisAlignedBB> retVal = makeBoxVoxelMethod(quads);
        List<AxisAlignedBB> simple = Collections.singletonList(makeBoxSimpleMethod(quads));

        double simpleVolume = getListVolume(simple);
        double voxelVolume = getListVolume(retVal);
        //use simple volume if voxel gives empty box, or if simple is not much bigger
        if(voxelVolume == 0.0 || ((simpleVolume - voxelVolume) / voxelVolume) < 0.1)
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

        if(quads.isEmpty()) return Collections.emptyList();
        
        ImmutableList.Builder<AxisAlignedBB> retVal = new ImmutableList.Builder<AxisAlignedBB>();

        VoxelBitField voxels = new VoxelBitField(3);

        
        for(int x = 0; x < 8; x++)
        {
            for(int y = 0; y < 8; y++)
            {
                for(int z = 0; z < 8; z++)
                {
                    if(isVoxelPresent(x, y, z, quads))
                    {
                        voxels.setFilled(x, y, z, true);
                    }
                }
            }
        }

        // Handle special case of thin horizontal layer at bottom.
        // Would be better to use Separating Axis Theorem tests to cover more edge cases,
        // but not willing to take time to implement that right now.

        for(RawQuad quad : quads)
        {                  
            if(quad.isOnFace(EnumFacing.DOWN))
            {
                for(int x = 0; x < 8; x++)
                {
                    for(int z = 0; z < 8; z++)
                    {
                        if(quad.containsPoint(new Vec3d((x+0.5)/8.0, 0.0, (z+0.5)/8.0)))
                        {
                            voxels.setFilled(x, 0, z, true);
                        }
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
    
    private static boolean isVoxelPresent(double x, double y, double z, List<RawQuad> quads)
    {
        Vec3d point = new Vec3d((x+0.5)/8.0, (y+0.5)/8.0, (z+0.5)/8.0);
        return isPointEnclosed(point, quads);
    }
    
    private static final Vec3d VOXEL_TEST_RAY = new Vec3d(5525, 13123, 7435);
    private static boolean isPointEnclosed(Vec3d point, List<RawQuad> quads)
    {
        int intersectionCount = 0;
        for(RawQuad quad : quads)
        {                  
            if(quad.intersectsWithRay(point, VOXEL_TEST_RAY)) intersectionCount++;
        }          
        return (intersectionCount & 0x1) == 1;
    }
    
    public static AxisAlignedBB makeBoxSimpleMethod(List<RawQuad> quads)
    {
        double minX = 1.0, minY = 1.0, minZ = 1.0, maxX = 0, maxY = 0, maxZ = 0;

        for(RawQuad quad : quads)
        {
            for(int i = 0; i < quad.getVertexCount(); i++)
            {
                Vertex v = quad.getVertex(i);
                if(v.xCoord > maxX) maxX = v.xCoord;
                if(v.yCoord > maxY) maxY = v.yCoord;
                if(v.zCoord > maxZ) maxZ = v.zCoord;
                if(v.xCoord < minX) minX = v.xCoord;
                if(v.yCoord < minY) minY = v.yCoord;
                if(v.zCoord < minZ) minZ = v.zCoord;
            }
        }

        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }
}