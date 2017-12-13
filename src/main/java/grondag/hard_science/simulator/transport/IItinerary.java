package grondag.hard_science.simulator.transport;

import java.util.List;

import grondag.hard_science.simulator.resource.StorageType;

public interface IItinerary<T extends StorageType<T>> extends IRoute<T>
{
    public List<IRoute<T>> subRoutes();
    public IRoute<T> activeRoute();
    public IRoute<T> nextRoute();
    public TransportStatus status();
}
