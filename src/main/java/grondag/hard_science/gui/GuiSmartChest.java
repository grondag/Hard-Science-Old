package grondag.hard_science.gui;


import java.io.IOException;

import org.lwjgl.input.Mouse;

import grondag.hard_science.gui.control.IClickHandler.StorageClickHandlerStack;
import grondag.hard_science.gui.control.Button;
import grondag.hard_science.gui.control.GuiControl;
import grondag.hard_science.gui.control.ItemStackPicker;
import grondag.hard_science.gui.control.Panel;
import grondag.hard_science.gui.control.TabBar;
import grondag.hard_science.machines.SmartChestTileEntity;
import grondag.hard_science.machines.support.ContainerLayout;
import grondag.hard_science.machines.support.MachineItemBlock;
import grondag.hard_science.machines.support.MachineStorageContainer;
import grondag.hard_science.simulator.wip.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.wip.OpenContainerStorageProxy;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSmartChest extends AbstractMachineGui<SmartChestTileEntity>
{
    protected final ContainerLayout layout;
    
    protected ItemStackPicker stackPicker;
    protected Panel stackPanel;
    
//    protected final Wrapper<ItemStack> hoverStack = new Wrapper<ItemStack>();
    
    public static final ContainerLayout LAYOUT;
    
    public static final int BUTTON_ID_SORT = 123;
    
    public static final int CAPACITY_BAR_WIDTH = 4;
    
    protected int capacityBarLeft;
    protected int itemPickerTop;
    protected int itemPickerHeight;
    
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

    public GuiSmartChest(SmartChestTileEntity tileEntity, MachineStorageContainer container) 
    {
        super(tileEntity, container);
        this.layout = container.layout;
        this.xSize = container.layout.dialogWidth;
        this.ySize = container.layout.dialogHeight;
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

        this.capacityBarLeft = this.guiLeft + layout.externalMargin;
        this.itemPickerTop = this.guiTop + this.layout.externalMargin + this.layout.expectedTextHeight;
        this.itemPickerHeight = this.layout.slotSpacing * 6;
        
        this.stackPicker = new ItemStackPicker(OpenContainerStorageProxy.ITEM_PROXY.LIST, this.fontRenderer, StorageClickHandlerStack.INSTANCE);
        this.stackPicker.setItemsPerRow(9);

        this.stackPanel = new Panel(true);
        this.stackPanel.setVerticalLayout(Layout.FIXED);
        this.stackPanel.setHorizontalLayout(Layout.FIXED);
        this.stackPanel.setBackgroundColor(0xFF777777);
        this.stackPanel.setOuterMarginWidth((LAYOUT.slotSpacing - 16) / 2);
        this.stackPanel.setLeft(this.guiLeft + this.layout.playerInventoryLeft);
        this.stackPanel.setWidth(this.layout.slotSpacing * 9 + this.stackPicker.getTabWidth());
        this.stackPanel.setTop(itemPickerTop);
        this.stackPanel.setHeight(itemPickerHeight);
        this.stackPanel.add(this.stackPicker);
        
        Button butt = new Button(BUTTON_ID_SORT, 
                this.guiLeft + this.xSize - 40 - this.layout.externalMargin, this.guiTop + this.layout.externalMargin - 2, 
                40, this.fontRenderer.FONT_HEIGHT + 2,
                AbstractResourceWithQuantity.SORT_LABELS[OpenContainerStorageProxy.ITEM_PROXY.getSortIndex()]);
        butt.textColor = 0xFF444444;
        this.addButton(butt);
        

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
        
        int barHeight = itemPickerHeight * OpenContainerStorageProxy.ITEM_PROXY.fillPercentage() / 100;
        
        // capacity bar
        drawRect(this.capacityBarLeft, this.itemPickerTop, 
                this.capacityBarLeft + CAPACITY_BAR_WIDTH, this.itemPickerTop + itemPickerHeight, 0xFF404040);
        drawRect(this.capacityBarLeft, this.itemPickerTop + itemPickerHeight - barHeight, 
                this.capacityBarLeft + CAPACITY_BAR_WIDTH, this.itemPickerTop + itemPickerHeight, MachineItemBlock.CAPACITY_COLOR);
        
        // Draw controls here because foreground layer is translated to frame of the GUI
        // and our controls are designed to render in frame of the screen.
        // And can't draw after super.drawScreen() because would potentially render on top of things.
        this.stackPanel.drawControl(this, mouseX, mouseY, partialTicks);
        
        //FIXME: localize
//        this.fontRenderer.drawString("Smart Chest", guiLeft + this.layout.playerInventoryLeft, guiTop + this.layout.externalMargin, 0xFF444444);
//        this.fontRenderer.drawString("Inventory", guiLeft + this.layout.playerInventoryLeft, guiTop + this.layout.playerInventoryTop - this.layout.expectedTextHeight, 0xFF444444);
    }

     @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
    
    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        int i = Mouse.getEventX() * width / mc.displayWidth;
        int j = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        int scrollAmount = Mouse.getEventDWheel();
        if(scrollAmount != 0)
        {
            this.stackPanel.mouseScroll(i, j, scrollAmount);
        }
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int clickedMouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, clickedMouseButton);
        this.stackPanel.mouseClick(mc, mouseX, mouseY, clickedMouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        this.stackPanel.mouseDrag(mc, mouseX, mouseY, clickedMouseButton);
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if(button.id == BUTTON_ID_SORT)
        {
            int nextSortIndex = OpenContainerStorageProxy.ITEM_PROXY.getSortIndex() + 1;
            if(nextSortIndex >= AbstractResourceWithQuantity.SORT_COUNT) nextSortIndex = 0;
            button.displayString = AbstractResourceWithQuantity.SORT_LABELS[nextSortIndex];
            OpenContainerStorageProxy.ITEM_PROXY.setSortIndex(nextSortIndex);
            OpenContainerStorageProxy.ITEM_PROXY.refreshListIfNeeded();
        }
    }

    @Override
    public void addControls()
    {
        // TODO Auto-generated method stub
        
    }
}