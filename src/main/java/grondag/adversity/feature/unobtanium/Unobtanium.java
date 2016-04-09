// TODO: Fix or remove

//package com.grondag.adversity.feature.unobtanium;
//
//import net.minecraft.block.material.Material;
//import net.minecraft.creativetab.CreativeTabs;
//import net.minecraft.item.Item;
//
//import com.grondag.adversity.Adversity;
//
//import cpw.mods.fml.common.event.FMLInitializationEvent;
//import cpw.mods.fml.common.event.FMLPostInitializationEvent;
//import cpw.mods.fml.common.event.FMLPreInitializationEvent;
//import cpw.mods.fml.common.registry.GameRegistry;
//
//public class Unobtanium {
//
//	// BLOCKS
//	public static BlockUnobtanium	blockUnobtanium;
//
//	// ITEMS
//	public static Item				itemUnobtaniumRubble;
//
//	public static void preInit(FMLPreInitializationEvent event) {
//
//		// BLOCKS
//		GameRegistry.registerBlock(Unobtanium.blockUnobtanium = new BlockUnobtanium("Unobtanium", Material.iron),
//				ItemBlockUnobtanium.class, "Unobtanium");
//
//		// ITEMS
//		itemUnobtaniumRubble = new Item().setUnlocalizedName("UnobtaniumRubble").setCreativeTab(CreativeTabs.tabMisc)
//				.setTextureName(Adversity.MODID + ":unob_rubble");
//		;
//		GameRegistry.registerItem(itemUnobtaniumRubble, "UnobtaniumRubble");
//
//	}
//
//	public static void init(FMLInitializationEvent event) {
//
//	}
//
//	public static void postInit(FMLPostInitializationEvent event) {
//
//	}
//
//}
