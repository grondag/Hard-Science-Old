package grondag.hard_science.crafting.synthesis;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.HardScience;
import grondag.hard_science.crafting.base.AbstractCraftingProcess;
import grondag.hard_science.crafting.base.GenericRecipe;
import grondag.hard_science.crafting.base.AbstractSingleModelProcess;
import grondag.hard_science.crafting.base.SingleParameterModel.Result;
import grondag.hard_science.external.jei.AbstractRecipeCategory;
import grondag.hard_science.init.ModBulkResources;
import grondag.hard_science.machines.energy.MachinePower;
import grondag.hard_science.matter.Compounds;
import grondag.hard_science.matter.Molecules;
import grondag.hard_science.simulator.resource.PowerResource;
import mezz.jei.api.IGuiHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public class SolarAmmoniaRecipe extends GenericRecipe
{
    public static final String UID = HardScience.prefixName("solar_ammonia");
   
    public final static Process PROCESS = new Process();

    protected SolarAmmoniaRecipe(AbstractCraftingProcess<?> process, Result result, int ticksDuration)
    {
        super(process, result, ticksDuration);
    }

    public SolarAmmoniaRecipe(NBTTagCompound tag)
    {
        super(tag);
    }
    
    public SolarAmmoniaRecipe(PacketBuffer pBuff)
    {
        super(pBuff);
    }
    
    public static class Process extends AbstractSingleModelProcess<SolarAmmoniaRecipe>
    {
        protected Process()
        {
            super(
                    ImmutableList.of(
                            ModBulkResources.H2O_FLUID.fluidResource(),
                            ModBulkResources.FRESH_AIR.fluidResource(),
                            PowerResource.JOULES),
                    ImmutableList.of(
                            ModBulkResources.AMMONIA_GAS.fluidResource(),
                            ModBulkResources.RETURN_AIR.fluidResource(),
                            ModBulkResources.O2_GAS.fluidResource()
                            ));
            
            model.createInput(ModBulkResources.H2O_FLUID.fluidResource(), ModBulkResources.H2O_FLUID.nlPerMol());
            
            double nitroFraction = Compounds.FRESH_AIR.getFraction(Molecules.N2_GAS);
            // divide by 2 because N2
            double airInputNL = ModBulkResources.FRESH_AIR.nlPerMol() / nitroFraction / 2;
            
            model.createInput(ModBulkResources.FRESH_AIR.fluidResource(),
                    airInputNL);

            model.createInput(
                    PowerResource.JOULES, 
                    // need to flip sign because water formation is exothermic
                    - Molecules.H2O_FLUID.enthalpyJoules / MachinePower.PHOTO_CHEMICAL_EFFICIENCY
                    
                    // ammonia enthalpy is negative, which reduces cost of this reaction
                    + Molecules.AMMONIA_GAS.enthalpyJoules);

            model.createOutput(
                    ModBulkResources.AMMONIA_GAS.fluidResource(),
                    // we'll get 2/3 yield per mol of water because
                    // 3 hydrogen instead of 2
                    ModBulkResources.AMMONIA_GAS.nlPerMol() * 2 / 3);
            
            model.createOutput(
                    ModBulkResources.O2_GAS.fluidResource(), 
                    ModBulkResources.O2_GAS.nlPerMol() / 2);
            
            model.createOutput(
                    ModBulkResources.RETURN_AIR.fluidResource(), 
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
