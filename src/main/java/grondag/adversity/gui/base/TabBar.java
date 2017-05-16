package grondag.adversity.gui.base;

import java.util.Collection;
import java.util.List;

import grondag.adversity.Output;
import grondag.adversity.gui.GuiUtil;
import grondag.adversity.library.Useful;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;

public abstract class TabBar<T> extends GuiControl
{
    public static final int TAB_MARGIN = 2;
    public static final int TAB_WIDTH = 8;
    public static final int ITEM_SPACING = 4;
    //TODO: remove
    public static final int ARROW_WIDTH = 10;
    
    private int tabCount;
//    private double tabWidth;
    private int itemsPerTab;
    private int columnsPerTab;
    private int rowsPerTab;
    private int selectedItemIndex;
    private int selectedTabIndex;
    
    public int buttonColor = DISABLED_COLOR_DEFAULT;
    public int selectedColor = BUTTON_COLOR_DEFAULT;
    
    private double maxItemSize = 64;
    private double actualItemSize;
    private double tabSize;
    
//    private double verticalItemMargin;
//    private double horizontalItemMargin;
//    private double tabTop;
//    private double tabBottom;
    private double scrollHeight;
//    private double tabMargin;
    
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

    @Override
    protected void drawContent(Minecraft mc, RenderItem itemRender, int mouseX, int mouseY, float partialTicks)
    {
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
                GuiUtil.drawBox(itemX - 2, itemY - 2, itemX + this.actualItemSize + 2, itemY + this.actualItemSize + 2, 1, GuiControl.FOCUS_COLOR_DEFAULT);
            }
            else if(this.selectedItemIndex == i)
            {
                GuiUtil.drawBox(itemX - 2, itemY - 2, itemX + this.actualItemSize + 2, itemY + this.actualItemSize + 2, 1, GuiControl.BUTTON_COLOR_DEFAULT);
            }
            
            this.drawItem(this.get(i), mc, itemRender, itemX, itemY, partialTicks);
            if(++column == this.columnsPerTab)
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
            
            GuiUtil.drawRect(this.right - TAB_WIDTH, tabStartY, this.right, tabStartY + this.scrollHeight, this.buttonColor);
     
            // box width is same as tab height, so need to have it be half that extra to the right so that we keep our margins with the arrows
            double selectionCenterY = tabStartY + TAB_WIDTH / 2.0 + (this.scrollHeight - TAB_WIDTH) * (double) this.selectedTabIndex / (this.tabCount - 1);
            
            GuiUtil.drawRect(this.right - TAB_WIDTH, selectionCenterY -  TAB_WIDTH / 2.0, this.right, selectionCenterY +  TAB_WIDTH / 2.0, this.selectedColor);
            
        }
        else
        {
            for(int i = 0; i < this.tabCount; i++)
            {
                GuiUtil.drawRect(this.right - TAB_WIDTH, tabStartY, this.right, tabStartY + this.tabSize,
                        i == this.selectedTabIndex ? this.selectedColor : this.buttonColor);
                tabStartY += (this.tabSize + TAB_MARGIN);
            }
        }
        
        double arrowCenterX = this.right - TAB_WIDTH / 2.0;

        GuiUtil.drawQuad(arrowCenterX, this.top, this.right - TAB_WIDTH, this.top + TAB_WIDTH, this.right, this.top + TAB_WIDTH, arrowCenterX, this.top, this.buttonColor);

        GuiUtil.drawQuad(arrowCenterX, this.bottom, this.right, this.bottom - TAB_WIDTH, this.right - TAB_WIDTH, this.bottom - TAB_WIDTH, arrowCenterX, this.bottom, this.buttonColor);
        
    }

    protected  abstract void drawItem(T item, Minecraft mc, RenderItem itemRender, double left, double top, float partialTicks);
    
    private void updateMouseLocation(int mouseX, int mouseY)
    {
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
                this.currentMouseIndex = Useful.clamp((int) ((mouseY - this.top - TAB_WIDTH - ITEM_SPACING / 2) / (this.scrollHeight) * this.tabCount), 0, this.tabCount - 1);
//                this.currentMouseIndex = (int) ((mouseX - this.left - TAB_WIDTH - this.actualItemMargin / 2) / (this.tabWidth + this.tabMargin));
            }
        }
        else
        {
            int newIndex = this.getFirstDisplayedIndex() + (int)((mouseY - this.top - ITEM_SPACING / 2) / (this.actualItemSize + ITEM_SPACING)) * this.columnsPerTab
                    + Math.min((int)((mouseX - this.left - ITEM_SPACING / 2) / (this.actualItemSize + ITEM_SPACING)), this.columnsPerTab - 1);
            
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
            this.actualItemSize = Useful.clamp(horizontalSpaceRemaining - ITEM_SPACING, 16, this.maxItemSize);

            this.columnsPerTab = Math.max(1, (int) (horizontalSpaceRemaining / (this.actualItemSize + ITEM_SPACING)));
            this.rowsPerTab = (int) ((this.height + ITEM_SPACING) / (actualItemSize + ITEM_SPACING));
            this.itemsPerTab = columnsPerTab * rowsPerTab;
            this.tabCount = this.itemsPerTab > 0 ? (this.items.size() + this.itemsPerTab - 1) / this.itemsPerTab : 0;
            this.scrollHeight = this.height - (TAB_WIDTH + ITEM_SPACING) * 2;
            this.tabSize = tabCount <= 0 ? 0 : (this.scrollHeight - (TAB_MARGIN * (this.tabCount - 1))) / tabCount;
            if(tabSize < TAB_MARGIN * 2) tabSize = 0;
        }
    }

    @Override
    protected void handleMouseClick(Minecraft mc, int mouseX, int mouseY)
    {
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
    
    public void add(T item)
    {
        this.items.add(item);
        this.isDirty = true;
    }

    public void addAll(Collection<T> items)
    {
        this.items.addAll(items);
        this.isDirty = true;
    }
    
    public void addAll(T[] itemsIn)
    {
        for(T item : itemsIn)
        {
            this.items.add(item);
        }
        this.isDirty = true;
    }
    
    public T get(int index)
    {
        return this.items.get(index);
    }
    
    public T getSelected()
    {
        return this.get(this.getSelectedIndex());
    }
    
    public List<T> getDisplayed()
    {
        return this.items.subList(this.getFirstDisplayedIndex(), this.getLastDisplayedIndex());
    }

    public void clear()
    {
        this.items.clear();
        this.isDirty = true;
    }
    
    public void setMaxItemSize(double maxItemSize)
    {
        this.maxItemSize = Math.max(16,  maxItemSize);
        this.isDirty = true;
    }
    
    public int getItemsPerTab()
    {
        this.refreshContentCoordinatesIfNeeded();
        return this.itemsPerTab;
    }
   
    public int size()
    {
        return this.items.size();
    }
    
    public void setSelectedIndex(int index)
    {
        this.selectedItemIndex = Useful.clamp(index, 0, this.items.size() - 1);
        this.showSelected();
    }
    
    public void setSelected(T selectedItem)
    {
        int i = this.items.indexOf(selectedItem);
        if(i >= 0) this.setSelectedIndex(i);
    }
    
    public int getSelectedIndex()
    {
        return this.selectedItemIndex;
    }

    /** index of first item on selected tab */
    public int getFirstDisplayedIndex()
    {
        this.refreshContentCoordinatesIfNeeded();
        return this.selectedTabIndex * this.itemsPerTab;
    }
    
    /** index of first item on selected tab, EXCLUSIVE of the last item */
    public int getLastDisplayedIndex()
    {
        this.refreshContentCoordinatesIfNeeded();
        return Useful.min((this.selectedTabIndex + 1) * this.itemsPerTab, this.items.size());
    }
    
    /** 
     * If the currently selected item is on the current tab, is the 0-based position within the tab.
     * Returns -1 if the currently selected item is not on the current tab.
     */
    public int getHighlightIndex()
    {
        this.refreshContentCoordinatesIfNeeded();
        int result = this.selectedItemIndex - this.getFirstDisplayedIndex();
        return (result < 0 || result >= this.getItemsPerTab()) ? -1 : result;
    }
    
    /** moves the tab selection to show the currently selected item */
    public void showSelected()
    {
        
        //TODO: doesn't work because layout not set when called - defer until first display
        this.refreshContentCoordinatesIfNeeded();
        if(this.getItemsPerTab() > 0) this.selectedTabIndex = this.selectedItemIndex / this.getItemsPerTab();
    }
    
    protected double actualItemSize() 
    {
        this.refreshContentCoordinatesIfNeeded();
        return this.actualItemSize;
    }   
}
