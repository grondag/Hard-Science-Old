package grondag.adversity.library.joinstate;

import grondag.adversity.niceblock.modelstate.IModelStateValue;
import grondag.adversity.niceblock.modelstate.ModelStateComponent;
import grondag.adversity.niceblock.modelstate.ModelStateComponents;
import net.minecraft.util.EnumFacing;

public class CornerJoinBlockState implements IModelStateValue<CornerJoinBlockState, CornerJoinBlockState>
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
    public ModelStateComponent<CornerJoinBlockState, CornerJoinBlockState> getComponentType()
    {
        return ModelStateComponents.CORNER_JOIN;
    }

    @Override
    public long getBits()
    {
        return index;
    }

    @Override
    public CornerJoinBlockState getValue()
    {
        return this;
    }    
}