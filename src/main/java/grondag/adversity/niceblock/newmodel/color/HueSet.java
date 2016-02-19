package grondag.adversity.niceblock.newmodel.color;

import grondag.adversity.Adversity;
import grondag.adversity.library.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class HueSet
   {

       private final ColorSet[] colorSets = new ColorSet[HuePosition.values().length];
       
       protected static final double MAX = 999.00;
       
       /**
        * Rotate our color cylinder by this many degrees
        * to tweak which colors we actually get.
        * A purely aesthetic choice.
        */
       protected static final double HUE_SALT = 5.12;

       public HueSet(double hue)
       {
           for(HuePosition hp : HuePosition.values())
           {
               colorSets[hp.ordinal()] = new ColorSet(hue + hp.hueOffset + HUE_SALT);
           }
       }

       public ColorSet getColorSetForHue(HuePosition offset)
       {
           return colorSets[offset.ordinal()];
       }
       
       public class ColorSet
       {
           private final int[] colors = new int[Tint.values().length];

           protected ColorSet(double hue)
           {
               for(Tint tint : Tint.values())
               {
                   double luminance = tint.luminance;
                   double chroma = tint.chroma;
                   
                   // if both are max then make as saturated as possible, then find max lightness
                   if(luminance == MAX && chroma == MAX){
//                       double maxLuminance = 0;
                       double maxChroma = 0;
                       
                       for (double l = 100; l > 20; l--)
                       {
                           for (double c = 100; c > 30; c--)
                           {
                               Color temp = Color.fromHCL(hue, c, l);
                               if(temp.IS_VISIBLE)
                               {
  //                                 maxLuminance = Math.max(maxLuminance, l);
                                   maxChroma = Math.max(maxChroma, c);
                               }
                           }
                       }
//                       if(maxLuminance > maxChroma)
//                       {
//                           luminance = maxLuminance;
//                           chroma = MAX;
//                       }
//                       else
//                       {
                           chroma = maxChroma;
                           luminance = MAX;
//                       }
                   }
                   
                   if(luminance == MAX){
                       for (double trial = 100; trial > 20; trial-= 0.1)
                       {
                           Color temp = Color.fromHCL(hue, chroma, trial);
                           if(temp.IS_VISIBLE)
                           {
                               luminance = trial;
                               break;
                           }
                       }
                   }
                   
                   if(chroma == MAX){
                       for (double trial = 100; trial > 30; trial-= 0.1)
                       {
                           Color temp = Color.fromHCL(hue, trial, luminance);
                           if(temp.IS_VISIBLE)
                           {
                               chroma = trial;
                               break;
                           }
                       }
                   }
                   
                   colors[tint.ordinal()] = Color.fromHCL(hue, chroma, luminance).RGB_int | 0xFF000000;
               }
           }
           
           public int getColor(Tint tint)
           {
               return colors[tint.ordinal()];
           }
       }
       
       public static enum HuePosition
       {
           FAR_LEFT("Far Left", -120),
           NEAR_LEFT("Near Left", -23),
           NONE("None", 0),
           NEAR_RIGHT("Near Right", 23),
           FAR_RIGHT("Far Right", 120),
           OPPOSITE("Opposite", 180);
    
           public final String positionName;
           public final double hueOffset;
           
           private HuePosition(String positionName, double hueOffset)
           {
               this.positionName = positionName;
               this.hueOffset = hueOffset;
           }
       }
       
       public static enum Tint
       {
           GLOW_WHITE("White Glow", 1, MAX),
           WHITE("White", 1.5, 90),
           GREY_BRIGHT("Bright Grey", 1.2, 82),
           GREY_LIGHT("Light Grey", 1.8, 67.75),
           GREY_MID("Mid Grey", 2, 53.5),
           GREY_DEEP("Deep Grey", 2.2, 39.25),
           GREY_DARK("Dark Grey", 2.5, 25),
           
           NEUTRAL_GLOW("Glowing Neutral", 5, MAX),
           NEUTRAL_BRIGHT("Bright Neutral", 5, 82),
           NEUTRAL_LIGHT("Light Neutral", 5, 67.75),
           NEUTRAL_MID("Mid Neutral", 5, 53.5),
           NEUTRAL_DEEP("Deep Neutral", 5, 39.25),
           NEUTRAL_DARK("Dark Neutral", 5, 25),
           
           RICH_GLOW("Glowing Rich", 10, MAX),
           RICH_BRIGHT("Bright Rich", 10, 82),
           RICH_LIGHT("Light Rich", 10, 67.75),
           RICH_MID("Mid Rich", 10, 53.5),
           RICH_DEEP("Deep Rich", 10, 39.25),
           RICH_DARK("Dark Rich", 10, 25),

           BOLD_GLOW("Glowing Bold", 15, MAX),
           BOLD_BRIGHT("Bright Bold", 15, 82),
           BOLD_LIGHT("Light Bold", 15, 67.75),
           BOLD_MID("Mid Bold", 15, 53.5),
           BOLD_DEEP("Deep Bold", 15, 39.25),
           BOLD_DARK("Dark Bold", 15, 25),
           
           ACCENT_GLOW("Glowing Accent", 25, MAX),
           ACCENT_LIGHT("Light Accent", 25, 67.75),
           ACCENT_MID("Mid Accent", 25, 53.5),
           ACCENT_DEEP("Deep Accent", 25, 40),
           CHROMA_MAX("Max Chroma", 40, 74),
           ACCENT_ULTRA("Ultra Accent", MAX, MAX);
           
           public final String tintName;
           public final double chroma;
           public final double luminance;
           
           private Tint(String tintName, double chroma, double luminance)
           {
               this.tintName = tintName;
               this.chroma = chroma;
               this.luminance = luminance;
           }
       }
   }