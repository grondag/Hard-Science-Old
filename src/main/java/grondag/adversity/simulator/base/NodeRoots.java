package grondag.adversity.simulator.base;

public enum NodeRoots
{
    SIMULATION,
    VOLCANO_MANAGER;
    
    public static final int FIRST_NORMAL_NODE_ID = 100;
    public static final String SUBNODES_TAG = "sn";
    
    public String getTagKey() { return "" + this.ordinal(); }
    
}
