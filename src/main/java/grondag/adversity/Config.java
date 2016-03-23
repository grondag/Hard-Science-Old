package grondag.adversity;

import java.io.File;
import java.util.HashMap;

import net.minecraftforge.common.config.Configuration;

public class Config {

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

		Substance flexstone = new Substance();
		flexstone.hardness = config.getInt("flexstoneHardness", "Substances", 2, 1, 50, "");
		flexstone.harvestTool = config.getString("flexstoneHarvestTool", "Substances", "pickaxe", "Tool used to break block", allowedTools);
		flexstone.harvestLevel = config.getInt("flexstoneHarvestLevel", "Substances", 1, 1, 3, "");
		flexstone.resistance = config.getInt("flexstoneResistance", "Substances", 10, 1, 50, "");
		substances.put("flexstone", flexstone);

		Substance durastone = new Substance();
		durastone.hardness = config.getInt("durastoneHardness", "Substances", 4, 1, 50, "");
		durastone.harvestTool = config.getString("durastoneHarvestTool", "Substances", "pickaxe", "Tool used to break block", allowedTools);
		durastone.harvestLevel = config.getInt("durastoneHarvestLevel", "Substances", 2, 1, 3, "");
		durastone.resistance = config.getInt("durastoneResistance", "Substances", 50, 1, 50, "");
		substances.put("durastone", durastone);

		Substance hyperstone = new Substance();
		hyperstone.hardness = config.getInt("hyperstoneHardness", "Substances", 10, 1, 50, "");
		hyperstone.harvestTool = config.getString("hyperstoneHarvestTool", "Substances", "pickaxe", "Tool used to break block", allowedTools);
		hyperstone.harvestLevel = config.getInt("hyperstoneHarvestLevel", "Substances", 3, 1, 3, "");
		hyperstone.resistance = config.getInt("hyperstoneResistance", "Substances", 200, 1, 50, "");
		substances.put("hyperstone", hyperstone);

        Substance superwood = new Substance();
        hyperstone.hardness = config.getInt("superwoodHardness", "Substances", 2, 1, 50, "");
        hyperstone.harvestTool = config.getString("superwoodHarvestTool", "Substances", "axe", "Tool used to break block", allowedTools);
        hyperstone.harvestLevel = config.getInt("superwoodHarvestLevel", "Substances", 1, 1, 3, "");
        hyperstone.resistance = config.getInt("superwoodResistance", "Substances", 10, 1, 50, "");
        substances.put("superwood", superwood);

        // END
		config.save();

	}

}
