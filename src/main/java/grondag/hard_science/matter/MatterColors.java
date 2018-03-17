package grondag.hard_science.matter;

import grondag.exotic_matter.varia.Color;
import grondag.exotic_matter.varia.Color.EnumHCLFailureMode;

public class MatterColors
{
    public static final int LABL_WHITE = 0xFFE0E0E0;
    public static final int LABL_BLACK = 0xFF101010;
    public static final int PCKG_WHITE = 0xFFE0E0E0;
    public static final int PCKG_LGRAY = 0xFF8F8F8F;
    public static final int PCKG_MGRAY = 0xFF6F6F6F;
    public static final int PCKG_DGRAY = 0xFF4F4F4F;
    public static final int PCKG_BLACK = 0xFF2F2F2F;
    
    public static final int SIZE_0 = Color.fromHCL(40, 50, 60, EnumHCLFailureMode.REDUCE_CHROMA).RGB_int | 0xFF000000;
    public static final int SIZE_1 = Color.fromHCL(91, 50, 60, EnumHCLFailureMode.REDUCE_CHROMA).RGB_int | 0xFF000000;
    public static final int SIZE_2 = Color.fromHCL(142, 50, 60, EnumHCLFailureMode.REDUCE_CHROMA).RGB_int | 0xFF000000;
    public static final int SIZE_3 = Color.fromHCL(193, 50, 60, EnumHCLFailureMode.REDUCE_CHROMA).RGB_int | 0xFF000000;
    public static final int SIZE_4 = Color.fromHCL(244, 50, 60, EnumHCLFailureMode.REDUCE_CHROMA).RGB_int | 0xFF000000;
    public static final int SIZE_5 = Color.fromHCL(295, 50, 60, EnumHCLFailureMode.REDUCE_CHROMA).RGB_int | 0xFF000000;
    public static final int SIZE_6 = Color.fromHCL(346, 50, 60, EnumHCLFailureMode.REDUCE_CHROMA).RGB_int | 0xFF000000;
    
    public static final int HDPE = 0xFFDDDDDD;
    public static final int UREA = 0xFFCCCCCC;
    public static final int WATER = 0xFF00FDFF;
    public static final int ETHANOL = 0xFF808080;
    public static final int RESIN_A = 0xFFa6e3e1;
    public static final int RESIN_B = 0xFFeceea8;
    public static final int RAW_MINERAL_DUST = 0xFF646973;
    public static final int DEPLETED_MINERAL_DUST = 0xFF737164;
    public static final int AMMONIA = 0x9046c490; // arbitrary
    
    public static final int FRESH_AIR = 0x30FFFFFF;
    
    public static final int RETURN_AIR = 0x60FFFF80;
    
    public static final int OXYGEN = 0x909ea6cd; // approximate liquid color
    
    public static final int HYDROGEN = 0x90ffdcf2; // approximate emission spectrum
    
    public static final int ETHENE = 0x90b6ff99; // arbitrary
    
    public static final int GRAPHITE = 0xFF202020;
    
    public static final int CYAN = 0xFF00FFFF;
    public static final int MAGENTA = 0xFFFF00FF;
    public static final int YELLOW = 0xFFFFFF00;
    
    public static final int SILICA = 0xFFfefdf9;
    
            
}