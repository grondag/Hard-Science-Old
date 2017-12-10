package grondag.hard_science.simulator.domain;

import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.superblock.placement.Build;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.nbt.NBTTagCompound;

public class DomainUser implements IReadWriteNBT
{
    /**
     * 
     */
    private final Domain domain;

    public String userName;
    
    private int privilegeFlags;
    
    private Int2IntOpenHashMap activeBuilds = new Int2IntOpenHashMap();
    
    DomainUser(Domain domain, String playerName)
    {
        this.domain = domain;
        this.userName = playerName;
    }
    
    DomainUser(Domain domain, NBTTagCompound tag)
    {
        this.domain = domain;
        this.deserializeNBT(tag);
    }

    /**
     * Will return true for admin users, regardless of other Privilege grants.
     * Will also return true if security is disabled for the domain.
     */
    public boolean hasPrivilege(Privilege p)
    {
        return  !this.domain.isSecurityEnabled
                || DomainManager.PRIVILEGE_FLAG_SET.isFlagSetForValue(Privilege.ADMIN, privilegeFlags)
                || DomainManager.PRIVILEGE_FLAG_SET.isFlagSetForValue(p, privilegeFlags);
    }
    
    public void grantPrivilege(Privilege p, boolean hasPrivilege)
    {
        this.privilegeFlags = DomainManager.PRIVILEGE_FLAG_SET.setFlagForValue(p, privilegeFlags, hasPrivilege);
        this.domain.setDirty();;
    }
    
    public void setPrivileges(Privilege... granted)
    {
        this.privilegeFlags = DomainManager.PRIVILEGE_FLAG_SET.getFlagsForIncludedValues(granted);
        this.domain.setDirty();;
    }

    @Override
    public void serializeNBT(NBTTagCompound nbt)
    {
        nbt.setString(ModNBTTag.DOMAIN_USER_NAME, this.userName);
        nbt.setInteger(ModNBTTag.DOMAIN_USER_FLAGS, this.privilegeFlags);
        if(!this.activeBuilds.isEmpty())
        {
            int[] buildData = new int[this.activeBuilds.size() * 2];
            int i = 0;
            for(Int2IntMap.Entry entry : this.activeBuilds.int2IntEntrySet())
            {
                buildData[i++] = entry.getIntKey();
                buildData[i++] = entry.getIntValue();
            }
            nbt.setIntArray(ModNBTTag.BUILD_ID, buildData);
        }
        this.domain.itemStorage.serializeNBT(nbt);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        this.userName = nbt.getString(ModNBTTag.DOMAIN_USER_NAME);
        this.privilegeFlags = nbt.getInteger(ModNBTTag.DOMAIN_USER_FLAGS);
        
        if(nbt.hasKey(ModNBTTag.BUILD_ID))
        {
            int[] buildData = nbt.getIntArray(ModNBTTag.BUILD_ID);
            if(buildData != null && buildData.length > 0 && (buildData.length & 1) == 0)
            {
                int i = 0;
                while(i < buildData.length)
                {
                    this.activeBuilds.put(buildData[i++], buildData[i++]);
                }
            }
        }
                
        this.domain.itemStorage.deserializeNBT(nbt);
    }
    
    /**
     * Retrieves active build in given dimension if exists,
     * creates new build in this domain and makes
     * it the active build for the player otherwise.
     */
    public Build getActiveBuild(int dimensionID)
    {
        int buildID = this.activeBuilds.get(dimensionID);
        Build result = DomainManager.INSTANCE.assignedNumbersAuthority().buildIndex().get(buildID);
        if(result == null || !result.isOpen())
        {
            result = this.domain.buildManager.newBuild(dimensionID);
            this.activeBuilds.put(dimensionID, result.getId());
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
    }
    
}