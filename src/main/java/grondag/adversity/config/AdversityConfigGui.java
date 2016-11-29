package grondag.adversity.config;

import grondag.adversity.Adversity;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;

public class AdversityConfigGui extends GuiConfig {

        public AdversityConfigGui(GuiScreen parentScreen) 
        {
            super(parentScreen, new ConfigElement(Config.instance.getCategory(Render.CATEGORY_NAME)).getChildElements(),
                    Adversity.MODID, false, false, "Adversity Render Settings");
        }
    }