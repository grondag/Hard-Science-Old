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

public class SolarEtheneRecipe extends AbstractRecipe
{
    public static final String UID = HardScience.prefixName("solar_ethene");
   
    public final static Process PROCESS = new Process();

    protected SolarEtheneRecipe(AbstractCraftingProcess<?> process, Result result, int ticksDuration)
    {
        super(process, result, ticksDuration);
    }

    public static class Process extends AbstractSingleModelProcess<SolarEtheneRecipe>
    {
        protected Process()
        {
            super(
                    ImmutableList.of(
                            Matters.H2O.resource(),
                            Matters.FRESH_AIR.resource(),
                            PowerResource.JOULES),
                    ImmutableList.of(
                            Matters.ETHENE_GAS.resource(),
                            Matters.RETURN_AIR.resource(),
                            Matters.OXYGEN_GAS.resource()
                            ));
            
            model.createInput(Matters.H2O.resource(), Matters.H2O.nlPerMol());
            
            double co2Fraction = Compounds.FRESH_AIR.getFraction(Molecules.CO2);
            // this gives us 1Mol of CO2
            // no need to divide or multiply - ration of C to H is already 1:2
            double airInputNL = Matters.FRESH_AIR.nlPerMol() / co2Fraction;
            
            model.createInput(Matters.FRESH_AIR.resource(),
                    airInputNL);

            model.createInput(
                    PowerResource.JOULES, 
                    // need to flip sign because water formation is exothermic
                    - Molecules.H2O_FLUID.enthalpyJoules / MachinePower.PHOTO_CHEMICAL_EFFICIENCY
                    
                    // flip sign because CO2 formation is exothermic
                    // and charge extra for hypothetical CO2 concentration mechanism
                    - Molecules.CO2.enthalpyJoules / MachinePower.CARBON_CAPTURE_EFFICIENCY
                    
                    // ethylene enthalpy is positive, increasing cost of this reaction
                    + Molecules.ETHENE_GAS.enthalpyJoules);

            model.createOutput(
                    Matters.ETHENE_GAS.resource(),
                    // half a mol, because 4H vs 2H in water
                    Matters.ETHENE_GAS.nlPerMol() / 2);
            
            // will have half mole of O2 from one liter of water
            // plus one mole of O2 from the CO2 we used
            model.createOutput(
                    Matters.OXYGEN_GAS.resource(), 
                    Matters.OXYGEN_GAS.nlPerMol() * 1.5);
            
            model.createOutput(
                    Matters.RETURN_AIR.resource(), 
                    airInputNL * (1 - co2Fraction));
        }

        @Override
        protected SolarEtheneRecipe makeRecipe(AbstractSingleModelProcess<SolarEtheneRecipe> abstractSingleModelProcess, Result result,
                int ticksDuration)
        {
            return new SolarEtheneRecipe(this, result, ticksDuration);
        }
    }
    
    public static class Category extends AbstractRecipeCategory<SolarEtheneRecipe>
    {
        public Category(IGuiHelper guiHelper)
        {
            super(
                    guiHelper, 
                    PROCESS.maxSlots(),
                    UID,
                    new ResourceLocation("hard_science", "textures/blocks/two_dots.png"));
        }
    }
}
