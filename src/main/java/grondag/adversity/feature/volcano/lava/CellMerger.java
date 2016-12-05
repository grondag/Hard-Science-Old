package grondag.adversity.feature.volcano.lava;

public class CellMerger
{
    /**
     * Merges two cells to form one cell.
     * Cells must be in the same X,Z world column.
     * Any vertical gap between them is assumed to be open space and is included in the merged cell.
     * 
     * Returns the merged cell.
     */
    public static LavaCell MergeWithVerticalSpace(LavaCell first, LavaCell second)
    {
        return second;
        
    }
}
