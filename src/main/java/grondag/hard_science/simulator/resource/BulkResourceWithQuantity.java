package grondag.hard_science.simulator.resource;

import java.util.Comparator;

import javax.annotation.Nonnull;

import grondag.hard_science.matter.VolumeUnits;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeBulk;
import net.minecraft.nbt.NBTTagCompound;

public class BulkResourceWithQuantity extends AbstractResourceWithQuantity<StorageTypeBulk>
{

    public BulkResourceWithQuantity(@Nonnull BulkResource resource, long nanoLiters)
    {
        super(resource, nanoLiters);
    }
    
    public BulkResourceWithQuantity()
    {
        super();
    }
    
    public BulkResourceWithQuantity(NBTTagCompound tag)
    {
        super(tag);
    }
    
    
    @Override
    public StorageTypeBulk storageType()
    {
        return StorageType.PRIVATE;
    }
    
    public static final Comparator<? super AbstractResourceWithQuantity<StorageTypeBulk>> 
        SORT_BY_QTY_ASC = new Comparator<AbstractResourceWithQuantity<StorageTypeBulk>>()
    {
        @Override
        public int compare(AbstractResourceWithQuantity<StorageTypeBulk> o1, AbstractResourceWithQuantity<StorageTypeBulk> o2)
        {  
            if(o1 == null)
            {
                if(o2 == null) 
                {
                    return 0;
                }
                return  1;
            }
            else if(o2 == null) 
            {
                return -1;
            }
            int result = Long.compare(o1.quantity, o2.quantity);
            return result == 0 ? o1.resource().displayName().compareTo(o2.resource().displayName()) : result;
        }
    };
    
    public static final Comparator<? super AbstractResourceWithQuantity<StorageTypeBulk>> 
        SORT_BY_QTY_DESC = new Comparator<AbstractResourceWithQuantity<StorageTypeBulk>>()
    {
        @Override
        public int compare(AbstractResourceWithQuantity<StorageTypeBulk> o1, AbstractResourceWithQuantity<StorageTypeBulk> o2)
        {
            return SORT_BY_QTY_ASC.compare(o2, o1);
        }
        
    };
    
    @Override
    public String toString()
    {
        return String.format("%s x %,dL", this.resource().toString(), VolumeUnits.nL2Liters(this.getQuantity()));
    }
}
