package grondag.hard_science.library.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Location extends BlockPos
{
    public static interface ILocated
    {
        public Location getLocation();
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
