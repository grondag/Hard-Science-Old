package grondag.hard_science.materials;

import static grondag.hard_science.materials.MatterModel.*;
import static grondag.hard_science.materials.PackageType.*;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.library.world.Rotation;
import grondag.hard_science.materials.MatterModel.Naked;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IForgeRegistry;


public enum Matter
{
    TIO2(VACPACK, PACKAGE_STANDARD_VACPACK, new Formula("TiO2", 0xFFFFFFFF), new SymbolBottom("dust", 0xFFFFFFFF)),//                  FORMULA,    "TiO2",         "dust",     LABL_BLACK, VACPACK,    "material_gradient",     0xFFFFFFFF),
    HDPE(NAKED, new Naked("noise_subtle_0_0", 0xFFDDDDDD),  new Formula("HDPE", 0xFF999999)),//                   NAME,       "HDPE",         "",         LABL_BLACK, NAKED,      "noise_subtle_0_0",     PCKG_WHITE),
    CARBON_BLACK(VACPACK, PACKAGE_STANDARD_VACPACK, new FormulaLeft("C", 0xFFFFFFFF), new SymbolRight("dust", 0xFF000000)),
    CARBON_GRAPHITE(VACPACK, PACKAGE_STANDARD_VACPACK, new FormulaLeft("C", 0xFFFFFFFF), new SymbolRight("big_hexagon", 0xFF000000)),
    
    CARBON_FIBER(VACPACK, PACKAGE_STANDARD_VACPACK, new FormulaLeft("C", 0xFFFFFFFF), new SymbolRight2("skinny_diagonal_ridges_seamless", 0xFF000000)),//           BOTH,       "C",          "skinny_diagonal_ridges_seamless",    LABL_WHITE, VACPACK,    "material_gradient",     0xFFFFFFFF),
    
    RAW_MINERAL_DUST(VACPACK, new Naked("noise_strong_0_0", 0xFF646973), new DustModel(0xFFFFFFFF, "dust", "arrow", "funnel")),
    DEPLETED_MINERAL_DUST(VACPACK, new Naked("noise_strong_0_0", 0xFF737164), new DustModel(0xFFFFFFFF, "funnel", "arrow", "dust")),
    
    /** Color of water is inverse of light absorbed by water: wavelength 698nm */
    WATER(IBC, PACKAGE_STANDARD_IBC, new Formula("H2O", 0xFFFFFFFF), new SymbolBottom("drip", 0xFF00FDFF)),//                  FORMULA,    "H2O",          "drip",     LABL_BLACK, IBC,        "material_gradient",     0xFF00FDFF),
    CONSTRUCTION_RESIN_A(IBC, PACKAGE_STANDARD_IBC, new SymbolCenter("mix", 0xFFa6e3e1)), //   SYMBOL,     "",             "mix",      0xFFa6e3e1, IBC,        "material_gradient",     0xFFFFFFFF),
    CONSTRUCTION_RESIN_B(IBC, PACKAGE_STANDARD_IBC, new SymbolCenterRotated("mix", 0xFFeceea8, Rotation.ROTATE_180)), //   SYMBOL,     "",             "mix",      0xFFeceea8, IBC,        "material_gradient",     0xFFFFFFFF),
    DYE_CYAN(VACPACK, PACKAGE_STANDARD_VACPACK, new SymbolCenter("dust", 0xFF00FFFF)),
    DYE_MAGENTA(VACPACK, PACKAGE_STANDARD_VACPACK, new SymbolCenter("dust", 0xFFFF00FF)),
    DYE_YELLOW(VACPACK, PACKAGE_STANDARD_VACPACK, new SymbolCenter("dust", 0xFFFFFF00)),
    NANO_LIGHTS(VACPACK, PACKAGE_STANDARD_VACPACK, new SymbolCenter("star_16", 0xFFFFFFFF)), //"star_16"
    UREA(VACPACK, PACKAGE_STANDARD_VACPACK, new SymbolCenter("big_diamond", 0xFFDDDDDD)); // diamond

    public final PackageType packageType;
    
    private final MatterCube items[] = new MatterCube[CubeSize.values().length];
    
    public final List<MatterModel> models;
    
    private Matter(PackageType packageType, MatterModel... models)
    {
        this.packageType = packageType;
        
        this.models = FMLCommonHandler.instance().getSide() == Side.CLIENT 
                ? ImmutableList.copyOf(models)
                : null;
    }
    
    public void register(IForgeRegistry<Item> itemReg)
    {
        for(CubeSize size : CubeSize.values())
        {
            MatterCube cube = new MatterCube(this, size);
            this.items[size.ordinal()] = cube;
            itemReg.register(cube);
        }
    }
    
    public MatterCube getCube(CubeSize size)
    {
        return this.items[size.ordinal()];
    }
}
