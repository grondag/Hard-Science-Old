package grondag.hard_science.crafting.synthesis;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.HardScience;
import grondag.hard_science.crafting.base.AbstractCraftingProcess;
import grondag.hard_science.crafting.base.AbstractRecipe;
import grondag.hard_science.crafting.base.AbstractSingleModelProcess;
import grondag.hard_science.crafting.base.SingleParameterModel.Result;
import grondag.hard_science.external.jei.AbstractRecipeCategory;
import grondag.hard_science.machines.energy.MachinePower;
import grondag.hard_science.matter.Matters;
import grondag.hard_science.matter.Molecules;
import grondag.hard_science.simulator.resource.PowerResource;
import mezz.jei.api.IGuiHelper;
import net.minecraft.util.ResourceLocation;

public class SolarElectrolysisRecipe extends AbstractRecipe
{
    public static final String UID = HardScience.prefixName("solar_electrolysis");
   
    public final static Process PROCESS = new Process();

    protected SolarElectrolysisRecipe(AbstractCraftingProcess<?> process, Result result, int ticksDuration)
    {
        super(process, result, ticksDuration);
    }

    public static class Process extends AbstractSingleModelProcess<SolarElectrolysisRecipe>
    {
        
        protected Process()
        {
            super(
                    ImmutableList.of(
                            Matters.H2O_FLUID.fluidResource(),
                            PowerResource.JOULES),
                    ImmutableList.of(
                            Matters.H2_GAS.fluidResource(),
                            Matters.O2_GAS.fluidResource()
                            ));
            
            model.createInput(Matters.H2O_FLUID.fluidResource(), Matters.H2O_FLUID.nlPerMol());
            model.createInput(
                    PowerResource.JOULES, 
                    // need to flip sign because water formation is exothermic
                    -Molecules.H2O_FLUID.enthalpyJoules / MachinePower.PHOTO_CHEMICAL_EFFICIENCY);

            model.createOutput(
                    Matters.H2_GAS.fluidResource(), 
                    Matters.H2_GAS.nlPerMol());
            model.createOutput(
                    Matters.O2_GAS.fluidResource(), 
                    Matters.O2_GAS.nlPerMol() / 2);
        }

        @Override
        protected SolarElectrolysisRecipe makeRecipe(AbstractSingleModelProcess<SolarElectrolysisRecipe> abstractSingleModelProcess, Result result,
                int ticksDuration)
        {
            return new SolarElectrolysisRecipe(this, result, ticksDuration);
        }
    }
    
    public static class Category extends AbstractRecipeCategory<SolarElectrolysisRecipe>
    {
        public Category(IGuiHelper guiHelper)
        {
            super(
                    guiHelper, 
                    PROCESS.maxSlots(),
                    UID,
                    new ResourceLocation("hard_science", "textures/blocks/star_16.png"));
        }
    }
}
