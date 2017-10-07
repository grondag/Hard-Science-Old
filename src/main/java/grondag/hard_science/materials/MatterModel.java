package grondag.hard_science.materials;

import java.util.ArrayList;
import com.google.common.collect.ImmutableList;

import grondag.hard_science.init.ModModels;
import grondag.hard_science.library.render.QuadBakery;
import grondag.hard_science.library.render.QuadHelper;
import grondag.hard_science.library.render.RawQuad;
import grondag.hard_science.library.world.Rotation;
import net.minecraft.client.renderer.block.model.BakedQuad;

public abstract class MatterModel
{
    public void addBakedQuads(ImmutableList.Builder<BakedQuad> builder)
    {
        ArrayList<RawQuad> quads = new ArrayList<RawQuad>();
        this.addRawQuads(quads);
        for(RawQuad raw : quads)
        {
            builder.add(QuadBakery.createBakedQuad(raw, true));
        }
    }
    
    protected abstract void addRawQuads(ArrayList<RawQuad> quadList);
    
    private abstract static class TexColor extends MatterModel
    {
        protected final String symbolString;
        protected final int color;
        
        protected TexColor(String symbolString, int color)
        {
            this.symbolString = symbolString;
            this.color = color;
        }
    }
    
    public static final MatterModel PACKAGE_STANDARD_VACPACK = new Naked("material_gradient", 0xFFAAAAAA);
    public static final MatterModel PACKAGE_STANDARD_IBC = new Naked("material_gradient", 0xFFAAAAAA);
    
    public static class Naked extends TexColor
    {

        public Naked(String symbolString, int color)
        {
            super(symbolString, color);
        }

        @Override
        public void addRawQuads(ArrayList<RawQuad> quadList)
        {
            QuadHelper.addTextureToAllFaces(this.symbolString, 0.0f, 1.0f, 1.0f, 1.00f, this.color, true, Rotation.ROTATE_NONE, quadList);            
        }
        
    }

    public static class SymbolCenter extends TexColor
    {
        public SymbolCenter(String symbolString, int color)
        {
            super(symbolString, color);
        }

        @Override
        public void addRawQuads(ArrayList<RawQuad> quadList)
        {
            QuadHelper.addTextureToAllFaces(this.symbolString, 0.25f, 0.96f, 0.5f, 1.025f, this.color, true, Rotation.ROTATE_NONE, quadList);
        }
    }
    
    public static class SymbolRight extends TexColor
    {
        public SymbolRight(String symbolString, int color)
        {
            super(symbolString, color);
        }

        @Override
        public void addRawQuads(ArrayList<RawQuad> quadList)
        {
            QuadHelper.addTextureToAllFaces(this.symbolString, 0.48f, 0.98f, 0.46f, 1.025f, this.color, true, Rotation.ROTATE_NONE, quadList);
        }
    }
    
    /** scales it slightly smaller and lower */
    
    public static class SymbolRight2 extends TexColor
    {
        public SymbolRight2(String symbolString, int color)
        {
            super(symbolString, color);
        }

        @Override
        public void addRawQuads(ArrayList<RawQuad> quadList)
        {
            QuadHelper.addTextureToAllFaces(this.symbolString, 0.50f, 0.96f, 0.42f, 1.025f, this.color, true, Rotation.ROTATE_NONE, quadList);
        }
    }
    
    public static class SymbolCenterRotated extends TexColor
    {
        private final Rotation rotation;
        
        protected SymbolCenterRotated(String symbolString, int color, Rotation rotation)
        {
            super(symbolString, color);
            this.rotation = rotation;
            
        }

        @Override
        public void addRawQuads(ArrayList<RawQuad> quadList)
        {
            QuadHelper.addTextureToAllFaces(this.symbolString, 0.25f, 0.96f, 0.5f, 1.025f, this.color, true, this.rotation, quadList);
        }
    }
    
    public static class SymbolBottom extends TexColor
    {
        protected SymbolBottom(String symbolString, int color)
        {
            super(symbolString, color);
        }

        @Override
        public void addRawQuads(ArrayList<RawQuad> quadList)
        {
            QuadHelper.addTextureToAllFaces(this.symbolString, 0.02f, 0.58f, 0.29f, 1.025f, this.color, true, Rotation.ROTATE_NONE, quadList);
        }
    }
   
    public static class DustModel extends MatterModel
    {
        private final int color;
        private final String symbol0;
        private final String symbol1;
        private final String symbol2;

        
        public DustModel(int color, String symbol0, String symbol1, String symbol2)
        {
            this.color = color;
            this.symbol0 = symbol0;
            this.symbol1 = symbol1;
            this.symbol2 = symbol2;
        }
        
        @Override
        public void addRawQuads(ArrayList<RawQuad> quadList)
        {
            QuadHelper.addTextureToAllFaces(this.symbol0, 0.0f, 0.98f, 0.42f, 1.025f, this.color, true, Rotation.ROTATE_NONE, quadList);
            QuadHelper.addTextureToAllFaces(this.symbol1, 0.40f, 0.96f, 0.20f, 1.025f, this.color, true, Rotation.ROTATE_NONE, quadList);
            QuadHelper.addTextureToAllFaces(this.symbol2, 0.58f, 0.98f, 0.42f, 1.025f, this.color, true, Rotation.ROTATE_NONE, quadList);
        }
    }
    
    private static class FormulaJustified extends TexColor
    {
        private final boolean leftJustify; 
        
        protected FormulaJustified(String symbolString, int color, boolean leftJustify)
        {
            super(symbolString, color);
            this.leftJustify = leftJustify;
        }

        @Override
        public void addRawQuads(ArrayList<RawQuad> quadList)
        {
            ModModels.FONT_RENDERER_SMALL.formulaBlockQuadsToList(this.symbolString, true, this.color, 1.025f, this.leftJustify, quadList);
        }
    }
    
    public static class Formula extends FormulaJustified
    {

        protected Formula(String symbolString, int color)
        {
            super(symbolString, color, false);
        }
    }
    
    public static class FormulaLeft extends FormulaJustified
    {

        protected FormulaLeft(String symbolString, int color)
        {
            super(symbolString, color, true);
        }
    }
 
//
//    switch(matterCube.matter.symbolType)
//    {
//    case FORMULA:
//        ModModels.FONT_RENDERER_SMALL.formulaBlockQuadsToList(matterCube.matter.symbolString, true, matterCube.matter.symbolColor, 1.025f, quadList);
//        break;
//    case SYMBOL:
//        QuadHelper.addTextureToAllFaces(matterCube.matter.symbolString, 0.25f, 0.96f, 0.5f, 1.025f, matterCube.matter.symbolColor, true, quadList);
//        break;
//    case NAME:
//    default:
//        ModModels.FONT_RENDERER_SMALL.formulaBlockQuadsToList(matterCube.matter.symbolString, false, matterCube.matter.symbolColor, 1.025f, quadList);
//        break;
    
//    }
}
