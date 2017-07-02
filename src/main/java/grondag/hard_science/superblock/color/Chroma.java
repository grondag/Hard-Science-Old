package grondag.hard_science.superblock.color;

import net.minecraft.util.text.translation.I18n;

public enum Chroma
{
    PURE_NETURAL(0),
    WHITE(2.5),
    GREY(5),
    NEUTRAL(10),
    RICH(20),
    DEEP(30),
    EXTRA_DEEP(40),
    BOLD(50),
    EXTRA_BOLD(60),
    ACCENT(70),
    INTENSE_ACCENT(80),
    ULTRA_ACCENT(90);

    public final double value;

    private Chroma(double chromaValue)
    {
        this.value = chromaValue;
    }

    @SuppressWarnings("deprecation")
    public String localizedName()
    {
        return I18n.translateToLocal("color.chroma." + this.name().toLowerCase());
    }
}