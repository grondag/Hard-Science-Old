package grondag.hard_science;

import java.util.IdentityHashMap;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.LangKey;
import net.minecraftforge.common.config.Config.RangeDouble;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.Config.RequiresMcRestart;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

@LangKey("config.general")
@Config(modid = HardScience.MODID, type = Type.INSTANCE)
public class Configurator
{

    @Comment("Enable tracing for machine jobs and processing. Highly verbose. Intended for dev environment and troubleshooeting.")
    public static boolean logMachineActivity = false;

    @Comment("Enable tracing for transport network activity. Highly verbose. Intended for dev environment and troubleshooeting.")
    public static boolean logTransportNetwork = false;

    @Comment("Enable tracing for excavation render tracking. Highly verbose. Intended for dev environment and troubleshooeting.")
    public static boolean logExcavationRenderTracking = false;

    @Comment("Enable tracing for structural device & device block changes. Highly verbose. Intended for dev environment and troubleshooeting.")
    public static boolean logDeviceChanges = false;


    public static void recalcDerived()
    {
        Volcano.recalcDerived();
        Machines.recalcDerived();
    }

    ////////////////////////////////////////////////////        
    // SUBSTANCES
    ////////////////////////////////////////////////////
    @LangKey("config.substance")
    @Comment("Hard Science material properties.")
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

        public Substance hdpe = new Substance(2, "axe", 1, 10, 1.0);

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
    // BLOCKS
    ////////////////////////////////////////////////////
    @LangKey("config.blocks")
    @Comment("Settings for blocks.")
    public static BlockSettings BLOCKS = new BlockSettings();

    public static class BlockSettings
    {
        @Comment("Allow user selection of hidden textures in SuperModel Block GUI. Generally only useful for testing.")
        public boolean showHiddenTextures = false;

        @Comment("Controls how much detail should be shown if The One Probe is enabled.")
        public ProbeInfoLevel probeInfoLevel = ProbeInfoLevel.BASIC;

        public static enum ProbeInfoLevel
        {
            BASIC,
            EXTRA,
            DEBUG
        }

        @Comment("Set true to enable tracing output for block model state.  Can spam the log quite a bit, so leave false unless having problems.")
        public boolean debugModelState = true;

        @Comment({"Maximum number of block states checked before placing virtual blocks.",
            " Try smaller values if placing large multi-block regions is causing FPS problems.",
            " With smaller values, species (connected textures) may not be selected properly ",
        " for large multi-block placements."})
        @RangeInt(min = 16, max = 4096)
        public int maxPlacementCheckCount = 512;
    }

    ////////////////////////////////////////////////////        
    // VOLCANO
    ////////////////////////////////////////////////////
    @LangKey("config.volcano")
    @Comment("Settings for Volcano feature.")
    public static Volcano VOLCANO = new Volcano();

    public static class Volcano
    {
        @Comment("Volcano feature is still WIP and disabled by default.")
        public boolean enableVolcano = false;

        @Comment("Fraction of alloted CPU usage must be drop below this before volcano in cooldown mode starts to flow again.")
        @RangeDouble(min = 0.3, max = 0.7)
        public float cooldownTargetLoadFactor = 0.5F;

        @Comment("After volcano in cooldown reaches the target load factor, it waits this many ticks before flowing again.")
        @RangeInt(min = 0, max = 6000)
        public int cooldownWaitTicks = 200;

        @Comment("Y-orthogonalAxis build limit at which Volcano becomes permanently dormant.")
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

        @Comment({"Maximum number of flying/falling volcalnic lava entities that may be in the world simultaneously.",
        "Higher numbers may provide more responsive flowing and better realism but can create lag."})
        @RangeInt(min = 10, max = 200)
        public int maxLavaEntities = 20;

        @Comment({"Number of ticks needed for lava or basalt to cool from one stage to another.",
        "Should be larger than minDormantTicks"})
        @RangeInt(min = 200, max = 200000)
        public int basaltCoolingTicks = 200;

        @Comment({"Block updates are buffered for at least this many ticks before applied to world.",
        "Higher numbers can be better for performance but may cause block updates to appear strangely."})
        @RangeInt(min = 0, max = 20)
        public int minBlockUpdateBufferTicks = 3;

        @Comment({"Block updates are considered high priority after this many ticks.",
        "Higher numbers can be better for performance but may cause block updates to appear strangely."})
        @RangeInt(min = 10, max = 40)
        public int maxBlockUpdateBufferTicks = 20;

        @Comment({"Maximum number of chunk updates applied to world each tick.",
            "The actual number of chunk render updates can be higher due to effects on neighboring chunks.",
        "Higher numbers provide more realism but can negatively affect performance."})
        @RangeInt(min = 1, max = 10)
        public int maxChunkUpdatesPerTick = 2;

        @Comment({"Blocks that will be destroyed on contact by volcanic lava.",
            "Blocks should be listed in modname:blockname format.",
        "At this time, metadata and NBT values cannot be specified."})
        public String[] blocksDestroyedByVolcanicLava = {
                "minecraft:sponge", 
                "minecraft:stone_pressure_plate",
                "minecraft:ice",
                "minecraft:snow",
                "minecraft:cactus",
                "minecraft:pumpkin",
                "minecraft:lit_pumpkin",
                "minecraft:cake",
                "minecraft:stained_glass",
                "minecraft:glass_pane",
                "minecraft:melon_block",
                "minecraft:redstone_lamp",
                "minecraft:lit_redstone_lamp",
                "minecraft:light_weighted_pressure_plate",
                "minecraft:heavy_weighted_pressure_plate",
                "minecraft:stained_glass_pane",
                "minecraft:slime",
                "minecraft:hay_block",
                "minecraft:coal_block",
                "minecraft:packed_ice",
        "minecraft:frosted_ice"};

        @Comment({"Blocks that will stop the flow of volcanic lava.",
            "Blocks should be listed in modname:blockname format.",
        "At this time, metadata and NBT values cannot be specified."})
        public String[] blocksSafeFromVolcanicLava = {"minecraft:end_gateway", "minecraft:portal", "minecraft:end_portal"};

        @RequiresMcRestart
        @Comment({"If true, volcano simulation will periodically output performance statistics to log.",
            "Does cause minor additional overhead and log spam so should generally only be enabled for testing.",
        "Turning this off does NOT disable the minimal performance counting needed to detect simulation overload."})
        public boolean enablePerformanceLogging = false;

        @RequiresMcRestart
        @Comment({"Number of seconds in each volcano fluid simulation performance sample.",
            "Larger numbers reduce log spam when performance logging is enabled.",
        "Smaller numbers (recommended) make fluid simulation performance throttling more responsive."})
        @RangeInt(min = 10, max = 120)
        public int performanceSampleInterval = 20;

        @RequiresMcRestart
        @Comment({"Percentage of each server tick (1/20 of a second) that can be devoted to volcano fluid simulation.",
            "This is the single-threaded part of the simulation that interacts with the game world.",
            "Larger numbers will enable larger lava flows but casue simulation to compete with other in-game objects that tick.",
        "If you are seeing log spam that the server can't keep up, reduce this mumber or disable volcanos."})
        @RangeInt(min = 2, max = 30)
        public int onTickProcessingBudget = 5;

        @RequiresMcRestart
        @Comment({"Percentage of elapsed time that can be devoted to volcano fluid simulation overall.",
            "This includes both single-threaded on-tick time and multi-threaded processing that occurs between server ticks.",
            "Larger numbers will enable larger lava flows but will consume more CPU used for other tasks on the machine where it runs.",
        "If you are seeing lag or log spam that the server can't keep up, reduce this mumber or disable volcanos."})
        @RangeInt(min = 5, max = 60)
        public int totalProcessingBudget = 10;

        @RequiresMcRestart
        @Comment({"If true, volcano simulation will track and output the amount of fluid that flows across cell connections.",
            "Can cause additional overhead and log spam so should generally only be enabled for testing.",
        "Turning this off does NOT disable the minimal performance counting needed to detect simulation overload."})
        public boolean enableFlowTracking = false;

        @Comment({"If true, volcano simulation will output cell debug information each performance interval.",
        "Will cause significant log spam so should only be enabled for testing."})
        public boolean outputLavaCellDebugSummaries = false;


        /** Contains block objects configured to be destroyed by lava */
        public static final IdentityHashMap<Block, Block> blocksDestroyedByLava = new IdentityHashMap<Block, Block>();

        /** Contains block objects configured to be unharmed by lava */
        public static final IdentityHashMap<Block, Block> blocksSafeFromLava = new IdentityHashMap<Block, Block>();

        /** Number of milliseconds in a volcano fluid simulation performance sample */
        public static int performanceSampleIntervalMillis;

        /** Number of nanoseconds in a volcano fluid simulation performance sample */
        public static long peformanceSampleIntervalNanos;

        /** Number of nanoseconds budgeted each interval for on-tick processing */
        public static long performanceBudgetOnTickNanos;

        /** Number of nanoseconds budgeted each interval for off-tick processing */
        public static long performanceBudgetTotalNanos;

        private static void recalcDerived()
        {
            performanceSampleIntervalMillis = VOLCANO.performanceSampleInterval * 1000;
            peformanceSampleIntervalNanos = (long)performanceSampleIntervalMillis * 1000000;
            performanceBudgetOnTickNanos = peformanceSampleIntervalNanos * VOLCANO.onTickProcessingBudget / 100;
            performanceBudgetTotalNanos = peformanceSampleIntervalNanos * VOLCANO.totalProcessingBudget / 100;

            if(VOLCANO.maxDormantTicks <= VOLCANO.minDormantTicks)
            {
                VOLCANO.maxDormantTicks = VOLCANO.minDormantTicks + 20;
            }

            IForgeRegistry<Block> reg = GameRegistry.findRegistry(Block.class);

            blocksDestroyedByLava.clear();
            for(String s : VOLCANO.blocksDestroyedByVolcanicLava)
            {
                ResourceLocation rl = new ResourceLocation(s);
                if(reg.containsKey(rl))
                {
                    Block b = reg.getValue(rl);
                    blocksDestroyedByLava.put(b, b);
                }
                else
                {
                    Log.warn("Did not find block " + s + " configured to be destroyed by volcanic lava. Confirm block name is correct." );
                }
            }

            blocksSafeFromLava.clear();
            for(String s : VOLCANO.blocksSafeFromVolcanicLava)
            {
                ResourceLocation rl = new ResourceLocation(s);
                if(reg.containsKey(rl))
                {
                    Block b = reg.getValue(rl);
                    blocksSafeFromLava.put(b, b);
                }
                else
                {
                    Log.warn("Did not find block " + s + " configured to be safe from volcanic lava. Confirm block name is correct." );
                }
            }
        }
    }

    ////////////////////////////////////////////////////        
    // HYPERSTONE
    ////////////////////////////////////////////////////
    @LangKey("config.hypermaterial")
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

    ////////////////////////////////////////////////////        
    // MACHINES
    ////////////////////////////////////////////////////
    @LangKey("config.machines")
    @Comment("Settings for machines.")
    public static Machines MACHINES = new Machines();

    public static class Machines
    {
        @Comment({"Machines display four-character random names. ",
        "What could possibly go wrong?"})
        public boolean filterOffensiveMachineNames = true;

        @Comment({"Radius for basic builder to find & build virtual blocks - in chunks.",
        "0 means can only build in chunk where machine is located."})
        @RequiresMcRestart
        @RangeInt(min = 0, max = 8)
        public int basicBuilderChunkRadius = 4;

        @Comment({"Number of milliseconds server waits between sending machine updates to clients.",
            "Lower values will provide more responsive machine status feedback at the cost of more network traffic",
        "Some specialized machines may not honor this value consistently."})
        @RangeInt(min = 0, max = 5000)
        public int machineUpdateIntervalMilliseconds = 200;

        @Comment({"Number of milliseconds between keepalive packets sent from client to server to notifiy ",
            "server that machine is being rendered and needs status information for external display.",
            "Values must match on both client and server for machine updates to work reliably!",
        "Not recommended to change this unless you are trying to address a specific problem."})
        @RangeInt(min = 1000, max = 30000)
        public int machineKeepaliveIntervalMilliseconds = 5000;

        @Comment({"Number of milliseconds grace period gives before timing out listeners when no keepalive packet is received.",
        "Lower values will sligntly reduce network traffice but are not recommended if any clients have high latency" })
        @RangeInt(min = 100, max = 2000)
        public int machineLatencyAllowanceMilliseconds = 1000;

        @Comment({"Track and display exponential average change in machine material & power buffers.",
        "Disabling may slightly improve client performance. Has no effect on server."})
        @RequiresMcRestart
        public boolean enableDeltaTracking = true;

        @Comment({"You have to be this close to machines for external displays to render.",
            "Visibility starts at this distance and then becomes full at 4 blocks less.",
        "Lower values may improve performance in worlds with many machines."})
        @RangeInt(min = 5, max = 16)
        public int machineMaxRenderDistance = 8;

        @RequiresMcRestart
        @Comment({"If true, machine simulation will periodically output performance statistics to log.",
            "Does cause minor additional overhead and log spam so should generally only be enabled for testing.",
        "Turning this off does NOT disable the minimal performance counting needed to detect simulation overload."})
        public boolean enablePerformanceLogging = false;

        public static int machineKeepAlivePlusLatency;

        private static void recalcDerived()
        {
            machineKeepAlivePlusLatency = MACHINES.machineKeepaliveIntervalMilliseconds + MACHINES.machineLatencyAllowanceMilliseconds;
        }
    }

    ////////////////////////////////////////////////////
    // PROCESSING
    ////////////////////////////////////////////////////
    @LangKey("config.processing")
    @Comment("Settings for automatic resource processing.")
    public static Processing PROCESSING = new Processing();

    public static class Processing
    {

        @Comment({"Defines chemical composition of micronizer outputs/digester inputs",
            "Comma separated parameters are... ",
            "    item ingredient - ore dictionary accepted, metadata optional",
            "    fluid name - defines the output", 
            "    liters output - floating point value, 1 block = 1000L",
            "    energy consumption factor - floating point value",
            "Energy consumption factor and output volume determine energy usage.",
            "Harder materials should have a higher energy consumption factor.",
            "Smooth stone is suggested as the reference value at 1.0."})
        @RequiresMcRestart
        public String[] micronizerOutputs =
        {
            "# Stone is modeled after approx earth crust composition.",
            "# Ends up being pretty close to a feldspar mineral, because trace components are small",
            "stone, 2.8, 0x646973",
            "Si, 0.282000000",
            "O, 0.461000000",
            "Fe, 0.056300000",
            "Al, 0.082300000",
            "Ca, 0.041500000",
            "K, 0.020900000",
            "Mg, 0.023300000",
            "Na, 0.023600000",
            "Ti, 0.005650000",
            "Mn, 0.000950000",
            "P, 0.001050000",
            "Zr, 0.000165000",
            "S, 0.000350000",
            "F, 0.000585000",
            "Nd, 0.000041500",
            "Cr, 0.000102000",
            "Cl, 0.000145000",
            "Ni, 0.000084000",
            "Zn, 0.000070000",
            "Cu, 0.000060000",
            "Pb, 0.000014000",
            "C, 0.000200000",
            "Co, 0.000025000",
            "H, 0.001400000",
            "Sn, 0.000002300",
            "N, 0.000019000",
            "W, 0.000001300",
            "Li, 0.000020000",
            "Mo, 0.000001200",
            "B, 0.000010000",
            "Ag, 0.000000075",
            "Au, 0.000000004",
            "Se, 0.000000050",
            "Pt, 0.000000005",
            "",
            "basalt, 3.7, 0x648090",
            "SiO2, 0.4",
            "Fe3O4, 0.6"
        };
        
        @Comment({"Recipe configuration for micronizer.",
            "Comma separated parameters are... ",
            "    item ingredient - ore dictionary accepted, metadata optional",
            "    fluid name - defines the output", 
            "    liters output - floating point value, 1 block = 1000L",
            "    energy consumption factor - floating point value",
            "Energy consumption factor and output volume determine energy usage.",
            "Harder materials should have a higher energy consumption factor.",
            "Smooth stone is suggested as the reference value at 1.0."})
        @RequiresMcRestart
        public String[] micronizerRecipes =
        {
            "ore:sand, stone, 1000.0, 0.5",
            "ore:gravel, stone, 1000.0, 0.65",
            "ore:sandstone, stone, 1000.0, 0.7",
            "ore:cobblestone, stone, 1000.0, 0.8",
            "ore:stone, stone, 1000.0, 1.0",
            "minecraft:cobblestone_wall, stone, 1000.0, 0.8",
            "minecraft:stone_slab:0, stone, 500.0, 0.8",
            "minecraft:stone_slab:1, stone, 500.0, 0.77",
            "minecraft:stone_slab:3, stone, 500.0, 1.0",
            "minecraft:stone_slab:5, stone, 500.0, 1.0",
            "minecraft:stone_slab2:0, stone, 500.0, 0.7",
            "hard_science:basalt_cobble, basalt, 1000.0, 1.0",
            "hard_science:basalt_cut, basalt, 1000.0, 1.2",
            "hard_science:basalt_rubble, basalt, 111.11111, 1.0"
        };

//        @Comment({"Recipe configuration for digester.",
//            "Each row lists a bulk resource (which are fluids in game)",
//            "Other inputs, energy usage and outputs are automatically derived",
//            "from the chemical composition of the input resource.",
//            "Air, water and electricityare used to generate nitric acid within the device.",
//            "Some of this is output in the form of nitrates.",
//            "All other reactants/catalysts are recovered/regenerated within the digester."})
//        @RequiresMcRestart
//        public String[] digesterInputs =
//        {
//            "micronized_stone",
//            "micronized_basalt"
//        };
        
        @Comment({"Ouput digester analysis debug information to log. Intended for testing."})
        public boolean enableDigesterAnalysisDebug = true;
        
        @Comment({"Output warning message if digester analysis violates physical constraints. "})
        public boolean enableDigesterRecipeWarnings = true;
        
        @Comment({"Default reserve and target stocking levels",
            "for new domains. Has no effect after a domain is created.",
            "Listed resources represent inputs or outputs of automatic production.",
            "When resource level drops below target, production starts.",
            "If resource drops below reserve, it is no longer used as an input",
            "for automated resource processing, but can still be used for player request.",
            "Fluid resources are given in liters (equivalent to millibucket).",
            "Order is fluid name, reserveStockLevel, targetStockLevel"})
        public String[] fluidResourceDefaults = 
        {
            "micronized_basalt, 16000, 64000",
            "micronized_stone, 16000, 64000",
            "co2_gas, 16000, 64000",
            "h2_gas, 16000, 64000",
            "ethene_gas, 16000, 64000",
            "return_air, 16000, 64000",
            "flex_resin, 16000, 64000",
            "h2o_vapor, 16000, 64000",
            "graphite, 16000, 64000",
            "flex_alloy, 16000, 64000",
            "lithium, 16000, 64000",
            "platinum, 16000, 64000",
            "tin, 16000, 64000",
            "dye_cyan, 16000, 64000",
            "raw_mineral_dust, 16000, 64000",
            "zinc, 16000, 64000",
            "potassium_nitrate, 16000, 64000",
            "magnesium_nitrate, 16000, 64000",
            "phosphorus, 16000, 64000",
            "molybdenum, 16000, 64000",
            "tungsten_powder, 16000, 64000",
            "calcium_nitrate, 16000, 64000",
            "dye_yellow, 16000, 64000",
            "ammonia_liquid, 16000, 64000",
            "magnesium, 16000, 64000",
            "water, 16000, 64000",
            "sodium, 16000, 64000",
            "ar_gas, 16000, 64000",
            "mineral_filler, 16000, 64000",
            "fresh_air, 16000, 64000",
            "silver, 16000, 64000",
            "n2_gas, 16000, 64000",
            "ethanol_liquid, 16000, 64000",
            "silicon_nitride, 16000, 64000",
            "hdpe, 16000, 64000",
            "ammonia_gas, 16000, 64000",
            "potassium, 16000, 64000",
            "silica, 16000, 64000",
            "titanium, 16000, 64000",
            "sodium_chloride, 16000, 64000",
            "cobalt, 16000, 64000",
            "aluminum, 16000, 64000",
            "manganese, 16000, 64000",
            "dura_resin, 16000, 64000",
            "calcium_fluoride, 16000, 64000",
            "gold, 16000, 64000",
            "copper, 16000, 64000",
            "dye_magenta, 16000, 64000",
            "nickel, 16000, 64000",
            "magnetite, 16000, 64000",
            "chromium, 16000, 64000",
            "calcium, 16000, 64000",
            "perovskite, 16000, 64000",
            "methane_gas, 16000, 64000",
            "calcium_carbonate, 16000, 64000",
            "sulfer, 16000, 64000",
            "flex_fiber, 16000, 64000",
            "o2_gas, 16000, 64000",
            "super_fuel, 16000, 64000",
            "sodium_nitrate, 16000, 64000",
            "carbon_vapor, 16000, 64000",
            "lead, 16000, 64000",
            "diamond, 16000, 64000",
            "silicon, 16000, 64000",
            "neodymium, 16000, 64000",
            "h2o_fluid, 16000, 64000",
            "iron, 16000, 64000",
            "boron, 16000, 64000",
            "monocalcium_phosphate, 16000, 64000",
            "lithium_nitrate, 16000, 64000"
        };

        @Comment({"Default reserve and target stocking levels",
            "for new domains. Has no effect after a domain is created.",
            "Listed resources represent inputs or outputs of automatic production.",
            "When resource level drops below target, production starts.",
            "If resource drops below reserve, it is no longer used as an input",
            "for automated resource processing, but can still be used for player request.",
            "Target = 0 means resource should not be retained if there is demand.",
            "If an item satisfies multiple ingredient, the highest reserve/target value applies.",
            "Item resources are given in plain counts. (1 = 1 item)",
            "Order is item name, reserveStockLevel, targetStockLevel.",
            "Use ore prefix for oredictionary ingredients."})
        public String[] itemResourceDefaults = 
        {
            "ore:sand, 64, 512",
            "ore:gravel, 64, 256",
            "ore:sandstone, 64, 256",
            "ore:cobblestone, 64, 512",
            "ore:stone, 64, 256",
            "minecraft:cobblestone_wall, 64, 64",
            "minecraft:stone_slab:0, 64, 64",
            "minecraft:stone_slab:1, 64, 64",
            "minecraft:stone_slab:3, 64, 64",
            "minecraft:stone_slab:5, 64, 64",
            "minecraft:stone_slab2:0, 64, 64",
            "hard_science:basalt_cobble, 64, 256",
            "hard_science:basalt_cut, 64, 256",
            "hard_science:basalt_rubble, 0, 0",
            "hard_science:basalt_cool_static_height, 0, 0",
            "hard_science:basalt_cool_static_filler, 0, 0"
        };

    }
}
