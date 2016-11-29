package grondag.adversity.config;

import net.minecraftforge.common.config.Configuration;

public class Volcano
{
    public final boolean enabled;
    public final int backtrackIncrement;// = 2;
    public final int baseTicksPerBlock;// = 1;
    public final int randTicksPerBlock;// = 0;
    public final int coolingLagTicks;// = 1;
    public final int blockTrackingMax;// = 4000;
    public final int maxYLevel;// = 213;
    public final int moundRadius;// = 7;
    public final int lavaSpreadRadius;
    public final int lavaSpreadRadiusSquared;// = 49;
    public final int boreRadius;
    public final int boreRadiusSquared;// = 49;
    public final int topSpreadRadius;
    public final int topSpreadRadiusSquared;// = 144;
    
    public final int minDormantTicks;
    public final int maxDormantTicks;
    public final int blockOperationsPerTick;
    
//    /** blocks that will always be destroyed by lava/mounding, checked before hardBlocks and hardMaterials */
//    public final HashSet<Block> softBlocks = new HashSet<Block>();
//    /** blocks that will not be destroyed by lava / mounding, but may be displaced */
//    public final HashSet<Block> hardBlocks = new HashSet<Block>();
//
//    /** Materials that will not be destroyed by lava / mounding, but may be displaced.
//     *  These are not actually configurable by config file because there is no
//     *  material registry and they are finalally identified - but want all reference data in one place.
//     */
//    public final HashSet<Material> hardMaterials = new HashSet<Material>();
    
    public Volcano(Configuration config)
    {
        config.addCustomCategoryComment("Volcano", "Settings for Volcano.");

        enabled = config.getBoolean("enabled", "Volcano", true, "Enables or disables Volcano feature.");

        backtrackIncrement = config.getInt("backtrackIncrement", "Volcano", 2, 1, 5, 
                "How deep lava can pool before stream ends.");
        
        baseTicksPerBlock = config.getInt("baseTicksPerBlock", "Volcano", 1, 1, 200, 
                "Minimum ticks between block placenent. Small values may impact performance.");
        
        randTicksPerBlock = config.getInt("randTicksPerBlock", "Volcano", 0, 0, 200, 
                "Random additional ticks between block placenent.");

        coolingLagTicks = config.getInt("coolingLagTicks", "Volcano", 1, 1, 200, 
                "Ticks for lava/basalt blocks to cool from one phase to another.");

        blockTrackingMax = config.getInt("blockTrackingMax", "Volcano", 4000, 3000, 6000, 
                "Max active blocks a volcano can track before it must pause to wait for cooldown.");

        maxYLevel = config.getInt("maxYLevel", "Volcano", 147, 128, 255, 
                "Y-axis build limit at which Volcano becomes permanently dormant.");

        moundRadius = config.getInt("moundRadius", "Volcano", 14, 7, 21, 
                "Radius of one standard deviation, in blocks, for underground volcano mounding.");
        
        lavaSpreadRadius = config.getInt("lavaSpreadRadius", "Volcano", 12, 5, 24, 
                "How far volcanic lava can spread.");
        lavaSpreadRadiusSquared = lavaSpreadRadius * lavaSpreadRadius;

        topSpreadRadius = config.getInt("topSpreadRadius", "Volcano", 12, 5, 24, 
                "How far volcanic lava can spread at top of volcano. Usually larger than regular spread.");
        topSpreadRadiusSquared = topSpreadRadius * topSpreadRadius;
        
        boreRadius = config.getInt("boreRadius", "Volcano", 7, 5, 15, 
                "Radius of central lava core. All blocks in core are destroyed and lava in core never cools.");
        boreRadiusSquared = boreRadius * boreRadius;
        
        minDormantTicks = config.getInt("minDormantTicks", "Volcano", 20, 20, 24000000, 
                "Radius of central lava core. All blocks in core are destroyed and lava in core never cools.");

        maxDormantTicks = Math.max(minDormantTicks + 20,config.getInt("maxDormantTicks", "Volcano", 200, 200, 240000000, 
                "Radius of central lava core. All blocks in core are destroyed and lava in core never cools."));
        
        blockOperationsPerTick = config.getInt("blockOperationsPerTick", "Volcano", 10, 1, 20, 
                "For testing and map making. Values > 1 cause volcano run in an accelerated mode. Likey to cause lag.");

    }
}