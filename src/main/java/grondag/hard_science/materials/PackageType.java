package grondag.hard_science.materials;

import net.minecraft.util.text.translation.I18n;

public enum PackageType
{
 
    /** Whatever it is doesn't need packaging */
    NAKED,
    
    /** For stuff that can holds it shape in vacuum pack. Common for dusts. */
    VACPACK,
    
    /** In real world known as intermediate bulk container. 
     * Reinforced HDPE, will hold somewhat less than cubic meter. */
    IBC;

    public String toolTip()
    {
        return I18n.translateToLocal("packagetype." + this.name().toLowerCase()).trim();
    }
}