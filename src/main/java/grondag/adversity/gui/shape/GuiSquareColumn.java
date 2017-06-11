package grondag.adversity.gui.shape;

import grondag.adversity.gui.control.Slider;
import grondag.adversity.gui.control.Toggle;
import grondag.adversity.superblock.model.shape.SquareColumnMeshFactory;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.translation.I18n;

public class GuiSquareColumn extends GuiShape
{

    private Toggle areCutsOnEdge;
    private Slider cutCount;
    private static final int cutCountSize = SquareColumnMeshFactory.MAX_CUTS - SquareColumnMeshFactory.MIN_CUTS + 1;
    
    @SuppressWarnings("deprecation")
    public GuiSquareColumn(Minecraft mc)
    {
        super(true);
        this.areCutsOnEdge = new Toggle().setLabel(I18n.translateToLocal("label.cuts_on_edge"));
        this.cutCount = new Slider(mc, cutCountSize, I18n.translateToLocal("label.cuts_count"), 0.2);
        this.add(areCutsOnEdge);
        this.add(cutCount);
    }

    @Override
    public void loadSettings(ModelState modelState)
    {
        this.areCutsOnEdge.setOn(SquareColumnMeshFactory.areCutsOnEdge(modelState));
        this.cutCount.setSelectedIndex(SquareColumnMeshFactory.getCutCount(modelState) - SquareColumnMeshFactory.MIN_CUTS);
    }

    @Override
    public boolean saveSettings(ModelState modelState)
    {
        boolean hadUpdate = false;
        
        if(this.areCutsOnEdge.isOn() != SquareColumnMeshFactory.areCutsOnEdge(modelState))
        {
            SquareColumnMeshFactory.setCutsOnEdge(this.areCutsOnEdge.isOn(), modelState);
            hadUpdate = true;
        }
        
        int cuts = this.cutCount.getSelectedIndex() + SquareColumnMeshFactory.MIN_CUTS;
        
        if(cuts  != SquareColumnMeshFactory.getCutCount(modelState))
        {
            SquareColumnMeshFactory.setCutCount(cuts, modelState);
            hadUpdate = true;
        }
        
        return hadUpdate;
    }

}
