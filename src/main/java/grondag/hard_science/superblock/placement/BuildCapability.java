package grondag.hard_science.superblock.placement;

import grondag.exotic_matter.serialization.NBTDictionary;
import grondag.exotic_matter.simulator.Simulator;
import grondag.exotic_matter.simulator.domain.DomainUser;
import grondag.exotic_matter.simulator.domain.IUserCapability;
import grondag.exotic_matter.simulator.persistence.AssignedNumber;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.nbt.NBTTagCompound;

public class BuildCapability implements IUserCapability
{
    private static final String NBT_TAG_SELF = NBTDictionary.claim("buildCap");
    private static final String NBT_TAG_DATA = NBTDictionary.claim("buildData");

    private Int2IntOpenHashMap activeBuilds = new Int2IntOpenHashMap();

    private DomainUser user;
    
    /**
     * Retrieves active build in given dimension if exists,
     * creates new build in this domain and makes
     * it the active build for the player otherwise.
     */
    public Build getActiveBuild(int dimensionID)
    {
        int buildID = this.activeBuilds.get(dimensionID);
        Build result = (Build)  Simulator.instance().assignedNumbersAuthority().get(buildID, AssignedNumber.BUILD);
        if(result == null || !result.isOpen())
        {
            BuildManager bm = this.user.getDomain().getCapability(BuildManager.class);
            if(bm != null)
            {
                result = bm.newBuild(dimensionID);
                this.activeBuilds.put(dimensionID, result.getId());
                this.user.getDomain().setDirty();
            }
        }
        return result;
    }
    
    /**
     * Makes given build the active build in its dimension.
     * Note that if the given build is not open, will be
     * re-assigned to a new build on retrieval.
     */
    public void setActiveBuild(Build build)
    {
        this.activeBuilds.put(build.dimensionID(), build.getId());
        this.user.getDomain().setDirty();
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        if(tag.hasKey(NBT_TAG_DATA))
        {
            int[] buildData = tag.getIntArray(NBT_TAG_DATA);
            if(buildData != null && buildData.length > 0 && (buildData.length & 1) == 0)
            {
                int i = 0;
                while(i < buildData.length)
                {
                    this.activeBuilds.put(buildData[i++], buildData[i++]);
                }
            }
        }
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        if(!this.activeBuilds.isEmpty())
        {
            int[] buildData = new int[this.activeBuilds.size() * 2];
            int i = 0;
            for(Int2IntMap.Entry entry : this.activeBuilds.int2IntEntrySet())
            {
                buildData[i++] = entry.getIntKey();
                buildData[i++] = entry.getIntValue();
            }
            tag.setIntArray(NBT_TAG_DATA, buildData);
        }
        
    }

    @Override
    public String tagName()
    {
        return NBT_TAG_SELF;
    }

    @Override
    public void setDomainUser(DomainUser user)
    {
        this.user = user;
    }

    @Override
    public boolean isSerializationDisabled()
    {
        return this.activeBuilds.isEmpty();
    }

}
