package grondag.adversity.niceblock.newmodel;

import grondag.adversity.niceblock.support.CornerStateFinder;

public class ModelReference
{
    /**
     * Use with NeighborBlocks test for lookup of state index for connected blocks
     * that depend on adjacent blocks and do require corner tests. (Blocks with
     * outside border.) Does not return a recipe directly - use the
     * CornerRecipeFinder to get it. Dimensions are UDNSEW. Value 0 means no
     * neighbor, 1 means neighbor present. The values in the array are not
     * continuous - the CornerRecipeFinder adds between 0 and 16 to the base
     * recipe number depending on the specific scenario and presence of absence
     * of corner blocks, giving 386 possible recipes..
     */
    public static CornerStateFinder[][][][][][] CONNECTED_CORNER_STATE_LOOKUP = new CornerStateFinder[2][2][2][2][2][2];
    
    /**
     * Use with NeighborBlocks test for fast lookup of state index for connected
     * blocks that depend on adjacent blocks but don't require corner tests. (No
     * outside border.) Dimensions are UDNSEW. Value 0 means no neighbor, 1
     * means neighbor present
     */
    public static Integer[][][][][][] SIMPLE_JOIN_STATE_LOOKUP = new Integer[2][2][2][2][2][2];


    static {
        
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
        
        CONNECTED_CORNER_STATE_LOOKUP[1][1][1][1][1][1] = new CornerStateFinder(0);
        CONNECTED_CORNER_STATE_LOOKUP[1][1][1][1][1][0] = new CornerStateFinder(162, "UE", "UW", "DE", "DW");
        CONNECTED_CORNER_STATE_LOOKUP[1][1][1][1][0][1] = new CornerStateFinder(162, "UE", "UW", "DE", "DW");
        CONNECTED_CORNER_STATE_LOOKUP[1][1][1][1][0][0] = new CornerStateFinder(162, "UE", "UW", "DE", "DW");
        CONNECTED_CORNER_STATE_LOOKUP[1][1][1][0][1][1] = new CornerStateFinder(178, "UN", "US", "DN", "DS");
        CONNECTED_CORNER_STATE_LOOKUP[1][1][1][0][1][0] = new CornerStateFinder(322, "UN", "UE", "DN", "DE");
        CONNECTED_CORNER_STATE_LOOKUP[1][1][1][0][0][1] = new CornerStateFinder(338, "UE", "US", "DE", "DS");
        CONNECTED_CORNER_STATE_LOOKUP[1][1][1][0][0][0] = new CornerStateFinder(66, "UE", "DE");
        CONNECTED_CORNER_STATE_LOOKUP[1][1][0][1][1][1] = new CornerStateFinder(178, "UN", "US", "DN", "DS");
        CONNECTED_CORNER_STATE_LOOKUP[1][1][0][1][1][0] = new CornerStateFinder(354, "UN", "UW", "DN", "DW");
        CONNECTED_CORNER_STATE_LOOKUP[1][1][0][1][0][1] = new CornerStateFinder(370, "US", "UW", "DS", "DW");
        CONNECTED_CORNER_STATE_LOOKUP[1][1][0][1][0][0] = new CornerStateFinder(74, "UW", "DW");
        CONNECTED_CORNER_STATE_LOOKUP[1][1][0][0][1][1] = new CornerStateFinder(178, "UN", "US", "DN", "DS");
        CONNECTED_CORNER_STATE_LOOKUP[1][1][0][0][1][0] = new CornerStateFinder(54, "UN", "DN");
        CONNECTED_CORNER_STATE_LOOKUP[1][1][0][0][0][1] = new CornerStateFinder(62, "US", "DS");
        CONNECTED_CORNER_STATE_LOOKUP[1][1][0][0][0][0] = new CornerStateFinder(7);
        CONNECTED_CORNER_STATE_LOOKUP[1][0][1][1][1][1] = new CornerStateFinder(146, "NE", "SE", "SW", "NW");
        CONNECTED_CORNER_STATE_LOOKUP[1][0][1][1][1][0] = new CornerStateFinder(242, "UE", "UW", "NE", "NW");
        CONNECTED_CORNER_STATE_LOOKUP[1][0][1][1][0][1] = new CornerStateFinder(226, "UE", "UW", "SE", "SW");
        CONNECTED_CORNER_STATE_LOOKUP[1][0][1][1][0][0] = new CornerStateFinder(34, "UE", "UW");
        CONNECTED_CORNER_STATE_LOOKUP[1][0][1][0][1][1] = new CornerStateFinder(210, "UN", "US", "NE", "SE");
        CONNECTED_CORNER_STATE_LOOKUP[1][0][1][0][1][0] = new CornerStateFinder(82, "UN", "UE", "NE");
        CONNECTED_CORNER_STATE_LOOKUP[1][0][1][0][0][1] = new CornerStateFinder(90, "UE", "US", "SE");
        CONNECTED_CORNER_STATE_LOOKUP[1][0][1][0][0][0] = new CornerStateFinder(10, "UE");
        CONNECTED_CORNER_STATE_LOOKUP[1][0][0][1][1][1] = new CornerStateFinder(194, "UN", "US", "SW", "NW");
        CONNECTED_CORNER_STATE_LOOKUP[1][0][0][1][1][0] = new CornerStateFinder(98, "UN", "UW", "NW");
        CONNECTED_CORNER_STATE_LOOKUP[1][0][0][1][0][1] = new CornerStateFinder(106, "US", "UW", "SW");
        CONNECTED_CORNER_STATE_LOOKUP[1][0][0][1][0][0] = new CornerStateFinder(12, "UW");
        CONNECTED_CORNER_STATE_LOOKUP[1][0][0][0][1][1] = new CornerStateFinder(38, "UN", "US");
        CONNECTED_CORNER_STATE_LOOKUP[1][0][0][0][1][0] = new CornerStateFinder(14, "UN");
        CONNECTED_CORNER_STATE_LOOKUP[1][0][0][0][0][1] = new CornerStateFinder(16, "US");
        CONNECTED_CORNER_STATE_LOOKUP[1][0][0][0][0][0] = new CornerStateFinder(1);
        CONNECTED_CORNER_STATE_LOOKUP[0][1][1][1][1][1] = new CornerStateFinder(146, "NE", "SE", "SW", "NW");
        CONNECTED_CORNER_STATE_LOOKUP[0][1][1][1][1][0] = new CornerStateFinder(306, "DE", "DW", "NE", "NW");
        CONNECTED_CORNER_STATE_LOOKUP[0][1][1][1][0][1] = new CornerStateFinder(290, "DE", "DW", "SE", "SW");
        CONNECTED_CORNER_STATE_LOOKUP[0][1][1][1][0][0] = new CornerStateFinder(42, "DE", "DW");
        CONNECTED_CORNER_STATE_LOOKUP[0][1][1][0][1][1] = new CornerStateFinder(274, "DN", "DS", "NE", "SE");
        CONNECTED_CORNER_STATE_LOOKUP[0][1][1][0][1][0] = new CornerStateFinder(114, "DN", "DE", "NE");
        CONNECTED_CORNER_STATE_LOOKUP[0][1][1][0][0][1] = new CornerStateFinder(122, "DE", "DS", "SE");
        CONNECTED_CORNER_STATE_LOOKUP[0][1][1][0][0][0] = new CornerStateFinder(18, "DE");
        CONNECTED_CORNER_STATE_LOOKUP[0][1][0][1][1][1] = new CornerStateFinder(258, "DN", "DS", "SW", "NW");
        CONNECTED_CORNER_STATE_LOOKUP[0][1][0][1][1][0] = new CornerStateFinder(130, "DN", "DW", "NW");
        CONNECTED_CORNER_STATE_LOOKUP[0][1][0][1][0][1] = new CornerStateFinder(138, "DS", "DW", "SW");
        CONNECTED_CORNER_STATE_LOOKUP[0][1][0][1][0][0] = new CornerStateFinder(20, "DW");
        CONNECTED_CORNER_STATE_LOOKUP[0][1][0][0][1][1] = new CornerStateFinder(46, "DN", "DS");
        CONNECTED_CORNER_STATE_LOOKUP[0][1][0][0][1][0] = new CornerStateFinder(22, "DN");
        CONNECTED_CORNER_STATE_LOOKUP[0][1][0][0][0][1] = new CornerStateFinder(24, "DS");
        CONNECTED_CORNER_STATE_LOOKUP[0][1][0][0][0][0] = new CornerStateFinder(2);
        CONNECTED_CORNER_STATE_LOOKUP[0][0][1][1][1][1] = new CornerStateFinder(146, "NE", "SE", "SW", "NW");
        CONNECTED_CORNER_STATE_LOOKUP[0][0][1][1][1][0] = new CornerStateFinder(50, "NE", "NW");
        CONNECTED_CORNER_STATE_LOOKUP[0][0][1][1][0][1] = new CornerStateFinder(58, "SE", "SW");
        CONNECTED_CORNER_STATE_LOOKUP[0][0][1][1][0][0] = new CornerStateFinder(8);
        CONNECTED_CORNER_STATE_LOOKUP[0][0][1][0][1][1] = new CornerStateFinder(70, "NE", "SE");
        CONNECTED_CORNER_STATE_LOOKUP[0][0][1][0][1][0] = new CornerStateFinder(26, "NE");
        CONNECTED_CORNER_STATE_LOOKUP[0][0][1][0][0][1] = new CornerStateFinder(28, "SE");
        CONNECTED_CORNER_STATE_LOOKUP[0][0][1][0][0][0] = new CornerStateFinder(3);
        CONNECTED_CORNER_STATE_LOOKUP[0][0][0][1][1][1] = new CornerStateFinder(78, "SW", "NW");
        CONNECTED_CORNER_STATE_LOOKUP[0][0][0][1][1][0] = new CornerStateFinder(30, "NW");
        CONNECTED_CORNER_STATE_LOOKUP[0][0][0][1][0][1] = new CornerStateFinder(32, "SW");
        CONNECTED_CORNER_STATE_LOOKUP[0][0][0][1][0][0] = new CornerStateFinder(4);
        CONNECTED_CORNER_STATE_LOOKUP[0][0][0][0][1][1] = new CornerStateFinder(9);
        CONNECTED_CORNER_STATE_LOOKUP[0][0][0][0][1][0] = new CornerStateFinder(5);
        CONNECTED_CORNER_STATE_LOOKUP[0][0][0][0][0][1] = new CornerStateFinder(6);
        CONNECTED_CORNER_STATE_LOOKUP[0][0][0][0][0][0] = new CornerStateFinder(0);
    }
}
