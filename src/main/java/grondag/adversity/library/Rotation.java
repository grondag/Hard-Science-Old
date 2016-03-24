package grondag.adversity.library;

/**
 * Texture rotations. Used mainly when rotated textures are used as
 * alternate textures.
 */
public enum Rotation {
	ROTATE_NONE(0, 0),
	ROTATE_90(1, 90),
	ROTATE_180(2, 180),
	ROTATE_270(3, 270);

	/**
	 * May be useful for dynamic manipulations.
	 */
	public final int index;

	/**
	 * Useful for locating model file names that use degrees as a suffix.
	 */
	public final int degrees;

	Rotation(int index, int degrees) {
		this.index = index;
		this.degrees = degrees;
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

}