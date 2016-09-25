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
	
	private static Configuration config;

	public static void init(File file) {
		config = new Configuration(file);
		load();
	}
	
	public static void load()
	{
        config.load();
        volcano = new Volcano(config);
        substances = new Substances(config);
        unobtanium = new Unobtanium(config);
        config.save(); 
	}
}
