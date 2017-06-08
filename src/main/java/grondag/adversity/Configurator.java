package grondag.adversity;

import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.*;

@LangKey("adversity.config.general")
@Config(modid = Adversity.MODID, type = Type.INSTANCE)
public class Configurator
{
    
    public static void recalcDervied()
    {
        Render.recalcDerived();
        Volcano.recalcDerived();
    }
    
    ////////////////////////////////////////////////////        
    // SUBSTANCES
    ////////////////////////////////////////////////////
    @LangKey("adversity.config.substance")
    @Comment("Adversity material properties.")
    public static Substances SUBSTANCES = new Substances();
    
    public static class Substances
    {
        public Substance flexstone = new Substance(2, "pickaxe", 1, 10, 1.0);
        
        public Substance durastone = new Substance(4, "pickaxe", 2, 50, 1.15);

        public Substance hyperstone = new Substance(10, "pickaxe", 3, 200, 1.3);
        
        public Substance flexiglass = new Substance(2, "pickaxe", 1, 10, 1.0);
        
        public Substance duraglass = new Substance(4, "pickaxe", 2, 50, 1.15);

        public Substance hyperglass = new Substance(10, "pickaxe", 3, 200, 1.3);
        
        public Substance flexwood = new Substance(2, "axe", 1, 10, 1.0);

        public Substance durawood = new Substance(4, "axe", 2, 50, 1.15);
        
        public Substance hyperwood = new Substance(10, "axe", 3, 200, 1.3);
        
        public Substance basalt = new Substance(2, "pickaxe", 1, 10, 1.0);

        public Substance volcanicLava = new Substance(-1, "shovel", 3, 2000, 0.75);
        
        
        public static class Substance
        {
            @RequiresMcRestart
            @Comment("Material hardness. 2 is typical for things like rock, wood. Anything above 10 is extreme. -1 is unbreakable")
            @RangeInt(min = -1, max = 2000)
            public int hardness;

            @RequiresMcRestart
            @Comment("Tool used to break block. Normal values are pickaxe, shovel and axe")
            public String harvestTool;
            
            @RequiresMcRestart
            @Comment("Level of tool needed to break block. Range 1-3 is normal for vanilla.")
            @RangeInt(min = 0, max = 10)
            public int harvestLevel;
            
            @RequiresMcRestart
            @Comment("Material explosion resistance")
            @RangeInt(min = 1, max = 2000)
            public int resistance;

            @RequiresMcRestart
            @Comment("Material speed modifier for entities walking on its surface.")
            @RangeDouble(min = 0.25, max = 2.0)
            public double walkSpeedFactor;
            
            public Substance(int hardness, String harvestTool, int harvestLevel, int resistance, double walkSpeedFactor)
            {
                this.hardness = hardness;
                this.harvestTool = harvestTool;
                this.harvestLevel = harvestLevel;
                this.resistance = resistance;
                this.walkSpeedFactor = walkSpeedFactor;
            }
        }
    }    
    
    ////////////////////////////////////////////////////        
    // RENDERING
    ////////////////////////////////////////////////////
    @LangKey("adversity.config.render")
    @Comment("Settings for visual appearance.")
    public static Render RENDER = new Render();
    
    public static class Render
    {
        @Comment("Maxiumum number of quads held in cache for reuse. Higher numbers may result is less memory consuption overall, up to a point.")
        @RangeInt(min = 0xFFFF, max = 0xFFFFF)
        public int quadCacheSizeLimit = 524280;
        
        @Comment("Collect statistics on quad caching.")
        public boolean enableQuadCacheStatistics = true;
        
        @Comment({"Shade blocks from this mod with a uniform light vector. Provides a somewhat better appearance for flowing ",
                  "lava blocks (for example) but may appear odd when next to blocks from Vanilla or other mods."})
        public boolean enableCustomShading = true;
        
        @Comment({"If true, Dynamic flow block (volcanic lava and basalt) will not render faces occulded by adjacent flow blocks.",
                  " True is harder on CPU and easier on your graphics card/chip.  Experiment if you have FPS problems.",
                  " Probably won't matter on systems with both a fast CPU and fast graphics."})
        public boolean enableFaceCullingOnFlowBlocks = true;
        
        @Comment("Minimum lighting on any block face with custom shading. Smaller values give deeper shadows.")
        @RangeDouble(min = 0, max = 0.9)
        public float minAmbientLight =0.3F;
        
        @Comment("X component of ambient light source.")
        @RangeDouble(min = -1, max = 1)
        public float normalX = 0.0F;
        
        @Comment("Y component of ambient light source.")
        @RangeDouble(min = -1, max = 1)
        public float normalY = 1.0F;

        @Comment("Z component of ambient light source.")
        @RangeDouble(min = -1, max = 1)
        public float normalZ = 0.25F;
      
        @Comment("Debug Feature: draw block boundaries for non-cubic blocks.")
        public boolean debugDrawBlockBoundariesForNonCubicBlocks = false;
        
        public static float normalLightFactor;
        
        public static Vec3d lightingNormal;
        
        private static void recalcDerived()
        {
            normalLightFactor = 0.5F * (1F - RENDER.minAmbientLight);
            lightingNormal = new Vec3d(RENDER.normalX, RENDER.normalY, RENDER.normalZ).normalize();
        }
    }
    
    ////////////////////////////////////////////////////        
    // VOLCANO
    ////////////////////////////////////////////////////
    @LangKey("adversity.config.volcano")
    @Comment("Settings for Volcano feature.")
    public static Volcano VOLCANO = new Volcano();
    
    public static class Volcano
    {
        @Comment("Fraction of alloted CPU usage must be drop below this before volcano starts to flow again.")
        @RangeDouble(min = 0.3, max = 0.7)
        public float cooldownTargetLoadFactor = 0.5F;
        
        @Comment("After volcano in cooldown reaches the target load factor, it waits this many ticks before flowing again.")
        @RangeInt(min = 0, max = 6000)
        public int cooldownWaitTicks = 200;
        
        @Comment("Y-axis build limit at which Volcano becomes permanently dormant.")
        @RangeInt(min = 128, max = 255)
        public int maxYLevel = 147; 
        
        @Comment("Number of blocks per tick that can be cleared by volcano mounding. Values above 1 are mostly useful for testing.")
        @RangeInt(min = 1, max = 64)
        public int moundBlocksPerTick = 64;
        
        @Comment("Radius of one standard deviation, in blocks, for underground volcano mounding.")
        @RangeInt(min = 14, max = 28)
        public int moundRadius = 20;
        
        @Comment("Minimum number of ticks between the time a volcano becomes dormant and the same or another erupts.")
        @RangeInt(min = 20, max = 24000000)
        public int minDormantTicks = 20;
      
        @Comment({"Maximum number of ticks between the time a volcano becomes dormant and the same or another erupts.",
                  "Should be larger than minDormantTicks"})
        @RangeInt(min = 20, max = 24000000)
        public int maxDormantTicks = 200;
      
        private static void recalcDerived()
        {
            if(VOLCANO.maxDormantTicks <= VOLCANO.minDormantTicks)
            {
                VOLCANO.maxDormantTicks = VOLCANO.minDormantTicks + 20;
            }
        }

    }
    
    ////////////////////////////////////////////////////        
    // HYPERSTONE
    ////////////////////////////////////////////////////
    @LangKey("adversity.config.hypermaterial")
    @Comment("Settings for hyperdimensional building materials.")
    public static HyperStone HYPERSTONE = new HyperStone();
    
    public static class HyperStone
    {
        @Comment("If false, mobs cannot spawn on hyper-dimensional blocks in darkness; similar to slabs.")
        public boolean allowMobSpawning = false;
        
        @Comment("If false, normal fires directly above hyper-dimensional blocks are immediately extinguished.")
        public boolean allowFire = false;
        
        @Comment("If false, players cannot harvest hyper-dimensional blocks without silk touch - they can be broken but drop rubble.")
        public boolean allowHarvest = false;
        
        @Comment("If true, hyper-dimensional blocks can be harvested intact with silk touch. Only matters if allowHarvest is true.")
        public boolean allowSilkTouch = true;
        
        @Comment("If true, hyper-dimensional blocks have a chance to lose durability due to damage from entities or explosions.")
        public boolean canBeDamaged;
     }
}
