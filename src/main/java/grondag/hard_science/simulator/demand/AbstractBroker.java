package grondag.hard_science.simulator.demand;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import com.google.common.collect.ComparisonChain;

import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import grondag.hard_science.simulator.resource.StorageType;

public class AbstractBroker<V extends StorageType<V>> implements IBroker<V>
{
    protected final BrokerManager brokerManager;
    
    protected SimpleUnorderedArrayList<IProducer<V>> producers = new SimpleUnorderedArrayList<IProducer<V>>();
    protected ConcurrentSkipListSet<IProcurementRequest<V>> requests = new ConcurrentSkipListSet<IProcurementRequest<V>>(new Comparator<IProcurementRequest<V>>() 
            {
                @Override
                public int compare(IProcurementRequest<V> o1, IProcurementRequest<V> o2)
                {
                    return ComparisonChain.start()
                            .compare(o1.job().getPriority().ordinal(), o2.job().getPriority().ordinal())
                            .compare(o1.getId(), o2.getId())
                            .result();
                }
            });
    
    private Set<IProcurementRequest<V>> requestsReadOnly = Collections.unmodifiableSet(this.requests);

    public AbstractBroker(BrokerManager brokerManager)
    {
        super();
        this.brokerManager = brokerManager;
    }

    @Override
    public synchronized void registerRequest(IProcurementRequest<V> request)
    {
        if(this.requests.add(request))
        {
            this.notifyNewDemand(request);
        }
    }

    @Override
    public synchronized void unregisterRequest(IProcurementRequest<V> request)
    {
        this.requests.remove(request);
    }

    @Override
    public synchronized void notifyPriorityChange(IProcurementRequest<V> request)
    {
        // remove and re-add to update sort
        this.requests.remove(request);
        this.requests.add(request);
    }

    @Override
    public synchronized void notifyNewDemand(IProcurementRequest<V> request)
    {
        for(IProducer<V> p : this.producers)
        {
            p.notifyNewDemand(this, request);
        }
    }

    @Override
    public Collection<IProcurementRequest<V>> openRequests()
    {
        return this.requestsReadOnly;
    }

    @Override
    public synchronized void registerProducer(IProducer<V> producer)
    {
        this.producers.addIfNotPresent(producer);
    }

    @Override
    public synchronized void unregisterProducer(IProducer<V> producer)
    {
        this.producers.removeIfPresent(producer);
    }
}