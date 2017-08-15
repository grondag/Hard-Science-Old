package grondag.hard_science.machines;

public class ContainerLayout
{
    public static final ContainerLayout DEFAULT = new ContainerLayout();
    
    static
    {

    }

    /** 
     * Number of pixels between each slot corner for slots.
     * MC default is 18, which leaves 2px border between slots.
     */
    public int slotSpacing = 20;
    
    public int externalMargin = 10;
    
    public int dialogWidth = 200;
    
    public int dialogHeight = 240;

    /** distance from edge of dialog to start of player inventory area */
    public int playerInventoryLeft = externalMargin;
    
    /** distance from top of dialog to start of player inventory area */
    public int playerInventoryTop = dialogHeight - 2 * externalMargin - 16 * 2 -  slotSpacing * 2;
    
    
}
