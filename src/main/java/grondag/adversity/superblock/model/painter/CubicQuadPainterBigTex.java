package grondag.adversity.superblock.model.painter;

import java.util.List;

import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.library.model.quadfactory.RawQuad;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;

public class CubicQuadPainterBigTex extends CubicQuadPainter
{


    protected CubicQuadPainterBigTex(ModelState modelState, int painterIndex)
    {
        super(modelState, painterIndex);
    }

    @Override
    public void addPaintedQuadToList(RawQuad inputQuad, List<RawQuad> outputList)
    {
        RawQuad result = inputQuad.clone();
        
        result.recolor(this.colorMap.getColor(this.lightingMode == LightingMode.FULLBRIGHT ? EnumColorMap.LAMP : EnumColorMap.BASE));
        if(this.isRotationEnabled)
        {
            result.rotation = this.rotation;
        }
        result.textureSprite = this.texture.getTextureSprite();
        result.lightingMode = this.lightingMode;
        
        outputList.add(result);    
    }

    public static QuadPainter makeQuadPainter(ModelState modelState, int painterIndex)
    {
        return new CubicQuadPainterBigTex(modelState, painterIndex);
    }
}
