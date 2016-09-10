package grondag.adversity.niceblock.base;

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

    private long modelKey;
    
    /** used by big blocks */
    private int placementShape;
    
    /** used by hyperstone */
	private byte damage = 0;
	
//	public IExtendedBlockState exBlockState;
	public boolean isModelKeyCacheDirty = true;
	public boolean isLoaded = false;
	public boolean isDeleted = false;


	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) 
	{
		if(this.worldObj.isRemote)
		{
			isDeleted = true;
//			Adversity.log.info("shouldRefresh " + pos.toString());
            //TODO: could this be better handled elsewhere, performance-wise?
			updateClientRenderState();
		}
		return super.shouldRefresh(world, pos, oldState, newSate);
	}

	@Override
	public void onLoad() 
	{
		super.onLoad();
		if(this.worldObj.isRemote)
		{
		    //TODO: could this be better handled elsewhere, performance-wise?
			updateClientRenderState();
		}
	}
	
	@SideOnly(Side.CLIENT)
	private void updateClientRenderState()
	{
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

        worldObj.markBlockRangeForRenderUpdate(pos.up().north().east(), pos.down().south().west());

	}
	
	@SideOnly(Side.CLIENT)
	private void invalidateClientCache(BlockPos updatePos)
	{
//		Adversity.log.info("updatify attempt @ " + updatePos.toString());
		TileEntity target = worldObj.getTileEntity(updatePos);
		if(target != null && target instanceof NiceTileEntity)
		{
//			Adversity.log.info("updatify success @ " + updatePos.toString());
			((NiceTileEntity)target).isModelKeyCacheDirty = true;
		}
	}

   @Override
    public SPacketUpdateTileEntity getDescriptionPacket() {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        doWriteToNBT(nbtTagCompound);
        int metadata = getBlockMetadata();
        return new SPacketUpdateTileEntity(this.pos, metadata, nbtTagCompound);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {

        // The description packet often arrives after render state is first cached on client
        // so we need to refresh render state once we have the server-side info.
        long oldModelKey = modelKey;
        doReadFromNBT(pkt.getNbtCompound());
        if(oldModelKey != modelKey && this.worldObj.isRemote)
        {
            this.isModelKeyCacheDirty = true;
            worldObj.markBlockRangeForRenderUpdate(pos, pos);
        }
    }
	    
    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        doReadFromNBT(compound);
        isLoaded = true;
    }

    private void doReadFromNBT(NBTTagCompound compound)
    {
        modelKey = compound.getLong(MODEL_KEY_TAG);
        placementShape = compound.getInteger(PLACEMENT_SHAPE_TAG);
        damage = compound.getByte(DAMAGE_TAG);
    }
    
    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        doWriteToNBT(compound);
    }
    
    private void doWriteToNBT(NBTTagCompound compound)
    {
        if(modelKey != 0L) compound.setLong(MODEL_KEY_TAG, modelKey);
        if(damage != 0) compound.setByte(DAMAGE_TAG, damage);
        if(placementShape != 0) compound.setInteger(PLACEMENT_SHAPE_TAG, placementShape);
    }
	
    public long getModelKey() { return modelKey; }
    public void setModelKey(long modelKey) 
    { 
        if(this.modelKey != modelKey)
        {
            this.modelKey = modelKey; 
            this.markDirty();
        }
    }
    
	public byte getDamage() { return damage; }
	public void setDamage( byte damage) 
	{ 
	    if(this.damage != damage)
	    {
    	    this.damage = damage; 
    	    this.markDirty();
	    }
    }
	
	public int getPlacementShape() { return placementShape; }
    public void setPlacementShape( int placementShape)
    { 
        if(this.placementShape != placementShape)
        {
            this.placementShape = placementShape;
            this.markDirty();
        }
    }
}
