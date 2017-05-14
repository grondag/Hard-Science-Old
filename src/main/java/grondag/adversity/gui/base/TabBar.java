package grondag.adversity.gui.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    private double actualItemMargin;
    private double tabTop;
    private double tabBottom;
    
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
    
    public TabBar(List<T> items, double left, double top, double width, double height)
    {
        super(left, top, width, height);
        this.items = items;
        this.handleCoordinateUpdate();
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
                itemY += (this.actualItemSize + actualItemMargin);
                itemX = this.left;
            }
            else
            {
                itemX += (this.actualItemSize + actualItemMargin);
            }
        }
        
        double tabMiddleY = (tabTop + tabBottom) / 2.0;
        
        double x = this.left + ARROW_WIDTH + this.actualItemMargin;
        for(int i = 0; i < this.tabCount; i++)
        {
            GuiUtil.drawRect(x, tabTop, x + this.tabWidth, tabTop + TAB_HEIGHT,
                    i == this.selectedTabIndex ? this.selectedColor : this.buttonColor);
            
            x += (this.tabWidth + this.actualItemMargin);
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
            if(mouseX <= this.left + ARROW_WIDTH + this.actualItemMargin / 2)
            {
                this.currentMouseLocation = MouseLocation.LEFT_ARROW;
            }
            else if(mouseX >= this.right - ARROW_WIDTH -  this.actualItemMargin / 2)
            {
                this.currentMouseLocation = MouseLocation.RIGHT_ARROW;
            }
            else
            {
                this.currentMouseLocation = MouseLocation.TAB;
                this.currentMouseIndex = (int) ((mouseX - this.left - ARROW_WIDTH - this.actualItemMargin / 2) / (this.tabWidth + this.actualItemMargin));
            }
        }
        else if(mouseY < this.tabTop - this.actualItemMargin / 2)
        {
            this.currentMouseLocation = MouseLocation.ITEM;
            int newIndex = this.getFirstDisplayedIndex() + (int)((mouseY - this.top - this.actualItemMargin / 2) / (this.actualItemSize + this.actualItemMargin)) * this.itemsPerRow
                    + (int)((mouseX - this.left - this.actualItemMargin / 2) / (this.actualItemSize + this.actualItemMargin));
            
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
            this.actualItemMargin = this.itemsPerRow == 0 ? 0 : itemGap / (this.itemsPerRow - 1);
            this.tabCount = this.itemsPerTab > 0 ? (this.items.size() + this.itemsPerTab - 1) / this.itemsPerTab : 0;
            this.tabWidth = this.tabCount > 0 ? (this.width - (this.tabCount + 1) * this.actualItemMargin - ARROW_WIDTH * 2) / this.tabCount : 0;
            this.tabTop = this.top + (actualItemSize + this.actualItemMargin) * this.rowsPerTab ;
            this.tabBottom = tabTop + TAB_HEIGHT;
        }
    }

    @Override
    public void handleMouseClick(Minecraft mc, int mouseX, int mouseY)
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
    public void handleMouseDrag(Minecraft mc, int mouseX, int mouseY)
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
        this.refreshContentCoordinates();
    }

    public void addAll(Collection<T> items)
    {
        this.items.addAll(items);
        this.refreshContentCoordinates();
    }
    
    public void addAll(T[] itemsIn)
    {
        for(T item : itemsIn)
        {
            this.items.add(item);
        }
        this.refreshContentCoordinates();
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
        this.refreshContentCoordinates();
    }
    
    public void setMaxItemSize(double maxItemSize)
    {
        this.maxItemSize = Math.max(16,  maxItemSize);
        this.handleCoordinateUpdate();
    }
    
    public int getItemsPerTab()
    {
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
        return this.selectedTabIndex * this.itemsPerTab;
    }
    
    /** index of first item on selected tab, EXCLUSIVE of the last item */
    public int getLastDisplayedIndex()
    {
        return Useful.min((this.selectedTabIndex + 1) * this.itemsPerTab, this.items.size());
    }
    
    /** 
     * If the currently selected item is on the current tab, is the 0-based position within the tab.
     * Returns -1 if the currently selected item is not on the current tab.
     */
    public int getHighlightIndex()
    {
        int result = this.selectedItemIndex - this.getFirstDisplayedIndex();
        return (result < 0 || result >= this.getItemsPerTab()) ? -1 : result;
    }
    
    /** moves the tab selection to show the currently selected item */
    public void showSelected()
    {
        this.selectedTabIndex = this.selectedItemIndex / this.getItemsPerTab();
    }
    
    protected double actualItemSize() 
    {
        return this.actualItemSize;
    }
}
