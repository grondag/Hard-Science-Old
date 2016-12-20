package grondag.adversity.feature.volcano;

import grondag.adversity.Adversity;
import grondag.adversity.feature.volcano.lava.EntityLavaParticle;
import grondag.adversity.feature.volcano.lava.LavaBlobItem;
import grondag.adversity.feature.volcano.lava.RenderFluidParticle;
import grondag.adversity.library.fluid.FluidSimParticle;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.BiomeManager.BiomeType;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

public class Volcano {

    // BIOMES
    public static BiomeType		volcano;


    //public static BlockHotBasalt	blockHotBasalt;
    //	public static BlockVolcanicLava	blockVolcanicLava;
    //public static BlockHaze			blockHaze;
    //public static BlockHazeRising	blockHazeRising;
    //public static BlockAsh			blockAsh;

    // ITEMS
    //	public static Item				itemVolcanicLavaBucket;
    public static final Item basaltRubble = new Item().setRegistryName("basalt_rubble").setUnlocalizedName("basalt_rubble");
    public static final VolcanoWand  volcanoWand = new VolcanoWand();
    public static final TerrainWand  terrainWand = new TerrainWand();
    
    //	// FLUIDS
    //	public static Fluid				fluidVolcanicLava;

    // TILE ENTITIES
    public static BlockVolcano		blockVolcano;
    
    public static Item             itemBlockVolcano;
    
    public static Item      lavaBlob;
    
    public static void preInit(FMLPreInitializationEvent event) 
    {
        blockVolcano = new BlockVolcano();
        GameRegistry.register(blockVolcano);
        
        itemBlockVolcano = new ItemBlock(blockVolcano).setRegistryName(blockVolcano.getRegistryName());
        GameRegistry.register(itemBlockVolcano);

        lavaBlob = new LavaBlobItem().setRegistryName("lava_blob").setUnlocalizedName("lava_blob");;
        GameRegistry.register(lavaBlob);
        
      
        EntityRegistry.registerModEntity(EntityLavaParticle.class, "adversity:lava_blob", 1, Adversity.instance, 64, 10, true);
        
        
        GameRegistry.register(basaltRubble);
        GameRegistry.register(volcanoWand);
        GameRegistry.register(terrainWand);

        if(event.getSide() == Side.CLIENT)
        {
            ModelLoader.setCustomModelResourceLocation(itemBlockVolcano, 0, new ModelResourceLocation("adversity:block_volcano", "inventory"));
            ModelLoader.setCustomModelResourceLocation(basaltRubble, 0, new ModelResourceLocation("adversity:basalt_rubble", "inventory"));
            ModelLoader.setCustomModelResourceLocation(volcanoWand, 0, new ModelResourceLocation("adversity:volcano_wand", "inventory"));
            ModelLoader.setCustomModelResourceLocation(terrainWand, 0, new ModelResourceLocation("adversity:terrain_wand", "inventory")); 
            
            ModelLoader.setCustomModelResourceLocation(lavaBlob, 0, new ModelResourceLocation("minecraft:fire_charge", "inventory"));  
            
            RenderingRegistry.registerEntityRenderingHandler(EntityLavaParticle.class, RenderFluidParticle.factory());

        }

        // TILE ENTITIES
        GameRegistry.registerTileEntity(TileVolcano.class, "TileVolcano");

        //GameRegistry.registerBlock(Volcano.blockHaze = new BlockHaze(Material.air), "Haze");
        //GameRegistry.registerBlock(Volcano.blockHazeRising = new BlockHazeRising(Material.glass), "HazeRising");
        //GameRegistry.registerBlock(Volcano.blockAsh = new BlockAsh("Ash", Material.snow), "Ash");

        // FLUIDS
        //	    Volcano.fluidVolcanicLava = new Fluid("volcanic_lava", new ResourceLocation(Adversity.MODID, "blocks/volcanic_lava_still"), new ResourceLocation(Adversity.MODID, "blocks/volcanic_lava_flow"))
        //	            .setLuminosity(0).setDensity(3000).setViscosity(12000).setTemperature(2000);

        //		FluidRegistry.registerFluid(fluidVolcanicLava);
        //		blockVolcanicLava = (BlockVolcanicLava) new BlockVolcanicLava(Volcano.fluidVolcanicLava, Material.lava)
        //				.setRegistryName("volcanic_lava");
        //		GameRegistry.register(blockVolcanicLava);
        //        GameRegistry.register(new ItemBlock(blockVolcanicLava).setRegistryName(blockVolcanicLava.getRegistryName().getResourcePath()));
        //        FluidRegistry.addBucketForFluid(fluidVolcanicLava);
        //            final ModelResourceLocation volcanicLavaLocation = new ModelResourceLocation(Adversity.MODID.toLowerCase() + ":block/fluids", "volcanic_lava");

        //          Item volcanicLavaItem = Item.getItemFromBlock(blockVolcanicLava);
        // no need to pass the locations here, since they'll be loaded by the block model logic.
        //          ModelBakery.registerItemVariants(volcanicLavaItem);
        //            ModelLoader.setCustomMeshDefinition(volcanicLavaItem, new ItemMeshDefinition()
        //            {
        //                public ModelResourceLocation getModelLocation(ItemStack stack)
        //                {
        //                    return volcanicLavaLocation;
        //                }
        //            });

        //            ModelLoader.setCustomStateMapper(blockVolcanicLava, new StateMapperBase()
        //            {
        //                protected ModelResourceLocation getModelResourceLocation(IBlockState state)
        //                {
        //                    return volcanicLavaLocation;
        //                }
        //            });


        // ITEMS
        //		itemVolcanicLavaBucket = new ItemVolcanicLavaBucket(Volcano.blockVolcanicLava);
        //		GameRegistry.registerItem(itemVolcanicLavaBucket, "ItemVolcanicLavaBucket");
        //		FluidContainerRegistry.registerFluidContainer(Volcano.fluidVolcanicLava, new ItemStack(itemVolcanicLavaBucket),
        //				new ItemStack(Items.bucket));


    }

    public static void init(FMLInitializationEvent event) {
        //		Volcano.volcano = new BiomeVolcano(Config.BiomeIDs.volcano);
        //		BiomeManager.addBiome(BiomeType.DESERT, new BiomeEntry(Volcano.volcano, 0));
        //		BiomeDictionary.registerBiomeType(Volcano.volcano, BiomeDictionary.Type.HOT);

        // convert rubble to cobble
        GameRegistry.addRecipe(new ItemStack(NiceBlockRegistrar.BASALT_COBBLE_BLOCK), new Object[]{
                "CCC",
                "CCC",
                "CCC",
                'C', Volcano.basaltRubble
        });
        
        // convert cobble to rubble
        GameRegistry.addShapelessRecipe(new ItemStack(Volcano.basaltRubble, 9), 
                new Object[]{new ItemStack(NiceBlockRegistrar.BASALT_COBBLE_BLOCK, 1)}
        );
        
        // smelt cobble to smooth basalt
        GameRegistry.addSmelting(NiceBlockRegistrar.BASALT_COBBLE_BLOCK, new ItemStack(NiceBlockRegistrar.COOL_SQUARE_BASALT_ITEM, 1, 0), 0.1F);

    }

    public static void postInit(FMLPostInitializationEvent event) {

        // in vanilla worlds, plants shouldn't stop volcanic lava from spreading
        // TODO: probably need to add some more blocks here
        Blocks.FIRE.setFireInfo(Blocks.CACTUS, 5, 5);
        Blocks.FIRE.setFireInfo(Blocks.DEADBUSH, 30, 100);
    }

}
