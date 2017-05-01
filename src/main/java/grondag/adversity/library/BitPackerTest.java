package grondag.adversity.library;

import org.junit.Test;

import grondag.adversity.library.BitPacker.BitElement.BooleanElement;
import grondag.adversity.library.BitPacker.BitElement.EnumElement;
import grondag.adversity.library.BitPacker.BitElement.IntElement;
import grondag.adversity.library.BitPacker.BitElement.LongElement;

public class BitPackerTest
{

    private enum Things1 {ONE, TWO, THREE}
    
    private enum Things2 {A, B, C, D, E, F, G, H, I, J, K}
    
    @Test
    public void test()
    {
        BitPacker packer = new BitPacker();
        
        BooleanElement bool1 = packer.createBooleanElement();
        
        IntElement int1 = packer.createIntElement(0, 67);
        EnumElement<Things1> enum1 = packer.createEnumElement(Things1.class);

        IntElement int2 = packer.createIntElement(-53555643, 185375);
        EnumElement<Things2> enum2 = packer.createEnumElement(Things2.class);
        
        BooleanElement bool2 = packer.createBooleanElement();
        
        LongElement long1 = packer.createLongElement(-1, 634235);
        
        long bits = 0;

        bits |= int1.getBits(42);
        bits |= enum1.getBits(Things1.THREE);
        bits |= int2.getBits(-582375);
        bits |= enum2.getBits(Things2.H);
        bits |= bool1.getBits(false);
        bits |= bool2.getBits(true);
        bits |= long1.getBits(0);
        
        
        assert(enum1.getValue(bits) == Things1.THREE);
        assert(enum2.getValue(bits) == Things2.H);
        assert(int1.getValue(bits) == 42);
        assert(int2.getValue(bits) == -582375);
        assert(bool1.getValue(bits) == false);
        assert(bool2.getValue(bits) == true);
        assert(long1.getValue(bits) == 0);
    }

}