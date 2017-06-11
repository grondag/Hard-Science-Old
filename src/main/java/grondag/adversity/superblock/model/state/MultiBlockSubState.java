package grondag.adversity.superblock.model.state;

import grondag.adversity.library.varia.BitPacker;
import grondag.adversity.library.varia.BitPacker.BitElement.BooleanElement;
import grondag.adversity.library.varia.BitPacker.BitElement.IntElement;

/**
 * Putting here for now - should probably be part of parent class for multiblock shapes
 * @author grondag
 *
 */
public class MultiBlockSubState
{
    static final BitPacker PACKER_3_MULTIBLOCK = new BitPacker();
    private static final BooleanElement P3M_IS_CORNER = PACKER_3_MULTIBLOCK.createBooleanElement();
    private static final IntElement P3M_OFFSET_X = PACKER_3_MULTIBLOCK.createIntElement(256);
    private static final IntElement P3M_OFFSET_Y = PACKER_3_MULTIBLOCK.createIntElement(256);
    private static final IntElement P3M_OFFSET_Z = PACKER_3_MULTIBLOCK.createIntElement(256);
    
    private static final IntElement P3M_SCALE_X = PACKER_3_MULTIBLOCK.createIntElement(128 * 4);
    private static final IntElement P3M_SCALE_Y = PACKER_3_MULTIBLOCK.createIntElement(128 * 4);
    private static final IntElement P3M_SCALE_Z = PACKER_3_MULTIBLOCK.createIntElement(128 * 4);

    // zero value indicates solid shape
    private static final IntElement P3M_WALL_THICKNESS = PACKER_3_MULTIBLOCK.createIntElement(128 * 4);
}
