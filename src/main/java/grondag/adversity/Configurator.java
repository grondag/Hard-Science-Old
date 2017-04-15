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
        public Substance flexstone = new Substance(2, "pickaxe", 1, 10);
        
        public Substance durastone = new Substance(4, "pickaxe", 2, 50);

        public Substance hyperstone = new Substance(10, "pickaxe", 3, 200);
        
        public Substance basalt = new Substance(2, "pickaxe", 1, 10);

        public Substance volcanicLava = new Substance(-1, "shovel", 3, 2000);
        
        public Substance superwood = new Substance(2, "axe", 1, 10);
        
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
            
            public Substance(int hardness, String harvestTool, int harvestLevel, int resistance)
            {
                this.hardness = hardness;
                this.harvestTool = harvestTool;
                this.harvestLevel = harvestLevel;
                this.resistance = resistance;
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
        @Comment("Y-axis build limit at which Volcano becomes permanently dormant.")
        @RangeInt(min = 128, max = 255)
        public int maxYLevel = 147; 
        
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
        @Comment("Enable special behaviors for hyper-dimensional building materials")
        public boolean enabled = true;
        
        @Comment("If false, mobs cannot spawn on hyper-dimensional blocks in darkness; similar to slabs.")
        public boolean allowMobSpawning = false;
        
        @Comment("If false, normal fires directly above hyper-dimensional blocks are immediately extinguished.")
        public boolean allowFire = false;
        
        @Comment("If false, players cannot harvest hyper-dimensional blocks without silk touch - they can be broken but drop rubble.")
        public boolean allowHarvest = false;
        
        @Comment("If true, hyper-dimensional blocks can be harvested intact with silk touch. Only matters if allowHarvest is true.")
        public boolean allowSilkTouch = true;
        
        @Comment("If > 0, hyper-dimensional blocks will slow repair themselves at random intervals.")
        @RangeDouble(min = 0, max = 1)
        public float selfRepairChance = 0.2F;
        
        @Comment("If true, hyper-dimensional blocks that are fully failed can still self repair. (But only if selfRepairChance > 0.)")
        public boolean selfRepairFailedBlocks = false;
        
        @Comment("If true, hyper-dimensional blacks have a chance to lose durability due to damage from entities or explosions.")
        public boolean canBeDamaged;
        
        @Comment("Hardnesss (mining speed) of undamaged hyper-dimensional blocks.")
        @RangeDouble(min = 0, max = 50)
        public float startingHardness = 25F;
        
        @Comment("If canBeDamaged=true, the remaining hardness each time a hyper-dimensional block is damaged.  1 will disable any loss.")
        @RangeDouble(min = 0.5, max = 1.0)
        public float hardnessDamageFactor = 0.8F;
        
        @Comment("The blast resistance of a hyper-dimensional block up until it is fully damaged.")
        @RangeInt(min = 1, max = 18000000)
        public int intactBlastResistance = 6000000;
        
        @Comment("If canBeDamaged=true, the blast resistance of a hyper-dimensional block when it is fully damaged.")
        @RangeInt(min = 1, max = 18000000)
        public int failedBlastResistance = 30;
        
        @Comment("If canBeDamaged=true, the chance that an entity will damage a hyper-dimensional block when attempting to destroy it.")
        @RangeDouble(min = 0.0, max = 1.0)
        public float destroyedFailureChance = 0.25F;
        
        @Comment("If canBeDamaged=true, the chance that an explosion that overrides blast resistance will damage a hyper-dimensional block.")
        @RangeDouble(min = 0.0, max = 1.0)
        public float explodedFailureChance = 0.25F;
        
        @Comment("If canBeDamaged=true, the chance that a blast resistance test (usually an explosion) will damage a hyper-dimensional block.")
        @RangeDouble(min = 0.0, max = 1.0)
        public float resistanceFailureChance = 0.001F;
     }
}
