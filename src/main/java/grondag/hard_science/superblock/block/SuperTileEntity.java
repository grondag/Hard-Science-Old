package grondag.hard_science.superblock.block;

import grondag.hard_science.library.serialization.ObjectSerializer;
import grondag.hard_science.library.serialization.SerializationManager;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.model.state.RenderPassSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class SuperTileEntity extends TileEntity
{
    ////////////////////////////////////////////////////////////////////////
    //  STATIC MEMBERS
    ////////////////////////////////////////////////////////////////////////
    public static final ObjectSerializer<SuperTileEntity, ModelState> SERIALIZER_MODEL_STATE = new ObjectSerializer<SuperTileEntity, ModelState>(false, ModelState.class)
    {
        @Override
        public ModelState getValue(SuperTileEntity target)
        {
            return target.modelState;
        }

        @Override
        public void notifyChanged(SuperTileEntity target)
        {
            target.onModelStateChange(true);
        } 
    };
    
    /**
     * Core serializers are included in MC packets but aren't expected to change frequently.
     * They can and should be excluded from more frequently-changing values that need to be sent
     * to clients more often. (Needed for machines, mostly, at time of writing.)
     */
    public static final SerializationManager<SuperTileEntity> CORE_SERIALIZERS = new SerializationManager<SuperTileEntity>()
            .addThen(SERIALIZER_MODEL_STATE);
 
    ////////////////////////////////////////////////////////////////////////
    //  INSTANCE MEMBERS
    ////////////////////////////////////////////////////////////////////////
    protected ModelState modelState = new ModelState();
    
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
     * Super calls writeInternal() instead of {@link #writeToNBT(NBTTagCompound)}
     * We call writeToNBT so that we include all info, but filter out
     * server-side only tag to prevent wastefully large packets.
     */
    @Override
    public NBTTagCompound getUpdateTag()
    {
        return SerializationManager.withoutServerTag(writeToNBT(super.getUpdateTag()));
    }

    /**
     * Generate packet sent to client for TE synch after block/chunk is loaded.
     * Is inefficient that the information is serialized twice: first to NBT
     * then to ByteBuffer but that is how MC does it and the packet only accepts NBT.
     */
    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        return new SPacketUpdateTileEntity(this.pos, getBlockMetadata(), this.getUpdateTag());
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
        CORE_SERIALIZERS.deserializeNBT(this, compound);
    }
    
    /**
     * Stores all state for this mod to the given tag.
     * Used internally for serialization but can also be used to restore state from ItemStack
     */
    public void writeModNBT(NBTTagCompound compound)
    {
        CORE_SERIALIZERS.serializeNBT(this, compound);
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
        // if making existing appearance static, don't need to refresh on client side
        boolean needsClientRefresh = this.world != null
                && this.world.isRemote
                && !(
                        this.modelState != null  
                        && this.modelState.equals(modelState)
                        && modelState.isStatic()
                        && this.modelState.isStatic() != modelState.isStatic());
            {
                this.modelState = modelState;
                this.onModelStateChange(!modelState.isStatic());
            }
      
        this.modelState = modelState;
        this.onModelStateChange(needsClientRefresh);
    }
    
    /** call whenever modelState changes (or at least probably did).
     * Parameter should always be true except in case of changing
     * dynamic blocks to static without altering appearance. 
     */
    private void onModelStateChange(boolean refreshClientRenderState)
    {
        /**
         * This can be called by onBlockPlaced after we've already been established.
         * If that happens, need to treat it like an update, markDirty(), refresh client state, etc.
         */
        this.isModelStateCacheDirty = true;
        if(this.world !=null)
        {
            if(this.world.isRemote)
            {
                if(refreshClientRenderState) this.updateClientRenderState();
            }
            else
            {
                this.markDirty();
            }
        }
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
