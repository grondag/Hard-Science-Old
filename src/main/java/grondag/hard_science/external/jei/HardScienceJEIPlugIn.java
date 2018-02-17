package grondag.hard_science.external.jei;

import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.HardScience;
import grondag.hard_science.crafting.BulkConversionRecipe;
import grondag.hard_science.crafting.fabrication.EmergencyFabricatorRecipe;
import grondag.hard_science.crafting.synthesis.SolarAmmoniaRecipe;
import grondag.hard_science.crafting.synthesis.SolarElectrolysisRecipe;
import grondag.hard_science.crafting.synthesis.SolarEtheneRecipe;
import grondag.hard_science.matter.MatterStack;
import grondag.hard_science.matter.Matters;
import grondag.hard_science.matter.VolumeUnits;
import grondag.hard_science.superblock.texture.Textures;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;


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
                new EmergencyFabricatorRecipe.Category(registry.getJeiHelpers().getGuiHelper()),
                new BulkConversionRecipe.Category(registry.getJeiHelpers().getGuiHelper()),
                new SolarElectrolysisRecipe.Category(registry.getJeiHelpers().getGuiHelper()),
                new SolarAmmoniaRecipe.Category(registry.getJeiHelpers().getGuiHelper()),
                new SolarEtheneRecipe.Category(registry.getJeiHelpers().getGuiHelper())
                );
    }

    @Override
    public void register(@Nonnull IModRegistry registry)
    {
        HardScienceJEIPlugIn.registry = registry;
        
//        registry.addRecipes(EmergencyFabricatorRecipe.allRecipes(), EmergencyFabricatorRecipe.UID);
//
//        registry.addRecipes(BulkConversionRecipe.allRecipes(), BulkConversionRecipe.UID);
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

    @Override
    public void registerIngredients(IModIngredientRegistration registry)
    {
        registry.register(
                MatterStack.class, 
                
                Matters.all().values().stream()
                    .map(m -> new MatterStack(m, VolumeUnits.LITER.nL))
                    .collect(Collectors.toList()),
                    
                new IIngredientHelper<MatterStack>()
                {
                    @Override
                    public List<MatterStack> expandSubtypes(List<MatterStack> ingredients)
                    {
                        return ingredients;
                    }

                    @Override
                    public MatterStack getMatch(Iterable<MatterStack> ingredients, MatterStack ingredientToMatch)
                    {
                        for(MatterStack m : ingredients)
                        {
                            if(m.matter == ingredientToMatch.matter) return m;
                        }
                        return null;
                    }

                    @Override
                    public String getDisplayName(MatterStack ingredient)
                    {
                        return ingredient.displayName();
                    }

                    @Override
                    public String getUniqueId(MatterStack ingredient)
                    {
                        return ingredient.systemName();
                    }

                    @Override
                    public String getWildcardId(MatterStack ingredient)
                    {
                        return ingredient.systemName();
                    }

                    @Override
                    public String getModId(MatterStack ingredient)
                    {
                        return HardScience.MODID;
                    }

                    @Override
                    public Iterable<Color> getColors(MatterStack ingredient)
                    {
                        return ImmutableList.of();
                    }

                    @Override
                    public String getResourceId(MatterStack ingredient)
                    {
                        return HardScience.prefixResource(ingredient.systemName());
                    }

                    @Override
                    public MatterStack copyIngredient(MatterStack ingredient)
                    {
                        // matter stacks are immutable
                        return ingredient;
                    }

                    @Override
                    public String getErrorInfo(MatterStack ingredient)
                    {
                        return ingredient.displayName();
                    }
                },
                
                new IIngredientRenderer<MatterStack>()
                {
                    @Override
                    public void render(Minecraft minecraft, int xCoord, int yCoord, MatterStack ingredient)
                    {
                        GlStateManager.enableBlend();
                        GlStateManager.enableAlpha();

                        float red = (ingredient.matter.color >> 16 & 0xFF) / 255.0F;
                        float green = (ingredient.matter.color >> 8 & 0xFF) / 255.0F;
                        float blue = (ingredient.matter.color & 0xFF) / 255.0F;

                        GlStateManager.color(red, green, blue, 1.0F);
                        
                        minecraft.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                        
                        TextureAtlasSprite textureSprite;
                        double uMin, uMax, vMin, vMax;
                        
                        switch(ingredient.matter.phase())
                        {
                        case GAS:
                            textureSprite = Textures.BIGTEX_CLOUDS.getSampleSprite();
                            uMax = textureSprite.getInterpolatedU(1);
                            vMax = textureSprite.getInterpolatedV(1);
                            break;
                            
                        case LIQUID:
                            textureSprite = Textures.BIGTEX_FLUID_VORTEX.getSampleSprite();
                            uMax = textureSprite.getInterpolatedU(1);
                            vMax = textureSprite.getInterpolatedV(1);
                            break;
                            
                        case SOLID:
                        default:
                            textureSprite = Textures.BLOCK_NOISE_STRONG.getSampleSprite();
                            uMax = textureSprite.getMaxU();
                            vMax = textureSprite.getMaxV();

                            break;
                        
                        }
                        uMin = textureSprite.getMinU();
                        vMin = textureSprite.getMinV();

                        Tessellator tessellator = Tessellator.getInstance();
                        BufferBuilder bufferBuilder = tessellator.getBuffer();
                        bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
                        bufferBuilder.pos(xCoord, yCoord + 16, 100).tex(uMin, vMax).endVertex();
                        bufferBuilder.pos(xCoord + 16, yCoord + 16, 100).tex(uMax, vMax).endVertex();
                        bufferBuilder.pos(xCoord + 16, yCoord, 100).tex(uMax, vMin).endVertex();
                        bufferBuilder.pos(xCoord, yCoord, 100).tex(uMin, vMin).endVertex();
                        tessellator.draw();
                        
                        GlStateManager.color(1, 1, 1, 1);

                        GlStateManager.disableAlpha();
                        GlStateManager.disableBlend();                        
                    }

                    @Override
                    public List<String> getTooltip(Minecraft minecraft, MatterStack ingredient, ITooltipFlag tooltipFlag)
                    {
                        return ImmutableList.of(ingredient.displayName());
                    }
                    
                });
    }
    
    
}
