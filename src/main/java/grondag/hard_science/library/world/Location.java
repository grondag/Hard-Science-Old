package grondag.hard_science.library.world;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.Useful;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class Location extends BlockPos
{
    public static interface ILocated
    {
        @Nullable
        public Location getLocation();
        
        public void setLocation(@Nullable Location loc);
        
        public default void setLocation(BlockPos pos, World world)
        {
            this.setLocation(new Location(pos, world));
        }
        
        public default boolean hasLocation()
        {
            return this.getLocation() != null;
        }
        
        public default void serializeLocation(NBTTagCompound tag)
        {
            saveToNBT(this.getLocation(), tag);
        }
        
        public default void deserializeLocation(NBTTagCompound tag)
        {
            this.setLocation(fromNBT(tag));
        }
    }

    public static void saveToNBT(@Nullable Location loc, @Nonnull NBTTagCompound nbt)
    {
        if(loc != null)
        {
            nbt.setInteger(ModNBTTag.LOCATION_DIMENSION, loc.dimensionID);
            nbt.setLong(ModNBTTag.LOCATION_POSITION, PackedBlockPos.pack(loc));
        }
    }
    
    @Nullable
    public static Location fromNBT(@Nullable NBTTagCompound nbt)
    {
        if(nbt != null && nbt.hasKey(ModNBTTag.LOCATION_POSITION))
        {
            int dim = nbt.getInteger(ModNBTTag.LOCATION_DIMENSION);
            long pos = nbt.getLong(ModNBTTag.LOCATION_POSITION);
            return new Location(PackedBlockPos.getX(pos), PackedBlockPos.getY(pos), PackedBlockPos.getZ(pos), dim);
        }
        else
        {
            return null;
        }
    }
    
    private final int dimensionID;
    
    public Location(int x, int y, int z, int dimensionID)
    {
        super(x, y, z);
        this.dimensionID = dimensionID;
    }
    
    public Location(int x, int y, int z, World world)
    {
        this(x, y, z, world.provider.getDimension());
    }
    
    public Location(BlockPos pos, World world)
    {
        this(pos.getX(), pos.getY(), pos.getZ(), world.provider.getDimension());
    }
    
    public Location(BlockPos pos, int dimensionID)
    {
        this(pos.getX(), pos.getY(), pos.getZ(), dimensionID);
    }
    
    public int dimensionID()
    {
        return this.dimensionID;
    }
    
    public World world()
    {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(this.dimensionID);
    }
    
    @Override
    public boolean equals(Object o)
    {
        if( o == null || !(o instanceof Location)) return false;
        Location other = (Location)o;
        return      this.getX() == other.getX()
                &&  this.getY() == other.getY()
                &&  this.getZ() == other.getZ()
                &&  this.dimensionID == other.dimensionID;
    }
    
    @Override
    public int hashCode()
    {
        return (int) Useful.longHash((long)super.hashCode() | (this.dimensionID << 32));
    }
}
