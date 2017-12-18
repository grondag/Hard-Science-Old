package grondag.hard_science.machines.support;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.machines.base.AbstractMachine;
import grondag.hard_science.machines.support.MaterialBufferManager.MaterialBufferDelegate;
import grondag.hard_science.materials.CubeSize;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;

public class PolyethyleneFuelCell extends FuelCell
{
    public static final PolyethyleneFuelCell BASIC_1KW = new PolyethyleneFuelCell(CubeSize.FIVE, 1, false);
    public static final PolyethyleneFuelCell ADVANCED_1KW = new PolyethyleneFuelCell(CubeSize.FIVE, 1, true);
    public static final PolyethyleneFuelCell ADVANCED_1MW = new PolyethyleneFuelCell(CubeSize.BLOCK, 1, true);
    
    /**
     * Type or cell
     */
    private PolyethyleneFuelCellType cellType;
    
    /**
     * Size of the catalytic plate(s) we use to convert PE fuel into electricity.
     */
    private CubeSize plateSize;
    
    /**
     * Number of plates in this cell.
     */
    private int plateQuantity;

    public PolyethyleneFuelCell()
    {
        
    }
    
    public PolyethyleneFuelCell(CubeSize plateSize, int plateQuantity, boolean hasThermalCapture)
    {
        this.setup(plateSize, plateQuantity, hasThermalCapture);
    }
    
    public PolyethyleneFuelCell(NBTTagCompound tag)
    {
        this.deserializeNBT(tag);
    }
    
    private void setup(CubeSize plateSize, int plateQuantity, boolean hasThermalCapture)
    {
        this.plateSize = plateSize;
        this.plateQuantity = plateQuantity;
        this.cellType = hasThermalCapture ? PolyethyleneFuelCellType.ADVANCED : PolyethyleneFuelCellType.BASIC;
        
        long maxFuelConsumptionNanoLitersPerSimulatedSecond = plateSize.faceSurfaceArea_micrometer2 * this.plateQuantity * MachinePower.POLYETHYLENE_MAX_CONVERSION_RATE_MICROMETERS / VolumeUnits.CUBIC_MICROMETERS_PER_NANOLITER;
        
        long maxFuelConsumptionNanoLitersPerTick = (long) (maxFuelConsumptionNanoLitersPerSimulatedSecond * TimeUnits.SIMULATED_SECONDS_PER_TICK);
        
        this.setMaxOutputJoulesPerTick((long) (maxFuelConsumptionNanoLitersPerTick * MachinePower.JOULES_PER_POLYETHYLENE_NANOLITER * this.cellType.conversionEfficiency));
    }

    @Override
    protected long provideEnergyImplementation(AbstractMachine mte, long maxOutput, boolean allowPartial, boolean simulate)
    {
        MaterialBufferDelegate fuelBuffer = mte.bufferHDPE();
        
        if(fuelBuffer == null) return 0;
        
        // compute fuel usage
        long fuel = MathHelper.ceil(maxOutput * this.cellType.fuelNanoLitersPerJoule); 
        
        // see what fuel we can get
        fuel = fuelBuffer.use(fuel, true, true);
        
        if(fuel == 0)
        {
            if(!simulate) fuelBuffer.blame();
            return 0;
        }
        
        long energy = (long) (fuel * this.cellType.joulesPerFuelNanoLiters);
        
        if(energy > 0)
        {
            // no energy if partial not allowed and can't meet demand
            if(!allowPartial && energy < maxOutput) return 0;
            
            // consume fuel if not simulating
            if(!simulate) fuelBuffer.use(fuel);
        }
        return energy;
    }   
    
    @Override
    public float powerInputWatts()
    {
        return 0;
    }

    @Override
    public long energyInputCurrentTickJoules()
    {
        return 0;
    }
    
    @Override
    public float maxPowerInputWatts()
    {
        return 0;
    }

    @Override
    public long maxEnergyInputJoulesPerTick()
    {
        return 0;
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        tag.setInteger(ModNBTTag.MACHINE_FUEL_CELL_PLATE_SIZE, this.plateSize.ordinal());
        tag.setInteger(ModNBTTag.MACHINE_FUEL_CELL_PLATE_COUNT, this.plateQuantity);
        tag.setBoolean(ModNBTTag.MACHINE_FUEL_CELL_HAS_THERMAL_CAPTURE, this.cellType == PolyethyleneFuelCellType.ADVANCED);
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        setup(
                Useful.safeEnumFromOrdinal(tag.getInteger(ModNBTTag.MACHINE_FUEL_CELL_PLATE_SIZE), CubeSize.THREE),
                Math.max(1, tag.getInteger(ModNBTTag.MACHINE_FUEL_CELL_PLATE_COUNT)),
                tag.getBoolean(ModNBTTag.MACHINE_FUEL_CELL_HAS_THERMAL_CAPTURE)
        );        
    }
    
    /**
     * Size of the catalytic plate(s) we use to convert PE fuel into electricity.
     */
    public CubeSize plateSize()
    {
        return this.plateSize;
    }
    
    /**
     * Number of plates in this cell.
     */
    public int plateQuantity()
    {
        return this.plateQuantity;
    }
    
    /**
     * If true, cell(s) is/are coupled with a thermoelectric generator to convert waste heat
     * and boost efficiency.
     */
    public PolyethyleneFuelCellType cellType()
    {
        return this.cellType;
    }
}