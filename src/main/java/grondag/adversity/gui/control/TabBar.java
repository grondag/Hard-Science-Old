package grondag.adversity.gui.control;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import grondag.adversity.gui.GuiUtil;
import grondag.adversity.library.Useful;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;

public class TabBar<T> extends GuiControl
{
    private static final int TAB_MARGIN = 1;
    
    private int tabCount;
    private double tabWidth;
    private int itemsPerTab = 10;
    private int selectedItemIndex;
    private int selectedTabIndex;
    
    public int buttonColor = BUTTON_COLOR_DEFAULT;
    public int selectedColor = FOCUS_COLOR_DEFAULT;
    
    private ArrayList<T> items = new ArrayList<T>();
    
    public TabBar(double left, double top, double width, double height)
    {
        super(left, top, width, height);
    }

    @Override
    protected void drawContent(Minecraft mc, RenderItem itemRender, int mouseX, int mouseY, float partialTicks)
    {
        double left = this.contentLeft();
        for(int i = 0; i < this.tabCount; i++)
        {
            GuiUtil.drawRect(left, this.contentTop(), left + this.tabWidth, this.contentBottom(), 
                    i == this.selectedTabIndex ? this.selectedColor : this.buttonColor);
            left += (this.tabWidth + TAB_MARGIN);
        }
        
    }

    @Override
    protected void handleCoordinateUpdate()
    {
        this.tabCount = this.itemsPerTab > 0 ? (this.items.size() + this.itemsPerTab - 1) / this.itemsPerTab : 0;
        this.tabWidth = this.tabCount > 0 ? (this.contentWidth - (this.tabCount - 1) * TAB_MARGIN) / this.tabCount : 0;
    }

    @Override
    public void handleMouseInput(int mouseX, int mouseY)
    {
        
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
    
    public List<T> getDisplayed()
    {
        return this.items.subList(this.getFirstDisplayedIndex(), this.getLastDisplayedIndex());
    }

    public void clear()
    {
        this.items.clear();
        this.refreshContentCoordinates();
    }
    
    public void setItemsPerTab(int itemsPerTab)
    {
        this.itemsPerTab = itemsPerTab;
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
}
