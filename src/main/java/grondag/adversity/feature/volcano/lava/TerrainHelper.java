package grondag.adversity.feature.volcano.lava;

import grondag.adversity.Adversity;
import grondag.adversity.feature.volcano.LavaManager;
import grondag.adversity.library.Useful;
import grondag.adversity.library.Useful.CircleFillCallBack;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TerrainHelper
{

    private final World world;
    private final ClosestRiseCallBack riseCallBack;
    private final ClosestFallCallBack fallCallBack;

    public TerrainHelper(World world)
    {
        this.world = world;
        this.riseCallBack = new ClosestRiseCallBack();
        this.fallCallBack = new ClosestFallCallBack();
    }

    public boolean isLavaSpace(IBlockState state)
    {
        return state.getBlock() == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK || LavaManager.canDisplace(state);
        
    }
    
    private class ClosestRiseCallBack implements CircleFillCallBack
    {
        protected float distance = -1;

        @Override
        public boolean handleCircleFill(BlockPos origin, BlockPos pos)
        {
            //TODO: this is shitty
            this.distance = -1;
            
            if(!isLavaSpace(world.getBlockState(pos)) && isLavaSpace(world.getBlockState(pos.up())))
            {
                // confirm level line of sight
                int x1 = origin.getX();
                int x2 = pos.getX();
                int z1 = origin.getZ();
                int z2 = pos.getZ();
                int dz = z2-z1;
                int dx = x2-x1;
                int p=dz-dx/2;
                int z=z1;
                for (int x=x1; x <=x2; x++) 
                {
                    if(!(x == x1 && z == z1) || (x == x2 && z == z2))  
                    {
                        if(!isLavaSpace(world.getBlockState(pos)) || isLavaSpace(world.getBlockState(pos.down())))
                        {
                            return false;
                        }
                    }
                    if(p > 0) 
                    {
                        z++;
                        p+= dz-dx;
                    }
                    else
                    {
                        p+= dz;
                    }
                }

                distance = (float) Math.sqrt(origin.distanceSq(pos));
//                world.setBlockState(pos.up(), Blocks.LAPIS_BLOCK.getDefaultState());
                return true;
            }
            else
            {
//                world.setBlockState(pos, Blocks.GLASS.getDefaultState());
                return false;
            }
        }
    }

    private class ClosestFallCallBack implements CircleFillCallBack
    {
        //TODO: this is shitty
        protected float distance = -1;

        @Override
        public boolean handleCircleFill(BlockPos origin, BlockPos pos)
        {
            this.distance = -1;
            if(isLavaSpace(world.getBlockState(pos)) && isLavaSpace(world.getBlockState(pos.down())))
            {
                // confirm level line of sight
                int x1 = origin.getX();
                int x2 = pos.getX();
                int z1 = origin.getZ();
                int z2 = pos.getZ();
                int dz = z2-z1;
                int dx = x2-x1;
                int p=dz-dx/2;
                int z=z1;
                for (int x=x1; x <=x2; x++) 
                {
                    if(!(x == x1 && z == z1) || (x == x2 && z == z2))  
                    {
                        if(!isLavaSpace(world.getBlockState(new BlockPos(x, origin.getY(), z))))
                        {
//                            world.setBlockState(pos, Blocks.GLASS.getDefaultState());
                            return false;
                        }
                    }
                    if(p > 0) 
                    {
                        z++;
                        p+= dz-dx;
                    }
                    else
                    {
                        p+= dz;
                    }
                }
                
                distance = (float) Math.sqrt(origin.distanceSq(pos));
//                world.setBlockState(pos.down(), Blocks.LAPIS_BLOCK.getDefaultState());
//                Adversity.log.info("Drop at " + pos.toString());
                return true;
            }
            else
            {
//                if(isLavaSpace(world.getBlockState(pos)))
//                {
//                    world.setBlockState(pos, Blocks.GLASS.getDefaultState());
//                }
                return false;
            }
        }
    }


    public float computeIdealBaseFlowHeight(BlockPos pos)
    {
        //TODO make configurable, static
        final int RADIUS = 5;
        final float INCREMENT =  1F/(RADIUS * 2 - 1);
        
//        Adversity.log.info("INCREMENT = " + INCREMENT);
        
        Useful.fill2dCircleInPlaneXZ(pos, RADIUS, this.fallCallBack);
        Useful.fill2dCircleInPlaneXZ(pos, RADIUS, this.riseCallBack);

//        Adversity.log.info("fall distance = " + this.fallCallBack.distance + ", rise distance = " + this.riseCallBack.distance);
        
        if(this.fallCallBack.distance == -1)
        {
            if(this.riseCallBack.distance == -1)
            {
                return 1;
            }
            else
            {
                return Math.max(1, 1.5F - this.riseCallBack.distance * INCREMENT);
            }
            
        }
        else if(this.riseCallBack.distance == -1)
        {
            return Math.min(1, 0.5F + (this.fallCallBack.distance - 1) * INCREMENT);
        }
//        else if(riseCallBack.distance == 1 && fallCallBack.distance == 1)
//        {
//            return 1F;
//        }
        else
        {
            return 1.5F - (riseCallBack.distance / (riseCallBack.distance + fallCallBack.distance - 1));
        }
        
        //RP-D -> R=1, D=2, P=1.5-1/(1+2-1)=1;
        //R-PD -> R=2, D=1, p=1.5-2/(2+1-1)=0.5;
        
        //        up in range, no down = min(1, 1.5. - dist * .1)
                //        down in range, no up = max(1, .5 + (dist - 1) * .1)
        //        up and down in range = 1.5 - (up dist / (combined dist - 1))

    }



}
