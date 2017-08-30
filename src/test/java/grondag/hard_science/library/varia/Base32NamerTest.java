package grondag.hard_science.library.varia;



import org.junit.Test;


public class Base32NamerTest
{

    @Test
    public void test()
    {
        assert Base32Namer.makeName(1).equals("1");
        assert Base32Namer.makeName(32).equals("10");
        assert Base32Namer.makeName(1024).equals("100");
        assert Base32Namer.makeName(32768).equals("1000");
        
        assert Base32Namer.makeName(31).equals("Z");
        assert Base32Namer.makeName(1023).equals("ZZ");
        assert Base32Namer.makeName(32767).equals("ZZZ");
        assert Base32Namer.makeName(32769).equals("1001");
        
    }

}
