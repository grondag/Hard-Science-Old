package grondag.adversity.niceblock.newmodel;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.BlockRenderLayer;

import net.minecraft.world.IBlockAccess;

public class HotBasaltController extends ColoredBlockController
{
    
    public HotBasaltController()
    {
        super("hot_basalt", 4, BlockRenderLayer.TRANSLUCENT, false, true);
        textureCount = 4;
    }
    
    /**
     * used by model for texture lookup
     */
    @Override
    public String getTextureName(int offset)
    {
        return "adversity:blocks/hot_basalt_" + ((offset >> 2) & 0x3) + "_0_" + (offset & 0x3);
    }

    public int getTextureOffsetFromShapeIndex(int shapeIndex) {
        return (shapeIndex >> 2) & 15;
    }
    
    public Rotation getTextureRotationFromShapeIndex(int shapeIndex){
        return Rotation.values()[shapeIndex & 3];
    }
    
    /**
     * in LSB order:
     * 2 bits rotation          | derived from alternator
     * 2 bits alternate texture | derived from alternator
     * 2 bits texture set       | derived from block metadata (hotness)
     */
    @Override
    public int getClientShapeIndex(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return ((state.getValue(NiceBlock.META) & 3) << 4) |  alternator.getAlternate(pos);
    }
    
    @Override
    public int getShapeCount()
    {
        return 64;
    }
}