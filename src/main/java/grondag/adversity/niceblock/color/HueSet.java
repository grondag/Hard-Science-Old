package grondag.adversity.niceblock.color;

import grondag.adversity.library.Color;

public class HueSet
   {
//
//       private final ColorSet[] colorSets = new ColorSet[HuePosition.values().length];
//       
//       public HueSet(double hue)
//       {
//           for(HuePosition hp : HuePosition.values())
//           {
//               colorSets[hp.ordinal()] = new ColorSet(hue + hp.hueOffset);
//           }
//       }

//       public ColorSet getColorSetForHue(HuePosition offset)
//       {
//           return colorSets[offset.ordinal()];
//       }
//       
//       public class ColorSet
//       {
//           private final int[] colors = new int[Tint.values().length];
//
//           protected ColorSet(double hue)
//           {
//               for(Tint tint : Tint.values())
//               {
//                   colors[tint.ordinal()] = Color.fromHCL(hue, tint.chroma, tint.luminance).RGB_int | 0xFF000000;
//               }
//           }
//           
//           public int getColor(Tint tint)
//           {
//               return colors[tint.ordinal()];
//           }
//       }
       
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
       
       public static enum Chroma
       {
           WHITE("White", 1.5),
           GREY("Grey", 2.5),
           NEUTRAL("Neutral", 5),
           RICH_NEUTRAL("Rich Neutral", 10),
           RICH("Rich", 15),
           DEEP("Deep", 25),
           BOLD("Bold", 40),
           EXTRA_BOLD("Extra Bold", 55),
           ULTRA_ACCENT("Ultra Accent", Color.HCL_MAX);
           
           public final String name;
           public final double value;
           
           private Chroma(String chromaName, double chromaValue)
           {
               this.name = chromaName;
               this.value = chromaValue;
           }
       }
       
       public static enum Luminance
       {
           EXTRA_BRIGHT("Extra Bright", 90),
           BRIGHT("Bright", 82),
           LIGHT("Light", 67.75),
           MEDIUM_LIGHT("Medium Light", 53.5),
           MEDIUM_DARK("Medium Dark", 39.25),
           DARK("Dark", 25);
           
           public final String name;
           public final double value;
           
           private Luminance(String luminanceName, double luminanceValue)
           {
               this.name = luminanceName;
               this.value = luminanceValue;
           }
       }
   }