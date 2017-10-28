//package grondag.hard_science.simulator.base;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//
//import org.apache.commons.lang3.tuple.Pair;
//
//import grondag.hard_science.Log;
//import grondag.hard_science.library.serialization.IReadWriteNBT;
//import grondag.hard_science.library.serialization.ModNBTTag;
//import grondag.hard_science.library.world.IntegerAABB;
//import grondag.hard_science.library.world.Location;
//import grondag.hard_science.library.world.Location.ILocated;
//import grondag.hard_science.simulator.IExecutor;
//import grondag.hard_science.simulator.base.DomainManager.Domain;
//import grondag.hard_science.simulator.base.DomainManager.IDomainMember;
//import grondag.hard_science.simulator.persistence.IDirtListener;
//import grondag.hard_science.simulator.persistence.IDirtListener.IDirtNotifier;
//import grondag.hard_science.superblock.placement.PlacementResult;
//import net.minecraft.entity.player.EntityPlayer;
//import net.minecraft.init.Items;
//import net.minecraft.item.ItemStack;
//import net.minecraft.nbt.NBTTagCompound;
//import net.minecraft.nbt.NBTTagList;
//import net.minecraft.util.math.BlockPos;
//
//public class ConstructionManager implements IDirtNotifier, IDirtListener, IDomainMember, IReadWriteNBT
//{
//    private boolean isDirty;
//    
//    protected IDirtListener dirtListener = NullDirtListener.INSTANCE;
//    protected Domain domain;
//    protected final HashSet<ConstructionRequest> requests = new HashSet<ConstructionRequest>();
//    
//    public synchronized void addRequest(ConstructionRequest request)
//    {
//        if(requests.contains(request))
//        {
//            Log.warn("Construction manager received request to add request it already has.  This is a bug.");
//            return;
//        }
//        
//        this.requests.add(request);
//        this.domain.domainManager().requestIndex().register(request);
//        
//        this.setDirty();
//    }
//    
//    public synchronized void removeRequest(ConstructionRequest request)
//    {
//        if(!requests.contains(request))
//        {
//            Log.warn("Construction manager received request to remove request it doesn't have.  This is a bug.");
//            return;
//        }
//        
//        this.domain.domainManager().requestIndex().unregister(request);
//        this.requests.remove(request);
//        this.setDirty();
//    }
//    
//    @Override
//    public void deserializeNBT(NBTTagCompound tag)
//    {
//        this.requests.clear();
//        NBTTagList nbtRequests = tag.getTagList(ModNBTTag.CONSTRUCTION_MANAGER_REQUESTS, 10);
//        if( nbtRequests != null && !nbtRequests.hasNoTags())
//        {
//            for (int i = 0; i < nbtRequests.tagCount(); ++i)
//            {
//                NBTTagCompound subTag = nbtRequests.getCompoundTagAt(i);
//                if(subTag != null)
//                {
//                    ConstructionRequest request = new ConstructionRequest(subTag);
//                    this.addRequest(request);
//                }
//            }   
//        }        
//    }
//
//    @Override
//    public void serializeNBT(NBTTagCompound tag)
//    {
//        if(!this.requests.isEmpty())
//        {
//            NBTTagList nbtRequests = new NBTTagList();
//            
//            for(ConstructionRequest request : this.requests)
//            {
//                nbtRequests.appendTag(request.serializeNBT());
//            }
//            tag.setTag(ModNBTTag.CONSTRUCTION_MANAGER_REQUESTS, nbtRequests);
//        }        
//    }
//
//    protected void setDomain(Domain domain)
//    {
//        this.domain = domain;
//        this.dirtListener = domain == null ? null : domain.getDirtListener();
//    }
//    
//    @Override
//    public Domain getDomain()
//    {
//        return this.domain;
//    }
//
//    @Override
//    public void setDirty()
//    {
//        // TODO Auto-generated method stub
//        
//    }
//
//    @Override
//    public void setDirtKeeper(IDirtKeeper listener)
//    {
//        // TODO Auto-generated method stub
//        
//    }
//    
//   
//    
//    public class ConstructionRequest implements IReadWriteNBT, ILocated, IDomainMember
//    {
//        
//        public class ConstructionEntry
//        {
//            private BlockPos pos;
//            private ItemStack stack;
//            private int excavationRequestID = IIdentified.UNASSIGNED_ID;
//            private int fabricationRequestID = IIdentified.UNASSIGNED_ID;
//            private int placementRequestID = IIdentified.UNASSIGNED_ID;
//        }
//        
//        /**
//         * How to organize?
//         * excavation, fab and placement in different collections with links between
//         *   OR
//         * all in same collection with different index collections
//         * 
//         */
//        
//        /**
//         * Identifies player who made the request
//         */
//        private String userName;
//        
//        /** 
//         * Identifies world and indicates approximate center of the request.
//         */
//        private Location location;
//        
//        private IntegerAABB excavationBounds = null;
//        private IntegerAABB placementBounds = null;
//        
//        private ArrayList<BlockPos> excavations = null;
//        
//        private HashMap<BlockPos, ItemStack> placements = null;
//         
//        private int id;
//        
//        private ConstructionRequest(NBTTagCompound tag)
//        {
//            this.deserializeID(tag);
//        }
//        
//        private ConstructionRequest(EntityPlayer player, PlacementResult placement)
//        {
//            this.userName = player.getName();
//            if(!placement.placements().isEmpty())
//            {
//                boolean isExcavation = placement.event().isExcavation;
//                IntegerAABB.Builder exAABB = new IntegerAABB.Builder();
//                IntegerAABB.Builder plAABB = new IntegerAABB.Builder();
//                
//                ArrayList<BlockPos> ex = new ArrayList<BlockPos>();
//                HashMap<BlockPos, ItemStack> pl = new HashMap<BlockPos, ItemStack>();
//                
//                for(Pair<BlockPos, ItemStack >  pair : placement.placements())
//                {
//                    ItemStack stack = pair.getRight();
//                    BlockPos pos = pair.getLeft();
//                    if(isExcavation || stack.isEmpty() || stack.getItem() == Items.AIR)
//                    {
//                        ex.add(pos);
//                        exAABB.add(pos);
//                    }
//                    else
//                    {
//                        pl.put(pos, stack);
//                        plAABB.add(pos);
//                    }
//                }
//                
//                if(!ex.isEmpty()) this.excavations = ex;
//                if(!pl.isEmpty()) this.placements = pl;
//                this.excavationBounds = exAABB.build();
//                this.placementBounds = plAABB.build();
//            }
//            
//            IntegerAABB combinedBounds = this.placementBounds == null 
//                    ? this.excavationBounds 
//                    : this.excavationBounds == null 
//                        ? this.placementBounds
//                        : this.excavationBounds.union(placementBounds);
//            
//            BlockPos center = combinedBounds == null ? placement.blockPos() : combinedBounds.getCenter();
//            this.location = new Location(center, player.world);
//        }
//        
//        public IntegerAABB excavationBounds() { return this.excavationBounds; }
//        public IntegerAABB placementBounds() { return this.placementBounds; }
//        public String userName() { return this.userName; }
//        public Location location() { return this.location; }
//        
//        @Override
//        public int getIdRaw()
//        {
//            return this.id;
//        }
//
//        @Override
//        public void setId(int id)
//        {
//            this.id = id;
//        }
//
//        @Override
//        public AssignedNumber idType()
//        {
//            return AssignedNumber.REQUEST;
//        }
//
//        @Override
//        public void deserializeNBT(NBTTagCompound tag)
//        {
//            this.deserializeID(tag);
//            this.location = Location.fromNBT(tag);
//            this.userName = tag.getString(ModNBTTag.DOMAIN_USER_NAME);
//            
//        }
//
//        @Override
//        public void serializeNBT(NBTTagCompound tag)
//        {
//            this.serializeID(tag);
//            Location.saveToNBT(this.location, tag);
//            tag.setString(ModNBTTag.DOMAIN_USER_NAME, this.userName);
//        }
//
//        @Override
//        public Location getLocation()
//        {
//            return this.location;
//        }
//
//        @Override
//        public void setLocation(Location loc)
//        {
//            this.location = loc;
//            setDirty();
//        }
//
//        @Override
//        public RequestStatus getStatus()
//        {
//            // TODO Auto-generated method stub
//            return null;
//        }
//
//        @Override
//        public Domain getDomain()
//        {
//            return ConstructionManager.this.getDomain();
//        }
//
//        @Override
//        public int getPriority()
//        {
//            // TODO Auto-generated method stub
//            return 0;
//        }
//
//        @Override
//        public void setPriority(int priority)
//        {
//            // TODO Auto-generated method stub
//            
//        }
//
//        @Override
//        public void cancel()
//        {
//            // TODO Auto-generated method stub
//            
//        }
//
//        @Override
//        public RequestStatus execute(IExecutionContext context)
//        {
//            // TODO Auto-generated method stub
//            return null;
//        }
//
//        @Override
//        public void setExecutor(IExecutor manager)
//        {
//            // TODO Auto-generated method stub
//            
//        }
//
//        @Override
//        public void addConsequent(IRequest consequent)
//        {
//            // TODO Auto-generated method stub
//            
//        }
//
//        @Override
//        public void addAntecedent(IRequest antecedent)
//        {
//            // TODO Auto-generated method stub
//            
//        }
//
//        @Override
//        public void onAntecedentTerminated(IRequest antecedent)
//        {
//            // TODO Auto-generated method stub
//            
//        }
//
//        @Override
//        public boolean areAllAntecedentsMet()
//        {
//            // TODO Auto-generated method stub
//            return false;
//        }
//    }
//}
