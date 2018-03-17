package grondag.hard_science.superblock.terrain;

import java.util.List;

import grondag.exotic_matter.ConfigXM;
import grondag.exotic_matter.varia.Useful;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.block.SuperSimpleBlock;
import grondag.hard_science.superblock.model.shape.ModelShape;
import grondag.hard_science.superblock.model.state.ModelState;
import grondag.hard_science.superblock.placement.PlacementItem;
import grondag.hard_science.superblock.varia.BlockSubstance;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TerrainDynamicBlock extends SuperSimpleBlock
{
    private final boolean isFiller;
    
    public TerrainDynamicBlock(String blockName, BlockSubstance substance, ModelState defaultModelState, boolean isFiller)
    {
        super(blockName, substance, defaultModelState);
        this.isFiller = isFiller;
        this.metaCount = this.isFiller ? 2 : TerrainState.BLOCK_LEVELS_INT;
        
        // make sure proper shape is set
        ModelState modelState = defaultModelState.clone();
        modelState.setShape(this.isFiller ? ModelShape.TERRAIN_FILLER : ModelShape.TERRAIN_HEIGHT);
        modelState.setStatic(false);
        this.defaultModelStateBits = modelState.serializeToInts();
    }
    
    /** 
     * This is an egregious hack to avoid performance hit of instanceof.
     * (Based on performance profile results.)
     * Returns true if this is a type of IFlowBlock
     */
    @Override
    public boolean isAssociatedBlock(Block other)
    {
        return other == TerrainBlock.FLOW_BLOCK_INDICATOR || super.isAssociatedBlock(other);
    }

    public boolean isFlowFiller()
    {
        return isFiller;
    }

    public boolean isFlowHeight()
    {
        return !isFiller;
    }
  
    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        //see Config.render().enableFaceCullingOnFlowBlocks for explanation
        IBlockState neighborState = blockAccess.getBlockState(pos.offset(side));
        if(ConfigXM.RENDER.enableFaceCullingOnFlowBlocks && TerrainBlock.isFlowBlock(neighborState.getBlock()))
        {
            int myOcclusionKey = this.getOcclusionKey(blockState, blockAccess, pos, side);
            int otherOcclusionKey = ((SuperBlock)neighborState.getBlock()).getOcclusionKey(neighborState, blockAccess, pos.offset(side), side.getOpposite());
            return myOcclusionKey != otherOcclusionKey;
        }
        else
        {
            return !neighborState.doesSideBlockRendering(blockAccess, pos.offset(side), side.getOpposite());
        }
    }

    @Override
    protected List<ItemStack> createSubItems()
    {
        List<ItemStack> items = super.createSubItems();
        
        for(ItemStack stack : items)
        {
            int meta = stack.getMetadata();
            ModelState modelState = PlacementItem.getStackModelState(stack);
            int level = this.isFiller ? TerrainState.BLOCK_LEVELS_INT - 1 : TerrainState.BLOCK_LEVELS_INT - meta;
            int [] quadrants = new int[] {level, level, level, level};
            TerrainState flowState = new TerrainState(level, quadrants, quadrants, 0);
            modelState.setTerrainState(flowState);
            PlacementItem.setStackModelState(stack, modelState);
        }
        return items;
    }

    /**
     * Convert this block to a static version of itself if a static version was given.
     */
    public void makeStatic(IBlockState state, World world, BlockPos pos)
    {
        Block staticVersion = TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.getStaticBlock(this);
        if(staticVersion == null || state.getBlock() != this) return;

        ModelState myModelState = this.getModelStateAssumeStateIsCurrent(state, world, pos, true);
        myModelState.setStatic(true);
        world.setBlockState(pos, staticVersion.getDefaultState()
                .withProperty(SuperBlock.META, state.getValue(SuperBlock.META)), 7);
        ((TerrainStaticBlock)staticVersion).setModelState(world, pos, myModelState);
    }
    
    @Override
    public int quantityDropped(IBlockAccess world, BlockPos pos, IBlockState state)
    {
        double volume = 0;
        ModelState modelState = this.getModelStateAssumeStateIsStale(state, world, pos, true);
        for(AxisAlignedBB box : modelState.getShape().meshFactory().collisionHandler().getCollisionBoxes(modelState))
        {
            volume += Useful.volumeAABB(box);
        }

        return (int) Math.min(9, volume * 9);
    }
 
    @Override
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos)
    {
        return TerrainBlock.isEmpty(worldIn.getBlockState(pos), worldIn, pos);
    }

    @Override
    public boolean isAir(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return TerrainBlock.isEmpty(state, world, pos);
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
    {
        TerrainBlock.freezeNeighbors(world, pos, state);
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }
    
    // setting to false drops AO light value
    @Override
    public boolean isFullCube(IBlockState state)
    {
        // don't have enough information without world access or extended state
        // to determine if is full cube.
        return false;    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        // don't have enough information without world access or extended state
        // to determine if is full cube.
        return false;
    }
    
    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return TerrainBlock.shouldBeFullCube(state, world, pos);
    }
    
    public boolean getUseNeighborBrightness(IBlockState state)
    {
        return true;
    }
}
