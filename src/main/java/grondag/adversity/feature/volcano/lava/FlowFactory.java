package grondag.adversity.feature.volcano.lava;

public class FlowFactory
{

    public static FlowNode createFlowFromCell(LavaManager2 lavaManager, LavaCell cell, FlowNode firstInput)
    {

        FlowNode newNode = null;
        
        switch(cell.getDefaultCellType(lavaManager.cellTracker))
        {
        case DROP:
            newNode = new DropNode(lavaManager, cell, firstInput);
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
