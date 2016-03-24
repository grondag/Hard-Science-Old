package grondag.adversity.library.joinstate;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Quat4f;

public class ModelReference
{
 

    /**
     * Use with NeighborBlocks test for fast lookup of state index for connected
     * blocks that depend on adjacent blocks but don't require corner tests. (No
     * outside border.) Dimensions are UDNSEW. Value 0 means no neighbor, 1
     * means neighbor present
     */
    public final static Integer[][][][][][] SIMPLE_JOIN_STATE_LOOKUP = new Integer[2][2][2][2][2][2];

    public final static FacadeFaceSelector[] MASONRY_FACADE_FACE_SELECTORS = new FacadeFaceSelector[64];

    public static void setup()
    {
        
        // Could have generated all this programmatically but did it by hand
        // before setting down the path of creating a generic block framework.
        // It wasn't broke and constants are fast and reliable, so I kept it.
        
        SIMPLE_JOIN_STATE_LOOKUP[0][0][0][0][0][0] = 0;
        SIMPLE_JOIN_STATE_LOOKUP[1][0][0][0][0][0] = 1;
        SIMPLE_JOIN_STATE_LOOKUP[0][1][0][0][0][0] = 2;
        SIMPLE_JOIN_STATE_LOOKUP[0][0][1][0][0][0] = 3;
        SIMPLE_JOIN_STATE_LOOKUP[0][0][0][1][0][0] = 4;
        SIMPLE_JOIN_STATE_LOOKUP[0][0][0][0][1][0] = 5;
        SIMPLE_JOIN_STATE_LOOKUP[0][0][0][0][0][1] = 6;
        SIMPLE_JOIN_STATE_LOOKUP[1][1][0][0][0][0] = 7;
        SIMPLE_JOIN_STATE_LOOKUP[0][0][1][1][0][0] = 8;
        SIMPLE_JOIN_STATE_LOOKUP[0][0][0][0][1][1] = 9;
        SIMPLE_JOIN_STATE_LOOKUP[1][0][1][0][0][0] = 10;
        SIMPLE_JOIN_STATE_LOOKUP[1][0][0][1][0][0] = 11;
        SIMPLE_JOIN_STATE_LOOKUP[1][0][0][0][1][0] = 12;
        SIMPLE_JOIN_STATE_LOOKUP[1][0][0][0][0][1] = 13;
        SIMPLE_JOIN_STATE_LOOKUP[0][1][1][0][0][0] = 14;
        SIMPLE_JOIN_STATE_LOOKUP[0][1][0][1][0][0] = 15;
        SIMPLE_JOIN_STATE_LOOKUP[0][1][0][0][1][0] = 16;
        SIMPLE_JOIN_STATE_LOOKUP[0][1][0][0][0][1] = 17;
        SIMPLE_JOIN_STATE_LOOKUP[0][0][1][0][1][0] = 18;
        SIMPLE_JOIN_STATE_LOOKUP[0][0][1][0][0][1] = 19;
        SIMPLE_JOIN_STATE_LOOKUP[0][0][0][1][1][0] = 20;
        SIMPLE_JOIN_STATE_LOOKUP[0][0][0][1][0][1] = 21;
        SIMPLE_JOIN_STATE_LOOKUP[1][0][1][1][0][0] = 22;
        SIMPLE_JOIN_STATE_LOOKUP[1][0][0][0][1][1] = 23;
        SIMPLE_JOIN_STATE_LOOKUP[0][1][1][1][0][0] = 24;
        SIMPLE_JOIN_STATE_LOOKUP[0][1][0][0][1][1] = 25;
        SIMPLE_JOIN_STATE_LOOKUP[0][0][1][1][1][0] = 26;
        SIMPLE_JOIN_STATE_LOOKUP[1][1][0][0][1][0] = 27;
        SIMPLE_JOIN_STATE_LOOKUP[0][0][1][1][0][1] = 28;
        SIMPLE_JOIN_STATE_LOOKUP[1][1][0][0][0][1] = 29;
        SIMPLE_JOIN_STATE_LOOKUP[1][1][1][0][0][0] = 30;
        SIMPLE_JOIN_STATE_LOOKUP[0][0][1][0][1][1] = 31;
        SIMPLE_JOIN_STATE_LOOKUP[1][1][0][1][0][0] = 32;
        SIMPLE_JOIN_STATE_LOOKUP[0][0][0][1][1][1] = 33;
        SIMPLE_JOIN_STATE_LOOKUP[1][0][1][0][1][0] = 34;
        SIMPLE_JOIN_STATE_LOOKUP[1][0][1][0][0][1] = 35;
        SIMPLE_JOIN_STATE_LOOKUP[1][0][0][1][1][0] = 36;
        SIMPLE_JOIN_STATE_LOOKUP[1][0][0][1][0][1] = 37;
        SIMPLE_JOIN_STATE_LOOKUP[0][1][1][0][1][0] = 38;
        SIMPLE_JOIN_STATE_LOOKUP[0][1][1][0][0][1] = 39;
        SIMPLE_JOIN_STATE_LOOKUP[0][1][0][1][1][0] = 40;
        SIMPLE_JOIN_STATE_LOOKUP[0][1][0][1][0][1] = 41;
        SIMPLE_JOIN_STATE_LOOKUP[0][0][1][1][1][1] = 42;
        SIMPLE_JOIN_STATE_LOOKUP[1][1][1][1][0][0] = 43;
        SIMPLE_JOIN_STATE_LOOKUP[1][1][0][0][1][1] = 44;
        SIMPLE_JOIN_STATE_LOOKUP[1][0][0][1][1][1] = 45;
        SIMPLE_JOIN_STATE_LOOKUP[1][0][1][0][1][1] = 46;
        SIMPLE_JOIN_STATE_LOOKUP[1][0][1][1][0][1] = 47;
        SIMPLE_JOIN_STATE_LOOKUP[1][0][1][1][1][0] = 48;
        SIMPLE_JOIN_STATE_LOOKUP[0][1][0][1][1][1] = 49;
        SIMPLE_JOIN_STATE_LOOKUP[0][1][1][0][1][1] = 50;
        SIMPLE_JOIN_STATE_LOOKUP[0][1][1][1][0][1] = 51;
        SIMPLE_JOIN_STATE_LOOKUP[0][1][1][1][1][0] = 52;
        SIMPLE_JOIN_STATE_LOOKUP[1][1][1][0][1][0] = 53;
        SIMPLE_JOIN_STATE_LOOKUP[1][1][1][0][0][1] = 54;
        SIMPLE_JOIN_STATE_LOOKUP[1][1][0][1][1][0] = 55;
        SIMPLE_JOIN_STATE_LOOKUP[1][1][0][1][0][1] = 56;
        SIMPLE_JOIN_STATE_LOOKUP[0][1][1][1][1][1] = 57;
        SIMPLE_JOIN_STATE_LOOKUP[1][0][1][1][1][1] = 58;
        SIMPLE_JOIN_STATE_LOOKUP[1][1][0][1][1][1] = 59;
        SIMPLE_JOIN_STATE_LOOKUP[1][1][1][0][1][1] = 60;
        SIMPLE_JOIN_STATE_LOOKUP[1][1][1][1][0][1] = 61;
        SIMPLE_JOIN_STATE_LOOKUP[1][1][1][1][1][0] = 62;
        SIMPLE_JOIN_STATE_LOOKUP[1][1][1][1][1][1] = 63;
        
 
        MASONRY_FACADE_FACE_SELECTORS[0] = new FacadeFaceSelector(15, 15, 15, 15, 15, 15);
        MASONRY_FACADE_FACE_SELECTORS[1] = new FacadeFaceSelector(15, 15, 1, 1, 1, 1);
        MASONRY_FACADE_FACE_SELECTORS[2] = new FacadeFaceSelector(15, 15, 4, 4, 4, 4);
        MASONRY_FACADE_FACE_SELECTORS[3] = new FacadeFaceSelector(2, 2, 15, 15, 8, 2);
        MASONRY_FACADE_FACE_SELECTORS[4] = new FacadeFaceSelector(8, 8, 15, 15, 2, 8);
        MASONRY_FACADE_FACE_SELECTORS[5] = new FacadeFaceSelector(1, 4, 2, 8, 15, 15);
        MASONRY_FACADE_FACE_SELECTORS[6] = new FacadeFaceSelector(4, 1, 8, 2, 15, 15);
        MASONRY_FACADE_FACE_SELECTORS[7] = new FacadeFaceSelector(15, 15, 5, 5, 5, 5);
        MASONRY_FACADE_FACE_SELECTORS[8] = new FacadeFaceSelector(10, 10, 15, 15, 10, 10);
        MASONRY_FACADE_FACE_SELECTORS[9] = new FacadeFaceSelector(5, 5, 10, 10, 15, 15);
        MASONRY_FACADE_FACE_SELECTORS[10] = new FacadeFaceSelector(15, 2, 15, 1, 9, 3);
        MASONRY_FACADE_FACE_SELECTORS[11] = new FacadeFaceSelector(15, 8, 1, 15, 3, 9);
        MASONRY_FACADE_FACE_SELECTORS[12] = new FacadeFaceSelector(15, 4, 3, 9, 15, 1);
        MASONRY_FACADE_FACE_SELECTORS[13] = new FacadeFaceSelector(15, 1, 9, 3, 1, 15);
        MASONRY_FACADE_FACE_SELECTORS[14] = new FacadeFaceSelector(2, 15, 15, 4, 12, 6);
        MASONRY_FACADE_FACE_SELECTORS[15] = new FacadeFaceSelector(8, 15, 4, 15, 6, 12);
        MASONRY_FACADE_FACE_SELECTORS[16] = new FacadeFaceSelector(1, 15, 6, 12, 15, 4);
        MASONRY_FACADE_FACE_SELECTORS[17] = new FacadeFaceSelector(4, 15, 12, 6, 4, 15);
        MASONRY_FACADE_FACE_SELECTORS[18] = new FacadeFaceSelector(3, 6, 15, 8, 15, 2);
        MASONRY_FACADE_FACE_SELECTORS[19] = new FacadeFaceSelector(6, 3, 15, 2, 8, 15);
        MASONRY_FACADE_FACE_SELECTORS[20] = new FacadeFaceSelector(9, 12, 2, 15, 15, 8);
        MASONRY_FACADE_FACE_SELECTORS[21] = new FacadeFaceSelector(12, 9, 8, 15, 2, 15);
        MASONRY_FACADE_FACE_SELECTORS[22] = new FacadeFaceSelector(15, 10, 15, 15, 11, 11);
        MASONRY_FACADE_FACE_SELECTORS[23] = new FacadeFaceSelector(15, 5, 11, 11, 15, 15);
        MASONRY_FACADE_FACE_SELECTORS[24] = new FacadeFaceSelector(10, 15, 15, 15, 14, 14);
        MASONRY_FACADE_FACE_SELECTORS[25] = new FacadeFaceSelector(5, 15, 14, 14, 15, 15);
        MASONRY_FACADE_FACE_SELECTORS[26] = new FacadeFaceSelector(11, 14, 15, 15, 15, 10);
        MASONRY_FACADE_FACE_SELECTORS[27] = new FacadeFaceSelector(15, 15, 7, 13, 15, 5);
        MASONRY_FACADE_FACE_SELECTORS[28] = new FacadeFaceSelector(14, 11, 15, 15, 10, 15);
        MASONRY_FACADE_FACE_SELECTORS[29] = new FacadeFaceSelector(15, 15, 13, 7, 5, 15);
        MASONRY_FACADE_FACE_SELECTORS[30] = new FacadeFaceSelector(15, 15, 15, 5, 13, 7);
        MASONRY_FACADE_FACE_SELECTORS[31] = new FacadeFaceSelector(7, 7, 15, 10, 15, 15);
        MASONRY_FACADE_FACE_SELECTORS[32] = new FacadeFaceSelector(15, 15, 5, 15, 7, 13);
        MASONRY_FACADE_FACE_SELECTORS[33] = new FacadeFaceSelector(13, 13, 10, 15, 15, 15);
        MASONRY_FACADE_FACE_SELECTORS[34] = new FacadeFaceSelector(15, 6, 15, 9, 15, 3);
        MASONRY_FACADE_FACE_SELECTORS[35] = new FacadeFaceSelector(15, 3, 15, 3, 9, 15);
        MASONRY_FACADE_FACE_SELECTORS[36] = new FacadeFaceSelector(15, 12, 3, 15, 15, 9);
        MASONRY_FACADE_FACE_SELECTORS[37] = new FacadeFaceSelector(15, 9, 9, 15, 3, 15);
        MASONRY_FACADE_FACE_SELECTORS[38] = new FacadeFaceSelector(3, 15, 15, 12, 15, 6);
        MASONRY_FACADE_FACE_SELECTORS[39] = new FacadeFaceSelector(6, 15, 15, 6, 12, 15);
        MASONRY_FACADE_FACE_SELECTORS[40] = new FacadeFaceSelector(9, 15, 6, 15, 15, 12);
        MASONRY_FACADE_FACE_SELECTORS[41] = new FacadeFaceSelector(12, 15, 12, 15, 6, 15);
        MASONRY_FACADE_FACE_SELECTORS[42] = new FacadeFaceSelector(0, 0, 15, 15, 15, 15);
        MASONRY_FACADE_FACE_SELECTORS[43] = new FacadeFaceSelector(15, 15, 15, 15, 0, 0);
        MASONRY_FACADE_FACE_SELECTORS[44] = new FacadeFaceSelector(15, 15, 0, 0, 15, 15);
        MASONRY_FACADE_FACE_SELECTORS[45] = new FacadeFaceSelector(15, 13, 11, 15, 15, 15);
        MASONRY_FACADE_FACE_SELECTORS[46] = new FacadeFaceSelector(15, 7, 15, 11, 15, 15);
        MASONRY_FACADE_FACE_SELECTORS[47] = new FacadeFaceSelector(15, 11, 15, 15, 11, 15);
        MASONRY_FACADE_FACE_SELECTORS[48] = new FacadeFaceSelector(15, 14, 15, 15, 15, 11);
        MASONRY_FACADE_FACE_SELECTORS[49] = new FacadeFaceSelector(13, 15, 14, 15, 15, 15);
        MASONRY_FACADE_FACE_SELECTORS[50] = new FacadeFaceSelector(7, 15, 15, 14, 15, 15);
        MASONRY_FACADE_FACE_SELECTORS[51] = new FacadeFaceSelector(14, 15, 15, 15, 14, 15);
        MASONRY_FACADE_FACE_SELECTORS[52] = new FacadeFaceSelector(11, 15, 15, 15, 15, 14);
        MASONRY_FACADE_FACE_SELECTORS[53] = new FacadeFaceSelector(15, 15, 15, 13, 15, 7);
        MASONRY_FACADE_FACE_SELECTORS[54] = new FacadeFaceSelector(15, 15, 15, 7, 13, 15);
        MASONRY_FACADE_FACE_SELECTORS[55] = new FacadeFaceSelector(15, 15, 7, 15, 15, 13);
        MASONRY_FACADE_FACE_SELECTORS[56] = new FacadeFaceSelector(15, 15, 13, 15, 7, 15);
        MASONRY_FACADE_FACE_SELECTORS[57] = new FacadeFaceSelector(0, 15, 15, 15, 15, 15);
        MASONRY_FACADE_FACE_SELECTORS[58] = new FacadeFaceSelector(15, 0, 15, 15, 15, 15);
        MASONRY_FACADE_FACE_SELECTORS[59] = new FacadeFaceSelector(15, 15, 0, 15, 15, 15);
        MASONRY_FACADE_FACE_SELECTORS[60] = new FacadeFaceSelector(15, 15, 15, 0, 15, 15);
        MASONRY_FACADE_FACE_SELECTORS[61] = new FacadeFaceSelector(15, 15, 15, 15, 0, 15);
        MASONRY_FACADE_FACE_SELECTORS[62] = new FacadeFaceSelector(15, 15, 15, 15, 15, 0);
        MASONRY_FACADE_FACE_SELECTORS[63] = new FacadeFaceSelector(15, 15, 15, 15, 15, 15);
    }
    

    
 
}
