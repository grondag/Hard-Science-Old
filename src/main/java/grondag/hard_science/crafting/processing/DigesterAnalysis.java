package grondag.hard_science.crafting.processing;

import static grondag.hard_science.matter.Molecules.CALCIUM_SILICATE;
import static grondag.hard_science.matter.Molecules.CHROMIUM_OXIDE;
import static grondag.hard_science.matter.Molecules.COBALT_OXIDE;
import static grondag.hard_science.matter.Molecules.CUPRIC_OXIDE;
import static grondag.hard_science.matter.Molecules.GOLD;
import static grondag.hard_science.matter.Molecules.H2O_FLUID;
import static grondag.hard_science.matter.Molecules.HEMATITE;
import static grondag.hard_science.matter.Molecules.LEAD_OXIDE;
import static grondag.hard_science.matter.Molecules.LITHIUM_METASILICATE;
import static grondag.hard_science.matter.Molecules.MAGNESIUM_SILICATE;
import static grondag.hard_science.matter.Molecules.MANGANESE_DIOXIDE;
import static grondag.hard_science.matter.Molecules.MOLYBDENUM_TRIOXIDE;
import static grondag.hard_science.matter.Molecules.N2_GAS;
import static grondag.hard_science.matter.Molecules.NEODYMIUM_OXIDE;
import static grondag.hard_science.matter.Molecules.NICKEL_OXIDE;
import static grondag.hard_science.matter.Molecules.O2_GAS;
import static grondag.hard_science.matter.Molecules.PLATINUM;
import static grondag.hard_science.matter.Molecules.POTASSIUM_METASILICATE;
import static grondag.hard_science.matter.Molecules.SELENIUM;
import static grondag.hard_science.matter.Molecules.SILVER;
import static grondag.hard_science.matter.Molecules.SODIUM_METASILICATE;
import static grondag.hard_science.matter.Molecules.TIN_DIOXIDE;
import static grondag.hard_science.matter.Molecules.TITANIUM_DIOXIDE;
import static grondag.hard_science.matter.Molecules.TUNGSTEN_TRIOXIDE;
import static grondag.hard_science.matter.Molecules.ZINC_OXIDE;
import static grondag.hard_science.matter.Molecules.ZIRCONIUM_DIOXIDE;

import java.util.HashMap;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import grondag.hard_science.Configurator;
import grondag.hard_science.HardScience;
import grondag.hard_science.init.ModBulkResources;
import grondag.hard_science.matter.Compound;
import grondag.hard_science.matter.Element;
import grondag.hard_science.matter.Molecule;
import grondag.hard_science.simulator.resource.BulkResource;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMaps;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.minecraftforge.fluids.Fluid;

public class DigesterAnalysis
{
    private static final HashMap<BulkResource, DigesterAnalysis> cache = new HashMap<>();
    
    public static DigesterAnalysis get(BulkResource input)
    {
        DigesterAnalysis result = cache.get(input);
        if(result == null)
        {
            result = new DigesterAnalysis(input);
            cache.put(input, result);
        }
        return result;
    }
    
    /**
     * Max number of output rows for JEI recipe display.
     */
    private static int maxOutputRows = 1;
    
    public static int maxOuputRowsJEI() { return maxOutputRows; }
    
    /**
     * Elements that can be included in residual outputs
     */
    private static final ImmutableMap<Element, Molecule> sludgePermits 
        = ImmutableMap.<Element, Molecule>builder()
            .put(Element.Fe, HEMATITE)
            .put(Element.Ti, TITANIUM_DIOXIDE)
            .put(Element.Mn, MANGANESE_DIOXIDE)
            .put(Element.Zr, ZIRCONIUM_DIOXIDE)
            .put(Element.Nd, NEODYMIUM_OXIDE)
            .put(Element.Cr, CHROMIUM_OXIDE)
            .put(Element.Ni, NICKEL_OXIDE)
            .put(Element.Zn, ZINC_OXIDE)
            .put(Element.Cu, CUPRIC_OXIDE)
            .put(Element.Pb, LEAD_OXIDE)
            .put(Element.Co, COBALT_OXIDE)
            .put(Element.Sn, TIN_DIOXIDE)
            .put(Element.W, TUNGSTEN_TRIOXIDE)
            .put(Element.Mo, MOLYBDENUM_TRIOXIDE)
            .put(Element.Ag, SILVER)
            .put(Element.Au, GOLD)
            .put(Element.Se, SELENIUM)
            .put(Element.Pt, PLATINUM)
            .build();
    
    /**
     * Other elements that can be digested
     */
    private static final ImmutableSet<Element> digested 
        = ImmutableSet.<Element>builder()
            .add(Element.H)
            .add(Element.O)
            .add(Element.N)
            .add(Element.B)
            .add(Element.F)
            .add(Element.Cl)
            .add(Element.S)
            .add(Element.C)
            .add(Element.Na)
            .add(Element.Ca)
            .add(Element.K)
            .add(Element.Mg)
            .add(Element.Li)
            .add(Element.Si)
            .add(Element.Al)
            .build();
    
    public final Compound residueCompound;
    private final BulkResource input;
    private final double residueMols;
    private final double waterInputMols;
    private final double oxygenInputMols;
    private final double nitrogenInputMols;
    
    /**
     * Holds map from input mols to output moles
     * after init, until needed to create output factors.
     * After that is set to null;
     */
    private ImmutableMap<BulkResource, Double> outputs;
    
    /**
     * Maps input liters to output liters.
     * Lazy instantiation because must happen after fluid registry.
     */
    private Object2DoubleMap<Fluid> outputFactors;
    
    @SuppressWarnings("null")
    private DigesterAnalysis(BulkResource input)
    {
        this.input = input;
        
        
        final boolean debug = Configurator.PROCESSING.enableDigesterAnalysisDebug;
        
        final Object2DoubleOpenHashMap<Element> reconIn = debug ? new Object2DoubleOpenHashMap<>() : null;
        
        double netOxygen = input.composition().countOf(Element.O);
        double netNitrogen = input.composition().countOf(Element.N);
        double netSilicon = input.composition().countOf(Element.Si);
        
        double nitrateUsage = 0;

        double totalResidueMols = 0;
        double netWater = 0;
        
        if(debug) HardScience.INSTANCE.info("BEGINNING DIGESTER ANALYSIS FOR " + input.displayName().toUpperCase());
        
        Compound.Builder residueBuilder = Compound.builder();
        
        for(Element e : input.composition().elements())
        {
            double count = input.composition().countOf(e);
            
            if(debug)
            {
                HardScience.INSTANCE.info("Input includes %f mols of %s", count, e.symbol);
                reconIn.put(e, count);
            }

            if(sludgePermits.containsKey(e))
            {
                Molecule residue = sludgePermits.get(e);
                double residueMols = count / residue.countOf(e);
                totalResidueMols += residueMols;
                residueBuilder.add(residue, residueMols);
                netOxygen -= residue.countOf(Element.O) * residueMols;
                
                if(debug) HardScience.INSTANCE.info("Residue includes %f mols of %s as %f mols of %s",
                        count, e.symbol, residueMols, residue.formula);
            }
            else
            {
                if(!digested.contains(e))
                {
                    if(debug || Configurator.PROCESSING.enableDigesterRecipeWarnings)
                        HardScience.INSTANCE.warn("Unsupported element " + e.symbol + "found in digester input. Content will be discarded.");
                }
            }
        }
        this.residueCompound = residueBuilder.build();
        this.residueMols = totalResidueMols;
        
        ImmutableMap.Builder<BulkResource, Double> outputBuilder = ImmutableMap.builder();
        
        {
            // calcium expected to be more common than fluorine
            double calcium = input.composition().countOf(Element.Ca);
            double calciumFluoride = input.composition().countOf(Element.F) / 2;
            
            if(calciumFluoride > calcium)
            {
                if(debug || Configurator.PROCESSING.enableDigesterRecipeWarnings)
                    HardScience.INSTANCE.warn("Digester recipe for " + input.displayName() + " has excess fluorine in composition.  Some fluorine will be lost. Add calcium to correct.");
                calciumFluoride = calcium;
            }
            
            if(calciumFluoride > 0)
            {
                outputBuilder.put(ModBulkResources.CALCIUM_FLUORIDE, calciumFluoride);
                calcium -= calciumFluoride;
            }
            
            
            if(calcium > 0)
            {
                // to reduce bulk prefer silicate if silicon available
                if(netSilicon > 0)
                {
                    double silicate = Math.min(
                            calcium / CALCIUM_SILICATE.countOf(Element.Ca),
                            netSilicon / CALCIUM_SILICATE.countOf(Element.Si));
                    if(silicate > 0)
                    {
                        outputBuilder.put(ModBulkResources.CALCIUM_SILICATE, silicate);
                        calcium -= silicate * CALCIUM_SILICATE.countOf(Element.Ca);
                        netSilicon -= silicate * CALCIUM_SILICATE.countOf(Element.Si);
                        netOxygen -= silicate * CALCIUM_SILICATE.countOf(Element.O);
                    }
                }
                
                if(calcium > 0)
                {
                    outputBuilder.put(ModBulkResources.CALCIUM_NITRATE, calcium);
                    nitrateUsage += calcium * 2;
                }
            }
        }
        
        {
            // sodium expected to be more common than chlorine
            double sodium = input.composition().countOf(Element.Na);
            double sodiumChloride = input.composition().countOf(Element.Cl);
            
            if(sodiumChloride > sodium)
            {
                if(debug || Configurator.PROCESSING.enableDigesterRecipeWarnings)
                    HardScience.INSTANCE.warn("Digester recipe for " + input.displayName() + " has excess chlorine in composition.  Some chlorine will be lost. Add sodium to correct.");
                sodiumChloride = sodium;
            }
            
            if(sodiumChloride > 0)
            {
                outputBuilder.put(ModBulkResources.SODIUM_CHLORIDE, sodiumChloride);
                sodium -= sodiumChloride;
            }
            
            if(sodium > 0)
            {
                // to reduce bulk prefer silicate if silicon available
                if(netSilicon > 0)
                {
                    double silicate = Math.min(
                            sodium / SODIUM_METASILICATE.countOf(Element.Na),
                            netSilicon / SODIUM_METASILICATE.countOf(Element.Si));
                    if(silicate > 0)
                    {
                        outputBuilder.put(ModBulkResources.SODIUM_METASILICATE, silicate);
                        sodium -= silicate * SODIUM_METASILICATE.countOf(Element.Na);
                        netSilicon -= silicate * SODIUM_METASILICATE.countOf(Element.Si);
                        netOxygen -= silicate * SODIUM_METASILICATE.countOf(Element.O);
                    }
                }
                
                if(sodium > 0)
                {
                    outputBuilder.put(ModBulkResources.SODIUM_NITRATE, sodium);
                    nitrateUsage += sodium;
                }
            }
        }
        
        // To reduce bulk, prefer silicates for reminaing alkali/ne elements
        // Output nitrates if not enouch silicon remaining.
        {
            double potassium = input.composition().countOf(Element.K);
            if(potassium > 0)
            {
                if(netSilicon > 0)
                {
                    double silicate = Math.min(
                            potassium / POTASSIUM_METASILICATE.countOf(Element.K),
                            netSilicon / POTASSIUM_METASILICATE.countOf(Element.Si));
                    if(silicate > 0)
                    {
                        outputBuilder.put(ModBulkResources.POTASSIUM_METASILICATE, silicate);
                        potassium -= silicate * POTASSIUM_METASILICATE.countOf(Element.K);
                        netSilicon -= silicate * POTASSIUM_METASILICATE.countOf(Element.Si);
                        netOxygen -= silicate * POTASSIUM_METASILICATE.countOf(Element.O);
                    }
                }
                
                if(potassium > 0)
                {
                    outputBuilder.put(ModBulkResources.POTASSIUM_NITRATE, potassium);
                    nitrateUsage += potassium;
                }
            }
        }
        
        {
            double magnesium = input.composition().countOf(Element.Mg);
            if(magnesium > 0)
            {
                if(netSilicon > 0)
                {
                    double silicate = Math.min(
                            magnesium / MAGNESIUM_SILICATE.countOf(Element.Mg),
                            netSilicon / MAGNESIUM_SILICATE.countOf(Element.Si));
                    if(silicate > 0)
                    {
                        outputBuilder.put(ModBulkResources.MAGNESIUM_SILICATE, silicate);
                        magnesium -= silicate * MAGNESIUM_SILICATE.countOf(Element.Mg);
                        netSilicon -= silicate * MAGNESIUM_SILICATE.countOf(Element.Si);
                        netOxygen -= silicate * MAGNESIUM_SILICATE.countOf(Element.O);
                    }
                }
                
                if(magnesium > 0)
                {
                    outputBuilder.put(ModBulkResources.MAGNESIUM_NITRATE, magnesium);
                    nitrateUsage += magnesium * 2;
                }
            }
        }
        
        {
            double lithium = input.composition().countOf(Element.Li);
            if(lithium > 0)
            {
                if(netSilicon > 0)
                {
                    double silicate = Math.min(
                            lithium / LITHIUM_METASILICATE.countOf(Element.Li),
                            netSilicon / LITHIUM_METASILICATE.countOf(Element.Si));
                    if(silicate > 0)
                    {
                        outputBuilder.put(ModBulkResources.LITHIUM_METASILICATE, silicate);
                        lithium -= silicate * LITHIUM_METASILICATE.countOf(Element.Li);
                        netSilicon -= silicate * LITHIUM_METASILICATE.countOf(Element.Si);
                        netOxygen -= silicate * LITHIUM_METASILICATE.countOf(Element.O);
                    }
                }

                if(lithium > 0)
                {
                    outputBuilder.put(ModBulkResources.LITHIUM_NITRATE, lithium);
                    nitrateUsage += lithium;
                }
            }
        }

        // if any silicon remaining, output as silica
        {
            if(netSilicon > 0)
            {
                outputBuilder.put(ModBulkResources.SILICA, netSilicon);
                netOxygen -= netSilicon * 2;
            }
        }
        
        {
            double waterOut = input.composition().countOf(Element.H) / 2;
            if(waterOut > 0)
            {
                netWater += waterOut;
                netOxygen -= waterOut;
            }
        }
        
        {
            double boron = input.composition().countOf(Element.B);
            if(boron > 0)
            {
                outputBuilder.put(ModBulkResources.BORIC_ACID, boron);
                netWater -= boron * 1.5;
                netOxygen -= boron * 1.5;
            }
        }
        
        {
            double phosphorus = input.composition().countOf(Element.P);
            if(phosphorus > 0)
            {
                outputBuilder.put(ModBulkResources.PHOSPHORIC_ACID, phosphorus);
                netWater -= phosphorus * 1.5;
                netOxygen -= phosphorus * 2.5;
            }
        }
        
        if(input.composition().countOf(Element.S) > 0)
        {
            outputBuilder.put(ModBulkResources.SULFUR, input.composition().countOf(Element.S));
        }

        if(input.composition().countOf(Element.C) > 0)
        {
            outputBuilder.put(ModBulkResources.GRAPHITE, input.composition().countOf(Element.C));
        }

        {
            double aluminum = input.composition().countOf(Element.Al);
            if(aluminum > 0)
            {
                outputBuilder.put(ModBulkResources.ALUMINA, aluminum / 2);
                netOxygen -= aluminum / 2 * 3;
            }
        }
        
        double massIn = input.composition().weight();

        if(debug) HardScience.INSTANCE.info("Base mass for one mol of input is " + massIn);
        
        if(netWater > 0)
        {
            outputBuilder.put(ModBulkResources.H2O_FLUID, netWater);
            this.waterInputMols = 0; 
        }
        else if(netWater > 0)
        {
            this.waterInputMols = -netWater;
            massIn += this.waterInputMols * H2O_FLUID.weight();
            if(debug)
            {
                HardScience.INSTANCE.info("Water consumption is %f mols with mass %f",
                    this.waterInputMols, this.waterInputMols * H2O_FLUID.weight());
                reconIn.addTo(Element.O, this.waterInputMols);
                reconIn.addTo(Element.H, this.waterInputMols * 2);
            }
        }
        else
        {
            this.waterInputMols = 0; 
            if(debug) HardScience.INSTANCE.info("No water in or out");
        }

    
        if(nitrateUsage > 0)
        {
            netNitrogen -= nitrateUsage;
            netOxygen -= nitrateUsage * 3;
        }
        
        
        if(netNitrogen > 0)
        {
            outputBuilder.put(ModBulkResources.N2_GAS, netNitrogen / 2);
            this.nitrogenInputMols = 0;
        }
        else if(netNitrogen < 0)
        {
            this.nitrogenInputMols = -netNitrogen / 2;
            massIn += this.nitrogenInputMols * N2_GAS.weight();
            if(debug)
            {
                HardScience.INSTANCE.info("Nitrogen (N2) consumption is %f mols with mass %f",
                    this.nitrogenInputMols, this.nitrogenInputMols * N2_GAS.weight());
                reconIn.addTo(Element.N, this.nitrogenInputMols * 2);
            }
        }
        else
        {
            this.nitrogenInputMols = 0;
            if(debug) HardScience.INSTANCE.info("No nitrogen in or out");
        }
        
        if(Math.abs(netOxygen) < 0.000001)
        {
            this.oxygenInputMols = 0;
            if(debug) HardScience.INSTANCE.info("No oxygen in or out");
        }
        else if(netOxygen > 0)
        {
            outputBuilder.put(ModBulkResources.O2_GAS, netOxygen /2);
            this.oxygenInputMols = 0;
        }
        else // netOxygen < 0
        {
            this.oxygenInputMols = -netOxygen / 2;
            massIn += this.oxygenInputMols * O2_GAS.weight();
            if(debug) HardScience.INSTANCE.info("Oxygen (O2) consumption is %f mols with mass %f",
                    this.oxygenInputMols, this.oxygenInputMols * O2_GAS.weight());
            reconIn.addTo(Element.O, this.oxygenInputMols * 2);
        }
        
        if(debug) HardScience.INSTANCE.info("Total mass in = " + massIn);
        
        final Object2DoubleOpenHashMap<Element> reconOut = debug ? new Object2DoubleOpenHashMap<>() : null;
        
        double massOut = this.residueCompound.weight() * this.residueMols;
        if(debug) 
        {
            HardScience.INSTANCE.info("Residue output is %f mols with mass %f",
                this.residueMols, this.residueMols * this.residueCompound.weight());
            for(Element e : this.residueCompound.elements())
            {
                reconOut.addTo(e, this.residueCompound.countOf(e) * this.residueMols);
            }
        }

        outputs = outputBuilder.build();
        
        // max jei rows will be max count of all output, plus one for residue output
        // which isn't include in the output map because it may not be created yet
        maxOutputRows = Math.max(maxOutputRows, outputs.size() + 1);
        
        for(Entry<BulkResource, Double> entry : outputs.entrySet())
        {
            massOut += entry.getKey().composition().weight() * entry.getValue();
            
            if(debug) 
            {
                HardScience.INSTANCE.info("Output includes %f mols of %s with mass %f",
                    entry.getValue(), entry.getKey().displayName(),  
                    entry.getKey().composition().weight() * entry.getValue());
                
                for(Element e : entry.getKey().composition().elements())
                {
                    reconOut.addTo(e, entry.getKey().composition().countOf(e) * entry.getValue());
                }
            }
        }
        
        if(debug)
        {
            HardScience.INSTANCE.info("Total mass out is " + massOut);
            HardScience.INSTANCE.info("Total element in/out reconciliation...");
            for(Element e : reconIn.keySet())
            {
                double in = reconIn.getDouble(e);
                double out = reconOut.getDouble(e);
                HardScience.INSTANCE.info("%s in=%f out=%f %s", e.symbol, in, out, 
                        (Math.abs(in - out) < 0.000001) ? "MATCH" : "ERROR");
                reconOut.removeDouble(e);
            }
            
            if(!reconOut.isEmpty())
            {
                for(Entry<Element, Double> e : reconOut.entrySet())
                {
                    HardScience.INSTANCE.info("Unmatched output %s with %f mols", 
                            e.getKey().symbol, e.getValue());
                }
            }
        }

        if((debug || Configurator.PROCESSING.enableDigesterRecipeWarnings) && Math.abs(massOut - massIn) > 0.0000001)
        {
            HardScience.INSTANCE.warn("Digester recipe for " + input.displayName() + " violates conservation of mass. Recipe still created.");
        }
    }
    
    public Object2DoubleMap<Fluid> outputFactors()
    {
        if(this.outputFactors == null)
        {
            Object2DoubleOpenHashMap<Fluid> temp = new Object2DoubleOpenHashMap<>();
        
            double inputMolsPerLiter = this.input.molsPerLiter();
        
            for(Entry<BulkResource, Double> e : outputs.entrySet())
            {
                // input map has output mols per input mol
                // convert to output liters per input liter
                temp.addTo(e.getKey().fluid(), 
                        inputMolsPerLiter * e.getValue() * e.getKey().litersPerMol());
            }
            this.outputFactors = Object2DoubleMaps.unmodifiable(temp);
            
            // release memory
            this.outputs = null;
        }
        return this.outputFactors;
    }

    public double waterInputLitersPerInputLiter()
    {
        return this.input.molsPerLiter() 
                * this.waterInputMols 
                * ModBulkResources.H2O_FLUID.litersPerMol();
    }

    /**
     * Note not in liters because don't have reference to output bulk substance
     */
    public double residueOutputMolsPerInputLiter()
    {
        return this.input.molsPerLiter() * this.residueMols;
    }
    
    public double airInputLitersPerInputLiter()
    {
        double airMolsN = this.nitrogenInputMols * 2
                / ModBulkResources.FRESH_AIR.composition().countOf(Element.N);
        
        double airMolsO = this.oxygenInputMols * 2
                / ModBulkResources.FRESH_AIR.composition().countOf(Element.O);
        
        return Math.max(airMolsN, airMolsO) 
                * ModBulkResources.FRESH_AIR.litersPerMol();
    }

    public double joulesPerInputLiter()
    {
        //TODO
        return 1000;
    }
}
