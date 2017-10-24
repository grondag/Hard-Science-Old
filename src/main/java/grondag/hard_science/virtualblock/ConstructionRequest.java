package grondag.hard_science.virtualblock;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.tuple.Pair;

import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.world.IntegerAABB;
import grondag.hard_science.library.world.Location;
import grondag.hard_science.library.world.Location.ILocated;
import grondag.hard_science.simulator.base.AssignedNumber;
import grondag.hard_science.simulator.base.DomainManager.Domain;
import grondag.hard_science.simulator.base.DomainManager.IDomainMember;
import grondag.hard_science.simulator.base.IIdentified;
import grondag.hard_science.simulator.base.IRequest;
import grondag.hard_science.simulator.base.RequestPriority;
import grondag.hard_science.simulator.base.RequestStatus;
import grondag.hard_science.superblock.placement.PlacementResult;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class ConstructionRequest implements IIdentified, IReadWriteNBT, ILocated, IRequest, IDomainMember
{
    public class ConstructionRequestEntry
    {
        private BlockPos pos;
        private ItemStack stack;
        private RequestStatus excavationStatus;
        private RequestStatus fabricationStatus;
        private RequestStatus placementStatus;
    }
    /**
     * How to organize?
     * excavation, fab and placement in different collections with links between
     *   OR
     * all in same collection with different index collections
     * 
     */
    private String userName;
    
    /** 
     * Identifies world and serves as origin for relative block positions.
     */
    private Location location;
    
    private IntegerAABB excavationBounds = null;
    private IntegerAABB placementBounds = null;
    
    private ArrayList<BlockPos> excavations = null;
    
    private HashMap<BlockPos, ItemStack> placements = null;
     
    private int id;
    
    public ConstructionRequest(EntityPlayer player, PlacementResult placement)
    {
        this.userName = player.getName();
        BlockPos origin = placement.blockPos();
        this.location = new Location(origin, player.world);
        if(!placement.placements().isEmpty())
        {
            boolean isExcavation = placement.event().isExcavation;
            IntegerAABB.Builder exAABB = new IntegerAABB.Builder();
            IntegerAABB.Builder plAABB = new IntegerAABB.Builder();
            
            ArrayList<BlockPos> ex = new ArrayList<BlockPos>();
            HashMap<BlockPos, ItemStack> pl = new HashMap<BlockPos, ItemStack>();
            
            for(Pair<BlockPos, ItemStack >  pair : placement.placements())
            {
                ItemStack stack = pair.getRight();
                BlockPos pos = pair.getLeft();
                if(isExcavation || stack.isEmpty() || stack.getItem() == Items.AIR)
                {
                    ex.add(pos);
                    exAABB.add(pos);
                }
                else
                {
                    pl.put(pos, stack);
                    plAABB.add(pos);
                }
            }
            
            if(!ex.isEmpty()) this.excavations = ex;
            if(!pl.isEmpty()) this.placements = pl;
            this.excavationBounds = exAABB.build();
            this.placementBounds = plAABB.build();
        }
    }
    
    public IntegerAABB excavationBounds() { return this.excavationBounds; }
    public IntegerAABB placementBounds() { return this.placementBounds; }
    public String userName() { return this.userName; }
    public Location location() { return this.location; }
    
    @Override
    public int getIdRaw()
    {
        return this.id;
    }

    @Override
    public void setId(int id)
    {
        this.id = id;
    }

    @Override
    public AssignedNumber idType()
    {
        return AssignedNumber.REQUEST;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        this.deserializeID(tag);
        this.location = Location.fromNBT(tag);
        this.userName = tag.getString(ModNBTTag.DOMAIN_USER_NAME);
        
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        this.serializeID(tag);
        Location.saveToNBT(this.location, tag);
        tag.setString(ModNBTTag.DOMAIN_USER_NAME, this.userName);
    }

    @Override
    public Location getLocation()
    {
        return null;
    }

    @Override
    public void setLocation(Location loc)
    {
        
    }

    @Override
    public RequestStatus getStatus()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RequestPriority getPriority()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int requestID()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Domain getDomain()
    {
        // TODO Auto-generated method stub
        return null;
    }
}
