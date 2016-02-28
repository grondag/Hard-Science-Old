package grondag.adversity.niceblock.newmodel;

import grondag.adversity.niceblock.model.ModelCookbookAxisOriented.AxisAlignedModel;
import net.minecraft.util.EnumWorldBlockLayer;

public class ColumnSquareController extends AxisOrientedController
{
    public ColumnSquareController(String textureName, int alternateTextureCount, boolean isShaded)
    {
        super(textureName, alternateTextureCount, EnumWorldBlockLayer.CUTOUT, isShaded);
        this.bakedModelFactory = new ColumnSquareModelFactory(this);
        this.textureCount = 9;
    }

    @Override
    protected void populateModelNames()
    {
        modelNames[AxisAlignedModel.FOUR_CLOSED.index] = "adversity:block/column_four_faces_full";
        modelNames[AxisAlignedModel.FOUR_TOP_CLOSED.index] = "adversity:block/column_four_faces_half";
        modelNames[AxisAlignedModel.FOUR_OPEN.index] = "adversity:block/column_four_faces";

        modelNames[AxisAlignedModel.THREE_CLOSED.index] = "adversity:block/column_three_faces_full";
        modelNames[AxisAlignedModel.THREE_TOP_CLOSED.index] = "adversity:block/column_three_faces_half";
        modelNames[AxisAlignedModel.THREE_OPEN.index] = "adversity:block/column_three_faces";

        modelNames[AxisAlignedModel.TWO_ADJACENT_CLOSED.index] = "adversity:block/column_adjacent_faces_full";
        modelNames[AxisAlignedModel.TWO_ADJACENT_TOP_CLOSED.index] = "adversity:block/column_adjacent_faces_half";
        modelNames[AxisAlignedModel.TWO_ADJACENT_OPEN.index] = "adversity:block/column_adjacent_faces";

        modelNames[AxisAlignedModel.TWO_OPPOSITE_CLOSED.index] = "adversity:block/column_opposite_faces_full";
        modelNames[AxisAlignedModel.TWO_OPPOSITE_TOP_CLOSED.index] = "adversity:block/column_opposite_faces_half";
        modelNames[AxisAlignedModel.TWO_OPPOSITE_OPEN.index] = "adversity:block/column_opposite_faces";

        modelNames[AxisAlignedModel.ONE_CLOSED.index] = "adversity:block/column_single_face_full";
        modelNames[AxisAlignedModel.ONE_TOP_CLOSED.index] = "adversity:block/column_single_face_half";
        modelNames[AxisAlignedModel.ONE_OPEN.index] = "adversity:block/column_single_face";

        modelNames[AxisAlignedModel.NONE_CLOSED.index] = "adversity:block/column_no_faces_full";
        modelNames[AxisAlignedModel.NONE_TOP_CLOSED.index] = "adversity:block/column_no_faces_half";
        modelNames[AxisAlignedModel.NONE_OPEN.index] = "adversity:block/column_no_faces";
    }
}
