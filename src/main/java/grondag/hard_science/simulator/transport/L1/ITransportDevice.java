package grondag.hard_science.simulator.transport.L1;

import grondag.hard_science.library.world.Location.ILocated;

public interface ITransportDevice extends ILocated
{
    public int getSpecies();
    public void setSpecies(int species);
    public boolean isFixedLocation();
}
