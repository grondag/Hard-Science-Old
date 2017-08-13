package grondag.hard_science.simulator.resource;

import java.util.List;

import grondag.hard_science.simulator.domain.Domain;
import grondag.hard_science.simulator.job.IExecutionManager;
import grondag.hard_science.simulator.take2.IResource;

public interface IResourceManager<T extends IResource<T>> extends IExecutionManager
{
    public Domain getDomain();
    
    
    
    public IResourceJob<T> request(IResouceRequest<T> search); 
}
