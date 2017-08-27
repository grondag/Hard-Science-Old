package grondag.hard_science.gui;

import java.io.IOException;

import grondag.hard_science.gui.control.GuiControl;
import grondag.hard_science.gui.control.Panel;
import grondag.hard_science.gui.control.TabBar;
import grondag.hard_science.library.varia.Wrapper;
import grondag.hard_science.machines.BasicBuilderTileEntity;
import grondag.hard_science.machines.base.MachineContainer;
import grondag.hard_science.machines.support.ContainerLayout;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiBasicBuilder extends GuiContainer
{
    protected final ContainerLayout layout;
    
    protected Panel mainPanel;
    
    protected final Wrapper<ItemStack> hoverStack = new Wrapper<ItemStack>();
    
    public static final ContainerLayout LAYOUT;
    
    public static final int CAPACITY_BAR_WIDTH = 4;
    
    static
    {
        LAYOUT = new ContainerLayout();
        LAYOUT.slotSpacing = 18;
        
        LAYOUT.externalMargin = 6;
        
        LAYOUT.expectedTextHeight = 12;

        LAYOUT.dialogWidth = LAYOUT.externalMargin * 2 + LAYOUT.slotSpacing * 9 + TabBar.DEFAULT_TAB_WIDTH + CAPACITY_BAR_WIDTH + GuiControl.CONTROL_INTERNAL_MARGIN;
            
        LAYOUT.dialogHeight = LAYOUT.externalMargin * 3 + LAYOUT.slotSpacing * 10 + LAYOUT.expectedTextHeight * 2;
        
        /** distance from edge of dialog to start of player inventory area */
        LAYOUT.playerInventoryLeft = LAYOUT.externalMargin + CAPACITY_BAR_WIDTH + GuiControl.CONTROL_INTERNAL_MARGIN;
        
        /** distance from top of dialog to start of player inventory area */
        LAYOUT.playerInventoryTop = LAYOUT.dialogHeight - LAYOUT.externalMargin - LAYOUT.slotSpacing * 4;
    }

    public GuiBasicBuilder(BasicBuilderTileEntity containerTileEntity, MachineContainer machineContainer) 
    {
        super(machineContainer);
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
        
        this.mainPanel = new Panel(true);
    }
    
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
//            drawRect(x, y, x + 16, y + 16, 0xFFA9A9A9);
        }
        
//        int barHeight = itemPickerHeight * OpenContainerStorageProxy.ITEM_PROXY.fillPercentage() / 100;
//        
//        // capacity bar
//        drawRect(this.capacityBarLeft, this.itemPickerTop, 
//                this.capacityBarLeft + CAPACITY_BAR_WIDTH, this.itemPickerTop + itemPickerHeight, 0xFF404040);
//        drawRect(this.capacityBarLeft, this.itemPickerTop + itemPickerHeight - barHeight, 
//                this.capacityBarLeft + CAPACITY_BAR_WIDTH, this.itemPickerTop + itemPickerHeight, MachineItemBlock.CAPACITY_COLOR);
        
        // Draw controls here because foreground layer is translated to frame of the GUI
        // and our controls are designed to render in frame of the screen.
        // And can't draw after super.drawScreen() because would potentially render on top of things.
        this.mainPanel.drawControl(mc, itemRender, mouseX, mouseY, partialTicks);
        
        //FIXME: localize
        this.fontRenderer.drawString("Basic Builder", guiLeft + this.layout.playerInventoryLeft, guiTop + this.layout.externalMargin, 0xFF444444);
        this.fontRenderer.drawString("Inventory", guiLeft + this.layout.playerInventoryLeft, guiTop + this.layout.playerInventoryTop - this.layout.expectedTextHeight, 0xFF444444);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) 
    {
        this.hoverStack.setValue(null);
        super.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        super.renderHoveredToolTip(mouseX, mouseY);
        if(this.hoverStack.getValue() != null)
        {
            this.renderToolTip(this.hoverStack.getValue(), mouseX, mouseY);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
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
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
//        if(button.id == BUTTON_ID_SORT)
//        {
//            int nextSortIndex = OpenContainerStorageProxy.ITEM_PROXY.getSortIndex() + 1;
//            if(nextSortIndex >= AbstractResourceWithQuantity.SORT_COUNT) nextSortIndex = 0;
//            button.displayString = AbstractResourceWithQuantity.SORT_LABELS[nextSortIndex];
//            OpenContainerStorageProxy.ITEM_PROXY.setSortIndex(nextSortIndex);
//            OpenContainerStorageProxy.ITEM_PROXY.refreshListIfNeeded();
//        }
    }
}