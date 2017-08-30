package grondag.hard_science.library.varia;

/**
 * Generates 1 to 4 digit alphanumeric IDs from input values.
 * Used for machine names.
 * @author grondag
 */
public class Base32Namer
{
    private static char[] GLYPHS = "0123456789ABCDEFGHJKLMNPRTUVWXYZ".toCharArray();
    
    public static String makeName(int num)
    {
        char[] digits = new char[4];
        digits[0] = GLYPHS[num >> 15 & 31];
        digits[1] = GLYPHS[num >> 10 & 31];
        digits[2] = GLYPHS[num >> 5 & 31];
        digits[3] = GLYPHS[num & 31];
        
        if(num < 0) num = -num;
        
        if(num > 32767)
        {
            // common 4-digit name
            return new String(digits, 0, 4);
            
        }
        else if(num > 1023)
        {
            // uncommon 3-digit name
            return new String(digits, 1, 3);
        }
        else if(num > 31)
        {
            // rare 2-digit name
            return new String(digits, 2, 2);
        }
        else
        {
            // ultra rare one-digit name
            return String.valueOf(digits[3]);
        }
            
    }

}
