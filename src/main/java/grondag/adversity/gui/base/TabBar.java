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
    public static final int TAB_HEIGHT = 8;
    public static final int ITEM_SPACING = 4;
    public static final int ARROW_WIDTH = 10;
    
    private int tabCount;
    private double tabWidth;
    private int itemsPerTab;
    private int rowsPerTab;
    private int itemsPerRow;
    private int selectedItemIndex;
    private int selectedTabIndex;
    
    public int buttonColor = DISABLED_COLOR_DEFAULT;
    public int selectedColor = BUTTON_COLOR_DEFAULT;
    
    private double maxItemSize = 32;
    private double actualItemSize;
    private double verticalItemMargin;
    private double horizontalItemMargin;
    private double tabTop;
    private double tabBottom;
    private double tabAllowance;
    private double tabMargin;
    
    private List<T> items;
    
    protected static enum MouseLocation
    {
        NONE,
        LEFT_ARROW,
        RIGHT_ARROW,
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
            if(++column == this.itemsPerRow)
            {
                column = 0;
                itemY += (this.actualItemSize + this.verticalItemMargin);
                itemX = this.left;
            }
            else
            {
                itemX += (this.actualItemSize + this.horizontalItemMargin);
            }
        }
        
        double tabMiddleY = (tabTop + tabBottom) / 2.0;
        
        
        
        // if tabs are too small, just do a continuous bar
        double tabLeft = this.left + ARROW_WIDTH + ITEM_SPACING;
        if(this.tabMargin == 0.0)
        {
            
            GuiUtil.drawRect(tabLeft, tabTop, tabLeft + this.tabAllowance, tabTop + TAB_HEIGHT,this.buttonColor);
     
            // box width is same as tab height, so need to have it be half that extra to the right so that we keep our margins with the arrows
            double selectionCenterX = this.tabCount > 1 
                ? tabLeft + TAB_HEIGHT / 2.0 + (this.tabAllowance - TAB_HEIGHT) * (double) this.selectedTabIndex / (this.tabCount - 1)
                : tabLeft + this.tabAllowance / 2.0;
            
            GuiUtil.drawRect(selectionCenterX -  TAB_HEIGHT / 2.0, tabTop, selectionCenterX +  TAB_HEIGHT / 2.0, tabTop + TAB_HEIGHT, this.selectedColor);
            
        }
        else
        {
            for(int i = 0; i < this.tabCount; i++)
            {
                GuiUtil.drawRect(tabLeft, tabTop, tabLeft + this.tabWidth, tabTop + TAB_HEIGHT,
                        i == this.selectedTabIndex ? this.selectedColor : this.buttonColor);
                
                tabLeft += (this.tabWidth + this.tabMargin);
            }
        }
        GuiUtil.drawQuad(this.left, tabMiddleY, this.left + ARROW_WIDTH, tabBottom, this.left + ARROW_WIDTH, tabTop, this.left, tabMiddleY, this.buttonColor);

        GuiUtil.drawQuad(this.right, tabMiddleY, this.right - ARROW_WIDTH, tabTop, this.right - ARROW_WIDTH, tabBottom, this.right, tabMiddleY, this.buttonColor);
        
    }

    protected  abstract void drawItem(T item, Minecraft mc, RenderItem itemRender, double left, double top, float partialTicks);
    
    private void updateMouseLocation(int mouseX, int mouseY)
    {
        if(mouseX < this.left || mouseX > this.right || mouseY < this.top || mouseY > this.bottom)
        {
            this.currentMouseLocation = MouseLocation.NONE;
        }
        else if(mouseY >= this.tabTop && mouseY <= this.tabBottom)
        {
            if(mouseX <= this.left + ARROW_WIDTH + ITEM_SPACING / 2.0)
            {
                this.currentMouseLocation = MouseLocation.LEFT_ARROW;
            }
            else if(mouseX >= this.right - ARROW_WIDTH - ITEM_SPACING / 2.0)
            {
                this.currentMouseLocation = MouseLocation.RIGHT_ARROW;
            }
            else
            {
                this.currentMouseLocation = MouseLocation.TAB;
                this.currentMouseIndex = Useful.clamp((int) ((mouseX - this.left - ARROW_WIDTH - ITEM_SPACING / 2) / (this.tabAllowance) * this.tabCount), 0, this.tabCount - 1);
//                this.currentMouseIndex = (int) ((mouseX - this.left - ARROW_WIDTH - this.actualItemMargin / 2) / (this.tabWidth + this.tabMargin));
            }
        }
        else if(mouseY < this.tabTop - this.verticalItemMargin / 2)
        {
            this.currentMouseLocation = MouseLocation.ITEM;
            int newIndex = this.getFirstDisplayedIndex() + (int)((mouseY - this.top - this.verticalItemMargin / 2) / (this.actualItemSize + this.verticalItemMargin)) * this.itemsPerRow
                    + (int)((mouseX - this.left - this.horizontalItemMargin / 2) / (this.actualItemSize + this.horizontalItemMargin));
            
            this.currentMouseIndex = (newIndex < this.items.size()) ? newIndex : -1;
        }
        else
        {
            this.currentMouseLocation = MouseLocation.NONE;
        }
        
    }
    
    @Override
    protected void handleCoordinateUpdate()
    {
        if(this.items != null)
        {
            
            double verticalSpaceRemaining = this.height - TAB_HEIGHT;
            this.rowsPerTab = Math.max(1, (int) (verticalSpaceRemaining / (this.maxItemSize + ITEM_SPACING)));
            this.actualItemSize = (verticalSpaceRemaining - rowsPerTab * ITEM_SPACING) / rowsPerTab;
            this.itemsPerRow = (int) ((this.width + ITEM_SPACING) / (actualItemSize + ITEM_SPACING));
            this.itemsPerTab = rowsPerTab * itemsPerRow;
            double itemGap = this.width - this.actualItemSize * itemsPerRow;
            this.horizontalItemMargin = this.itemsPerRow == 0 ? 0 : itemGap / (this.itemsPerRow - 1);
            this.verticalItemMargin = ITEM_SPACING;
            this.tabCount = this.itemsPerTab > 0 ? (this.items.size() + this.itemsPerTab - 1) / this.itemsPerTab : 0;
            this.tabAllowance = this.width - (ARROW_WIDTH + ITEM_SPACING) * 2;
            this.tabMargin = this.tabCount <= 1 ? 0.0 : Useful.clamp((tabAllowance - this.tabCount * TAB_HEIGHT) / (tabCount - 1), 0.0, TAB_MARGIN);
            if(this.tabMargin < 1) this.tabMargin = 0.0;
            this.tabWidth = this.tabCount <= 1 ? this.tabAllowance : (this.tabAllowance - (this.tabCount - 1) * this.tabMargin) / this.tabCount;
            this.tabTop = this.top + (actualItemSize + this.verticalItemMargin) * this.rowsPerTab ;
            this.tabBottom = tabTop + TAB_HEIGHT;
            
            if(Output.DEBUG_MODE) assert(this.tabBottom <= this.bottom);
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

        case LEFT_ARROW:
            if(this.selectedTabIndex > 0) this.selectedTabIndex--;
            GuiUtil.playPressedSound(mc);
            break;

        case RIGHT_ARROW:
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

        case LEFT_ARROW:
            break;

        case RIGHT_ARROW:
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
    
//    public List<T> getDisplayed()
//    {
//        return this.items.subList(this.getFirstDisplayedIndex(), this.getLastDisplayedIndex());
//    }

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
        this.refreshContentCoordinatesIfNeeded();
        if(this.getItemsPerTab() > 0) this.selectedTabIndex = this.selectedItemIndex / this.getItemsPerTab();
    }
    
    protected double actualItemSize() 
    {
        this.refreshContentCoordinatesIfNeeded();
        return this.actualItemSize;
    }   
}
