package grondag.adversity.superblock.model.painter;

import java.util.List;

import grondag.adversity.library.Rotation;
import grondag.adversity.library.model.quadfactory.RawQuad;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;

public class CubicQuadPainterTiles extends CubicQuadPainter
{
    private final Rotation rotation;
    private final int blockVersion;
    
    protected CubicQuadPainterTiles(ModelState modelState, int painterIndex)
    {
        super(modelState, painterIndex);
        this.rotation = modelState.getRotation();
        this.blockVersion = modelState.getBlockVersion();
    }

    @Override
    public void addPaintedQuadToList(RawQuad inputQuad, List<RawQuad> outputList)
    {
        RawQuad result = inputQuad.clone();
        
        result.recolor(this.colorMap.getColor(EnumColorMap.BASE));
        if(this.isRotationEnabled)
        {
            result.rotation = this.rotation;
        }
        result.textureSprite = this.texture.getTextureSprite(this.blockVersion);
        result.lightingMode = this.lightingMode;
        
        outputList.add(result);
    }

    public static QuadPainter makeQuadPainter(ModelState modelState, int painterIndex)
    {
        return new CubicQuadPainterTiles(modelState, painterIndex);
    }
}
