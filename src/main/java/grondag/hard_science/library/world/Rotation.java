package grondag.hard_science.library.world;

import net.minecraft.util.EnumFacing;

/**
 * Texture rotations. Used mainly when rotated textures are used as
 * alternate textures.
 */
public enum Rotation {
	ROTATE_NONE(0, EnumFacing.NORTH),
	ROTATE_90(90, EnumFacing.EAST),
	ROTATE_180(180, EnumFacing.SOUTH),
	ROTATE_270(270, EnumFacing.WEST);

	/**
	 * Useful for locating model file names that use degrees as a suffix.
	 */
	public final int degrees;

	/**
     * Opposite of degress - useful for GL transforms. 0 and 180 are same, 90 and 270 are flipped
     */
    public final int degreesInverse;
	
	/**
	 * Horizontal face that corresponds to this rotation for SuperBlocks that have a single rotated face.
	 */
	public final EnumFacing horizontalFace;
	
	private static Rotation[] FROM_HORIZONTAL_FACING = new Rotation[EnumFacing.VALUES.length];
	
	static
	{
	       FROM_HORIZONTAL_FACING[EnumFacing.NORTH.ordinal()] = ROTATE_NONE;
	       FROM_HORIZONTAL_FACING[EnumFacing.EAST.ordinal()] = ROTATE_90;
	       FROM_HORIZONTAL_FACING[EnumFacing.SOUTH.ordinal()] = ROTATE_180;
	       FROM_HORIZONTAL_FACING[EnumFacing.WEST.ordinal()] = ROTATE_270;
	       FROM_HORIZONTAL_FACING[EnumFacing.NORTH.ordinal()] = ROTATE_NONE;
	       FROM_HORIZONTAL_FACING[EnumFacing.NORTH.ordinal()] = ROTATE_NONE;
	}
	
	Rotation(int degrees, EnumFacing horizontalFace)
	{
		this.degrees = degrees;
		this.degreesInverse = (360 - degrees) % 360;
		this.horizontalFace = horizontalFace;

	}
	
	public Rotation clockwise(){
		switch (this){
		case ROTATE_180:
			return ROTATE_270;
		case ROTATE_270:
			return ROTATE_NONE;
		case ROTATE_90:
			return ROTATE_180;
		case ROTATE_NONE:
		default:
			return ROTATE_90;
		}
	}
	
	
	/**
	 * Gives the rotation with horiztonalFace matching the given NSEW face
	 * For up and down will return ROTATE_NONE
	 */
	public static Rotation fromHorizontalFacing(EnumFacing face)
	{
	    return FROM_HORIZONTAL_FACING[face.ordinal()];
	}

}