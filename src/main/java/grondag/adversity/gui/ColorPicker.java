package grondag.adversity.gui;

import grondag.adversity.niceblock.color.NiceHues;
import grondag.adversity.niceblock.color.NiceHues.Hue;

public class ColorPicker
{
    private Hue selectedHue = NiceHues.Hue.AZURE;
    
    private double top;
    private double left;
    private double size;
    private double centerX;
    private double centerY;
    private double radiusInner;
    private double radiusOuter;
    private double arc = 360 / NiceHues.Hue.values().length;
    
    public Hue getHue() { return selectedHue; }
    public void setHue(Hue h) { selectedHue = h; }
    
    public ColorPicker(double left, double top, double size)
    {
        resize(left, top, size);
    }
    
    public void resize(double left, double top, double size)
    {
        this.left = left;
        this.top = top;
        this.size = size;
        
        radiusOuter = size / 2.0;
        centerX = left + radiusOuter;
        centerY = top + radiusOuter;
        radiusInner = radiusOuter * 0.85;
    }
    
    public void drawControl(int mouseX, int mouseY, float partialTicks)
    {
      
      for(int h = 0; h < Hue.values().length; h++)
      {
          double arcStart = Math.toRadians(arc * h + 0.25);
          double arcEnd = Math.toRadians(arc * (h + 1) - 0.25);
          
          double x0 =  centerX + Math.sin(arcStart) * radiusInner;
          double y0 =  centerY + Math.cos(arcStart) * radiusInner;
          
          double x1 =  centerX + Math.sin(arcStart) * radiusOuter;
          double y1 =  centerY + Math.cos(arcStart) * radiusOuter;

          double x2 =  centerX + Math.sin(arcEnd) * radiusOuter;
          double y2 =  centerY + Math.cos(arcEnd) * radiusOuter;
          
          double x3 =  centerX + Math.sin(arcEnd) * radiusInner;
          double y3 =  centerY + Math.cos(arcEnd) * radiusInner;
          
          GuiUtil.drawQuad(x0, y0, x1, y1, x2, y2, x3, y3, Hue.values()[h].hueSample());
      }
      
      GuiUtil.drawRect(centerX - 10, centerY - 10, centerX + 10, centerY + 10, this.selectedHue.hueSample());
      
//      for(int j = 0; j < Hue.values().length; j++)
//      {
//          int color = BlockColorMapProvider.INSTANCE.getColorMap(j * BlockColorMapProvider.INSTANCE.COLORS_PER_HUE + i).getColor(EnumColorMap.BASE);
//          float x = colorWidth * i + left;
//          float y = colorHeight * j + top;
//          drawRect(x, y, x + colorWidth, y + colorHeight, color);
//      }
    }
    
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        
        double distance = Math.sqrt((Math.pow(mouseX - centerX, 2) + Math.pow(mouseY - centerY, 2)));
        
        if(distance < this.radiusInner - 2 || distance > this.radiusOuter + 2) return;
        
        double angle = Math.toDegrees(Math.atan2(mouseX - centerX, mouseY - centerY));
        if(angle < 1) angle += 360;
        int index = (int) Math.floor(angle / this.arc);
        
        if(index >= Hue.values().length) index = 0;
        
        this.selectedHue = Hue.values()[index];
        
//        window.mouseMovedOrUp(mouseX, mouseY, state);
    }
    
}
