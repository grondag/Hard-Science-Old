package grondag.hard_science.machines;

import grondag.hard_science.machines.base.MachineTESR;
import grondag.hard_science.machines.base.MachineTileEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;

public class SmartChestTESR extends MachineTESR
{
    public static SmartChestTESR INSTANCE = new SmartChestTESR();

    @Override
    protected void renderControlFace(Tessellator tessellator, BufferBuilder buffer, MachineTileEntity te, int alpha)
    {
        // nothing special yet
    }

}
