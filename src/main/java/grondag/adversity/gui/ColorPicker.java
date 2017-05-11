package grondag.adversity.gui;

import grondag.adversity.niceblock.color.BlockColorMapProvider;
import grondag.adversity.niceblock.color.ColorMap;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;
import grondag.adversity.niceblock.color.HueSet;
import grondag.adversity.niceblock.color.NiceHues;
import grondag.adversity.niceblock.color.HueSet.Chroma;
import grondag.adversity.niceblock.color.HueSet.Luminance;
import grondag.adversity.niceblock.color.NiceHues.Hue;

public class ColorPicker
{
    private Hue selectedHue = NiceHues.Hue.AZURE;
    
    private int colorMapID = 0;
    
    private double top;
//    private double left;
//    private double size;
    private double centerX;
    private double centerY;
    private double radiusInner;
    private double radiusOuter;
    private double arc = 360 / NiceHues.Hue.values().length;
    
    private double gridLeft;
    private double gridTop;
    private double gridIncrement;
    
    public Hue getHue() { return selectedHue; }
    public void setHue(Hue h) { selectedHue = h; }
    public int getColorMapID() { return colorMapID; }
    public void setColorMapID( int colorMapID ) 
    { 
        this.colorMapID = colorMapID;
        this.selectedHue = BlockColorMapProvider.INSTANCE.getColorMap(colorMapID).hue;
    }
    
    public ColorPicker(double left, double top, double diameter)
    {
        resize(left, top, diameter);
    }
    
    public void resize(double left, double top, double diameter)
    {
//        this.left = left;
        this.top = top;
//        this.size = size;
        
        radiusOuter = diameter / 2.0;
        centerX = left + radiusOuter;
        centerY = top + radiusOuter;
        radiusInner = radiusOuter * 0.85;
        
        this.gridIncrement = radiusInner * 2 / Luminance.values().length;

        this.gridLeft = left + diameter + 10;
        this.gridTop = this.centerY - radiusInner;
    }
    
    public void drawControl(int mouseX, int mouseY, float partialTicks)
    {
      
      for(int h = 0; h < Hue.values().length; h++)
      {
          double radius = (h == this.selectedHue.ordinal()) ? radiusOuter : radiusInner;
          double arcStart = Math.toRadians(arc * h);
          double arcEnd = Math.toRadians(arc * (h + 1));
          
          double x0 =  centerX;// + Math.sin(arcStart) * radiusInner;
          double y0 =  centerY;// + Math.cos(arcStart) * radiusInner;
          
          double x1 =  centerX + Math.sin(arcStart) * radius;
          double y1 =  centerY + Math.cos(arcStart) * radius;

          double x2 =  centerX + Math.sin(arcEnd) * radius;
          double y2 =  centerY + Math.cos(arcEnd) * radius;
          
          double x3 =  centerX;// + Math.sin(arcEnd) * radiusInner;
          double y3 =  centerY;// + Math.cos(arcEnd) * radiusInner;
          
          GuiUtil.drawQuad(x0, y0, x1, y1, x2, y2, x3, y3, Hue.values()[h].hueSample());
      }
      
      double left;
      double top = this.gridTop;
      double right;
      double bottom;
      for(Luminance l : Luminance.values())
      {
          bottom = top + this.gridIncrement;
          left = this.gridLeft;
          for(Chroma c : Chroma.values())
          {
              right = left + this.gridIncrement;
              ColorMap colormap = BlockColorMapProvider.INSTANCE.getColorMap(selectedHue, c, l);
              if(colormap != null)
              {
                  GuiUtil.drawRect(left, top, right, bottom, colormap.getColor(EnumColorMap.BASE));
              }
              left = right;
          }
          top = bottom;
      }
      
      ColorMap selectedColormap = BlockColorMapProvider.INSTANCE.getColorMap(this.colorMapID);
      
      double sLeft = this.gridLeft + selectedColormap.chroma.ordinal() * this.gridIncrement;
      double sTop = this.gridTop + selectedColormap.luminance.ordinal() * this.gridIncrement;
      
      GuiUtil.drawRect(sLeft - 1, sTop - 1, sLeft + this.gridIncrement + 1, sTop + this.gridIncrement + 1, 0xFFFFFFFF);
      GuiUtil.drawRect(sLeft - 0.5, sTop - 0.5, sLeft + this.gridIncrement + 0.5, sTop + this.gridIncrement + 0.5, selectedColormap.getColor(EnumColorMap.BASE));
      
//      for(int j = 0; j < Hue.values().length; j++)
//      {
//          int color = BlockColorMapProvider.INSTANCE.getColorMap(j * BlockColorMapProvider.INSTANCE.COLORS_PER_HUE + i).getColor(EnumColorMap.BASE);
//          float x = colorWidth * i + left;
//          float y = colorHeight * j + top;
//          drawRect(x, y, x + colorWidth, y + colorHeight, color);
//      }
    }
    
    protected void handleMouseInput(int mouseX, int mouseY)
    {
        
        double distance = Math.sqrt((Math.pow(mouseX - centerX, 2) + Math.pow(mouseY - centerY, 2)));
        
        if(distance < this.radiusOuter + 2)
        {
            double angle = Math.toDegrees(Math.atan2(mouseX - centerX, mouseY - centerY));
            if(angle < 1) angle += 360;
            int index = (int) Math.floor(angle / this.arc);
            
            if(index >= Hue.values().length) index = 0;
            
            this.selectedHue = Hue.values()[index];
        }
        else if(mouseX >= this.gridLeft)
        {
            int l = (int) Math.floor((mouseY - this.gridTop) / this.gridIncrement);
            int c = (int) Math.floor((mouseX - this.gridLeft) / this.gridIncrement);
            
            if(l >= 0 && l <  HueSet.Luminance.values().length 
                    && c >= 0 && c < HueSet.Chroma.values().length )
            {
                ColorMap testMap = BlockColorMapProvider.INSTANCE.getColorMap(
                        this.selectedHue, 
                        HueSet.Chroma.values()[c], 
                        HueSet.Luminance.values()[l]);
                
                if(testMap != null) this.colorMapID = testMap.ordinal;
            }
        }
//        window.mouseMovedOrUp(mouseX, mouseY, state);
    }
    
}
