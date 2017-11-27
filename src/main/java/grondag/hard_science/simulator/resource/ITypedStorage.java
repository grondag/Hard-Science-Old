package grondag.hard_science.simulator.resource;

public interface ITypedStorage<V extends StorageType<V>>
{
    public V storageType();
}