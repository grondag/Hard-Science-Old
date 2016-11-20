package grondag.adversity.config;

import java.io.File;
import net.minecraftforge.common.config.Configuration;

public class Config {

	private static Volcano volcano;
	public static Volcano volcano() { return volcano; }
	
	private static Substances substances;
	public static Substances substances() { return substances; }

	private static Unobtanium unobtanium;
	public static Unobtanium unobtanium() { return unobtanium; }
	
	private static Render render;
	public static Render render() { return render; }
	
	public static Configuration instance;

	public static void init(File file) {
		instance = new Configuration(file);
		load();
	}
	
	private static void load()
	{
        instance.load();
        volcano = new Volcano(instance);
        substances = new Substances(instance);
        unobtanium = new Unobtanium(instance);
        render = new Render(instance);
        instance.save(); 
	}
	
	public static void reload()
	{
	    if(instance.hasChanged())
        {
	        render = new Render(instance);
	        instance.save(); 
        }
	}
}
