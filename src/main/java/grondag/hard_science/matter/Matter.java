package grondag.hard_science.matter;

import static grondag.hard_science.matter.MatterModel.*;
import static grondag.hard_science.matter.PackageType.*;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.library.world.Rotation;
import grondag.hard_science.matter.MatterModel.Naked;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IForgeRegistry;


public enum Matter
{
    TIO2(VACPACK, PACKAGE_STANDARD_VACPACK, new Formula("TiO2", 0xFFFFFFFF), new SymbolBottom("dust", 0xFFFFFFFF)),
    HDPE(NAKED, new Naked("noise_subtle_0_0", MatterColors.HDPE),  new Formula("C2H4", 0xFF999999)),
    CARBON_BLACK(VACPACK, PACKAGE_STANDARD_VACPACK, new FormulaLeft("C", 0xFFFFFFFF), new SymbolRight("dust", 0xFF000000)),
    CARBON_GRAPHITE(VACPACK, PACKAGE_STANDARD_VACPACK, new FormulaLeft("C", 0xFFFFFFFF), new SymbolRight("big_hexagon", 0xFF000000)),
    
    CARBON_FIBER(VACPACK, PACKAGE_STANDARD_VACPACK, new FormulaLeft("C", 0xFFFFFFFF), new SymbolRight2("skinny_diagonal_ridges_seamless", 0xFF000000)),
    
    RAW_MINERAL_DUST(VACPACK, new Naked("noise_strong_0_0", MatterColors.RAW_MINERAL_DUST), new DustModel(0xFFFFFFFF, "dust", "arrow", "funnel")),
    DEPLETED_MINERAL_DUST(VACPACK, new Naked("noise_strong_0_0", MatterColors.DEPLETED_MINERAL_DUST), new DustModel(0xFFFFFFFF, "funnel", "arrow", "dust")),
    
    /** Color of water is inverse of light absorbed by water: wavelength 698nm */
    WATER(IBC, PACKAGE_STANDARD_IBC, new Formula("H2O", 0xFFFFFFFF), new SymbolBottom("drip", MatterColors.WATER)),
    CONSTRUCTION_RESIN_A(IBC, PACKAGE_STANDARD_IBC, new SymbolCenter("mix", MatterColors.RESIN_A)),
    CONSTRUCTION_RESIN_B(IBC, PACKAGE_STANDARD_IBC, new SymbolCenterRotated("mix", MatterColors.RESIN_B, Rotation.ROTATE_180)),
    DYE_CYAN(VACPACK, PACKAGE_STANDARD_VACPACK, new SymbolCenter("dust", MatterColors.CYAN)),
    DYE_MAGENTA(VACPACK, PACKAGE_STANDARD_VACPACK, new SymbolCenter("dust", MatterColors.MAGENTA)),
    DYE_YELLOW(VACPACK, PACKAGE_STANDARD_VACPACK, new SymbolCenter("dust", MatterColors.YELLOW)),
    NANO_LIGHTS(VACPACK, PACKAGE_STANDARD_VACPACK, new SymbolCenter("star_16", 0xFFFFFFFF)),
    UREA(VACPACK, PACKAGE_STANDARD_VACPACK, new SymbolCenter("big_diamond", MatterColors.UREA));

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
