package grondag.hard_science.matter;

import com.google.common.collect.ImmutableList;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;

public class Compound implements IComposition
{
    private final Object2DoubleOpenHashMap<Molecule> molecules;
    
    private final Object2DoubleOpenHashMap<Element> elements;
    
    private final double weight;
    
    private Compound(Object2DoubleOpenHashMap<Molecule> map)
    {
        this.molecules = map;
        double w = 0;
        
        Object2DoubleOpenHashMap<Element> els = new Object2DoubleOpenHashMap<Element>();

        for(Entry<Molecule> entry : map.object2DoubleEntrySet())
        {
            Molecule m = entry.getKey();
            double fraction = entry.getDoubleValue();
            
            w += m.weight() * fraction;
            for(Element e : entry.getKey().elements())
            {
                els.addTo(e, m.countOf(e) * fraction);
            }
        }
        this.weight = w;
        this.elements = els;
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
