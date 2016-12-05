package grondag.adversity.feature.volcano.lava;

public class FlowFactory
{

    public static FlowNode createFlowFromCell(LavaManager2 lavaManager, LavaCell cell)
    {

        FlowNode newNode = null;
        
        switch(cell.getDefaultCellType(lavaManager.cellTracker))
        {
        case DROP:
            newNode = new FlowCell((SpaceCell) cell, cellTracker, null);
            nodes.addLast(newCell);
            lava.addLast(newCell);

            break;

        case SPREAD:
            break;

        case POOL:
        default:
            break;
        }
        
        return newNode;
    }
}
