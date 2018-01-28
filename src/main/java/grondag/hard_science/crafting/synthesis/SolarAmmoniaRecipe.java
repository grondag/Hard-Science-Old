package grondag.hard_science.crafting.synthesis;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.HardScience;
import grondag.hard_science.crafting.base.AbstractCraftingProcess;
import grondag.hard_science.crafting.base.AbstractRecipe;
import grondag.hard_science.crafting.base.AbstractSingleModelProcess;
import grondag.hard_science.crafting.base.SingleParameterModel.Result;
import grondag.hard_science.external.jei.AbstractRecipeCategory;
import grondag.hard_science.machines.energy.MachinePower;
import grondag.hard_science.matter.Compounds;
import grondag.hard_science.matter.Matters;
import grondag.hard_science.matter.Molecules;
import grondag.hard_science.simulator.resource.PowerResource;
import mezz.jei.api.IGuiHelper;
import net.minecraft.util.ResourceLocation;

public class SolarAmmoniaRecipe extends AbstractRecipe
{
    public static final String UID = HardScience.prefixName("solar_ammonia");
   
    public final static Process PROCESS = new Process();

    protected SolarAmmoniaRecipe(AbstractCraftingProcess<?> process, Result result, int ticksDuration)
    {
        super(process, result, ticksDuration);
    }

    public static class Process extends AbstractSingleModelProcess<SolarAmmoniaRecipe>
    {
        protected Process()
        {
            super(
                    ImmutableList.of(
                            Matters.H2O.resource(),
                            Matters.FRESH_AIR.resource(),
                            PowerResource.JOULES),
                    ImmutableList.of(
                            Matters.AMMONIA.resource(),
                            Matters.RETURN_AIR.resource(),
                            Matters.OXYGEN_GAS.resource()
                            ));
            
            model.createInput(Matters.H2O.resource(), Matters.H2O.nlPerMol());
            
            double nitroFraction = Compounds.FRESH_AIR.getFraction(Molecules.N2);
            // divide by 2 because N2
            double airInputNL = Matters.FRESH_AIR.nlPerMol() / nitroFraction / 2;
            
            model.createInput(Matters.FRESH_AIR.resource(),
                    airInputNL);

            model.createInput(
                    PowerResource.JOULES, 
                    // need to flip sign because water formation is exothermic
                    - Molecules.H2O_FLUID.enthalpyJoules / MachinePower.PHOTO_CHEMICAL_EFFICIENCY
                    
                    // ammonia enthalpy is negative, which reduces cost of this reaction
                    + Molecules.AMMONIA_GAS.enthalpyJoules);

            model.createOutput(
                    Matters.AMMONIA.resource(),
                    // we'll get 2/3 yield per mol of water because
                    // 3 hydrogen instead of 2
                    Matters.AMMONIA.nlPerMol() * 2 / 3);
            
            model.createOutput(
                    Matters.OXYGEN_GAS.resource(), 
                    Matters.OXYGEN_GAS.nlPerMol() / 2);
            
            model.createOutput(
                    Matters.RETURN_AIR.resource(), 
                    airInputNL * (1 - nitroFraction));
        }

        @Override
        protected SolarAmmoniaRecipe makeRecipe(AbstractSingleModelProcess<SolarAmmoniaRecipe> abstractSingleModelProcess, Result result,
                int ticksDuration)
        {
            return new SolarAmmoniaRecipe(this, result, ticksDuration);
        }
    }
    
    public static class Category extends AbstractRecipeCategory<SolarAmmoniaRecipe>
    {
        public Category(IGuiHelper guiHelper)
        {
            super(
                    guiHelper, 
                    PROCESS.maxSlots(),
                    UID,
                    new ResourceLocation("hard_science", "textures/blocks/big_triangle.png"));
        }
    }
}
