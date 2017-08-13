package grondag.hard_science.simulator.scratch;

import grondag.hard_science.library.world.Location;
import grondag.hard_science.simulator.resource.ILocatedResource;

public abstract class ProcessingResource implements IResource<ProcessingResource>
{
    public static class Located extends ProcessingResource implements ILocatedResource<ProcessingResource>
    {
        protected Location location;
        
        @Override
        public Location getLocation()
        {
            return this.location;
        }
        
        public void setLocation(Location location)
        {
            this.location = location;
        }
    }

}
