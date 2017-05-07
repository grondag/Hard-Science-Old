package grondag.adversity.superblock.block;

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

public class SuperTileEntity extends TileEntity
{

    public static final String PLACEMENT_SHAPE_TAG = "APS";
    public static final String DAMAGE_TAG = "ADT";
    private static final String MODEL_STATE_TAG = "AMK";
    
    public static final String CMD_SET_MODEL_KEY = "setModelKey";

    private ModelState modelState;

    /** used by big blocks */
    private int placementShape;

    /** used by hyperstone */
    private byte damage = 0;

    //  public IExtendedBlockState exBlockState;
    private boolean isModelStateCacheDirty = true;
    public boolean isLoaded = false;

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) 
    {
//        Adversity.log.info("shouldRefresh pos=" + pos.toString());
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
//        Adversity.log.info("onLoad");
        super.onLoad();
//        if(this.worldObj.isRemote)
//        {
//            //TODO: could this be better handled elsewhere, performance-wise?
//            updateClientRenderState();
//        }
    }

    
    @SideOnly(Side.CLIENT)
    public void updateClientRenderState()
    {
//        Adversity.log.info("updateClientRenderState pos=" + pos.toString());

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
//        Adversity.log.info("invalidateClientCache thread=" + Thread.currentThread().getName() + " pos=" + pos.toString());
        TileEntity target = this.world.getTileEntity(updatePos);
        if(target != null && target instanceof SuperTileEntity)
        {
            //          Adversity.log.info("updatify success @ " + updatePos.toString());
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
//        Adversity.log.info("getUpdateTag pos=" + pos.toString());

        return this.doWriteToNBT(super.getUpdateTag());
    }

    /**
     * Process tag sent to client when block/chunk first loads.
     */
    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        // The description packet often arrives after render state is first cached on client
        // so we need to refresh render state once we have the server-side info.
//        Adversity.log.info("handleUpdateTag pos=" + pos.toString());

        super.handleUpdateTag(tag);
        ModelState oldModelState = modelState;
        doReadFromNBT(tag);
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
//        Adversity.log.info("getUpdatePacket pos=" + pos.toString());
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        doWriteToNBT(nbtTagCompound);
        int metadata = getBlockMetadata();
        return new SPacketUpdateTileEntity(this.pos, metadata, nbtTagCompound);
    }

    /**
     * Process packet sent to client for TE synch after block/chunk is loaded.
     */
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) 
    {
//        Adversity.log.info("OnDataPacket pos=" + pos.toString());
        ModelState oldModelState = modelState;
        doReadFromNBT(pkt.getNbtCompound());
        if(!oldModelState.equals(modelState) && this.world.isRemote)
        {
            this.updateClientRenderState();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
//        Adversity.log.info("readFromNBT START pos=" + pos.toString());
        super.readFromNBT(compound);
//        Adversity.log.info("readFromNBT POST-SUPER pos=" + pos.toString());
        doReadFromNBT(compound);
        isLoaded = true;
    }

    private void doReadFromNBT(NBTTagCompound compound)
    {
        int[] stateBits = compound.getIntArray(MODEL_STATE_TAG);
        if(stateBits == null || stateBits.length != 8)
        {
            modelState = ((SuperBlock)this.getBlockType()).getDefaultModelState();
        }
        else
        {
            modelState = new ModelState(compound.getIntArray(MODEL_STATE_TAG));            
        }
        placementShape = compound.getInteger(PLACEMENT_SHAPE_TAG);
        damage = compound.getByte(DAMAGE_TAG);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
//        Adversity.log.info("writeToNBT pos=" + pos.toString());
        doWriteToNBT(compound);
        return super.writeToNBT(compound);
    }

    private NBTTagCompound doWriteToNBT(NBTTagCompound compound)
    {
        if(modelState != null) compound.setIntArray(MODEL_STATE_TAG, modelState.getBits());
        if(damage != 0) compound.setByte(DAMAGE_TAG, damage);
        if(placementShape != 0) compound.setInteger(PLACEMENT_SHAPE_TAG, placementShape);
        return compound;
    }

    public ModelState getModelState(IBlockState state, IBlockAccess world, BlockPos pos)
    { 
        if(this.modelState == null)
        {
            this.modelState = ((SuperBlock)this.blockType).getDefaultModelState();
            this.isModelStateCacheDirty = true;
        }
        if(this.isModelStateCacheDirty)
        {
            this.modelState.refreshFromWorld(state, world, pos);
            this.isModelStateCacheDirty = false;
        }
        return this.modelState; 
    }
    
    public void setModelState(ModelState modelState) 
    { 
//        Adversity.log.info("setModelKey pos=" + pos.toString());
//        Adversity.log.info("oldModelKey=" + this.modelKey + " newModelKey=" + modelKey );
        if(!this.modelState.equals(modelState))
        {
            this.modelState = modelState;
            this.isModelStateCacheDirty = true;
            if(!this.world.isRemote)
            {
                this.markDirty();
            }
        }
    }

    /** returns values 0-255 */
    public int getDamage() { return (int)damage - Byte.MIN_VALUE; }
    /** expects values 0-255 */
    public void setDamage( int damageIn) 
    { 
        byte newVal = (byte)(damageIn + Byte.MIN_VALUE);
        if(this.damage != newVal)
        {
            this.damage = newVal; 
            this.markDirty();
        }
    }

    public int getPlacementShape() { return placementShape; }
    public void setPlacementShape( int placementShape)
    { 
//        Adversity.log.info("setPlacementShape pos=" + pos.toString());

        if(this.placementShape != placementShape)
        {
            this.placementShape = placementShape;
            this.markDirty();
        }
    }

}
