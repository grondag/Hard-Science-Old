package grondag.hard_science.matter;

import java.util.Scanner;

import com.google.common.collect.ImmutableList;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class Molecule implements IComposition
{

    public final String formula;
    
    private final Object2IntOpenHashMap<Element> map;
    
    private final double weight;
    
    /**
     * Standard enthalpy of formation in J/mol.
     * Will be zero for elements in "natural" state.
     * Negative if formation is exothermic.
     */
    public final double enthalpyJoules;
    
    @Override
    public double countOf(Element e)
    {
        return map.getInt(e);
    }
    
    @Override
    public double weightOf(Element e)
    {
        return countOf(e) * e.weight;
    }
    
    /**
     * Note that joules input is kJ for convenience
     * but is stored/referenced as J.
     */
    public Molecule(String forumula, double enthalpyKJ)
    {
        this.formula = forumula;
        this.enthalpyJoules = enthalpyKJ * 1000;
        
        Scanner scan = new Scanner(formula);
        double weight = 0;
        
        Object2IntOpenHashMap<Element> map = new Object2IntOpenHashMap<Element>();
        
        while(scan.hasNext())
        {
            String n = scan.next();
            Element e = null;
            String count = "";
            
            if(n.length() > 1)
            {
                String s = n.substring(0, 2);
                e = Element.all().get(s);
                if(e != null && n.length() > 2)
                {
                    count = n.substring(2);
                }
            }
            if(e == null)
            {
                String s = n.substring(0, 1);
                e = Element.all().get(s);
                if(e != null && n.length() > 1)
                {
                    count = n.substring(1);
                }
            }
            if(e != null)
            {
                int c = count.length() > 0 ? Integer.parseInt(count) : 1;
                map.addTo(e, c);
                weight += e.weight * c;
            }
        }
        scan.close();
        this.weight = weight;
        this.map = map;
    }

    @Override
    public double weight()
    {
        return weight;
    }

    @Override
    public ImmutableList<Element> elements()
    {
        return ImmutableList.copyOf(this.map.keySet());
    }
}
