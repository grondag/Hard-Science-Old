package grondag.hard_science.materials;

import grondag.hard_science.library.varia.Color;
import grondag.hard_science.library.varia.Color.EnumHCLFailureMode;

class MatterColors
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
}