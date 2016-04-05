package grondag.adversity.feature.volcano;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import grondag.adversity.Adversity;
import grondag.adversity.library.Useful;
import grondag.adversity.niceblock.NiceBlockRegistrar;


public class TileVolcano extends TileEntity implements ITickable{

	// Activity cycle.
	private int						state;
	private int						level;
	private int						timer;
	private int						buildLevel;
	private int						levelsTilDormant;

	private final VolcanoHazeMaker	hazeMaker		= new VolcanoHazeMaker();

	// these are all derived or ephemeral - not saved to NBT
	private int						chatTimer;
	private int						hazeTimer		= 60;

	public final int				INACTIVE		= 0;
	public final int				NEW_LEVEL		= 4;
	public final int				BUILDING_INNER	= 1;
	public final int				BUILDING_LOWER	= 2;
	public final int				TESTING_OUTER	= 3;
	public final int				DORMANT			= 5;

	private void placeBlockIfNeeded(BlockPos pos, IBlockState state) {
	    if(worldObj.getBlockState(pos) != state)
		{
			this.worldObj.setBlockState(pos, state);
		}
	}

	private boolean isBlockOpen(BlockPos pos, boolean allowSourceLava) {
		final Block b = this.worldObj.getBlockState(pos).getBlock();
		final int lavaMeta = allowSourceLava ? -1 : 0;
		final int m = this.worldObj.getBlockMetadata(x, y, z);
		return b == Volcano.blockVolcanicLava && m != lavaMeta || b.getMaterial() == Material.air;
	}

	// true if relatively few nearby blocks at this level would stop lava
	private boolean areOuterBlocksOpen(int y) {

		int closedCount = 0;
		final int THRESHOLD = 16;

		for (int x = this.xCoord - 7; x <= this.xCoord + 7; ++x) {
			for (int z = this.zCoord - 7; z <= this.zCoord + 7; ++z) {
				if (!(this.worldObj.getBlock(x, y, z) == Volcano.blockVolcanicLava || Volcano.blockVolcanicLava
						.canDisplace(this.worldObj, x, y, z))) {
					++closedCount;
					if (closedCount >= THRESHOLD) {
						break;
					}
				}
			}
		}

		return closedCount < THRESHOLD;

	}

	private boolean areInnerBlocksOpen(int y) {
		return this.isBlockOpen(this.xCoord, y, this.zCoord, true)
				&& this.isBlockOpen(this.xCoord - 2, y, this.zCoord - 1, true)
				&& this.isBlockOpen(this.xCoord - 2, y, this.zCoord, true)
				&& this.isBlockOpen(this.xCoord - 2, y, this.zCoord + 1, true) &&

				this.isBlockOpen(this.xCoord - 1, y, this.zCoord - 2, true)
				&& this.isBlockOpen(this.xCoord - 1, y, this.zCoord - 1, true)
				&& this.isBlockOpen(this.xCoord - 1, y, this.zCoord, true)
				&& this.isBlockOpen(this.xCoord - 1, y, this.zCoord + 1, true)
				&& this.isBlockOpen(this.xCoord - 1, y, this.zCoord + 2, true) &&

				this.isBlockOpen(this.xCoord, y, this.zCoord - 2, true)
				&& this.isBlockOpen(this.xCoord, y, this.zCoord - 1, true)
				&& this.isBlockOpen(this.xCoord, y, this.zCoord + 1, true)
				&& this.isBlockOpen(this.xCoord, y, this.zCoord + 2, true) &&

				this.isBlockOpen(this.xCoord + 1, y, this.zCoord - 2, true)
				&& this.isBlockOpen(this.xCoord + 1, y, this.zCoord - 1, true)
				&& this.isBlockOpen(this.xCoord + 1, y, this.zCoord, true)
				&& this.isBlockOpen(this.xCoord + 1, y, this.zCoord + 1, true)
				&& this.isBlockOpen(this.xCoord + 1, y, this.zCoord + 2, true) &&

				this.isBlockOpen(this.xCoord + 2, y, this.zCoord - 1, true)
				&& this.isBlockOpen(this.xCoord + 2, y, this.zCoord, true)
				&& this.isBlockOpen(this.xCoord + 2, y, this.zCoord + 1, true);
	}

	private void blowOut(int distanceFromCenter, int blastRadius) {
		int dx = 0;
		int dz = 0;

		switch (this.worldObj.rand.nextInt(8)) {
		case 0:
			dx = -1;
			dz = -1;
			break;
		case 1:
			dx = -1;
			dz = 0;
			break;
		case 2:
			dx = -1;
			dz = 1;
			break;
		case 3:
			dx = 0;
			dz = -1;
			break;
		case 4:
			dx = 0;
			dz = 1;
			break;
		case 5:
			dx = 1;
			dz = -1;
			break;
		case 6:
			dx = 1;
			dz = 0;
			break;
		case 7:
			dx = 1;
			dz = 1;
			break;
		}
		final int x = this.xCoord + dx * distanceFromCenter;
		final int z = this.zCoord + dz * distanceFromCenter;
		//new FlyingBlocksExplosion(this.worldObj, x, this.level - 1, z, blastRadius).doExplosion();
		Useful.fill2dCircleInPlaneXZ(this.worldObj, x, this.level - 2, z, blastRadius, NiceBlockRegistrar.BLOCK_HOT_BASALT.getDefaultState());
	}

	private void makeHaze() {
		if (this.hazeTimer > 0) {
			--this.hazeTimer;
		} else {
			this.hazeTimer = 5;
			this.hazeMaker.update(this.worldObj, this.xCoord, this.zCoord);
			// if(worldObj.rand.nextInt(3)==0){
			// worldObj.setBlock(xCoord+2-worldObj.rand.nextInt(5), level+2, zCoord+2-worldObj.rand.nextInt(5),
			// Adversity.blockHazeRising, 0, 2);
			// worldObj.scheduleBlockUpdate(xCoord, level+2, zCoord, Adversity.blockHazeRising, 15);
			// }
		}
	}

	@Override
	public void update() {
		if (!this.worldObj.isRemote) {
			this.markDirty();

			if (this.chatTimer <= 0) {
				Adversity.log.info("State=" + this.state + "  Timer=" + this.timer);
				this.chatTimer = 200;

			} else {
				--this.chatTimer;
			}

			this.makeHaze();

			if (this.timer > 0) {
				--this.timer;
			} else if (this.state == this.INACTIVE) {

				this.level = this.getPos().getY();

				++this.level;

				this.levelsTilDormant = 80 - this.level;
				this.state = this.NEW_LEVEL;
				this.timer = 20;

			} else if (this.state == this.DORMANT) {

				if (this.level >= 240) {
					this.timer = 10000;
					return;
				}
				this.levelsTilDormant = this.worldObj.rand.nextInt(3);// + Math.max(0, (80-level)/2);
				if (this.level >= 70) {
					this.blowOut(4, 5);
				}
				this.state = this.BUILDING_INNER;
				this.timer = 20;

			} else if (this.state == this.NEW_LEVEL) {

				if (!this.areInnerBlocksOpen(this.level)) {
					new FlyingBlocksExplosion(this.worldObj, this.xCoord, this.level - 1, this.zCoord, 5).doExplosion();
				}
				this.state = this.BUILDING_INNER;
				this.timer = 20;

			} else if (this.state == this.BUILDING_INNER) {

				if (this.buildLevel <= this.yCoord || this.buildLevel > this.level) {
					this.buildLevel = this.yCoord + 1;
				}
				OddUtils.fill2dCircleInPlaneXZ(this.worldObj, this.xCoord, this.buildLevel, this.zCoord, 3,
						Volcano.blockVolcanicLava, 0);
				if (this.buildLevel < this.level) {
					++this.buildLevel;
				} else {
					this.buildLevel = 0;
					this.state = this.TESTING_OUTER;
				}
				this.timer = 20;

			} else if (this.state == this.TESTING_OUTER) {

				if (this.areOuterBlocksOpen(this.level)) {
					this.state = this.BUILDING_INNER;
					this.timer = 20;
				} else if (this.levelsTilDormant == 0) {
					this.state = this.DORMANT;
					this.timer = 20 * 60 * 25;
				} else {
					++this.level;
					--this.levelsTilDormant;
					this.state = this.NEW_LEVEL;
					this.timer = 20;
				}
			}

		}
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		this.state = tagCompound.getInteger("state");
		this.level = tagCompound.getInteger("level");
		this.timer = tagCompound.getInteger("timer");
		this.buildLevel = tagCompound.getInteger("buildLevel");
		this.levelsTilDormant = tagCompound.getInteger("levelsTilDormant");

		this.hazeMaker.readFromNBT(tagCompound);
	}

	@Override
	public void writeToNBT(NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		tagCompound.setInteger("state", this.state);
		tagCompound.setInteger("level", this.level);
		tagCompound.setInteger("timer", this.timer);
		tagCompound.setInteger("buildLevel", this.buildLevel);
		tagCompound.setInteger("levelsTilDormant", this.levelsTilDormant);

		this.hazeMaker.writeToNBT(tagCompound);

	}
}