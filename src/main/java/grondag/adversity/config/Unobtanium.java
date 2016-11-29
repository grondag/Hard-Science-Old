package grondag.adversity.config;

import net.minecraftforge.common.config.Configuration;

public class Unobtanium {
	public final boolean enabled;
	public final boolean logging;
	public final boolean allowMobSpawning;
	public final boolean allowFire;
	public final boolean allowHarvest;
	public final boolean allowSilkTouch;
	public final float selfRepairChance;
	public final boolean selfRepairFailedBlocks;
	public final boolean canBeDamaged;
	public final float startingHardness;
	public final float hardnessDamageFactor;
	public final int intactBlastResistance;
	public final int failedBlastResistance;
	public final float destroyedFailureChance;
	public final float explodedFailureChance;
	public final float resistanceFailureChance;
	
	public Unobtanium(Configuration config)
	{
	    config.addCustomCategoryComment("Unobtanium", "General settings for Unobtanium.");

        this.enabled = config.getBoolean("enabled", "Unobtanium", true, "Enables or disables Unobtanium blocks.");

        this.logging = config.getBoolean("logging", "Unobtanium", false,
                "If true, unobtanium damage will be logged to console - warning: can be very verbose!");

        this.allowMobSpawning = config.getBoolean("allowMobSpawning", "Unobtanium", false,
                "If false, mobs cannot spawn on Unobtanium blocks in darkness; similar to slabs.");

        this.allowFire = config.getBoolean("allowFire", "Unobtanium", false,
                "If false, normal fires directly above Unobtanium blocks are immediately extinguished.");

        this.allowHarvest = config.getBoolean("allowHarvest", "Unobtanium", false,
                "If false, players cannot harvest Unobtanium blocks - they can be broken but do not drop.");

        this.allowSilkTouch = config
                .getBoolean("allowSilkTouch", "Unobtanium", true,
                        "If true, Unobtanium blocks can be harvested intact with silk touch. Only matters if allowHarvest is true.");

        this.selfRepairChance = config.getFloat("selfRepairChance", "Unobtanium", 0.2F, 0.0F, 1.0F,
                "If > 0, Unobtanium blocks will slow repair themselves at random intervals.");

        this.selfRepairFailedBlocks = config
                .getBoolean("selfRepairFailedBlocks", "Unobtanium", false,
                        "If true, Unobtanium blocks that are fully failed can still self repair. (But only if selfRepairChance > 0.)");

        this.canBeDamaged = config.getBoolean("canBeDamaged", "Unobtanium", true,
                "If true, Unobtanium has a chance to lose durability due to damage from entities or explosions.");

        this.startingHardness = config.getFloat("startingHardness", "Unobtanium", 25.0F, 0.0F, 50.0F,
                "Hardnesss (mining speed) of undamaged this.");

        this.hardnessDamageFactor = config
                .getFloat("hardnessDamageFactor", "Unobtanium", 0.8F, 0.5F, 1.0F,
                        "If canBeDamaged=true, the remaining hardness each time Unobtanium is damaged.  1 will disable any loss.");

        this.intactBlastResistance = config.getInt("intactBlastResistance", "Unobtanium", 6000000, 1, 18000000,
                "The blast resistance of Unobtanium up until it is fully damaged.");

        this.failedBlastResistance = config.getInt("failedBlastResistance", "Unobtanium", 30, 1, 18000000,
                "If canBeDamaged=true, the blast resistance of Unobtanium when it is fully damaged.");

        this.destroyedFailureChance = config.getFloat("destroyedFailureChance", "Unobtanium", 0.25F, 0.0F, 1.0F,
                "If canBeDamaged=true, the chance that an entity will damage a block when attempting to destroy it.");

        this.explodedFailureChance = config
                .getFloat("explodedFailureChance", "Unobtanium", 0.25F, 0.0F, 1.0F,
                        "If canBeDamaged=true, the chance that an explosion that overrides blast resistance will damage an Unobtanium block.");

        this.resistanceFailureChance = config
                .getFloat("resistanceFailureChance", "Unobtanium", 0.001F, 0.0F, 1.0F,
                        "If canBeDamaged=true, the chance that a blast resistance test (usually an explosion) will damage an Unobtanium block.");

	}
}