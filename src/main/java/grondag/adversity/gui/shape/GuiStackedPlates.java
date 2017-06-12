package grondag.adversity.gui.shape;

import grondag.adversity.gui.control.Slider;
import grondag.adversity.gui.control.Toggle;
import grondag.adversity.superblock.model.shape.SquareColumnMeshFactory;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.translation.I18n;

public class GuiStackedPlates extends GuiShape
{

    private Toggle isSlab;
    private Slider thickness;
    
    /** used to detect what changed during mouse handling */
    private boolean lastIsSlab;
    
    @SuppressWarnings("deprecation")
    public GuiStackedPlates(Minecraft mc)
    {
        super(true);
        this.isSlab = new Toggle().setLabel(I18n.translateToLocal("label.half_slab"));
        this.thickness = new Slider(mc, 16, I18n.translateToLocal("label.thickness"), 0.2);
        this.add(isSlab);
        this.add(thickness);
    }
    
    private boolean isSlab(ModelState modelState)
    {
        return modelState.getSpecies() == 7;
    }

    @Override
    public void loadSettings(ModelState modelState)
    {
        this.isSlab.setOn(isSlab(modelState));
        this.thickness.setSelectedIndex(modelState.getSpecies());
        saveLast();
    }

    @Override
    public boolean saveSettings(ModelState modelState)
    {
        int t = this.thickness.getSelectedIndex();
        if(t  != modelState.getSpecies())
        {
            modelState.setSpecies(t);
            return true;
        }
        else
        {
            return false;
        }
    }

    private void saveLast()
    {
        this.lastIsSlab = this.isSlab.isOn();
    }
    
    private void handleMouse()
    {
        if(this.isSlab.isOn())
        {
            // if user clicks slab toggle, set to half thickness
            if(!this.lastIsSlab)
            {
                this.thickness.setSelectedIndex(7);
            }
            else
            {
                this.isSlab.setOn(this.thickness.getSelectedIndex() == 7);
            }
        }
        else
        {
            if(this.thickness.getSelectedIndex() == 7) this.isSlab.setOn(true);
        }
        this.saveLast();
    }
    
    @Override
    public void handleMouseClick(Minecraft mc, int mouseX, int mouseY)
    {
        super.handleMouseClick(mc, mouseX, mouseY);
        this.handleMouse();
    }

    @Override
    public void handleMouseDrag(Minecraft mc, int mouseX, int mouseY)
    {
        super.handleMouseDrag(mc, mouseX, mouseY);
        this.handleMouse();
    }

    
}
