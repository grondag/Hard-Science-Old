package grondag.hard_science.superblock.block;

import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.model.state.RenderPassSet;
import grondag.hard_science.superblock.varia.SuperBlockNBTHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class SuperTileEntity extends TileEntity implements SuperBlockNBTHelper.ModelStateNBTReadHandler
{
    protected ModelState modelState;

    //  public IExtendedBlockState exBlockState;
    private boolean isModelStateCacheDirty = true;
 
    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) 
    {
        if(oldState.getBlock() == newSate.getBlock())
        {
            return false;
        }
        else
        {
            if(world.isRemote) updateClientRenderState();
            return true;
        }
    }
 
    public void updateClientRenderState()
    {
        this.isModelStateCacheDirty = true;

        invalidateClientCache(pos.up());
        invalidateClientCache(pos.down());
        invalidateClientCache(pos.east());
        invalidateClientCache(pos.west());
        invalidateClientCache(pos.north());
        invalidateClientCache(pos.south());

        invalidateClientCache(pos.up().east());
        invalidateClientCache(pos.up().west());
        invalidateClientCache(pos.up().north());
        invalidateClientCache(pos.up().south());

        invalidateClientCache(pos.down().east());
        invalidateClientCache(pos.down().west());
        invalidateClientCache(pos.down().north());
        invalidateClientCache(pos.down().south());

        invalidateClientCache(pos.north().east());
        invalidateClientCache(pos.north().west());
        invalidateClientCache(pos.south().east());
        invalidateClientCache(pos.south().west());

        invalidateClientCache(pos.up().north().east());
        invalidateClientCache(pos.up().south().east());
        invalidateClientCache(pos.up().north().west());
        invalidateClientCache(pos.up().south().west());

        invalidateClientCache(pos.down().north().east());
        invalidateClientCache(pos.down().south().east());
        invalidateClientCache(pos.down().north().west());
        invalidateClientCache(pos.down().south().west());

        this.world.markBlockRangeForRenderUpdate(pos.up().north().east(), pos.down().south().west());

    }

    private void invalidateClientCache(BlockPos updatePos)
    {
        TileEntity target = this.world.getTileEntity(updatePos);
        if(target != null && target instanceof SuperTileEntity)
        {
            ((SuperTileEntity)target).isModelStateCacheDirty = true;
        }
    }

    /**
     * Generate tag sent to client when block/chunk first loads.
     * MUST include x, y, z tags so client knows which TE belong to.
     */
    @Override
    public NBTTagCompound getUpdateTag()
    {
        return writeToNBT(super.getUpdateTag());
    }

    /**
     * Process tag sent to client when block/chunk first loads.
     */
    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        // The description packet often arrives after render state is first cached on client
        // so we need to refresh render state once we have the server-side info.
        ModelState oldModelState = modelState;
        super.handleUpdateTag(tag);
        if((oldModelState == null || (!oldModelState.equals(modelState))) && this.world.isRemote)
        {
            this.updateClientRenderState();
        }
    }

    /**
     * Generate packet sent to client for TE synch after block/chunk is loaded.
     */
    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        NBTTagCompound nbtTagCompound = writeToNBT(new NBTTagCompound());
        int metadata = getBlockMetadata();
        return new SPacketUpdateTileEntity(this.pos, metadata, nbtTagCompound);
    }

    /**
     * Process packet sent to client for TE synch after block/chunk is loaded.
     */
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) 
    {
        handleUpdateTag(pkt.getNbtCompound());
    }

    @Override
    public final void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        this.readModNBT(compound);
    }
    
    /**
     * Restores all state previously serialized by {@link #writeModNBT(NBTTagCompound)}
     */
    public void readModNBT(NBTTagCompound compound)
    {
        SuperBlockNBTHelper.readFromNBT(compound, this);
        /**
         * This can be called by onBlockPlaced after we've already been established.
         * If that happens, need to treat it like an update, markDirty(), refresh client state, etc.
         */
        this.isModelStateCacheDirty = true;
        if(this.world !=null)
        {
            if(this.world.isRemote)
            {
                this.updateClientRenderState();
            }
            else
            {
                this.markDirty();
            }
        }
    }
    
    /**
     * Stores all state for this mod to the given tag.
     * Used internally for serialization but can also be used to restore state from ItemStack
     */
    public void writeModNBT(NBTTagCompound compound)
    {
        SuperBlockNBTHelper.writeToNBT(compound, this.modelState);
    }
    

    @Override
    public final NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        this.writeModNBT(compound);
        return super.writeToNBT(compound);
    }

    public ModelState getModelState(IBlockState state, IBlockAccess world, BlockPos pos, boolean refreshFromWorldIfNeeded)
    { 
        if(this.modelState == null)
        {
            this.modelState = ((SuperBlock)state.getBlock()).getDefaultModelState();
            this.isModelStateCacheDirty = true;
            
            // necessary for species
            refreshFromWorldIfNeeded = true;
        }
        
        if(this.isModelStateCacheDirty && refreshFromWorldIfNeeded)
        {
            this.modelState.refreshFromWorld(state, world, pos);
            this.isModelStateCacheDirty = false;
        }
        
        
        return this.modelState; 
    }
    
    /**
     * Use this version when you don't have world state handy
     */
    public ModelState getModelState()
    {
        if(!(this.modelState == null || this.isModelStateCacheDirty)) 
        {
            return this.modelState;
        }
        else
        {
            return this.getModelState(this.world.getBlockState(this.pos), world, pos, true); 
        }
    }
    
    /** intended for use in TESR - don't refresh unless missing because should be up to date from getExtendedState called before this */
    public ModelState getCachedModelState()
    {
        return this.modelState == null ? this.getModelState(this.world.getBlockState(this.pos), world, pos, true) : this.modelState;
    }
    
    public void setModelState(ModelState modelState) 
    { 
        if(this.modelState == null)
        {
            this.setModelStateInner(modelState);
        }
        else if(this.modelState.equals(modelState))
        {
            if(this.modelState.isStatic() != modelState.isStatic())
            {
                if(modelState.isStatic())
                {
                    // if making existing appearance static, just mark to save if on server
                    this.modelState = modelState;
                    if(!this.world.isRemote) this.markDirty();
                }
                else
                {
                    // if going from static to dynamic, force refresh
                    this.setModelStateInner(modelState);
                }
            }
        }
        else
        {
            this.setModelStateInner(modelState);
        }
    }

    private void setModelStateInner(ModelState modelState)
    {
        this.modelState = modelState;
        this.isModelStateCacheDirty = true;
        if(this.world.isRemote)
        {
            this.updateClientRenderState();
        }
        else
        {
            this.markDirty();
        }
    }
 

    @Override
    public void handleModelStateNBTRead(ModelState modelState)
    {
        this.modelState = (modelState == null)
                ? ((SuperBlock)this.getBlockType()).getDefaultModelState()
                : modelState;   
    }
    
    @Override
    public boolean shouldRenderInPass(int pass)
    {
        if(this.modelState == null || this.isModelStateCacheDirty)
        {
            IBlockState myState = this.world.getBlockState(getPos());
            
            if(this.modelState == null) this.modelState = ((SuperBlock)myState.getBlock()).getDefaultModelState();
            
            this.modelState.refreshFromWorld(myState, world, pos);
            this.isModelStateCacheDirty = false;
            
        }
        
        RenderPassSet rps = this.modelState.getRenderPassSet();
        return !rps.canRenderAsNormalBlock() 
                    && rps.renderLayout.containsBlockRenderLayer(pass == 0 ? BlockRenderLayer.SOLID : BlockRenderLayer.TRANSLUCENT);
        
    }
    
    /**
     * Only true for virtual blocks.  Prevents "instanceof" checking.
     */
    public boolean isVirtual() { return false; }
    
}
