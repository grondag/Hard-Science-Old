package grondag.hard_science.superblock.placement;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.init.ModBlocks;
import grondag.hard_science.library.world.BlockCorner;
import grondag.hard_science.library.world.NeighborBlocks;
import grondag.hard_science.library.world.WorldHelper;
import grondag.hard_science.player.ModPlayerCaps;
import grondag.hard_science.library.world.NeighborBlocks.NeighborTestResults;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.items.SuperItemBlock;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.varia.BlockTests;
import jline.internal.Log;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class CubicPlacementHandler implements IPlacementHandler
{
    public static final CubicPlacementHandler INSTANCE = new CubicPlacementHandler();

    @Override
    public List<Pair<BlockPos, ItemStack>> getPlacementResults(EntityPlayer playerIn, World worldIn, BlockPos posOn, EnumHand hand, EnumFacing facing, float hitX,
            float hitY, float hitZ, ItemStack stack)
    {
        if(!(stack.getItem() instanceof SuperItemBlock)) return Collections.emptyList();

        SuperItemBlock item = (SuperItemBlock)stack.getItem();
        PlacementMode placementMode = item.getMode(stack);
        
        SuperBlock stackBlock = (SuperBlock) item.getBlock();
        
        ModelState stackModelState = SuperItemBlock.getModelStateFromStack(stack);
        ItemStack result = stack.copy();
        IBlockState blockStateOn = worldIn.getBlockState(posOn);
        Block onBlock = blockStateOn.getBlock();
        BlockPos posPlaced = (onBlock != ModBlocks.virtual_block && onBlock.isReplaceable(worldIn, posOn)) ? posOn : posOn.offset(facing);
        
        // abort if target space is occupied
        if(!WorldHelper.isBlockReplaceable(worldIn, posPlaced, stackBlock != ModBlocks.virtual_block))
        {
            return Collections.emptyList();
        }

        ModelState closestModelState = null;
        if(placementMode == PlacementMode.MATCH_CLOSEST)
        {
            if(onBlock instanceof SuperBlock)
            {
                closestModelState = ((SuperBlock)onBlock).getModelState(worldIn, posOn, true);
            }
            else
            {
                Vec3d location = new Vec3d(posOn.getX() + hitX, posOn.getY() + hitY, posOn.getZ() + hitZ);
                double closestDistSq = Double.MAX_VALUE;
                for(int x = -1; x <= 1; x++)
                {
                    for(int y = -1; y <= 1; y++)
                    {
                        for(int z = -1; z <= 1; z++)
                        {
                            if((x | y | z) != 0)
                            {
                                BlockPos testPos = posOn.add(x, y, z);
                                IBlockState testBlockState = worldIn.getBlockState(testPos);
                                if(testBlockState.getBlock() instanceof SuperBlock)
                                {
                                    double distSq = location.squareDistanceTo(posOn.getX() + 0.5 + x, posOn.getY() + 0.5 + y, posOn.getZ() + 0.5 + z);
                                    if(distSq < closestDistSq)
                                    {
                                        SuperBlock testBlock = (SuperBlock)testBlockState.getBlock();
                                        ModelState testModelState = testBlock.getModelState(worldIn, testPos, true);
                                        if(testModelState.getShape() == stackModelState.getShape())
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
        }
        
        boolean isRotationDone = false;
        
        if(stackModelState.hasAxis())
        {
            if(placementMode == PlacementMode.STATIC)
            {
                stackModelState.setAxis(item.getFace(stack).getAxis());
                if(stackModelState.hasAxisOrientation())
                {
                    stackModelState.setAxisInverted(item.getFace(stack).getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE);
                }
            }
            else
            {
                boolean isAxisDone = false;
                
                if(placementMode == PlacementMode.MATCH_CLOSEST && closestModelState != null)
                {
                    stackModelState.setAxis(closestModelState.getAxis());
                    if(stackModelState.hasAxisOrientation())
                    {
                        stackModelState.setAxisInverted(closestModelState.isAxisInverted());
                    }
                    isAxisDone = true;
                }

                if(!isAxisDone)
                {
                    if(stackModelState.isAxisOrthogonalToPlacementFace())
                    {
                        // handle hit-sensitive placement for stairs, wedges
                        EnumFacing adjacentFace = WorldHelper.closestAdjacentFace(facing, hitX, hitY, hitZ);
                        
                        
                        BlockCorner corner = BlockCorner.find(facing.getOpposite(), adjacentFace);
                        
                        if(corner == null)
                        {
                            Log.warn("Unable to find block corner from placement. This is very strange but probably harmless.");
                        }
                        else
                        {
                            stackModelState.setAxis(corner.orthogonalAxis);
                        
                            if(stackModelState.hasAxisRotation())
                            {
                                stackModelState.setAxisRotation(corner.modelRotation);
                                isRotationDone = true;
                            }
                        }
                    }
                    else
                    {
                        stackModelState.setAxis(facing.getAxis());
                        if(stackModelState.hasAxisOrientation())
                        {
                            stackModelState.setAxisInverted(facing.getAxisDirection() == AxisDirection.NEGATIVE);
                        }
                    }
                }
            }
        }
        
        if(!isRotationDone && stackModelState.hasAxisRotation())
        {
            if(placementMode == PlacementMode.MATCH_CLOSEST && closestModelState != null)
            {
                stackModelState.setAxisRotation(closestModelState.getAxisRotation());
            }
            else
            {
                stackModelState.setAxisRotation(item.getRotation(stack));
            }
        }
        
        if(stackModelState.hasSpecies())
        {
            int species = getSpecies(playerIn, worldIn, posOn, blockStateOn, onBlock, posPlaced, stackBlock, stackModelState);
            stackModelState.setSpecies(species);
            result.setItemDamage(stackModelState.getMetaData());
        }
        SuperItemBlock.setModelState(result, stackModelState);
 
        return ImmutableList.of(Pair.of(posPlaced, result));
    }

    
    private int getSpecies(EntityPlayer player, World worldIn, BlockPos posOn, IBlockState blockStateOn, Block blockOn, BlockPos posPlaced, SuperBlock myBlock, ModelState myModelState)
    {
        // If player is sneaking, force no match to adjacent species.
        // If not sneaking, try to match block on which placed, or failing that, any adjacent block it can match.
        if(ModPlayerCaps.isPlacementModifierOn(player))
        {
            // Force non-match of species for any neighboring blocks
            int speciesInUseFlags = 0;

            NeighborBlocks neighbors = new NeighborBlocks(worldIn, posPlaced, false);
            NeighborTestResults results = neighbors.getNeighborTestResults(new BlockTests.SuperBlockBorderMatch(myBlock, myModelState, false));
            
            for(EnumFacing face : EnumFacing.VALUES)            
            {
                if (results.result(face)) 
                {
                    speciesInUseFlags |= (1 << neighbors.getModelState(face).getSpecies());
                }
            }

            // try to avoid corners also if picking a species that won't connect
            for(BlockCorner corner : BlockCorner.values())
            {
                if (results.result(corner)) 
                {
                    speciesInUseFlags |= (1 << neighbors.getModelState(corner).getSpecies());
                }
            }

            // now randomly choose a species 
            //that will not connect to what is surrounding
            int salt = ThreadLocalRandom.current().nextInt(16);
            for(int i = 0; i < 16; i++)
            {
                int candidate = (i + salt) % 16;
                if((speciesInUseFlags & (1 << candidate)) == 0)
                {
                    return candidate;
                }
            }
            
            // give up
            return 0;
        }
        else
        {
            // try to match block placed on
            if(blockOn == myBlock)
            {
                ModelState modelStateOn = ((SuperBlock)blockOn).getModelStateAssumeStateIsCurrent(blockStateOn, worldIn, posOn, true);
                if(myModelState.doShapeAndAppearanceMatch(modelStateOn)) return modelStateOn.getSpecies();

            }
            
            // try to match an adjacent block
            NeighborBlocks neighbors = new NeighborBlocks(worldIn, posPlaced, false);
            NeighborTestResults results = neighbors.getNeighborTestResults(new BlockTests.SuperBlockBorderMatch(myBlock, myModelState, false));
            
            for(EnumFacing face : EnumFacing.VALUES)            
            {
                if (results.result(face)) 
                {
                    return neighbors.getModelState(face).getSpecies();
                }
            }
            
            // give up
            return 0;
        }
    }
}
