package grondag.adversity.niceblock.newmodel;

import grondag.adversity.Adversity;
import grondag.adversity.library.NeighborBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class NiceTileEntity extends TileEntity{
    public ModelState modelState = new ModelState();
    
    /** used by big blocks */
    public int placementShape;
	public IExtendedBlockState exBlockState;
	public boolean isClientShapeIndexDirty = true;
	public boolean isLoaded = false;
	public boolean isDeleted = false;

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) 
	{
		if(this.worldObj.isRemote)
		{
			isDeleted = true;
//			Adversity.log.info("shouldRefresh " + pos.toString());
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
			updateClientRenderState();
		}
	}
	
	@SideOnly(Side.CLIENT)
	private void updateClientRenderState()
	{
		this.isClientShapeIndexDirty = true;

		updatify(pos.up());
		updatify(pos.down());
		updatify(pos.east());
		updatify(pos.west());
		updatify(pos.north());
		updatify(pos.south());

		updatify(pos.up().east());
		updatify(pos.up().west());
		updatify(pos.up().north());
		updatify(pos.up().south());

		updatify(pos.down().east());
		updatify(pos.down().west());
		updatify(pos.down().north());
		updatify(pos.down().south());

		updatify(pos.north().east());
		updatify(pos.north().west());
		updatify(pos.south().east());
		updatify(pos.south().west());
	}
	
	@SideOnly(Side.CLIENT)
	private void updatify(BlockPos updatePos)
	{
//		Adversity.log.info("updatify attempt @ " + updatePos.toString());
		TileEntity target = worldObj.getTileEntity(updatePos);
		if(target != null && target instanceof NiceTileEntity && !((NiceTileEntity)target).isClientShapeIndexDirty)
		{
//			Adversity.log.info("updatify success @ " + updatePos.toString());
			((NiceTileEntity)target).isClientShapeIndexDirty = true;
			worldObj.markBlockForUpdate(updatePos);
		}
	}

   @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        doWriteToNBT(nbtTagCompound);
        int metadata = getBlockMetadata();
        return new S35PacketUpdateTileEntity(this.pos, metadata, nbtTagCompound);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        doReadFromNBT(pkt.getNbtCompound());
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
        modelState.readFromNBT(compound);
        placementShape = compound.getInteger(BigBlockHelper.PLACEMENT_SHAPE_TAG);
    }
    
    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        doWriteToNBT(compound);
    }
    
    private void doWriteToNBT(NBTTagCompound compound)
    {
        modelState.writeToNBT(compound);
        compound.setInteger(BigBlockHelper.PLACEMENT_SHAPE_TAG, placementShape);
    }
	
	
}
