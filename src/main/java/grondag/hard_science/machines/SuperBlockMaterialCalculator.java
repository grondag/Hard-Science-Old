package grondag.hard_science.machines;

import grondag.hard_science.library.varia.ColorHelper;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.library.varia.ColorHelper.CMY;
import grondag.hard_science.machines.support.StandardUnits;
import grondag.hard_science.machines.support.VolumeUnits;
import grondag.hard_science.superblock.color.ColorMap.EnumColorMap;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.model.state.PaintLayer;
import grondag.hard_science.superblock.varia.BlockSubstance;
import net.minecraft.util.math.BlockPos;

public class SuperBlockMaterialCalculator
{
    public final long resinA_nL;
    public final long resinB_nL;
    public final long filler_nL;
    public final long nanoLights_nL;
    public final long cyan_nL;
    public final long magenta_nL;
    public final long yellow_nL;
    public final long TiO2_nL;
    public final BlockSubstance actualSubtance;
    
    public SuperBlockMaterialCalculator(ModelState modelState, BlockSubstance requestedSubstance, int lightValue)
    {

        this.nanoLights_nL = lightValue > 0 || modelState.getRenderPassSet().hasFlatRenderPass ? StandardUnits.nL_NANO_LIGHTS_PER_BLOCK : 0;
        
        final long volume = (long) (Useful.volumeAABB(modelState.collisionBoxes(BlockPos.ORIGIN)) * StandardUnits.nL_ONE_BLOCK);
        
        switch(requestedSubstance)
        {
            case DURAWOOD:
            case FLEXWOOD:
            case HYPERWOOD:
            {
                this.actualSubtance = BlockSubstance.FLEXWOOD;
                this.filler_nL = 0;
                final long halfResinVolume = (long) ((volume - this.nanoLights_nL) * StandardUnits.RESIN_WOOD_FRACTION_BY_VOLUME / 2);
                this.resinA_nL = halfResinVolume;
                this.resinB_nL = halfResinVolume;
                break;
            }
            
            case DURAGLASS:
            case FLEXIGLASS:
            case HYPERGLASS:
            {
                final long halfResinVolume = (long) ((volume - this.nanoLights_nL) / 2);
                this.actualSubtance = BlockSubstance.FLEXIGLASS;
                this.filler_nL = 0;
                this.resinA_nL = halfResinVolume;
                this.resinB_nL = halfResinVolume;
                break;
            }
            
            default:
            {
                this.actualSubtance = BlockSubstance.FLEXSTONE;
                this.filler_nL = volume;
                final long halfResinVolume = (long) ((volume - this.nanoLights_nL) * StandardUnits.FILLER_VOID_RATIO / 2);
                this.resinA_nL = halfResinVolume;
                this.resinB_nL = halfResinVolume;
                break;
            }
        }
       
        
        CMY cmy = ColorHelper.cmy(modelState.getColorMap(PaintLayer.BASE).getColor(EnumColorMap.BASE));
        
        int basis = 5;
        float cyan = cmy.cyan * basis;
        float magenta = cmy.magenta * basis;
        float yellow = cmy.yellow * basis;
        
        if(modelState.isMiddleLayerEnabled())
        {
            basis++;
            cmy = ColorHelper.cmy(modelState.getColorMap(PaintLayer.MIDDLE).getColor(EnumColorMap.BASE));
            if(cmy.cyan != 0) cyan += cmy.cyan;
            if(cmy.magenta != 0) magenta += cmy.magenta;
            if(cmy.yellow != 0) yellow += cmy.yellow;
        }
        
        if(modelState.isOuterLayerEnabled())
        {
            basis++;
            cmy = ColorHelper.cmy(modelState.getColorMap(PaintLayer.OUTER).getColor(EnumColorMap.BASE));
            if(cmy.cyan != 0) cyan += cmy.cyan;
            if(cmy.magenta != 0) magenta += cmy.magenta;
            if(cmy.yellow != 0) yellow += cmy.yellow;
        }

        if(modelState.hasLampSurface())
        {
            basis++;
            cmy = ColorHelper.cmy(modelState.getColorMap(PaintLayer.LAMP).getColor(EnumColorMap.BASE));
            if(cmy.cyan != 0) cyan += cmy.cyan;
            if(cmy.magenta != 0) magenta += cmy.magenta;
            if(cmy.yellow != 0) yellow += cmy.yellow;
        }

        cyan = cyan / basis;
        magenta = magenta / basis;
        yellow = yellow / basis;
        
        // Dye consumption should by driven by surface area instead of volume.
        // We assume the shape is a cube for this purpose - some shapes could have higher surface areas
        double surfaceArea_M2 = Useful.squared(Math.cbrt((double) volume / StandardUnits.nL_ONE_BLOCK)) * 6;
        
        double pigmentVolume_nL =  surfaceArea_M2 / StandardUnits.M2_PIGMENT_COVERAGE_SQUARE_METERS_PER_LITER * VolumeUnits.LITER.nL;
        double pigmentVolumePerComponent_nL = pigmentVolume_nL / 3;

        
        this.cyan_nL = cyan > 0 ? (long) (cyan * pigmentVolumePerComponent_nL) : 0;
        this.magenta_nL = magenta > 0 ? (long) (magenta * pigmentVolumePerComponent_nL) : 0;
        this.yellow_nL = yellow > 0 ? (long) (yellow * pigmentVolumePerComponent_nL) : 0;
        
        // Perfect white would be all TiO2 and black would be none
        // In real world would not use CMY pigments to get block, 
        // but in game assuming this is cost-effective.
        this.TiO2_nL = (long) pigmentVolume_nL - this.cyan_nL - this.magenta_nL - this.yellow_nL;
    }
}