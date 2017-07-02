package grondag.hard_science.superblock.color;

import net.minecraft.util.text.translation.I18n;

public enum Luminance
{
    BRILLIANT(90),
    EXTRA_BRIGHT(81),
    BRIGHT(72),
    EXTRA_LIGHT(63),
    LIGHT(54),
    MEDIUM_LIGHT(45),
    MEDIUM_DARK(36),
    DARK(27),
    EXTRA_DARK(18);

    public final double value;

    private Luminance(double luminanceValue)
    {
        this.value = luminanceValue;
    }

    @SuppressWarnings("deprecation")
    public String localizedName()
    {
        return I18n.translateToLocal("color.luminance." + this.name().toLowerCase());
    }
}