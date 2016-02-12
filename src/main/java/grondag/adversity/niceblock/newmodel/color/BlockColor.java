package grondag.adversity.niceblock.newmodel.color;

import grondag.adversity.library.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class BlockColor
   {

        public final String colorName;
        public final ColorVariant base;
        public final ColorVariant opposite;
        public final ColorVariant farLeft;
        public final ColorVariant farRight;
        public final ColorVariant nearLeft;
        public final ColorVariant nearRight;
       
       private TextureAtlasSprite particleTexture;
       
       public BlockColor(String name, double hue, double chroma, double luminance){
           
           this.colorName = name;
           base = new ColorVariant(hue, chroma, luminance);
           opposite = new ColorVariant(hue + 180.0, chroma, luminance);
           farLeft = new ColorVariant(hue - 120.0, chroma, luminance);
           farRight = new ColorVariant(hue + 120.0, chroma, luminance);
           nearLeft = new ColorVariant(hue - 23.0, chroma, luminance);
           nearRight = new ColorVariant(hue + 23.0, chroma, luminance);
       }
       
       public String getParticleTextureName(){
           return "adversity:blocks/raw_0_0";
       }
       
       public TextureAtlasSprite getParticleTexture()
       {
           if(particleTexture == null)
           {
               particleTexture = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(getParticleTextureName());
           }
           return particleTexture;
       }
       
       public class ColorVariant
       {
           public final Color normal;
           
           /** similar saturation, a little brighter or darker **/
           public final Color shade1;
           
           /** similar saturation, brighter or darker **/
           public final Color shade2;
//           
//           /** a little more/less saturated, a little brighter or darker **/
//           public final Color detail1;
//           
//           /** a little more/less saturated, brighter or darker **/
//           public final Color detail2;
//           
//           /** much more saturated, much brighter or darker **/
//           public final Color accent1;
//           
//           /** much more saturated, much brighter or darker **/
//           public final Color accent2;
//           
//           /** use for illuminated surfaces and glow effects **/
//           public final Color glow;


           protected ColorVariant(double hue, double chroma, double luminance){
               
               normal = Color.fromHCL(hue, chroma, luminance);
               
               Color temp = Color.fromHCL(hue, chroma + 5.0, luminance - 12.0);
               if(!temp.IS_VISIBLE) temp = Color.fromHCL(hue, chroma + 5.0, luminance + 12.0);
               if(!temp.IS_VISIBLE) temp = Color.fromHCL(hue, chroma - 5.0, luminance - 12.0);
               if(!temp.IS_VISIBLE) temp = Color.fromHCL(hue, chroma - 5.0, luminance + 12.0);
               shade1 = temp;
               
               temp = Color.fromHCL(hue, chroma + 8.0, luminance + 20);
               if(!temp.IS_VISIBLE) temp = Color.fromHCL(hue, chroma + 8.0, luminance - 20.0);
               if(!temp.IS_VISIBLE) temp = Color.fromHCL(hue, chroma - 8.0, luminance - 20.0);
               if(!temp.IS_VISIBLE) temp = Color.fromHCL(hue, chroma - 8.0, luminance + 20.0);
               shade2 = temp;
               
           }

       }
       
       public static enum HueOffset
       {
           FAR_LEFT("Far Left"),
           NEAR_LEFT("Near Left"),
           NONE("None"),
           NEAR_RIGHT("Near Right"),
           FAR_RIGHT("Far Right"),
           OPPOSITE("Opposite");
           
           public final String offsetName;
       
           private HueOffset(String offsetName)
           {
               this.offsetName = offsetName;
           }
       }
       
       public static enum Tint
       {
           WHITE("White"),
           GREY_LIGHT("Light Grey"),
           GREY_MID("Mid Grey"),
           GREY_DARK("Dark Grey"),
           NEUTRAL_LIGHT("Light Neutral"),
           NEUTRAL_MID("Mid Neutral"),
           NEUTRAL_DARK("Dark Neutral");
           
           public final String tintName;
           
           private Tint(String tintName)
           {
               this.tintName = tintName;
           }
       }
   }