package grondag.hard_science.matter;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.Log;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class Molecule implements IComposition
{

    public final String formula;
    
    private final Object2IntOpenHashMap<Element> map;
    
    private final double weight;
    
//    private final double density;
    
    /**
     * Standard enthalpy of formation in J/mol.
     * Will be zero for elements in "natural" state.
     * Negative if formation is exothermic.
     */
    public final double enthalpyJoules;
    
    @Override
    public double countOf(Element e)
    {
        return map == null ? 0 : map.getInt(e);
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
    public Molecule(String formula, double enthalpyKJ)//, double density)
    {
        this.formula = formula;
//        this.density = density;
        this.enthalpyJoules = enthalpyKJ * 1000;
        this.map = parse(formula);
        
        double totalWeight = 0;
        if(this.map != null && !this.map.isEmpty())
        {
            for(Entry<Element> e : this.map.object2IntEntrySet())
            {
                totalWeight += e.getKey().weight * e.getIntValue();
            }
        }
        this.weight = totalWeight;
    }

    public static boolean isValidFormula(String formula)
    {
        Object2IntOpenHashMap<Element> test = parse(formula);
        return !(test == null || test.isEmpty());
    }
    
    private static Object2IntOpenHashMap<Element> parse(String formula)
    {
        Object2IntOpenHashMap<Element> map = new Object2IntOpenHashMap<Element>();
        
        char[] chars = formula.toCharArray();
        
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
                    return null;
                }
                else
                {
                    int count = number.length() > 0 ? Integer.parseInt(number) : 1;
                    map.addTo(e, count);
                }
                symbol = "";
                number = "";
            }
        }
        return map;
    }
    
    @Override
    public double weight()
    {
        return weight;
    }
//
//    @Override
//    public double density()
//    {
//        return this.density;
//    }
    
    @Override
    public ImmutableList<Element> elements()
    {
        return this.map == null ? ImmutableList.of() : ImmutableList.copyOf(this.map.keySet());
    }
}
