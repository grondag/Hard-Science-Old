package grondag.adversity.library.render;

import grondag.adversity.library.world.WorldHelper;
import net.minecraft.util.EnumFacing;

public class FaceVertex
{
    public double x;
    public double y;
    public double u;
    public double v;
    public double depth;

    public FaceVertex(double x, double y, double depth)
    {
        this.x = x;
        this.y = y;
        this.u = x;
        this.v = y;
        this.depth = depth;
    }

    public FaceVertex(double x, double y, double depth, double u, double v)
    {
        this.x = x;
        this.y = y;
        this.u = u;
        this.v = v;
        this.depth = depth;
    }

    public void rotateFacing(EnumFacing onFace, EnumFacing toFace, EnumFacing fromFace, boolean includeUV)
    {
        if(toFace == fromFace)
        {
            //   NOOP
        }
        else if(toFace == WorldHelper.rightOf(onFace, fromFace))
        {
            double oldX = this.x;
            double oldY = this.y;
            this.x = oldY;
            this.y = 1.0 - oldX;

            if(includeUV)
            {
                double oldU = this.u;
                double oldV = this.v;
                this.u = oldV;
                this.v = 1.0 - oldU;
            }
        }
        else if(toFace == WorldHelper.bottomOf(onFace, fromFace))
        {
            double oldX = this.x;
            double oldY = this.y;
            this.x = 1.0 - oldX;
            this.y = 1.0 - oldY;

            if(includeUV)
            {
                double oldU = this.u;
                double oldV = this.v;
                this.u = 1.0 - oldU;
                this.v = 1.0 - oldV;
            }
        }
        else // left of
        {
            double oldX = this.x;
            double oldY = this.y;
            this.x = 1.0 - oldY;
            this.y = oldX;

            if(includeUV)
            {
                double oldU = this.u;
                double oldV = this.v;
                this.u = 1.0 - oldV;
                this.v = oldU;
            }
        }
    }

    public FaceVertex clone()
    {
        return new FaceVertex(x, y, depth, u, v);
    }

    public int getColor(int defaultColor)
    {
        return defaultColor;
    }

    public static class Colored extends FaceVertex
    {
        private int color = 0xFFFFFFFF;

        public Colored(double x, double y, double depth, int color)
        {
            super(x, y, depth);
            this.color = color;
        }

        public Colored(double x, double y, double depth, double u, double v, int color)
        {
            super(x, y, depth, u, v);
            this.color = color;
        }

        @Override
        public FaceVertex clone()
        {
            return new FaceVertex.Colored(x, y, depth, u, v, color);
        }

        @Override
        public int getColor(int defaultColor)
        {
            return color;
        }
    }
}