package grondag.hard_science.superblock.block;

import grondag.exotic_matter.model.BlockSubstance;
import grondag.exotic_matter.model.ISuperModelState;
import grondag.hard_science.superblock.model.state.WorldLightOpacity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * For worldgen blocks and other blocks that cannot be configured by user.
 * @author grondag
 *
 */
public class SuperSimpleBlock extends SuperBlock
{
    protected final BlockSubstance substance;
    private final boolean isGeometryFullCube;
    private final WorldLightOpacity worldLightOpacity;
    
    public SuperSimpleBlock(String blockName, BlockSubstance substance, ISuperModelState defaultModelState)
    {
        super(blockName, substance.material, defaultModelState, null);
        this.substance = substance;
        this.blockHardness = substance.hardness;
        this.blockResistance = substance.resistance;
        this.setHarvestLevel(substance.harvestTool, substance.harvestLevel);
        
        this.isGeometryFullCube = defaultModelState.isCube();
        this.worldLightOpacity = WorldLightOpacity.getClosest(substance, defaultModelState);
        this.metaCount = 1;
    }

    @Override
    public BlockSubstance getSubstance(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return this.substance;
    }

    @Override
    public boolean isGeometryFullCube(IBlockState state)
    {
        return this.isGeometryFullCube;
    }

    @Override
    public boolean isHypermatter()
    {
        return this.substance.isHyperMaterial;
    }

    @Override
    protected WorldLightOpacity worldLightOpacity(IBlockState state)
    {
        return this.worldLightOpacity;
    }

}
