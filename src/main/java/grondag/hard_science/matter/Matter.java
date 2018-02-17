package grondag.hard_science.matter;

import javax.annotation.Nullable;

import grondag.hard_science.simulator.resource.FluidResource;
import grondag.hard_science.simulator.transport.carrier.Channel;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

/**
 * Describes a substance used in material processing
 * that is handled/measured by volume. Will always
 * have an associated Fluid and Fluid Resource.<p>
 * 
 * Solid materials are generally powdered, granulated
 * or plastic such that they can be handled as a fluid.
 * Many in-game items representing non-flowable pieces
 * of solid matter will need to be converted somehow
 * into the fluid resource before they can be used.
 *
 */
public class Matter
{
    private final String systemName;
    public final int color;
    public final String label;
    private final IComposition composition;
    private final double tempK;
    private final double pressureP;
    private final MatterPhase phase;
    
    /**
     * g/cm3, water ~1.0
     */
    private final double density;
    
    private Fluid fluid;
    private FluidResource resource;
    private int channel = Channel.INVALID_CHANNEL;

    public Matter(
            String systemName,
            int color,
            String label,
            IComposition composition,
            double tempCelsius,
            double pressureAtm,
            MatterPhase phase,
            double density
            )
    {
        this.systemName = systemName;
        this.color = color;
        this.label = label;
        this.composition = composition;
        this.tempK = Temperature.celsiusToKelvin(tempCelsius);
        this.pressureP = Gas.atmToPascals(pressureAtm);
        this.phase = phase;
        this.density = density;
    }
    
    /**
     * Use this form for gasses that can
     * have density estimated as an ideal gas.
     */
    public Matter(
            String systemName,
            int color,
            String label,
            IComposition molecule,
            double tempCelsius,
            double pressureAtm
            )
    {
        this(systemName, color, label, molecule, tempCelsius, pressureAtm, MatterPhase.GAS, 
               Gas.idealGasDensityCA(molecule, tempCelsius, pressureAtm));
    }
    
    public String systemName()
    {
        return this.systemName;
    }
    
    public String displayName()
    {
        return I18n.translateToLocal("matter." + this.systemName).trim();
    }
    
    /**
     * Null if not a fluid or gas.
     */
    @Nullable
    public Fluid fluid()
    {
        return this.fluid;
    }

    /**
     * Null if not a fluid or gas.
     */
    @Nullable
    public FluidResource fluidResource()
    {
        return this.resource;
    }
    
    /**
     * Only valid if this is a fluid.
     */
    public int channel()
    {
        return this.channel;
    }
    
    public MatterPhase phase()
    {
        return this.phase;
    }
    
    public IComposition composition()
    {
        return this.composition;
    }
    
    public double temperatureK()
    {
        return this.tempK;
    }
    
    public double temperatureC()
    {
        return Temperature.kelvinToCelsius(this.tempK);
    }
    
    public double pressureAtm()
    {
        return Gas.pascalsToAtm(this.pressureP);
    }
    
    public double pressurePascals()
    {
        return this.pressureP;
    }
    
    /**
     * g/cm3, water ~1.0
     */
    public double density()
    {
        return this.density;
    }
    
    public double gPerMol()
    {
        return this.composition.weight();
    }
    
    public double molsPerLiter()
    {
        return this.density / this.gPerMol() * 1000;
        // g / cm3 / (g / mol)
        // mol / cm3
    }
    
    public double molsPerKL()
    {
        return this.molsPerLiter() * 1000;
    }
    
    public double litersPerMol()
    {
        return this.gPerMol() / this.density * 1000;
    }
    
    public double nlPerMol()
    {
        return this.gPerMol() / this.density * VolumeUnits.MILLILITER.nL;
    }
    
    public double kLPerMol()
    {
        return this.litersPerMol() / 1000;
    }
    
    public MatterStack defaultStack()
    {
        return this.phase == MatterPhase.SOLID
               ? new MatterStack(this, MassUnits.KILOGRAM.ng)
               : new MatterStack(this, VolumeUnits.LITER.nL);
    }
    public void register()
    {
        if(this.phase != MatterPhase.SOLID)
        {
            this.fluid = FluidRegistry.getFluid(systemName);
            if(this.fluid == null)
            {
                this.fluid = new Fluid(this.systemName, this.phase.iconResource, this.phase.iconResource, this.color);
                if(this.phase == MatterPhase.GAS) this.fluid.setGaseous(true);
                this.fluid.setDensity((int) this.density());
                this.fluid.setTemperature((int) this.temperatureK());
                FluidRegistry.registerFluid(this.fluid);
            }
            this.channel = Channel.channelForFluid(this.fluid);
            this.resource = new FluidResource(this.fluid, null);
        }
    }

    public long kgPerBlock()
    {
        return (long) (this.density * 1000);
    }
}
