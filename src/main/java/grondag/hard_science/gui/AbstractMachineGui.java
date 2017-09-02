package grondag.hard_science.gui;

import java.io.IOException;

import grondag.hard_science.gui.control.GuiControl;
import grondag.hard_science.gui.control.IGuiRenderContext;
import grondag.hard_science.gui.control.Panel;
import grondag.hard_science.gui.control.machine.AbstractMachineControl;
import grondag.hard_science.gui.control.machine.MachineControlRenderer.RenderBounds;
import grondag.hard_science.machines.base.MachineContainer;
import grondag.hard_science.machines.base.MachineTileEntity;
import grondag.hard_science.machines.support.ContainerLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;

public abstract class AbstractMachineGui<T extends MachineTileEntity> extends GuiContainer implements IGuiRenderContext
{
  protected final ContainerLayout layout;
    
    protected Panel mainPanel;
    
//    protected final Wrapper<ItemStack> hoverStack = new Wrapper<ItemStack>();
    
    public static final ContainerLayout LAYOUT;
    
    public static final int CAPACITY_BAR_WIDTH = 4;
    
    protected final T te;
    
    protected GuiControl<?> hoverControl;
    
    static
    {
        LAYOUT = new ContainerLayout();
                   
        LAYOUT.dialogHeight = LAYOUT.externalMargin * 3 + LAYOUT.slotSpacing * 4 + LAYOUT.playerInventoryWidth + GuiControl.CONTROL_INTERNAL_MARGIN;
        
        /** distance from top of dialog to start of player inventory area */
        LAYOUT.playerInventoryTop = LAYOUT.dialogHeight - LAYOUT.externalMargin - LAYOUT.slotSpacing * 4 - GuiControl.CONTROL_INTERNAL_MARGIN;
    }

    public AbstractMachineGui(T containerTileEntity, MachineContainer machineContainer) 
    {
        super(machineContainer);
        this.te = containerTileEntity;
        this.layout = machineContainer.layout;
        this.xSize = machineContainer.layout.dialogWidth;
        this.ySize = machineContainer.layout.dialogHeight;
    }
    
    @Override
    public void initGui()
    {
        super.initGui();
        
        // if using JEI, center on left 2/3 of screen to allow more room for JEI
        if(Loader.instance().getIndexedModList().containsKey("jei"))
        {
            this.guiLeft = ((this.width * 2 / 3) - this.xSize) / 2;
        }
        
        Panel mainPanel = new Panel(true);
        mainPanel.setLayoutDisabled(true);
        mainPanel.setLeft(this.guiLeft + this.layout.playerInventoryLeft);
        mainPanel.setTop(this.guiTop + this.layout.externalMargin);
        mainPanel.setSquareSize(this.layout.playerInventoryWidth);
        mainPanel.setBackgroundColor(0xFF404040);
        this.mainPanel = mainPanel;
        
        this.addControls();
    }
    
    /** add controls to main panel here */
    public abstract void addControls();
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) 
    {
        drawRect(guiLeft, guiTop, guiLeft + xSize, guiTop + ySize, 0xFFCCCCCC);
        
        // player slot backgrounds
        for(Slot slot : this.inventorySlots.inventorySlots)
        {
            int x = guiLeft + slot.xPos;
            int y = guiTop + slot.yPos;
            this.drawGradientRect(x, y, x + 16, y + 16, 0xFFA9A9A9, 0xFF898989);
        }
        
        // Draw controls here because foreground layer is translated to frame of the GUI
        // and our controls are designed to render in frame of the screen.
        // And can't draw after super.drawScreen() because would potentially render on top of things.
        this.mainPanel.drawControl(this, mouseX, mouseY, partialTicks);
        
//        MachineControlRenderer.renderMachineName(MachineControlRenderer.LAYOUT_GUI, te.machineName(), 0xFF);
        
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) 
    {
        this.hoverControl = null;
        super.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        super.renderHoveredToolTip(mouseX, mouseY);
        if(this.hoverControl != null)
        {
            hoverControl.drawToolTip(this, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int clickedMouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, clickedMouseButton);
        this.mainPanel.mouseClick(mc, mouseX, mouseY, clickedMouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        this.mainPanel.mouseDrag(mc, mouseX, mouseY, clickedMouseButton);
    }
    
    protected AbstractMachineControl<?> sizeControl(AbstractMachineControl<?> control, RenderBounds bounds)
    {
        control.setLeft(mainPanel.getLeft() + mainPanel.getWidth() * bounds.left);
        control.setTop(mainPanel.getTop() + mainPanel.getHeight() * bounds.top);
        control.setWidth(mainPanel.getWidth() * bounds.width);
        control.setHeight(mainPanel.getHeight() * bounds.height);
        return control;
    }

    @Override
    public Minecraft minecraft()
    {
        return this.mc;
    }

    @Override
    public RenderItem renderItem()
    {
        return this.itemRender;
    }

    @Override
    public GuiScreen screen()
    {
        return this;
    }

    @Override
    public FontRenderer fontRenderer()
    {
        return this.fontRenderer;
    }
    
    @Override
    public void setHoverControl(GuiControl<?> control)
    {
        this.hoverControl = control;
    }

    @Override
    public void drawToolTip(ItemStack hoverStack, int mouseX, int mouseY)
    {
        this.renderToolTip(hoverStack, mouseX, mouseY);
        
    }
}
