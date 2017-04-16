package grondag.adversity.niceblock.base;


//import mcjty.lib.entity.GenericTileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class NiceTileEntity extends TileEntity{

    public static final String PLACEMENT_SHAPE_TAG = "APS";
    public static final String DAMAGE_TAG = "ADT";
    private static final String MODEL_KEY_TAG = "AMK";
    
    public static final String CMD_SET_MODEL_KEY = "setModelKey";

    private long modelKey;

    /** used by big blocks */
    private int placementShape;

    /** used by hyperstone */
    private byte damage = 0;

    //	public IExtendedBlockState exBlockState;
    public boolean isModelKeyCacheDirty = true;
    public boolean isLoaded = false;

    public static enum ModelRefreshMode
    {
        /** 
         * Normal setting for cached model state keys.
         * Only changes to persistent bits will trigger an update packet/render refresh.
         */
        CACHE, 
        
//        /**
//         * Currently unused/unimplemented. 
//         * Changes to model key will never trigger an update packet/render refresh 
//         */
//        NEVER,
        
        /**
         * For static flow blocks or other blocks that are a static version
         * of a block with dynamic model state components.
         * All changes to model key will trigger an update packet/render refresh 
         */
        ALWAYS
    }

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

        this.isModelKeyCacheDirty = true;

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
        if(target != null && target instanceof NiceTileEntity)
        {
            //			Adversity.log.info("updatify success @ " + updatePos.toString());
            ((NiceTileEntity)target).isModelKeyCacheDirty = true;
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
        long oldModelKey = modelKey;
        doReadFromNBT(tag);
        if(oldModelKey != modelKey && this.world.isRemote)
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
        long oldModelKey = modelKey;
        doReadFromNBT(pkt.getNbtCompound());
        if(oldModelKey != modelKey && this.world.isRemote)
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
        if(this.world == null || ((NiceBlockPlus)this.getBlockType()).getModelRefreshMode() == ModelRefreshMode.ALWAYS)
        {
            //Block will be unknown if no world reference yet.
            //In that case, no reason to update only the persistent bits.
            //Also always use all bits from server if refresh mode indicates necessary.
            modelKey = (compound.getLong(MODEL_KEY_TAG));
//            Adversity.log.info("doReadFromNBT static modelKey=" + modelKey);

        }
        else
        {
            long mask = ((NiceBlockPlus)this.getBlockType()).dispatcher.getStateSet().getPersistenceMask();
//            Adversity.log.info("doReadFromNBT mask=" + mask
//                + " oldModelKey=" + modelKey
//                + " NBTModelKey=" +  compound.getLong(MODEL_KEY_TAG)
//                + " newModelKey=" + ((compound.getLong(MODEL_KEY_TAG) & mask) | (modelKey & ~mask))
//                + " pos=" + pos.toString());

            modelKey = (compound.getLong(MODEL_KEY_TAG) & mask)
                    | (modelKey & ~mask);
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
        if(modelKey != 0L) compound.setLong(MODEL_KEY_TAG, modelKey);
        if(damage != 0) compound.setByte(DAMAGE_TAG, damage);
        if(placementShape != 0) compound.setInteger(PLACEMENT_SHAPE_TAG, placementShape);
        return compound;
    }

    public long getModelKey() { return modelKey; }
    public void setModelKey(long modelKey) 
    { 
//        Adversity.log.info("setModelKey pos=" + pos.toString());
//        Adversity.log.info("oldModelKey=" + this.modelKey + " newModelKey=" + modelKey );
        if(this.modelKey != modelKey)
        {
            this.modelKey = modelKey; 
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
