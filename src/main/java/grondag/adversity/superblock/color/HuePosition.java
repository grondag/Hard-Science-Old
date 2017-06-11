package grondag.adversity.superblock.color;

public enum HuePosition
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