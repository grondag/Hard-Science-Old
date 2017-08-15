package grondag.hard_science.gui;


import grondag.hard_science.machines.MachineContainerBase;
import grondag.hard_science.machines.MachineContainerTEBase;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

public class ContainerGUI extends GuiContainer 
{
//    protected final ContainerLayout layout;
    
    public ContainerGUI(MachineContainerTEBase tileEntity, MachineContainerBase container) 
    {
        super(container);
//        this.layout = container.layout;
        xSize = container.layout.dialogWidth;
        ySize = container.layout.dialogHeight;
        
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) 
    {
        drawRect(guiLeft, guiTop, guiLeft + xSize, guiTop + ySize, 0xFFCCCCCC);
        
        for(Slot slot : this.inventorySlots.inventorySlots)
        {
            int x = guiLeft + slot.xPos;
            int y = guiTop + slot.yPos;
            drawRect(x, y, x + 16, y + 16, 0xFFA9A9A9);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) 
    {
        super.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}