package grondag.adversity.gui.control;

import java.util.Collection;
import java.util.List;

import grondag.adversity.gui.GuiUtil;
import grondag.adversity.library.varia.Useful;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class TabBar<T> extends GuiControl
{
    public static final int TAB_MARGIN = 2;
    public static final int TAB_WIDTH = 8;
    public static final int ITEM_SPACING = 4;
    
    public static final int NO_SELECTION = -1;
    
    private int tabCount;
    private int itemsPerTab;
    private int columnsPerRow = 5;
    private int rowsPerTab;
    private int selectedItemIndex = NO_SELECTION;
    private int selectedTabIndex;
    
    private double actualItemSize;
    private double tabSize;
    private double scrollHeight;
    
    private boolean focusOnSelection = false;
    
    private List<T> items;
    
    protected static enum MouseLocation
    {
        NONE,
        TOP_ARROW,
        BOTTOM_ARROW,
        TAB,
        ITEM
    }
    
    private MouseLocation currentMouseLocation;
    private int currentMouseIndex;
    
    public TabBar(List<T> items)
    {
        this.items = items;
    }
    
    public void setList(List<T> items)
    {
        this.items = items;
        this.isDirty = true;
    }

    @Override
    protected void drawContent(Minecraft mc, RenderItem itemRender, int mouseX, int mouseY, float partialTicks)
    {
        if(items == null) return;
        
        updateMouseLocation(mouseX, mouseY);
        
        int start = this.getFirstDisplayedIndex();
        int end = this.getLastDisplayedIndex();
        double itemX = this.left;
        double itemY = this.top;
        
        int column = 0;
        
        int highlightIndex = this.currentMouseLocation == MouseLocation.ITEM ? this.currentMouseIndex : -1;
        
        for(int i = start; i < end; i++)
        {
            if(highlightIndex == i )
            {
                GuiUtil.drawBoxRightBottom(itemX - 2, itemY - 2, itemX + this.actualItemSize + 2, itemY + this.actualItemSize + 2, 1, GuiControl.BUTTON_COLOR_FOCUS);
            }
            else if(this.selectedItemIndex == i)
            {
                GuiUtil.drawBoxRightBottom(itemX - 2, itemY - 2, itemX + this.actualItemSize + 2, itemY + this.actualItemSize + 2, 1, GuiControl.BUTTON_COLOR_ACTIVE);
            }
            
            this.drawItem(this.get(i), mc, itemRender, itemX, itemY, partialTicks);
            if(++column == this.columnsPerRow)
            {
                column = 0;
                itemY += (this.actualItemSize + ITEM_SPACING);
                itemX = this.left;
            }
            else
            {
                itemX += (this.actualItemSize + ITEM_SPACING);
            }
        }
        
        
        // skip drawing tabs if there is only one
        if(this.tabCount <= 1) return;
        
        
        // if tabs are too small, just do a continuous bar
        double tabStartY = this.top + TAB_WIDTH + ITEM_SPACING;
        if(this.tabSize == 0.0)
        {
            
            GuiUtil.drawRect(this.right - TAB_WIDTH, tabStartY, this.right, tabStartY + this.scrollHeight, BUTTON_COLOR_INACTIVE);
     
            // box width is same as tab height, so need to have it be half that extra to the right so that we keep our margins with the arrows
            double selectionCenterY = tabStartY + TAB_WIDTH / 2.0 + (this.scrollHeight - TAB_WIDTH) * (double) this.selectedTabIndex / (this.tabCount - 1);
            
            GuiUtil.drawRect(this.right - TAB_WIDTH, selectionCenterY -  TAB_WIDTH / 2.0, this.right, selectionCenterY +  TAB_WIDTH / 2.0, BUTTON_COLOR_ACTIVE);
            
        }
        else
        {
            highlightIndex = this.currentMouseLocation == MouseLocation.TAB ? this.currentMouseIndex : -1;
            
            for(int i = 0; i < this.tabCount; i++)
            {
                GuiUtil.drawRect(this.right - TAB_WIDTH, tabStartY, this.right, tabStartY + this.tabSize,
                        i == highlightIndex ? BUTTON_COLOR_FOCUS : i == this.selectedTabIndex ? BUTTON_COLOR_ACTIVE : BUTTON_COLOR_INACTIVE);
                tabStartY += (this.tabSize + TAB_MARGIN);
            }
        }
        
        double arrowCenterX = this.right - TAB_WIDTH / 2.0;

        GuiUtil.drawQuad(arrowCenterX, this.top, this.right - TAB_WIDTH, this.top + TAB_WIDTH, this.right, this.top + TAB_WIDTH, arrowCenterX, this.top, 
                this.currentMouseLocation == MouseLocation.TOP_ARROW ? BUTTON_COLOR_FOCUS : BUTTON_COLOR_INACTIVE);

        GuiUtil.drawQuad(arrowCenterX, this.bottom, this.right, this.bottom - TAB_WIDTH, this.right - TAB_WIDTH, this.bottom - TAB_WIDTH, arrowCenterX, this.bottom, 
                this.currentMouseLocation == MouseLocation.BOTTOM_ARROW ? BUTTON_COLOR_FOCUS : BUTTON_COLOR_INACTIVE);
        
    }

    protected  abstract void drawItem(T item, Minecraft mc, RenderItem itemRender, double left, double top, float partialTicks);
    
    private void updateMouseLocation(int mouseX, int mouseY)
    {
        if(items == null) return;
        
        if(mouseX < this.left || mouseX > this.right || mouseY < this.top || mouseY > this.bottom)
        {
            this.currentMouseLocation = MouseLocation.NONE;
        }
        else if(mouseX >= this.right - TAB_WIDTH)
        {
            if(mouseY <= this.top + TAB_WIDTH + ITEM_SPACING / 2.0)
            {
                this.currentMouseLocation = MouseLocation.TOP_ARROW;
            }
            else if(mouseY >= this.bottom - TAB_WIDTH - ITEM_SPACING / 2.0)
            {
                this.currentMouseLocation = MouseLocation.BOTTOM_ARROW;
            }
            else
            {
                this.currentMouseLocation = MouseLocation.TAB;
                this.currentMouseIndex = MathHelper.clamp((int) ((mouseY - this.top - TAB_WIDTH - ITEM_SPACING / 2) / (this.scrollHeight) * this.tabCount), 0, this.tabCount - 1);
//                this.currentMouseIndex = (int) ((mouseX - this.left - TAB_WIDTH - this.actualItemMargin / 2) / (this.tabWidth + this.tabMargin));
            }
        }
        else
        {
            int newIndex = this.getFirstDisplayedIndex() + (int)((mouseY - this.top - ITEM_SPACING / 2) / (this.actualItemSize + ITEM_SPACING)) * this.columnsPerRow
                    + Math.min((int)((mouseX - this.left - ITEM_SPACING / 2) / (this.actualItemSize + ITEM_SPACING)), this.columnsPerRow - 1);
            
            this.currentMouseIndex = (newIndex < this.items.size()) ? newIndex : -1;
            this.currentMouseLocation = currentMouseIndex >= 0 ? MouseLocation.ITEM : MouseLocation.NONE;
        }
    }
    
    @Override
    protected void handleCoordinateUpdate()
    {
        if(this.items != null)
        {
            
            double horizontalSpaceRemaining = this.width - TAB_WIDTH;
            this.actualItemSize = horizontalSpaceRemaining / this.columnsPerRow - ITEM_SPACING;
            this.rowsPerTab = (int) ((this.height + ITEM_SPACING) / (actualItemSize + ITEM_SPACING));
            this.itemsPerTab = columnsPerRow * rowsPerTab;
            this.tabCount = this.itemsPerTab > 0 ? (this.items.size() + this.itemsPerTab - 1) / this.itemsPerTab : 0;
            this.scrollHeight = this.height - (TAB_WIDTH + ITEM_SPACING) * 2;
            this.tabSize = tabCount <= 0 ? 0 : (this.scrollHeight - (TAB_MARGIN * (this.tabCount - 1))) / tabCount;
            if(tabSize < TAB_MARGIN * 2) tabSize = 0;
        }
        
        if(this.focusOnSelection && this.selectedItemIndex != NO_SELECTION)
        {
            if(this.itemsPerTab > 0) this.selectedTabIndex = this.selectedItemIndex / this.itemsPerTab;
            this.focusOnSelection = false;
        }
    }

    @Override
    protected void handleMouseClick(Minecraft mc, int mouseX, int mouseY)
    {
        if(items == null) return;
        
        this.updateMouseLocation(mouseX, mouseY);
        switch(this.currentMouseLocation)
        {
        case ITEM:
            if(this.currentMouseIndex >= 0) this.setSelectedIndex(this.currentMouseIndex);
            break;

        case TOP_ARROW:
            if(this.selectedTabIndex > 0) this.selectedTabIndex--;
            GuiUtil.playPressedSound(mc);
            break;

        case BOTTOM_ARROW:
            if(this.selectedTabIndex < this.tabCount - 1) this.selectedTabIndex++;
            GuiUtil.playPressedSound(mc);
            break;

        case TAB:
            this.selectedTabIndex = this.currentMouseIndex;
            break;
            
        case NONE:
        default:
            break;
        
        }
    }
    
    @Override
    protected void handleMouseDrag(Minecraft mc, int mouseX, int mouseY)
    {
        if(items == null) return;
        
        this.updateMouseLocation(mouseX, mouseY);
        switch(this.currentMouseLocation)
        {
        case ITEM:
            if(this.currentMouseIndex >= 0) this.setSelectedIndex(this.currentMouseIndex);
            break;

        case TOP_ARROW:
            break;

        case BOTTOM_ARROW:
            break;

        case TAB:
            this.selectedTabIndex = this.currentMouseIndex;
            break;
            
        case NONE:
        default:
            break;
        }
    }
    
    @Override 
    protected void handleMouseScroll(int mouseX, int mouseY, int scrollDelta)
    {
        if(items == null) return;
        
        this.selectedTabIndex = MathHelper.clamp(this.selectedTabIndex + this.mouseIncrementDelta(), 0, this.tabCount - 1);
    }
    
    public void add(T item)
    {
        if(items == null) return;
        
        this.items.add(item);
        this.isDirty = true;
    }

    public void addAll(Collection<T> items)
    {
        if(items == null) return;
        
        this.items.addAll(items);
        this.isDirty = true;
    }
    
    public void addAll(T[] itemsIn)
    {
        if(items == null) return;
        
        for(T item : itemsIn)
        {
            this.items.add(item);
        }
        this.isDirty = true;
    }
    
    public T get(int index)
    {
        if(items == null || index == NO_SELECTION) return null;
        
        return this.items.get(index);
    }
    
    public T getSelected()
    {
        if(items == null || this.selectedItemIndex == NO_SELECTION) return null;
        
        return this.get(this.getSelectedIndex());
    }
    
    public List<T> getDisplayed()
    {
        if(items == null) return null;
        
        return this.items.subList(this.getFirstDisplayedIndex(), this.getLastDisplayedIndex());
    }

    public void clear()
    {
        if(items == null) return;
        this.items.clear();
        this.isDirty = true;
    }
    
    public void setItemsPerRow(int itemsPerRow)
    {
        this.columnsPerRow = Math.max(1, itemsPerRow);
        this.isDirty = true;
    }
    
    public int getItemsPerTab()
    {
        if(items == null) return 0;
        this.refreshContentCoordinatesIfNeeded();
        return this.itemsPerTab;
    }
   
    public int size()
    {
        if(items == null) return 0;
        return this.items.size();
    }
    
    public void setSelectedIndex(int index)
    {
        if(items == null) return;
        this.selectedItemIndex = MathHelper.clamp(index, NO_SELECTION, this.items.size() - 1);
        this.showSelected();
    }
    
    public void setSelected(T selectedItem)
    {
        if(items == null || selectedItem == null)
        {
            this.setSelectedIndex(NO_SELECTION);
        }
        else
        {
            int i = this.items.indexOf(selectedItem);
            if(i >= -1) this.setSelectedIndex(i);
        }
    }
    
    public int getSelectedIndex()
    {
        if(items == null) return NO_SELECTION;
        return this.selectedItemIndex;
    }

    /** index of first item on selected tab */
    public int getFirstDisplayedIndex()
    {
        if(items == null) return -1;
        this.refreshContentCoordinatesIfNeeded();
        return this.selectedTabIndex * this.itemsPerTab;
    }
    
    /** index of first item on selected tab, EXCLUSIVE of the last item */
    public int getLastDisplayedIndex()
    {
        if(items == null) return -1;
        this.refreshContentCoordinatesIfNeeded();
        return Useful.min((this.selectedTabIndex + 1) * this.itemsPerTab, this.items.size());
    }
    
    /** 
     * If the currently selected item is on the current tab, is the 0-based position within the tab.
     * Returns NO_SELECTION if the currently selected item is not on the current tab or if no selection.
     */
    public int getHighlightIndex()
    {
        if(items == null || this.selectedItemIndex == NO_SELECTION) return NO_SELECTION;
        this.refreshContentCoordinatesIfNeeded();
        int result = this.selectedItemIndex - this.getFirstDisplayedIndex();
        return (result < 0 || result >= this.getItemsPerTab()) ? -1 : result;
    }
    
    /** moves the tab selection to show the currently selected item */
    public void showSelected()
    {
        //can't implement here because layout may not be set when called - defer until next refresh
        this.focusOnSelection = true;
        this.isDirty = true;
    }
    
    protected double actualItemSize() 
    {
        this.refreshContentCoordinatesIfNeeded();
        return this.actualItemSize;
    }   
}
