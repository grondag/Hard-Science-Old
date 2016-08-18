package grondag.adversity.library.joinstate;

import grondag.adversity.library.IBlockTest;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.modelstate.IModelStateComponent;
import grondag.adversity.niceblock.modelstate.ModelStateComponentType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class CornerJoinBlockState implements IModelStateComponent<CornerJoinBlockState>
{
    private final int index;
    
    private byte faceJoinIndex[] = new byte[EnumFacing.values().length];
    
    CornerJoinBlockState(int index)
    {
        this.index = index;
    }
    
    public int getIndex()
    {
        return index;
    }
    
    void setFaceJoinState(EnumFacing face, CornerJoinFaceState state)
    {
        faceJoinIndex[face.ordinal()]=(byte)state.ordinal();
    }
    
    public CornerJoinFaceState getFaceJoinState(EnumFacing face)
    {
        return CornerJoinFaceState.values()[faceJoinIndex[face.ordinal()]];
    }

    @Override
    public ModelStateComponentType getComponentType()
    {
        return ModelStateComponentType.CORNER_JOIN;
    }

    @Override
    public long getBits()
    {
        return index;
    }    
}