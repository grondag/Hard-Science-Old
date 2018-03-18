package grondag.hard_science.movetogether;

import grondag.exotic_matter.varia.Color;
import net.minecraft.util.text.translation.I18n;

public enum Hue
{
    INFRARED,
    CHERRY,
    ROSE,
    POMEGRANATE,
    CRIMSON,
    SCARLET,
    RED,
    VERMILLION,
    TANGERINE,
    ORANGE,
    EMBER,
    SUNSET,
    PUMPKIN,
    CHEDDAR,
    MANGO,
    SUNFLOWER,
    GOLD,
    TORCH,
    YELLOW,
    LEMON,
    LIME,
    PERIDOT,
    CHARTREUSE,
    CACTUS,
    GREEN,
    FOLIAGE,
    MINT,
    SAGE,
    JUNIPER,
    CELADON,
    EMERALD,
    VERDIGRIS,
    TURQUOISE,
    SEA_FOAM,
    CYAN,
    ICE,
    BERYL,
    APATITE,
    MARINE,
    AQUA,
    ROBIN_EGG,
    MORNING,
    CERULEAN,
    TOPAZ,
    SKY,
    SAPPHIRE,
    PERIWINKLE,
    TWILIGHT,
    AZURE,
    OCEAN,
    COBALT,
    BLUE,
    LAPIS,
    INDIGO,
    VIOLET,
    PURPLE,
    AMETHYST,
    LILAC,
    MAGENTA,
    FUSCHIA,
    TULIP,
    PINK,
    PEONY;

    /**
     * Rotate our color cylinder by this many degrees
     * to tweak which colors we actually get.
     * A purely aesthetic choice.
     */
    private static final double HUE_SALT = 0;

    private int hueSample = 0;

    public double hueDegrees()
    {
        return this.ordinal() * 360.0 / Hue.values().length + HUE_SALT;
    }

    /**
     * Initialized lazily because ordinal not available during instantiation.
     * @return
     */
    public int hueSample()
    {
        if(hueSample == 0)
        {
            this.hueSample = Color.fromHCL(this.hueDegrees(), Color.HCL_MAX, Color.HCL_MAX).RGB_int | 0xFF000000;
        }
        return this.hueSample;
    }

    @SuppressWarnings("deprecation")
    public String localizedName()
    {
        return I18n.translateToLocal("color.hue." + this.name().toLowerCase());
    }
}