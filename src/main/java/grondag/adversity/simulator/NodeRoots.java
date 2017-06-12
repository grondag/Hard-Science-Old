package grondag.adversity.simulator;

public enum NodeRoots
{
    SIMULATION,
    VOLCANO_MANAGER,
    LAVA_SIMULATOR;
    
    public static final int FIRST_NORMAL_NODE_ID = 100;
    public static final String SUBNODES_TAG = "sn";
    
    public String getTagKey() { return "" + this.ordinal(); }
    
}