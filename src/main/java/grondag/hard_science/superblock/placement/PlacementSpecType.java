package grondag.hard_science.superblock.placement;

import java.util.function.Supplier;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.superblock.placement.AbstractPlacementSpec.AdditivePlacementSpec;
import grondag.hard_science.superblock.placement.AbstractPlacementSpec.CSGPlacementSpec;
import grondag.hard_science.superblock.placement.AbstractPlacementSpec.CuboidPlacementSpec;
import grondag.hard_science.superblock.placement.AbstractPlacementSpec.PredicatePlacementSpec;
import grondag.hard_science.superblock.placement.AbstractPlacementSpec.SinglePlacementSpec;
import grondag.hard_science.superblock.placement.AbstractPlacementSpec.SurfacePlacementSpec;
import net.minecraft.nbt.NBTTagCompound;

public enum PlacementSpecType
{
    SINGLE(new Supplier<AbstractPlacementSpec>() { public AbstractPlacementSpec 
        get() {return new SinglePlacementSpec(); }}),
    
    SURFACE(new Supplier<AbstractPlacementSpec>() { public AbstractPlacementSpec 
        get() {return new SurfacePlacementSpec(); }}),
    
    ADDITIVE(new Supplier<AbstractPlacementSpec>() { public AbstractPlacementSpec 
        get() {return new AdditivePlacementSpec(); }}),
    
    PREDICATE(new Supplier<AbstractPlacementSpec>() { public AbstractPlacementSpec 
        get() {return new PredicatePlacementSpec(); }}),
    
    CUBOID(new Supplier<AbstractPlacementSpec>() { public AbstractPlacementSpec 
        get() {return new CuboidPlacementSpec(); }}),
    
    CSG(new Supplier<AbstractPlacementSpec>() { public AbstractPlacementSpec 
        get() {return new CSGPlacementSpec(); }});

    private final Supplier<AbstractPlacementSpec> supplier;
    
    private PlacementSpecType(Supplier<AbstractPlacementSpec> supplier)
    {
        this.supplier = supplier;
    }
    
    public static NBTTagCompound serializeSpec(AbstractPlacementSpec spec)
    {
        NBTTagCompound result = new NBTTagCompound();
        Useful.saveEnumToTag(result, ModNBTTag.PLACEMENT_SPEC_TYPE, spec.specType());
        spec.serializeNBT(result);
        return result;
    }
    
    public static AbstractPlacementSpec deserializeSpec(NBTTagCompound tag)
    {
        AbstractPlacementSpec result = Useful.safeEnumFromTag(tag, 
                ModNBTTag.PLACEMENT_SPEC_TYPE, PlacementSpecType.SINGLE).supplier.get();
        if(result != null) result.deserializeNBT(tag);
        return result;
    }
    
   
}
