package grondag.adversity.superblock.color;

import net.minecraft.util.text.translation.I18n;

public class HueSet
{
    public static enum HuePosition
    {
        FAR_LEFT("Far Left", -120),
        NEAR_LEFT("Near Left", -23),
        NONE("None", 0),
        NEAR_RIGHT("Near Right", 23),
        FAR_RIGHT("Far Right", 120),
        OPPOSITE("Opposite", 180);

        public final String positionName;
        public final double hueOffset;

        private HuePosition(String positionName, double hueOffset)
        {
            this.positionName = positionName;
            this.hueOffset = hueOffset;
        }
    }

    public static enum Chroma
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

    public static enum Luminance
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
}