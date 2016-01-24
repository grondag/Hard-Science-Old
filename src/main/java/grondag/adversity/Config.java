package grondag.adversity;

import java.io.File;
import java.util.HashMap;

import net.minecraftforge.common.config.Configuration;

public class Config {

	public static class BiomeIDs {
		public static int drylandHills = 51;
		public static int drylandMesa = 52;
		public static int drylandFlats = 53;
		public static int volcano = 54;
	}

	public static class Unobtanium {
		public static boolean enabled = true;
		public static boolean logging = false;
		public static boolean allowMobSpawning = false;
		public static boolean allowFire = false;
		public static boolean allowHarvest = true;
		public static boolean allowSilkTouch = true;
		public static float selfRepairChance = 0.01F;
		public static boolean selfRepairFailedBlocks = false;
		public static boolean canBeDamaged = true;
		public static float startingHardness = 25.0F;
		public static float hardnessDamageFactor = 0.8F;
		public static int intactBlastResistance = 6000000;
		public static int failedBlastResistance = 30;
		public static float destroyedFailureChance = 0.25F;
		public static float explodedFailureChance = 0.25F;
		public static float resistanceFailureChance = 0.001F;
	}

	public static class Substance {
		public int hardness;
		public int resistance;
		public String harvestTool;
		public int harvestLevel;
	}

	public static HashMap<String, Substance> substances = new HashMap<String, Substance>();

	public static Configuration config;

	public static void init(File file) {
		config = new Configuration(file);
		Config.load();
	}

	public static void load() {
		// START
		config.load();

		// BIOMES
		config.addCustomCategoryComment("BiomeIDs", "Change these to prevent Biome ID conflicts.");

		BiomeIDs.drylandHills = config.getInt("drylandHills", "BiomeIDs", 51, 1, 255, "");
		BiomeIDs.drylandMesa = config.getInt("drylandMesa", "BiomeIDs", 52, 1, 255, "");
		BiomeIDs.drylandFlats = config.getInt("drylandFlats", "BiomeIDs", 53, 1, 255, "");
		BiomeIDs.volcano = config.getInt("volcano", "BiomeIDs", 54, 1, 255, "");

		// UNOBTANIUM
		config.addCustomCategoryComment("Unobtanium", "General settings for Unobtanium.");

		Unobtanium.enabled = config.getBoolean("enabled", "Unobtanium", true, "Enables or disables Unobtanium blocks.");

		Unobtanium.logging = config.getBoolean("logging", "Unobtanium", false,
				"If true, unobtanium damage will be logged to console - warning: can be very verbose!");

		Unobtanium.allowMobSpawning = config.getBoolean("allowMobSpawning", "Unobtanium", false,
				"If false, mobs cannot spawn on Unobtanium blocks in darkness; similar to slabs.");

		Unobtanium.allowFire = config.getBoolean("allowFire", "Unobtanium", false,
				"If false, normal fires directly above Unobtanium blocks are immediately extinguished.");

		Unobtanium.allowHarvest = config.getBoolean("allowHarvest", "Unobtanium", false,
				"If false, players cannot harvest Unobtanium blocks - they can be broken but do not drop.");

		Unobtanium.allowSilkTouch = config
				.getBoolean("allowSilkTouch", "Unobtanium", true,
						"If true, Unobtanium blocks can be harvested intact with silk touch. Only matters if allowHarvest is true.");

		Unobtanium.selfRepairChance = config.getFloat("selfRepairChance", "Unobtanium", 0.2F, 0.0F, 1.0F,
				"If > 0, Unobtanium blocks will slow repair themselves at random intervals.");

		Unobtanium.selfRepairFailedBlocks = config
				.getBoolean("selfRepairFailedBlocks", "Unobtanium", false,
						"If true, Unobtanium blocks that are fully failed can still self repair. (But only if selfRepairChance > 0.)");

		Unobtanium.canBeDamaged = config.getBoolean("canBeDamaged", "Unobtanium", true,
				"If true, Unobtanium has a chance to lose durability due to damage from entities or explosions.");

		Unobtanium.startingHardness = config.getFloat("startingHardness", "Unobtanium", 25.0F, 0.0F, 50.0F,
				"Hardnesss (mining speed) of undamaged Unobtanium.");

		Unobtanium.hardnessDamageFactor = config
				.getFloat("hardnessDamageFactor", "Unobtanium", 0.8F, 0.5F, 1.0F,
						"If canBeDamaged=true, the remaining hardness each time Unobtanium is damaged.  1 will disable any loss.");

		Unobtanium.intactBlastResistance = config.getInt("intactBlastResistance", "Unobtanium", 6000000, 1, 18000000,
				"The blast resistance of Unobtanium up until it is fully damaged.");

		Unobtanium.failedBlastResistance = config.getInt("failedBlastResistance", "Unobtanium", 30, 1, 18000000,
				"If canBeDamaged=true, the blast resistance of Unobtanium when it is fully damaged.");

		Unobtanium.destroyedFailureChance = config.getFloat("destroyedFailureChance", "Unobtanium", 0.25F, 0.0F, 1.0F,
				"If canBeDamaged=true, the chance that an entity will damage a block when attempting to destroy it.");

		Unobtanium.explodedFailureChance = config
				.getFloat("explodedFailureChance", "Unobtanium", 0.25F, 0.0F, 1.0F,
						"If canBeDamaged=true, the chance that an explosion that overrides blast resistance will damage an Unobtanium block.");

		Unobtanium.resistanceFailureChance = config
				.getFloat("resistanceFailureChance", "Unobtanium", 0.001F, 0.0F, 1.0F,
						"If canBeDamaged=true, the chance that a blast resistance test (usually an explosion) will damage an Unobtanium block.");

		// SUBSTANCES

		// UNOBTANIUM

		String[] allowedTools = { "pickaxe", "shove", "axe" };

		config.addCustomCategoryComment("Substances", "General settings for various in-game materials.");

		substances.clear();

		Substance extrudedStone = new Substance();
		extrudedStone.hardness = config.getInt("dessedStoneHardness", "Substances", 2, 1, 50, "");
		extrudedStone.harvestTool = config.getString("extrudedStoneHarvestTool", "Substances", "pickaxe", "Tool used to break block", allowedTools);
		extrudedStone.harvestLevel = config.getInt("dessedStoneHarvestLevel", "Substances", 1, 1, 3, "");
		extrudedStone.resistance = config.getInt("extrudedStoneResistance", "Substances", 10, 1, 50, "");
		substances.put("extruded_stone", extrudedStone);

		Substance composite = new Substance();
		composite.hardness = config.getInt("compositeHardness", "Substances", 4, 1, 50, "");
		composite.harvestTool = config.getString("compositeHarvestTool", "Substances", "pickaxe", "Tool used to break block", allowedTools);
		composite.harvestLevel = config.getInt("compositeHarvestLevel", "Substances", 2, 1, 3, "");
		composite.resistance = config.getInt("compositeResistance", "Substances", 50, 1, 50, "");
		substances.put("composite", composite);

		Substance duraplast = new Substance();
		duraplast.hardness = config.getInt("duraplastHardness", "Substances", 10, 1, 50, "");
		duraplast.harvestTool = config.getString("duraplastHarvestTool", "Substances", "pickaxe", "Tool used to break block", allowedTools);
		duraplast.harvestLevel = config.getInt("duraplastHarvestLevel", "Substances", 3, 1, 3, "");
		duraplast.resistance = config.getInt("duraplastResistance", "Substances", 200, 1, 50, "");
		substances.put("duraplast", duraplast);

		// END
		config.save();

	}

}
