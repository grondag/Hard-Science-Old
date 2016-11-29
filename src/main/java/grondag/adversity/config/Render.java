package grondag.adversity.config;

import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.config.Configuration;

public class Render
{
    public final boolean enableCustomShading;
    public final boolean enableFaceCullingOnFlowBlocks;
    public final float minAmbientLight;
    public final float normalLightFactor;
    public final Vec3d lightingNormal;

    public final static String CATEGORY_NAME = "Render";
    
    public Render(Configuration config)
    {
        config.addCustomCategoryComment(CATEGORY_NAME, "Settings for visual appearance.");

        this.enableCustomShading = config.getBoolean("enableCustomShading", CATEGORY_NAME, true, 
                "Shade blocks from this mod with a uniform light vector. Provides a somewhat better appearance for flowing"
                + " lava blocks (for example) but may appear odd when next to blocks from Vanilla or other mods.");
        
        this.enableFaceCullingOnFlowBlocks = config.getBoolean("enableFaceCullingOnFlowBlocks", CATEGORY_NAME, true, 
                "If true, Dynamic flow block (volcanic lava and basalt) will not render faces occulded by adjacent flow blocks."
                + " True is harder on CPU and easier on your graphics card/chip.  Experiment if you have FPS problems."
                + " Probably won't matter on systems with both a fast CPU and fast graphics.");
        
        this.minAmbientLight = config.getFloat("minAmbientLight", CATEGORY_NAME, 0.3F, 0.0F, 0.9F, 
                "Minimum lighting on any block face with custom shading. Smaller values give deeper shadows.");
        
        normalLightFactor = 0.5F * (1F - minAmbientLight);

        float normalX = config.getFloat("lightingNormalX", CATEGORY_NAME, 0.0F, -1.0F, 1.0F, 
                "X component of ambient light source.");
        
        float normalY = config.getFloat("lightingNormalY", CATEGORY_NAME, 1.0F, -1.0F, 1.0F, 
                "Y component of ambient light source.");
        
        float normalZ = config.getFloat("lightingNormalZ", CATEGORY_NAME, 0.25F, -1.0F, 1.0F, 
                "Z component of ambient light source.");
        
        this.lightingNormal = new Vec3d(normalX, normalY, normalZ).normalize();

    }

}
