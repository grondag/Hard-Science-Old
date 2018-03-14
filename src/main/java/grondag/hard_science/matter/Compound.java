package grondag.hard_science.matter;

import com.google.common.collect.ImmutableList;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;

public class Compound implements IComposition
{
    private final Object2DoubleOpenHashMap<Molecule> molecules;
    
    private final Object2DoubleOpenHashMap<Element> elements;
    
    private final double weight;
    
//    private final double density;
    
    /**
     * Map contains molecule/fraction pairs.
     * If the compound fractions don't add up to 1.0,
     * fractions will be renormalized so that they do.
     */
    private Compound(Object2DoubleOpenHashMap<Molecule> map)
    {
        // renormalize mol fractions
        double totalFraction = 0;
        for(double d : map.values())
        {
            totalFraction += d;
        }
        this.molecules = new Object2DoubleOpenHashMap<Molecule>();
        for(Entry<Molecule> entry : map.object2DoubleEntrySet())
        {
            this.molecules.put(entry.getKey(), entry.getDoubleValue() / totalFraction);
        }
        
        // compute total weight and per-element counts
        double totalWeight = 0;
        
//        // compute weighted avg density
//        double avgDensity = 0;
        
        this.elements = new Object2DoubleOpenHashMap<Element>();

        for(Entry<Molecule> entry : this.molecules.object2DoubleEntrySet())
        {
            Molecule m = entry.getKey();
            double fraction = entry.getDoubleValue();
            
            totalWeight += m.weight() * fraction;
//            avgDensity += m.density() * fraction;
            
            for(Element e : entry.getKey().elements())
            {
                this.elements.addTo(e, m.countOf(e) * fraction);
            }
        }
        this.weight = totalWeight;
//        this.density = avgDensity;
    }
    
    public static Builder builder()
    {
        return new Builder();
    }
    
    public static class Builder
    {
        private Object2DoubleOpenHashMap<Molecule> map = new Object2DoubleOpenHashMap<Molecule>();
    
        private Builder() {};
        
        public Builder add(Molecule molecule, double moleFraction)
        {
            map.addTo(molecule, moleFraction);
            return this;
        }
        
        public Compound build()
        {
            return new Compound(map);
        }
        
        public boolean isEmpty()
        {
            return map.isEmpty();
        }
    }

    @Override
    public double countOf(Element e)
    {
        return this.elements.getDouble(e);
    }

    @Override
    public double weightOf(Element e)
    {
        return this.countOf(e) * e.weight;
    }

    @Override
    public double weight()
    {
        return this.weight;
    }

    @Override
    public ImmutableList<Element> elements()
    {
        return ImmutableList.copyOf(this.elements.keySet());
    }
    
    public double getFraction(Molecule molecule)
    {
        return this.molecules.getDouble(molecule);
    }
    
    public boolean contains(Molecule molecule)
    {
        return this.molecules.containsKey(molecule);
    }
}
