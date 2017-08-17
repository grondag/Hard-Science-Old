package grondag.hard_science.gui;


import java.io.IOException;

import org.lwjgl.input.Mouse;

import com.raoulvdberge.refinedstorage.RSItems;
import com.raoulvdberge.refinedstorage.api.network.grid.GridType;
import com.raoulvdberge.refinedstorage.api.network.grid.handler.IFluidGridHandler;
import com.raoulvdberge.refinedstorage.api.network.grid.handler.IItemGridHandler;
import com.raoulvdberge.refinedstorage.api.storage.IStorageDiskProvider;
import com.raoulvdberge.refinedstorage.container.slot.SlotDisabled;
import com.raoulvdberge.refinedstorage.container.slot.SlotFilter;
import com.raoulvdberge.refinedstorage.container.slot.SlotFilterLegacy;
import com.raoulvdberge.refinedstorage.container.slot.SlotGridCrafting;
import com.raoulvdberge.refinedstorage.tile.grid.portable.IPortableGrid;
import com.raoulvdberge.refinedstorage.tile.grid.portable.PortableGrid;

import grondag.hard_science.ClientProxy;
import grondag.hard_science.gui.control.ItemStackPicker;
import grondag.hard_science.gui.control.Panel;
import grondag.hard_science.gui.control.TabBar;
import grondag.hard_science.machines.ContainerLayout;
import grondag.hard_science.machines.MachineContainerBase;
import grondag.hard_science.machines.MachineContainerTEBase;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BaseContainerScreen extends GuiContainer 
{
    protected final ContainerLayout layout;
    
    protected ItemStackPicker stackPicker;
    protected Panel stackPanel;
    
    public static final ContainerLayout LAYOUT;
    
    static
    {
        LAYOUT = new ContainerLayout();
        LAYOUT.slotSpacing = 18;
        
        LAYOUT.externalMargin = 6;
        
        LAYOUT.expectedTextHeight = 12;

        LAYOUT.dialogWidth = LAYOUT.externalMargin * 2 + LAYOUT.slotSpacing * 9 + TabBar.DEFAULT_TAB_WIDTH;
            
        LAYOUT.dialogHeight = LAYOUT.externalMargin * 3 + LAYOUT.slotSpacing * 10 + LAYOUT.expectedTextHeight * 2;
        
        /** distance from edge of dialog to start of player inventory area */
        LAYOUT.playerInventoryLeft = LAYOUT.externalMargin;
        
        /** distance from top of dialog to start of player inventory area */
        LAYOUT.playerInventoryTop = LAYOUT.dialogHeight - LAYOUT.externalMargin - LAYOUT.slotSpacing * 4;
    }

    public BaseContainerScreen(MachineContainerTEBase tileEntity, MachineContainerBase container) 
    {
        super(container);
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
        
        this.stackPicker = new ItemStackPicker(ClientProxy.TEST_PROXY.LIST, this.fontRenderer);
        this.stackPicker.setItemsPerRow(9);

        this.stackPanel = new Panel(true);
        this.stackPanel.setVerticalLayout(Layout.FIXED);
        this.stackPanel.setHorizontalLayout(Layout.FIXED);
        this.stackPanel.setBackgroundColor(0xFF777777);
        this.stackPanel.setOuterMarginWidth((LAYOUT.slotSpacing - 16) / 2);
        this.stackPanel.setLeft(this.guiLeft + this.layout.playerInventoryLeft);
        this.stackPanel.setWidth(this.layout.slotSpacing * 9 + this.stackPicker.getTabWidth());
        this.stackPanel.setTop(this.guiTop + this.layout.externalMargin + this.layout.expectedTextHeight);
        this.stackPanel.setHeight(this.layout.slotSpacing * 6);
        this.stackPanel.add(this.stackPicker);
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
        
        // Draw controls here because foreground layer is translated to frame of the GUI
        // and our controls are designed to render in frame of the screen.
        // And can't draw after super.drawScreen() because would potentially render on top of things.
        this.stackPanel.drawControl(mc, itemRender, mouseX, mouseY, partialTicks);
        
        this.fontRenderer.drawString("Whatever Thingy", guiLeft + this.layout.playerInventoryLeft, guiTop + this.layout.externalMargin, 0xFF444444);
        this.fontRenderer.drawString("Inventory", guiLeft + this.layout.playerInventoryLeft, guiTop + this.layout.playerInventoryTop - this.layout.expectedTextHeight, 0xFF444444);
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
        this.stackPanel.mouseClick(mc, mouseX, mouseY);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        this.stackPanel.mouseDrag(mc, mouseX, mouseY);
    }
}