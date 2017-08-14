package grondag.hard_science.library.world;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Location extends BlockPos
{
    public static interface ILocated
    {
        @Nullable
        public Location getLocation();
        
        public default boolean hasLocation()
        {
            return this.getLocation() != null;
        }
    }

    public static void saveToNBT(@Nullable Location loc, @Nonnull NBTTagCompound nbt)
    {
        if(loc != null)
        {
            nbt.setInteger("loc_dim", loc.dimensionID);
            nbt.setLong("loc_pos", PackedBlockPos.pack(loc));
        }
    }
    
    @Nullable
    public static Location fromNBT(@Nullable NBTTagCompound nbt)
    {
        if(nbt != null && nbt.hasKey("loc_pos"))
        {
            int dim = nbt.getInteger("loc_dim");
            long pos = nbt.getLong("loc_pos");
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
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        else if (!(o instanceof Location))
        {
            return false;
        }
        else
        {
            Location loc = (Location)o;

            return      this.getX() == loc.getX()
                    &&  this.getY() == loc.getY()
                    &&  this.getZ() == loc.getZ()
                    &&  this.dimensionID == loc.dimensionID;
        }
    }
    
    
}
