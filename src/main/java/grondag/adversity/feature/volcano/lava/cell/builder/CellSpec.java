package grondag.adversity.feature.volcano.lava.cell.builder;

import grondag.adversity.feature.volcano.lava.cell.LavaCell2;

class CellSpec
{
    int floor;
    boolean isFlowFloor;
    int ceiling;
    int lavaLevel;
    byte suspendedLevel = LavaCell2.SUSPENDED_NONE;
}