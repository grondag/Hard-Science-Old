package grondag.hard_science.simulator.transport.L1;

import grondag.hard_science.library.world.Location;

public class TransportDevice implements ITransportDevice
{
    private Location location;
    private int species;
    
    @Override
    public Location getLocation()
    {
        return this.location;
    }

    @Override
    public void setLocation(Location loc)
    {
        this.location = loc;
    }

    @Override
    public int getSpecies()
    {
        return this.species;
    }

    @Override
    public void setSpecies(int species)
    {
        this.species = species;
    }

    @Override
    public boolean isFixedLocation()
    {
        // TODO Auto-generated method stub
        return false;
    }

}
