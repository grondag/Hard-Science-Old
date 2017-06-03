package grondag.adversity.superblock.block;

import grondag.adversity.niceblock.support.BlockSubstance;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SuperTileEntity extends TileEntity implements SuperBlockNBTHelper.NBTReadHandler
{
    private ModelState modelState;

    /** used by big blocks */
    private int placementShape;
    
    /** non-zero if block emits light */
    private byte lightValue = 0;

    private BlockSubstance substance = BlockSubstance.FLEXSTONE;
    
    //  public IExtendedBlockState exBlockState;
    private boolean isModelStateCacheDirty = true;
    public boolean isLoaded = false;

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

    @Override
    public void onLoad() 
    {
        super.onLoad();
    }

    
    @SideOnly(Side.CLIENT)
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

    @SideOnly(Side.CLIENT)
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
        return SuperBlockNBTHelper.writeToNBT(super.getUpdateTag(), this.modelState, this.placementShape, this.lightValue, this.substance);
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
        if(!oldModelState.equals(modelState) && this.world.isRemote)
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
        NBTTagCompound nbtTagCompound = SuperBlockNBTHelper.writeToNBT(new NBTTagCompound(), this.modelState, this.placementShape, this.lightValue, this.substance);
        int metadata = getBlockMetadata();
        return new SPacketUpdateTileEntity(this.pos, metadata, nbtTagCompound);
    }

    /**
     * Process packet sent to client for TE synch after block/chunk is loaded.
     */
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) 
    {
        ModelState oldModelState = modelState;
        int oldLight = this.lightValue;
        SuperBlockNBTHelper.readFromNBT(pkt.getNbtCompound(), this);
        if(!oldModelState.equals(modelState) && this.world.isRemote)
        {
            this.updateClientRenderState();
        }
        if(oldLight != this.lightValue)
        {
            this.world.checkLight(this.pos);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        SuperBlockNBTHelper.readFromNBT(compound, this);
        isLoaded = true;
    }

    @Override
    public void handleNBTRead(ModelState modelState, int placementShape, byte lightValue, BlockSubstance substance)
    {
        this.modelState = (modelState == null)
                ? ((SuperBlock)this.getBlockType()).getDefaultModelState()
                : modelState;
        this.placementShape = placementShape;
        this.lightValue = lightValue;
        this.substance = substance;
    }
   
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        SuperBlockNBTHelper.writeToNBT(compound, this.modelState, this.placementShape, this.lightValue, this.substance);
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
    
    public void setModelState(ModelState modelState) 
    { 
        if(this.modelState == null || !this.modelState.equals(modelState))
        {
            this.modelState = modelState;
            this.isModelStateCacheDirty = true;
            if(!this.world.isRemote) this.markDirty();
        }
    }

    public int getPlacementShape()
    { 
        return placementShape;
    }
    
    public void setPlacementShape( int placementShape)
    { 
        if(this.placementShape != placementShape)
        {
            this.placementShape = placementShape;
            if(!this.world.isRemote) this.markDirty();
        }
    }

    public byte getLightValue()
    {
        return this.lightValue;
    }

    public void setLightValue(byte lightValue)
    {
        if(this.lightValue != lightValue)
        {
            this.lightValue = lightValue;
            if(this.world.isRemote)
                this.world.checkLight(this.pos);
            else
                this.markDirty();
            
        }
    }

    public BlockSubstance getSubstance()
    {
        return this.substance;
    }
    
    public void setSubstance(BlockSubstance substance)
    {
        if(this.substance != substance)
        {
            this.substance = substance;
            if(!this.world.isRemote) this.markDirty();
        }
    }
}
