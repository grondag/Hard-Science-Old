package grondag.adversity.config;

import java.util.HashMap;

import net.minecraftforge.common.config.Configuration;

public class Substances extends HashMap<String, Substance> 
{
    private static final long serialVersionUID = -3480896819857841728L;
	
    public Substances(Configuration config)
    {
       super();
	   String[] allowedTools = { "pickaxe", "shove", "axe" };

       config.addCustomCategoryComment("Substances", "General settings for various in-game materials.");

       this.clear();

       Substance flexstone = new Substance(
               config.getInt("flexstoneHardness", "Substances", 2, 1, 50, ""),
               config.getString("flexstoneHarvestTool", "Substances", "pickaxe", "Tool used to break block", allowedTools),
               config.getInt("flexstoneHarvestLevel", "Substances", 1, 1, 3, ""),
               config.getInt("flexstoneResistance", "Substances", 10, 1, 50, ""));
       this.put("flexstone", flexstone);

       Substance durastone = new Substance(
               config.getInt("durastoneHardness", "Substances", 4, 1, 50, ""),
               config.getString("durastoneHarvestTool", "Substances", "pickaxe", "Tool used to break block", allowedTools),
               config.getInt("durastoneHarvestLevel", "Substances", 2, 1, 3, ""),
               config.getInt("durastoneResistance", "Substances", 50, 1, 50, ""));
       this.put("durastone", durastone);

       Substance hyperstone = new Substance(
               config.getInt("hyperstoneHardness", "Substances", 10, 1, 50, ""),
               config.getString("hyperstoneHarvestTool", "Substances", "pickaxe", "Tool used to break block", allowedTools),
               config.getInt("hyperstoneHarvestLevel", "Substances", 3, 1, 3, ""),
               config.getInt("hyperstoneResistance", "Substances", 200, 1, 50, ""));
       this.put("hyperstone", hyperstone);
       
       Substance basalt = new Substance(
               config.getInt("basaltHardness", "Substances", 2, 1, 50, ""),
               config.getString("basaltHarvestTool", "Substances", "pickaxe", "Tool used to break block", allowedTools),
               config.getInt("basaltHarvestLevel", "Substances", 1, 1, 3, ""),
               config.getInt("basaltResistance", "Substances", 10, 1, 50, ""));
       this.put("basalt", basalt);

       Substance volcanicLava = new Substance(
               config.getInt("volcanicLavaHardness", "Substances", -1, -1, 50, ""),
               config.getString("volcanicLavaHarvestTool", "Substances", "shovel", "Tool used to break block", allowedTools),
               config.getInt("volcanicLavaHarvestLevel", "Substances", 3, 1, 3, ""),
               config.getInt("volcanicLavaResistance", "Substances", 2000, 1, 2000, ""));
       this.put("volcanicLava", volcanicLava);
       
       Substance superwood = new Substance(
               config.getInt("superwoodHardness", "Substances", 2, 1, 50, ""),
               config.getString("superwoodHarvestTool", "Substances", "axe", "Tool used to break block", allowedTools),
               config.getInt("superwoodHarvestLevel", "Substances", 1, 1, 3, ""),
               config.getInt("superwoodResistance", "Substances", 10, 1, 50, ""));
       this.put("superwood", superwood);
    }
}