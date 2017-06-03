package grondag.adversity.gui.shape;

import grondag.adversity.gui.control.Slider;
import grondag.adversity.gui.control.Toggle;
import grondag.adversity.superblock.model.shape.SquareColumnMeshFactory;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.client.Minecraft;

public class GuiSquareColumn extends GuiShape
{

    private Toggle areCutsOnEdge;
    private Slider cutCount;
    private static final int cutCountSize = SquareColumnMeshFactory.MAX_CUTS - SquareColumnMeshFactory.MIN_CUTS + 1;
    
    public GuiSquareColumn(Minecraft mc)
    {
        super(true);
        //TODO: localize
        this.areCutsOnEdge = new Toggle().setLabel("Cuts On Edge");
        this.cutCount = new Slider(mc, cutCountSize,"Cuts", 0.2);
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
