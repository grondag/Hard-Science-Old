package grondag.hard_science.simulator.transport.L4;

import java.util.List;

import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.TransportStatus;
import grondag.hard_science.simulator.transport.L3.IRoute;

public interface IItinerary<T extends StorageType<T>> extends IRoute<T>
{
    public List<IRoute<T>> subRoutes();
    public IRoute<T> activeRoute();
    public IRoute<T> nextRoute();
    public TransportStatus status();
}
