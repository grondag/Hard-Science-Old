package grondag.adversity.feature.volcano.lava;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.Iterator;

import gnu.trove.list.TLongList;
import grondag.adversity.feature.volcano.lava.simulator.WorldStateBuffer;
import grondag.adversity.library.PackedBlockPos;
import grondag.adversity.library.Useful;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.support.BlockSubstance;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;

public class LavaTerrainHelper
{

    private final WorldStateBuffer worldBuffer;

    //TODO make configurable
    private static final int RADIUS = 5;
    private static final int MASK_WIDTH = RADIUS * 2 + 1;
    private static final int MASK_SIZE = MASK_WIDTH * MASK_WIDTH;
    
    /** information about positions within the configured radius, order by distance ascending */
    private static final VisibilityNode[] VISIBILITY_NODES;
    private static final float INCREMENT =  1F/(RADIUS * 2 - 1);
    
    private static class VisibilityNode
    {
        public final int bitIndex;
        public final long packedBlockPos;
        public final int xOffset;
        public final int zOffset;
        public final float distance;
        /** position offsets that must be open are true, all others are false */
        public final BitSet visibilityMask;
        
        private VisibilityNode(long packedBlockPos, BitSet visibilityMask)
        {
            this.packedBlockPos = packedBlockPos;
            this.xOffset = PackedBlockPos.getX(packedBlockPos);
            this.zOffset = PackedBlockPos.getZ(packedBlockPos);
            this.bitIndex = getBitIndex(xOffset, zOffset);
            this.distance = (float) Math.sqrt(xOffset * xOffset + zOffset * zOffset);
            this.visibilityMask = visibilityMask;
        }
    }
    
    static
    {
        TLongList circlePositions = Useful.fill2dCircleInPlaneXZ(RADIUS);
        
        long origin = PackedBlockPos.pack(0, 0, 0);
        
        ArrayList<VisibilityNode> result = new ArrayList<VisibilityNode>();

        // start at 1 to skip origin
        for(int i = 1; i < circlePositions.size(); i++)
        {
            BitSet visibilityMask = new BitSet(MASK_SIZE);
            
            TLongList linePositions = Useful.line2dInPlaneXZ(origin, circlePositions.get(i));
            for(int l = 0; l < linePositions.size(); l++)
            {
                visibilityMask.set(getBitIndex(linePositions.get(l)));
            }
            
            result.add(new VisibilityNode(circlePositions.get(i), visibilityMask));
        }
        
        result.sort(new Comparator<VisibilityNode>() 
        {
            @Override
            public int compare(VisibilityNode o1, VisibilityNode o2)
            {
                return Float.compare(o1.distance, o2.distance);
            }
        });
        
        VISIBILITY_NODES = result.toArray(new VisibilityNode[0]);
        
    }
    
    private static final int getBitIndex(long packedBlockPos)
    {
        return getBitIndex(PackedBlockPos.getX(packedBlockPos), PackedBlockPos.getZ(packedBlockPos));
    }
    
    private static final int getBitIndex(int x, int z)
    {
        return (x + RADIUS) * MASK_WIDTH + z + RADIUS;
    }
    
    public LavaTerrainHelper(WorldStateBuffer worldBuffer)
    {
        this.worldBuffer = worldBuffer;
//        this.riseCallBack = new ClosestRiseCallBack();
//        this.fallCallBack = new ClosestFallCallBack();
    }

    /**
     * Returns true if space already contains lava or could contain lava.
     * IOW, like canLavaDisplace except returns true if contains lava height block.
     */
    public boolean isLavaSpace(IBlockState state)
    {
        return state.getBlock() == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK || LavaTerrainHelper.canLavaDisplace(state);
        
    }
    
    /**
     * Returns true if space already contains lava or could contain lava or did contain lava at some point.
     * Used to compute the native (non-flow) smoothed terrain surface irrespective of any solidified lava.
     * IOW, like canLavaDisplace except returns true if contains lava height block.
     */
    public boolean isOpenTerrainSpace(IBlockState state)
    {
        Block block = state.getBlock();
        return block == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK
                || (IFlowBlock.isFlowHeight(block) && ((NiceBlock)block).material == BlockSubstance.BASALT)
                || LavaTerrainHelper.canLavaDisplace(state);
    }
    
//    private class ClosestRiseCallBack implements CircleFillCallBack
//    {
//        protected float distance = -1;
//
//        @Override
//        public boolean handleCircleFill(BlockPos origin, BlockPos pos)
//        {
//            //TODO: this is shitty
//            this.distance = -1;
//            
//            if(!isLavaSpace(world.getBlockState(pos)) && isLavaSpace(world.getBlockState(pos.up())))
//            {
//                // confirm level line of sight
//                int x1 = origin.getX();
//                int x2 = pos.getX();
//                int z1 = origin.getZ();
//                int z2 = pos.getZ();
//                int dz = z2-z1;
//                int dx = x2-x1;
//                int p=dz-dx/2;
//                int z=z1;
//                for (int x=x1; x <=x2; x++) 
//                {
//                    if(!(x == x1 && z == z1) || (x == x2 && z == z2))  
//                    {
//                        if(!isLavaSpace(world.getBlockState(pos)) || isLavaSpace(world.getBlockState(pos.down())))
//                        {
//                            return false;
//                        }
//                    }
//                    if(p > 0) 
//                    {
//                        z++;
//                        p+= dz-dx;
//                    }
//                    else
//                    {
//                        p+= dz;
//                    }
//                }
//
//                distance = (float) Math.sqrt(origin.distanceSq(pos));
////                world.setBlockState(pos.up(), Blocks.LAPIS_BLOCK.getDefaultState());
//                return true;
//            }
//            else
//            {
////                world.setBlockState(pos, Blocks.GLASS.getDefaultState());
//                return false;
//            }
//        }
//    }

//    private class ClosestFallCallBack implements CircleFillCallBack
//    {
//        //TODO: this is shitty
//        protected float distance = -1;
//
//        @Override
//        public boolean handleCircleFill(BlockPos origin, BlockPos pos)
//        {
//            this.distance = -1;
//            if(isLavaSpace(world.getBlockState(pos)) && isLavaSpace(world.getBlockState(pos.down())))
//            {
//                // confirm level line of sight
//                int x1 = origin.getX();
//                int x2 = pos.getX();
//                int z1 = origin.getZ();
//                int z2 = pos.getZ();
//                int dz = z2-z1;
//                int dx = x2-x1;
//                int p=dz-dx/2;
//                int z=z1;
//                for (int x=x1; x <=x2; x++) 
//                {
//                    if(!(x == x1 && z == z1) || (x == x2 && z == z2))  
//                    {
//                        if(!isLavaSpace(world.getBlockState(new BlockPos(x, origin.getY(), z))))
//                        {
//                            return false;
//                        }
//                    }
//                    if(p > 0) 
//                    {
//                        z++;
//                        p+= dz-dx;
//                    }
//                    else
//                    {
//                        p+= dz;
//                    }
//                }
//                
//                distance = (float) Math.sqrt(origin.distanceSq(pos));
////                world.setBlockState(pos.down(), Blocks.LAPIS_BLOCK.getDefaultState());
////                Adversity.log.info("Drop at " + pos.toString());
//                return true;
//            }
//            else
//            {
////                if(isLavaSpace(world.getBlockState(pos)))
////                {
////                    world.setBlockState(pos, Blocks.GLASS.getDefaultState());
////                }
//                return false;
//            }
//        }
//    }


    /**
     * Ideal height of flowing lava retained on base (non-flow) terrain at the given location.  
     * Returned as fraction of 1 block.
     */
    public float computeIdealBaseFlowHeight(long originPackedPos)
    {               
        final float NOT_FOUND = -1;
        float nearestRiseDistance = NOT_FOUND;
        float nearestFallDistance = NOT_FOUND;
        
        /** updated each time we find a visible space as we expand from origin.  If we go 1 or more without a new visible space, walled in and can stop checking */
        float maxVisibleDistance = 0;
        
        BitSet blockedPositions = new BitSet(MASK_SIZE);
        
        for(VisibilityNode node : VISIBILITY_NODES)
        {
            long targetPos = PackedBlockPos.add(originPackedPos, node.packedBlockPos);
        
            boolean isVisible = !node.visibilityMask.intersects(blockedPositions);
            
            
            if(!isVisible) 
            {
                if(node.distance - maxVisibleDistance > 1) break;
            }
            else
            {
                    maxVisibleDistance = node.distance;
            }

            boolean isOpen = isOpenTerrainSpace(worldBuffer.getBlockState(targetPos));
            
            if(!isOpen)
            {
                blockedPositions.set(node.bitIndex);

                if(nearestRiseDistance == NOT_FOUND && isVisible)
                {
                    nearestRiseDistance = node.distance;
                }
            }
            else
            {
                // space is open, check for nearest drop if not already found and position is visible from origin
                if(nearestFallDistance == NOT_FOUND
                        && isVisible 
                        && isOpenTerrainSpace(worldBuffer.getBlockState(PackedBlockPos.down(targetPos))))
                {
                    nearestFallDistance = node.distance;
                    
                }
            }
            
            if(nearestRiseDistance != NOT_FOUND && nearestFallDistance != NOT_FOUND) break;
        }
        
        
//                        distance = (float) Math.sqrt(origin.distanceSq(pos));
//            //            world.setBlockState(pos.down(), Blocks.LAPIS_BLOCK.getDefaultState());
//            //            Adversity.log.info("Drop at " + pos.toString());
//                        return true;
//                    }
//            }
//            }
//            
            if(nearestFallDistance == NOT_FOUND)
            {
                if(nearestRiseDistance == NOT_FOUND)
                {
                    return 1;
                }
                else
                {
                    return Math.max(1, 1.5F - nearestRiseDistance * INCREMENT);
                }
                
            }
            else if(nearestRiseDistance == NOT_FOUND)
            {
                return Math.min(1, 0.5F + (nearestFallDistance - 1) * INCREMENT);
            }

            else
            {
                return 1.5F - (nearestRiseDistance / (nearestRiseDistance + nearestFallDistance - 1));
            }
            
            //RP-D -> R=1, D=2, P=1.5-1/(1+2-1)=1;
            //R-PD -> R=2, D=1, p=1.5-2/(2+1-1)=0.5;
            
            //        up in range, no down = min(1, 1.5. - dist * .1)
            //        down in range, no up = max(1, .5 + (dist - 1) * .1)
            //        up and down in range = 1.5 - (up dist / (combined dist - 1))
    }

    public static boolean canLavaDisplace(IBlockState state)
    {
        Block block = state.getBlock();
        
        if (IFlowBlock.isFlowFiller(block)) return true;

        if (IFlowBlock.isFlowHeight(block)) return false;
        
        //TODO: make material list configurable
   
        Material material = state.getMaterial();
        
        if(material.isReplaceable()) return true;

        if(material == Material.AIR) return true;
        if (material == Material.GRASS ) return false;
        if (material == Material.GROUND ) return false;
        if (material == Material.SAND ) return false;
        if (material == Material.ROCK ) return false;
        if (material == Material.CLAY) return false;
        if (material == Material.DRAGON_EGG ) return false;
        if (material == Material.IRON ) return false;
        if (material == Material.PORTAL ) return false;
        if (material == Material.ANVIL ) return false;
        
        //TODO: remove, is for testing
        if (material == Material.GLASS) return false;

    
        // Volcanic lava don't give no shits about your stuff.
        return true;        
    }
    
    public static String[] generateDefaultDisplaceableList()
    {
        ArrayList<String> results = new ArrayList<String>();
        
        Iterator<Block> blocks = Block.REGISTRY.iterator();
        while(blocks.hasNext())
        {
            Block b = blocks.next();
            @SuppressWarnings("deprecation")
            Material m = b.getMaterial(b.getDefaultState());
            if(m.isLiquid() || m.isReplaceable())
            {
                results.add(b.getRegistryName().toString());
            }
        }
        
        return results.toArray(new String[results.size()]);
        
    }
}
