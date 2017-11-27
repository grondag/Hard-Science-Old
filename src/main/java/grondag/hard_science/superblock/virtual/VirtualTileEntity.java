package grondag.hard_science.superblock.virtual;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.simulator.domain.Domain;
import grondag.hard_science.simulator.persistence.IIdentified;
import grondag.hard_science.superblock.block.SuperModelTileEntity;
import grondag.hard_science.superblock.placement.Build;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class VirtualTileEntity extends SuperModelTileEntity
{
    /**
     * Identifies the domain to which this block belongs.
     * Should be set immediately after creation and not changed. <p>
     * 
     * Somewhat redundant of {@link #buildID} but want both to
     * be available client-side.<p>
     * 
     * NB: Not looking up domain instance here because needs to run on the client.
     */
    private int domainID = IIdentified.UNASSIGNED_ID;
    
    /**
     * Identifies the build to which this block belongs.
     * Should be set immediately after creation and not changed.
     * 
     * Should be in the domain identified by {@link #domainID}
     */
    private int buildID = IIdentified.UNASSIGNED_ID;
    
    @Override
    public boolean isVirtual() { return true; }

    @Override
    public void onLoad()
    {
        super.onLoad();
    }
    
    /**
     * See {@link #domainID}
     */
    public int domainID()
    {
        return this.domainID;
    }
    
    public boolean hasDomain()
    {
        return this.domainID != IIdentified.UNASSIGNED_ID;
    }
    
    /**
     * See {@link #domainID}
     */
    public void setDomain(Domain domain)
    {
        if(domain != null)
        {
            this.domainID = domain.getId();
            this.markDirty();
        }
    }
    
    /**
     * See {@link #buildID}
     */
    public int buildID()
    {
        return this.buildID;
    }
    
    public boolean hasBuild()
    {
        return this.buildID != IIdentified.UNASSIGNED_ID;
    }
    
    /**
     * See {@link #buildID}
     * Also sets domain.
     */
    public void setBuild(Build build)
    {
        if(build != null)
        {
            this.buildID = build.getId();
            this.setDomain(build.getDomain());
            this.markDirty();
        }
    }
    
    @SideOnly(Side.CLIENT)
    public boolean isVisible()
    {
        //TODO: what and how?
        // maybe just have it global so that people can see each other's blocks?
        // if so, could have virtual blocks act a opaque cubes and enable chunk culling?
        return true;
    }

    @Override
    public void writeModNBT(NBTTagCompound compound)
    {
        super.writeModNBT(compound);
        compound.setInteger(ModNBTTag.DOMAIN_ID, this.domainID);
        compound.setInteger(ModNBTTag.BUILD_ID, this.buildID);
    }

    @Override
    public void readModNBT(NBTTagCompound compound)
    {
        super.readModNBT(compound);
        
        this.domainID = compound.hasKey(ModNBTTag.DOMAIN_ID) 
                ? compound.getInteger(ModNBTTag.DOMAIN_ID) 
                : IIdentified.UNASSIGNED_ID;
                
        this.buildID = compound.hasKey(ModNBTTag.BUILD_ID) 
                ? compound.getInteger(ModNBTTag.BUILD_ID) 
                : IIdentified.UNASSIGNED_ID;          
    }
    
    
}
