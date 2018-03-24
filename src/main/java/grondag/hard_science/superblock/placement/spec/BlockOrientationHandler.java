package grondag.hard_science.superblock.placement.spec;

import grondag.exotic_matter.block.SuperBlockStackHelper;
import grondag.exotic_matter.model.ISuperBlock;
import grondag.exotic_matter.model.ISuperModelState;
import grondag.exotic_matter.placement.PlacementPosition;
import grondag.exotic_matter.world.BlockCorner;
import grondag.exotic_matter.world.Rotation;
import grondag.exotic_matter.world.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Handles configuration of block orientation
 * before placement based on stack settings and placement context.
 */
public class BlockOrientationHandler
{
    /**
     * Updates model state in provided stack if necessary based on other inputs.
     * Prior to calling, should already be verified that onFace and onPos can be placed on (are solid)
     * and that the space in which the block will be placed is empty or replaceable.
     */
    public static void configureStackForPlacement(ItemStack stack, EntityPlayer player, PlacementPosition pPos)
    {
        // does not attempt to configure non super-blocks
        if(!(stack.getItem() instanceof PlacementItem)) return;

        PlacementItem item = (PlacementItem)stack.getItem();
        
        if(item.isBlockOrientationFixed(stack))
        {
            applyFixedOrientation(stack);
        }
        else if(item.isBlockOrientationMatchClosest(stack))
        {
            applyClosestOrientation(stack, player, pPos);
        }
        else if(item.isBlockOrientationDynamic(stack))
        {
            applyDynamicOrientation(stack, player, pPos);
        }
    }

    private static void applyFixedOrientation(ItemStack stack)
    {
        if(!PlacementItem.isPlacementItem(stack)) return;
        
        PlacementItem item = (PlacementItem)stack.getItem();
        
        ISuperModelState modelState = SuperBlockStackHelper.getStackModelState(stack);

        if(modelState.hasAxis())
        {
            modelState.setAxis(item.getBlockPlacementAxis(stack));

            if(modelState.hasAxisOrientation())
            {
                modelState.setAxisInverted(item.getBlockPlacementAxisIsInverted(stack));
            }
        }
        if(modelState.hasAxisRotation())
        {
            modelState.setAxisRotation(item.getBlockPlacementRotation(stack));
        }
        
        SuperBlockStackHelper.setStackModelState(stack, modelState);
    }

    private static void applyClosestOrientation(ItemStack stack, EntityPlayer player, PlacementPosition pPos)
    {
        // find closest instance, starting with block placed on
        ISuperModelState outputModelState = SuperBlockStackHelper.getStackModelState(stack);
        ISuperModelState closestModelState = null;
        World world = player.world;
        IBlockState onBlockState = world.getBlockState(pPos.onPos);
        Block onBlock = onBlockState.getBlock();

        if(onBlock instanceof ISuperBlock)
        {
            closestModelState = ((ISuperBlock)onBlock).getModelState(world, pPos.onPos, true);

            //can't use onBlock as reference if is of a different type
            if(closestModelState.getShape() != outputModelState.getShape()) closestModelState = null;
        }

        // block placed on was bust, so look around
        if(closestModelState == null)
        {
            Vec3d location = new Vec3d(
                    pPos.onPos.getX() + pPos.hitX, 
                    pPos.onPos.getY() + pPos.hitY, 
                    pPos.onPos.getZ() + pPos.hitZ);

            double closestDistSq = Double.MAX_VALUE;
            for(int x = -1; x <= 1; x++)
            {
                for(int y = -1; y <= 1; y++)
                {
                    for(int z = -1; z <= 1; z++)
                    {
                        if((x | y | z) != 0)
                        {
                            BlockPos testPos = pPos.onPos.add(x, y, z);
                            IBlockState testBlockState = world.getBlockState(testPos);
                            if(testBlockState.getBlock() instanceof ISuperBlock)
                            {
                                double distSq = location.squareDistanceTo(
                                        pPos.onPos.getX() + 0.5 + x, 
                                        pPos.onPos.getY() + 0.5 + y, 
                                        pPos.onPos.getZ() + 0.5 + z);
                                if(distSq < closestDistSq)
                                {
                                    ISuperBlock testBlock = (ISuperBlock)testBlockState.getBlock();
                                    ISuperModelState testModelState = testBlock.getModelState(world, testPos, true);
                                    if(testModelState.getShape() == outputModelState.getShape())
                                    {
                                        closestDistSq = distSq;
                                        closestModelState = testModelState;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // if didn't find any matching shapes nearby, fall back to dyanmic orientation
        if(closestModelState == null)
        {
            applyDynamicOrientation(stack, player, pPos);
        }
        else
        {
            if(outputModelState.hasAxis())
            {
                outputModelState.setAxis(closestModelState.getAxis());

                if(outputModelState.hasAxisOrientation())
                {
                    outputModelState.setAxisInverted(closestModelState.isAxisInverted());
                }
            }
            if(outputModelState.hasAxisRotation())
            {
                outputModelState.setAxisRotation(closestModelState.getAxisRotation());
            }
        }
        
        SuperBlockStackHelper.setStackModelState(stack, outputModelState);
    }

    /** handle hit-sensitive placement for stairs, wedges */
    public static void applyDynamicOrientation(ItemStack stack, EntityPlayer player, PlacementPosition pPos)
    {
        ISuperModelState outputModelState = SuperBlockStackHelper.getStackModelState(stack);

        boolean isRotationDone = false;

        if(outputModelState.isAxisOrthogonalToPlacementFace())
        {
            EnumFacing adjacentFace = WorldHelper.closestAdjacentFace(
                    pPos.onFace, 
                    (float)pPos.hitX, 
                    (float)pPos.hitY, 
                    (float)pPos.hitZ);

            BlockCorner corner = BlockCorner.find(pPos.onFace.getOpposite(), adjacentFace);

            if(corner == null)
            {
                assert false : "Unable to find block corner from placement. This is very strange but probably harmless.";
            }
            else
            {
                outputModelState.setAxis(corner.orthogonalAxis);

                if(outputModelState.hasAxisRotation())
                {
                    outputModelState.setAxisRotation(corner.modelRotation);
                    isRotationDone = true;
                }
            }
        }
        else
        {
            outputModelState.setAxis(pPos.onFace.getAxis());
            if(outputModelState.hasAxisOrientation())
            {
                outputModelState.setAxisInverted(pPos.onFace.getAxisDirection() == AxisDirection.NEGATIVE);
            }
        }

        if(!isRotationDone && outputModelState.hasAxisRotation())
        {
            outputModelState.setAxisRotation(
                    Rotation.fromHorizontalFacing(player.getHorizontalFacing().getOpposite()));
        }
        
        SuperBlockStackHelper.setStackModelState(stack, outputModelState);
    }
}

