package grondag.hard_science.external.jei;

import javax.annotation.Nonnull;

import grondag.hard_science.crafting.processing.DigesterRecipe;
import grondag.hard_science.crafting.processing.MicronizerRecipe;
import grondag.hard_science.init.ModBlocks;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.ItemStack;


@JEIPlugin
public class HardScienceJEIPlugIn implements IModPlugin
{

    private static IJeiRuntime runtime;
    public static IJeiRuntime runtime() { return runtime; }
    
    private static IModRegistry registry;
    public static IModRegistry registry() { return registry; }
    
    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime)
    {
        runtime = jeiRuntime;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry)
    {
        registry.addRecipeCategories(
                new MicronizerRecipe.Category(registry.getJeiHelpers().getGuiHelper()),
                new DigesterRecipe.Category(registry.getJeiHelpers().getGuiHelper())

//                new EmergencyFabricatorRecipe.Category(registry.getJeiHelpers().getGuiHelper()),
//                new SolarElectrolysisRecipe.Category(registry.getJeiHelpers().getGuiHelper()),
//                new SolarAmmoniaRecipe.Category(registry.getJeiHelpers().getGuiHelper()),
//                new SolarEtheneRecipe.Category(registry.getJeiHelpers().getGuiHelper())
                );
    }

    @Override
    public void register(@Nonnull IModRegistry registry)
    {
        HardScienceJEIPlugIn.registry = registry;
        
        registry.addRecipes(MicronizerRecipe.allForJEI(), MicronizerRecipe.JEI_UID);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.micronizer), MicronizerRecipe.JEI_UID);

        registry.addRecipes(DigesterRecipe.allForJEI(), DigesterRecipe.JEI_UID);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.digester), DigesterRecipe.JEI_UID);

        
//        registry.addRecipes(EmergencyFabricatorRecipe.allRecipes(), EmergencyFabricatorRecipe.UID);

//        registry.addIngredientInfo(Matters.MINERAL_FILLER.fluidResource().newStackWithLiters(Fluid.BUCKET_VOLUME), FluidStack.class, "fluid.flowable_mineral_filler.desc");
//
//        registry.addRecipes(
//                ImmutableList.of(SolarElectrolysisRecipe.PROCESS
//                        .configureFromInputs(ImmutableList.of(Matters.H2O_FLUID.fluidResource().withQuantity(VolumeUnits.LITER.nL)))), 
//                SolarElectrolysisRecipe.UID);
//        // fix this with actual block
//        registry.addRecipeCatalyst(new ItemStack(ModBlocks.solar_cell), SolarElectrolysisRecipe.UID);
//
//        registry.addRecipes(
//                ImmutableList.of(SolarAmmoniaRecipe.PROCESS
//                        .configureFromInputs(ImmutableList.of(Matters.H2O_FLUID.fluidResource().withQuantity(VolumeUnits.LITER.nL)))), 
//                SolarAmmoniaRecipe.UID);
//        registry.addRecipeCatalyst(new ItemStack(ModBlocks.solar_cell), SolarAmmoniaRecipe.UID);
//        
//        registry.addRecipes(
//                ImmutableList.of(SolarEtheneRecipe.PROCESS
//                        .configureFromInputs(ImmutableList.of(Matters.H2O_FLUID.fluidResource().withQuantity(VolumeUnits.LITER.nL)))), 
//                SolarEtheneRecipe.UID);
//        registry.addRecipeCatalyst(new ItemStack(ModBlocks.solar_cell), SolarEtheneRecipe.UID);
        
        //        registry.addRecipes(ModRecipes.builderRecipes, BuilderRecipeCategory.UID);
//        registry.addRecipes(ModRecipes.synthesizerRecipes, SynthesizerRecipeCategory.UID);

//        registry.addRecipeCatalyst(new ItemStack(ModBlocks.block_fabricator), BuilderRecipeCategory.UID);

//        registry.getRecipeTransferRegistry().addRecipeTransferHandler(ContainerCraftingHalo.class, VanillaRecipeCategoryUid.CRAFTING, 1, 9, 10, 36);
    }

//    @Override
//    public void registerIngredients(IModIngredientRegistration registry)
//    {
//        registry.register(
//                BulkResourceWithQuantity.class, 
//                
//                ModBulkResources.all().values().stream()
//                    .map(m -> m.defaultStack())
//                    .collect(Collectors.toList()),
//                    
//                new IIngredientHelper<BulkResourceWithQuantity>()
//                {
//                    @Override
//                    public List<BulkResourceWithQuantity> expandSubtypes(List<BulkResourceWithQuantity> ingredients)
//                    {
//                        return ingredients;
//                    }
//
//                    @Override
//                    public BulkResourceWithQuantity getMatch(Iterable<BulkResourceWithQuantity> ingredients, BulkResourceWithQuantity ingredientToMatch)
//                    {
//                        for(BulkResourceWithQuantity m : ingredients)
//                        {
//                            if(m.resource() == ingredientToMatch.resource()) return m;
//                        }
//                        return null;
//                    }
//
//                    @Override
//                    public String getDisplayName(BulkResourceWithQuantity ingredient)
//                    {
//                        return ingredient.toString();
//                    }
//
//                    @Override
//                    public String getUniqueId(BulkResourceWithQuantity ingredient)
//                    {
//                        return ingredient.systemName();
//                    }
//
//                    @Override
//                    public String getWildcardId(BulkResourceWithQuantity ingredient)
//                    {
//                        return ingredient.systemName();
//                    }
//
//                    @Override
//                    public String getModId(BulkResourceWithQuantity ingredient)
//                    {
//                        return HardScience.MODID;
//                    }
//
//                    @Override
//                    public Iterable<Color> getColors(BulkResourceWithQuantity ingredient)
//                    {
//                        return ImmutableList.of();
//                    }
//
//                    @Override
//                    public String getResourceId(BulkResourceWithQuantity ingredient)
//                    {
//                        return HardScience.prefixResource(ingredient.systemName());
//                    }
//
//                    @Override
//                    public BulkResourceWithQuantity copyIngredient(BulkResourceWithQuantity ingredient)
//                    {
//                        return (BulkResourceWithQuantity) ingredient.clone();
//                    }
//
//                    @Override
//                    public String getErrorInfo(BulkResourceWithQuantity ingredient)
//                    {
//                        return ingredient.toString();
//                    }
//                },
//                
//                new IIngredientRenderer<BulkResourceWithQuantity>()
//                {
//                    @Override
//                    public void render(Minecraft minecraft, int xCoord, int yCoord, BulkResourceWithQuantity ingredient)
//                    {
//                        GlStateManager.enableBlend();
//                        GlStateManager.enableAlpha();
//                        BulkResource matter = (BulkResource)ingredient.resource();
//                        
//                        float red = (matter.color >> 16 & 0xFF) / 255.0F;
//                        float green = (matter.color >> 8 & 0xFF) / 255.0F;
//                        float blue = (matter.color & 0xFF) / 255.0F;
//
//                        GlStateManager.color(red, green, blue, 1.0F);
//                        
//                        minecraft.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
//                        
//                        TextureAtlasSprite textureSprite;
//                        double uMin, uMax, vMin, vMax;
//                        
//                        switch(matter.phase())
//                        {
//                        case GAS:
//                            textureSprite = Textures.BIGTEX_CLOUDS.getSampleSprite();
//                            uMax = textureSprite.getInterpolatedU(1);
//                            vMax = textureSprite.getInterpolatedV(1);
//                            break;
//                            
//                        case LIQUID:
//                            textureSprite = Textures.BIGTEX_FLUID_VORTEX.getSampleSprite();
//                            uMax = textureSprite.getInterpolatedU(1);
//                            vMax = textureSprite.getInterpolatedV(1);
//                            break;
//                            
//                        case SOLID:
//                        default:
//                            textureSprite = Textures.BLOCK_NOISE_STRONG.getSampleSprite();
//                            uMax = textureSprite.getMaxU();
//                            vMax = textureSprite.getMaxV();
//
//                            break;
//                        
//                        }
//                        uMin = textureSprite.getMinU();
//                        vMin = textureSprite.getMinV();
//
//                        Tessellator tessellator = Tessellator.getInstance();
//                        BufferBuilder bufferBuilder = tessellator.getBuffer();
//                        bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
//                        bufferBuilder.pos(xCoord, yCoord + 16, 100).tex(uMin, vMax).endVertex();
//                        bufferBuilder.pos(xCoord + 16, yCoord + 16, 100).tex(uMax, vMax).endVertex();
//                        bufferBuilder.pos(xCoord + 16, yCoord, 100).tex(uMax, vMin).endVertex();
//                        bufferBuilder.pos(xCoord, yCoord, 100).tex(uMin, vMin).endVertex();
//                        tessellator.draw();
//                        
//                        GlStateManager.color(1, 1, 1, 1);
//
//                        GlStateManager.disableAlpha();
//                        GlStateManager.disableBlend();                        
//                    }
//
//                    @Override
//                    public List<String> getTooltip(Minecraft minecraft, BulkResourceWithQuantity ingredient, ITooltipFlag tooltipFlag)
//                    {
//                        return ImmutableList.of(ingredient.toString());
//                    }
//                    
//                });
//    }
    
    
}
