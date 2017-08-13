package grondag.hard_science.simulator.resource.processing;

import grondag.hard_science.simulator.domain.Location;
import grondag.hard_science.simulator.resource.ILocatedResource;
import grondag.hard_science.simulator.take2.IResource;

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
