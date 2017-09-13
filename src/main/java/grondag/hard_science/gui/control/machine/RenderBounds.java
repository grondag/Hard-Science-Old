package grondag.hard_science.gui.control.machine;

public interface RenderBounds<T extends RenderBounds<T>>
{
    public static final RadialRenderBounds BOUNDS_SYMBOL = new RadialRenderBounds(0.10, 0.10, 0.07);
    public static final RectRenderBounds BOUNDS_NAME = new RectRenderBounds(0.25, 0.04, 0.5, 0.12);
    public static final RadialRenderBounds BOUNDS_ON_OFF = new RadialRenderBounds(0.90, 0.10, 0.07);

    public static final RadialRenderBounds BOUNDS_PROGRESS = new RadialRenderBounds(0.24, 0.38, 0.20);
    public static final PowerRenderBounds BOUNDS_POWER = new PowerRenderBounds(0.76, 0.38, 0.20);
    
    public static final RadialRenderBounds BOUNDS_REDSTONE = new RadialRenderBounds(0.90, 0.90, 0.06);
    public static final RadialRenderBounds BOUNDS_GAUGE[] = 
    {
            new RadialRenderBounds(0.12, 0.88, 0.08),
            new RadialRenderBounds(0.30, 0.88, 0.08),
            new RadialRenderBounds(0.48, 0.88, 0.08),
            new RadialRenderBounds(0.66, 0.88, 0.08),
            new RadialRenderBounds(0.12, 0.70, 0.08),
            new RadialRenderBounds(0.30, 0.70, 0.08),
            new RadialRenderBounds(0.48, 0.70, 0.08),
            new RadialRenderBounds(0.66, 0.70, 0.08)
    };
    
    public abstract T offset(double x, double y);

    public abstract T scale(double left, double top, double width, double height);
    
    public static abstract class AbstractRectRenderBounds
    {
        public final double left;
        public final double top;
        public final double width;
        public final double height;
    
        public AbstractRectRenderBounds (double left, double top, double width, double height)
        {
            this.left = left;
            this.top = top;
            this.height = height;
            this.width = width;
        }
    
        public AbstractRectRenderBounds (double left, double top, double size)
        {
            this(left, top, size, size);
        }
        public double left() { return left; }
        public double top() { return top; }
        public double width() { return width; }
        public double height() { return height; }
        public double right() { return left + width; }
        public double bottom() { return top + height; }
        
        public boolean contains(double x, double y)
        {
            return !(x < this.left || x > this.right() || y < this.top || y > this.bottom());
        }
    }
    
    public static class RectRenderBounds extends AbstractRectRenderBounds implements RenderBounds<RectRenderBounds>
    {
        public RectRenderBounds(double left, double top, double width, double height)
        {
            super(left, top, width, height);
        }

        public RectRenderBounds(double left, double top, double size)
        {
            super(left, top, size);
        }
        
        public RectRenderBounds offset(double x, double y)
        {
            return new RectRenderBounds(this.left + x, this.top + y, this.width, this.height);
        }
    
        public RectRenderBounds scale(double left, double top, double width, double height)
        {
            return new RectRenderBounds(left, top, width, height);
        }
    }

    public static abstract class AbstractRadialRenderBounds extends AbstractRectRenderBounds
    {
        public final double centerX;
        public final double centerY;
        public final double radius;
        

        public AbstractRadialRenderBounds(double centerX, double centerY, double radius)
        {
            super(centerX - radius, centerY - radius, radius * 2);
            this.centerX = centerX;
            this.centerY = centerY;
            this.radius = radius;
        }
        
        public double centerX() { return centerX; }
        public double centerY() { return centerY; }
        public double radius() { return radius; }
       
    }
    
    public static class RadialRenderBounds extends AbstractRadialRenderBounds implements RenderBounds<RadialRenderBounds>
    {
        private AbstractRadialRenderBounds innerBounds;

        public RadialRenderBounds(double centerX, double centerY, double radius)
        {
            super(centerX, centerY, radius);
        }
        
        public AbstractRadialRenderBounds innerBounds()
        {
            if(this.innerBounds == null)
            {
                this.innerBounds = new RadialRenderBounds(this.centerX, this.centerY, this.radius / 2);
            }
            return this.innerBounds;
        }

        @Override
        public RadialRenderBounds offset(double x, double y)
        {
            return new RadialRenderBounds(this.centerX + x, this.centerY + y, this.radius);
        }

        @Override
        public RadialRenderBounds scale(double left, double top, double width, double height)
        {
            return new RadialRenderBounds(left + width / 2, top + height / 2, width / 2);
        }
    }
    
    public static class PowerRenderBounds extends AbstractRadialRenderBounds implements RenderBounds<PowerRenderBounds>
    {
        public final RectRenderBounds gainLossTextBounds;
        public final RectRenderBounds energyTextBounds;
        public final RectRenderBounds energyLevelBounds;
        
        public PowerRenderBounds(double centerX, double centerY, double radius)
        {
            super(centerX, centerY, radius);
            this.gainLossTextBounds = new RectRenderBounds(left(), centerY - radius / 4, width(), radius / 5);
            this.energyTextBounds = new RectRenderBounds(left(), centerY + radius / 1.8, width(), radius / 2.8);
            this.energyLevelBounds = new RectRenderBounds(left(), centerY + radius / 10, width(), radius / 3);
        }

        @Override
        public PowerRenderBounds offset(double x, double y)
        {
            return new PowerRenderBounds(this.centerX + x, this.centerY + y, this.radius);
        }

        @Override
        public PowerRenderBounds scale(double left, double top, double width, double height)
        {
            return new PowerRenderBounds(left + width / 2, top + height / 2, width / 2);
        }
    }
}