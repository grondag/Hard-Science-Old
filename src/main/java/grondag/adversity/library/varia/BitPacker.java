package grondag.adversity.library.varia;

import java.util.ArrayList;

public class BitPacker
{
    @SuppressWarnings("unused")
    private ArrayList<BitElement> elements = new ArrayList<BitElement>();
    
    private int totalBitLength;
    private long bitMask;
    
    private void addElement(BitElement element)
    {
        element.shift = this.totalBitLength;
        element.shiftedMask = element.mask << element.shift;
        element.shiftedInverseMask = ~ element.shiftedMask;
        
        this.totalBitLength += element.bitLength;
        this.bitMask = Useful.longBitMask(totalBitLength);
        assert(totalBitLength <= 64);
    }
    
    public int bitLength() { return this.totalBitLength; }
    public long bitMask() { return this.bitMask; }

    public <T extends Enum<?>> BitElement.EnumElement<T> createEnumElement(Class<T> e)
    {
        BitElement.EnumElement<T> result = new BitElement.EnumElement<T>(e);
        this.addElement(result);
        return result;
    }
    
    public BitElement.IntElement createIntElement(int minValue, int maxValue)
    {
        BitElement.IntElement result = new BitElement.IntElement(minValue, maxValue);
        this.addElement(result);
        return result;
    }
    
    /** use this when you just need zero-based positive integers. Same as createIntElement(0, count-1) */
    public BitElement.IntElement createIntElement(int valueCount)
    {
        BitElement.IntElement result = new BitElement.IntElement(0, valueCount - 1);
        this.addElement(result);
        return result;
    }
    
    public BitElement.LongElement createLongElement(long minValue, long maxValue)
    {
        BitElement.LongElement result = new BitElement.LongElement(minValue, maxValue);
        this.addElement(result);
        return result;
    }
    
    /** use this when you just need zero-based positive (long) integers. Same as createLongElement(0, count-1) */
    public BitElement.LongElement createLongElement(long valueCount)
    {
        BitElement.LongElement result = new BitElement.LongElement(0, valueCount - 1);
        this.addElement(result);
        return result;
    }
    
    public BitElement.BooleanElement createBooleanElement()
    {
        BitElement.BooleanElement result = new BitElement.BooleanElement();
        this.addElement(result);
        return result;
    }
    
    public static abstract class BitElement
    {
        protected final int bitLength;
        protected long mask;
        protected int shift;
        protected long shiftedMask;
        protected long shiftedInverseMask;
        
        private BitElement(long valueCount)
        {
            this.bitLength = Useful.bitLength(valueCount);
            this.mask = Useful.longBitMask(this.bitLength);
        }
        
        /** 
         * Mask that isolates bits for this element. 
         * Useful to compare this and other elements simultaneously 
         */
        public long comparisonMask()
        {
            return this.shiftedMask;
        }
        
        public static class EnumElement<T extends Enum<?>> extends BitElement
        {
            private T[] values;
            
            private EnumElement(Class<T> e)
            {
                super(e.getEnumConstants().length);
                this.values = e.getEnumConstants();
            }
            
            public long getBits(T e)
            {
                return (e.ordinal() & mask) << shift; 
            }
            
            public long setValue(T e, long bitsIn)
            { 
                return (bitsIn & this.shiftedInverseMask) | getBits(e);
            }
            
            public T getValue(long bits)
            { 
                return values[(int) ((bits >> shift) & mask)]; 
            }
        }
        
        /** Stores values in given range as bits.  Handles negative values */
        public static class IntElement extends BitElement
        {
            private final int minValue;
            
            private IntElement(int minValue, int maxValue)
            {
                super(maxValue - minValue + 1);
                this.minValue = minValue;
            }
            
            public long getBits(int i) 
            { 
                return (((long)i - minValue) & mask) << shift; 
            }
            
            public long setValue(int i, long bitsIn)
            { 
                return (bitsIn & this.shiftedInverseMask) | getBits(i);
            }
            
            public int getValue(long bits)
            { 
                return (int) (((bits >> shift) & mask) + minValue); 
            }
        }
        
        /** 
         * Stores values in given range as bits.  Handles negative values 
         * but if range is too large will overflow due lack of unsigned numeric types. 
         */
        public static class LongElement extends BitElement
        {
            private final long minValue;
            
            private LongElement(long minValue, long maxValue)
            {
                super(maxValue - minValue + 1);
                this.minValue = minValue;
            }
            
            public long getBits(long i) 
            { 
                return ((i - minValue) & mask) << shift; 
            }
            
            public long setValue(long i, long bitsIn)
            { 
                return (bitsIn & this.shiftedInverseMask) | getBits(i);
            }
            
            public long getValue(long bits)
            { 
                return ((bits >> shift) & mask) + minValue; 
            }
        }
        
        public static class BooleanElement extends BitElement
        {
            private BooleanElement()
            {
                super(2);
            }
            
            public long getBits(boolean b) 
            { 
                return ((b ? 1 : 0) & mask) << shift; 
            }
            
            public long setValue(boolean b, long bitsIn)
            { 
                return (bitsIn & this.shiftedInverseMask) | getBits(b);
            }
            
            public boolean getValue(long bits)
            { 
                return ((bits >> shift) & mask) == 1; 
            }
        }
    }
}
