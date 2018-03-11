package grondag.hard_science.matter;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.Log;
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
        
        Object2IntOpenHashMap<Element> map = new Object2IntOpenHashMap<Element>();
        
        char[] chars = formula.toCharArray();
        
        double weight = 0;
        String symbol = "";
        String number = "";
        
        for(int i = 0; i < chars.length; i++)
        {
            char c = chars[i];
            if(Character.isDigit(c))
            {
                number += String.valueOf(c);
            }
            else if(Character.isWhitespace(c))
            {
                // skip
            }
            else if(Character.isUpperCase(c))
            {
                symbol = String.valueOf(c);
            }
            else
            {
                // lower case, add to symbol
                symbol += String.valueOf(c);
            }
            
            if(i == (chars.length - 1) || Character.isUpperCase(chars[i + 1]))
            {
                Element e = Element.all().get(symbol);
                if(e == null)
                {
                    Log.warn("Chemical formula parse error: %s unrecognized.", symbol);
                }
                else
                {
                    int count = number.length() > 0 ? Integer.parseInt(number) : 1;
                    map.addTo(e, count);
                    weight += e.weight * count;
                }
                symbol = "";
                number = "";
            }
        }
        
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
