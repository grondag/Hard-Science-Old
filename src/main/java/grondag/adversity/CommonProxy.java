package grondag.adversity;

import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;

//import grondag.adversity.event.CommonEventHandler;
//import grondag.adversity.event.OreGenEventHandler;
//import grondag.adversity.event.TerrainGenEventHandler;
//import grondag.adversity.feature.drylands.Drylands;
//import grondag.adversity.feature.unobtanium.Unobtanium;
import grondag.adversity.feature.volcano.Volcano;
//import grondag.adversity.world.AdversityWorldProvider;
//import grondag.adversity.world.AdversityWorldType;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.registry.LanguageRegistry;

public class CommonProxy {

	public void preInit(FMLPreInitializationEvent event) {

		Adversity.log = event.getModLog();
		Config.init(event.getSuggestedConfigurationFile());

//		Drylands.preInit(event);
		Volcano.preInit(event);
//		Unobtanium.preInit(event);

	}

	public void init(FMLInitializationEvent event) {
		// GameRegistry.registerWorldGenerator(new Drylands(), Integer.MAX_VALUE);
//		MinecraftForge.EVENT_BUS.register(new CommonEventHandler());
//		MinecraftForge.TERRAIN_GEN_BUS.register(new TerrainGenEventHandler());
//		MinecraftForge.ORE_GEN_BUS.register(new OreGenEventHandler());
		// Some events, especially tick, are handled on FML bus
		// FMLCommonHandler.instance().bus().register(new WildAnimalsFMLEventHandler());

//		Drylands.init(event);
		Volcano.init(event);
//		Unobtanium.init(event);

	}

	public void postInit(FMLPostInitializationEvent event) {
//		Adversity.adversityWorld = new AdversityWorldType("ADVERSITY");
//		LanguageRegistry.instance().addStringLocalization("generator.ADVERSITY", "en_US", "Adversity");
//
//		DimensionManager.unregisterDimension(0);
//		DimensionManager.unregisterProviderType(0);
//		DimensionManager.registerProviderType(0, AdversityWorldProvider.class, true);
//		DimensionManager.registerDimension(0, 0);
//
//		Drylands.postInit(event);
		Volcano.postInit(event);
//		Unobtanium.postInit(event);

	}

	public void serverLoad(FMLServerStartingEvent event) {

	}

}