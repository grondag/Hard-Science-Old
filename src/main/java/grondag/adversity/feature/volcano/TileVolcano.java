package grondag.adversity.feature.volcano;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import grondag.adversity.Adversity;
import grondag.adversity.library.Useful;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.simulator.Simulator;
import grondag.adversity.simulator.VolcanoManager;
import grondag.adversity.simulator.VolcanoManager.VolcanoNode;


public class TileVolcano extends TileEntity implements ITickable{

	// Activity cycle.
	private VolcanoStage stage = VolcanoStage.NEW;
	private int						level;
	private int						buildLevel;
	private int						levelsTilDormant;
	/** Weight used by simulation to calculate odds of activation. 
	 * Incremented whenever this TE is loaded and ticks. */
	private int                     weight = 2400000;
	
	private VolcanoNode             node;

	private final VolcanoHazeMaker	hazeMaker		= new VolcanoHazeMaker();

	// these are all derived or ephemeral - not saved to NBT
	private int						hazeTimer		= 60;
	
	private static enum VolcanoStage
	{
	    NEW,
	    DORMANT,
	    NEW_LEVEL,
	    BUILDING_INNER,
	    BUILDING_LOWER,
	    TESTING_OUTER,
	    DEAD
	}

//	private void placeBlockIfNeeded(BlockPos pos, IBlockState state) {
//	    if(worldObj.getBlockState(pos) != state)
//		{
//			this.worldObj.setBlockState(pos, state);
//		}
//	}

	private boolean isBlockOpen(BlockPos pos, boolean allowSourceLava) {
	    final IBlockState state = this.worldObj.getBlockState(pos);
		final Block block = state.getBlock();
		return block.isAir(state, this.worldObj, pos) 
		        ||  (block instanceof BlockVolcanicLava && (allowSourceLava || !((BlockVolcanicLava)block).isSourceBlock(this.worldObj, pos)));
	}

	// true if relatively few nearby blocks at this level would stop lava
	private boolean areOuterBlocksOpen(int y) {

		int closedCount = 0;
		final int THRESHOLD = 16;

		for (int x = this.pos.getX() - 7; x <= this.pos.getX() + 7; ++x) {
			for (int z = this.pos.getZ() - 7; z <= this.pos.getZ() + 7; ++z) {
				if (!(this.worldObj.getBlockState(pos).getBlock() == Volcano.blockVolcanicLava 
				        || Volcano.blockVolcanicLava.canDisplace(this.worldObj, pos))) {
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
		return this.isBlockOpen(new BlockPos(this.pos.getX(), y, this.pos.getZ()), true)
				&& this.isBlockOpen(new BlockPos(this.pos.getX() - 2, y, this.pos.getZ() - 1), true)
				&& this.isBlockOpen(new BlockPos(this.pos.getX() - 2, y, this.pos.getZ()), true)
				&& this.isBlockOpen(new BlockPos(this.pos.getX() - 2, y, this.pos.getZ() + 1), true) &&

				this.isBlockOpen(new BlockPos(this.pos.getX() - 1, y, this.pos.getZ() - 2), true)
				&& this.isBlockOpen(new BlockPos(this.pos.getX() - 1, y, this.pos.getZ() - 1), true)
				&& this.isBlockOpen(new BlockPos(this.pos.getX() - 1, y, this.pos.getZ()), true)
				&& this.isBlockOpen(new BlockPos(this.pos.getX() - 1, y, this.pos.getZ() + 1), true)
				&& this.isBlockOpen(new BlockPos(this.pos.getX() - 1, y, this.pos.getZ() + 2), true) &&

				this.isBlockOpen(new BlockPos(this.pos.getX(), y, this.pos.getZ() - 2), true)
				&& this.isBlockOpen(new BlockPos(this.pos.getX(), y, this.pos.getZ() - 1), true)
				&& this.isBlockOpen(new BlockPos(this.pos.getX(), y, this.pos.getZ() + 1), true)
				&& this.isBlockOpen(new BlockPos(this.pos.getX(), y, this.pos.getZ() + 2), true) &&

				this.isBlockOpen(new BlockPos(this.pos.getX() + 1, y, this.pos.getZ() - 2), true)
				&& this.isBlockOpen(new BlockPos(this.pos.getX() + 1, y, this.pos.getZ() - 1), true)
				&& this.isBlockOpen(new BlockPos(this.pos.getX() + 1, y, this.pos.getZ()), true)
				&& this.isBlockOpen(new BlockPos(this.pos.getX() + 1, y, this.pos.getZ() + 1), true)
				&& this.isBlockOpen(new BlockPos(this.pos.getX() + 1, y, this.pos.getZ() + 2), true) &&

				this.isBlockOpen(new BlockPos(this.pos.getX() + 2, y, this.pos.getZ() - 1), true)
				&& this.isBlockOpen(new BlockPos(this.pos.getX() + 2, y, this.pos.getZ()), true)
				&& this.isBlockOpen(new BlockPos(this.pos.getX() + 2, y, this.pos.getZ() + 1), true);
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
		final int x = this.pos.getX() + dx * distanceFromCenter;
		final int z = this.pos.getZ() + dz * distanceFromCenter;
		//new FlyingBlocksExplosion(this.worldObj, x, this.level - 1, z, blastRadius).doExplosion();
		Useful.fill2dCircleInPlaneXZ(this.worldObj, x, this.level - 2, z, blastRadius, NiceBlockRegistrar.BLOCK_HOT_BASALT.getDefaultState());
	}

	private void makeHaze() {
		if (this.hazeTimer > 0) {
			--this.hazeTimer;
		} else {
			this.hazeTimer = 5;
			this.hazeMaker.update(this.worldObj, this.pos.getX(), this.pos.getZ());
			// if(worldObj.rand.nextInt(3)==0){
			// worldObj.setBlock(xCoord+2-worldObj.rand.nextInt(5), level+2, zCoord+2-worldObj.rand.nextInt(5),
			// Adversity.blockHazeRising, 0, 2);
			// worldObj.scheduleBlockUpdate(xCoord, level+2, zCoord, Adversity.blockHazeRising, 15);
			// }
		}
	}

	@Override
	public void update() {
	    if(this.worldObj.isRemote) return;
	    
	    // dead volcanoes don't do anything
	    if(stage == VolcanoStage.DEAD) return;
        
        if(weight < Integer.MAX_VALUE) weight++;
	    
        // everything after this point only happens 1x every 16 ticks.
        if ((this.worldObj.getTotalWorldTime() & 15) != 15) return;

        this.markDirty();

        if (node.isActive()) {

            this.markDirty();

			if ((this.worldObj.getTotalWorldTime() & 255) == 255) {
				Adversity.log.info("Volcanot State @" + this.pos.toString() + " = " + this.stage);
			}

			this.makeHaze();
			
			if (this.stage == VolcanoStage.NEW) 
			{
				this.level = this.getPos().getY();

				++this.level;

				this.levelsTilDormant = 80 - this.level;
				this.stage = VolcanoStage.NEW_LEVEL;
			}
			else if (this.stage == VolcanoStage.DORMANT) 
			{

			    // Simulation shouldn't reactivate a volcano that is already at build limit
			    // but deactivate if it somehow does.
				if (this.level >= VolcanoManager.VolcanoNode.MAX_VOLCANO_HEIGHT) 
				{
				    this.weight = 0;
				    this.stage = VolcanoStage.DEAD;
					this.node.deActivate();
					return;
				}
				
				int window = VolcanoManager.VolcanoNode.MAX_VOLCANO_HEIGHT - this.level;
				int interval = Math.min(1, window / 10);
				
                // always grow at least 10% of the available window
                // plus 0 to 36% with a total average around 28%
				this.levelsTilDormant = Math.min(window,
				        interval
				        + this.worldObj.rand.nextInt(interval)
				        + this.worldObj.rand.nextInt(interval)
				        + this.worldObj.rand.nextInt(interval)
				        + this.worldObj.rand.nextInt(interval));

				if (this.level >= 70) {
					this.blowOut(4, 5);
				}
				this.stage = VolcanoStage.BUILDING_INNER;
			} 
			else if (this.stage == VolcanoStage.NEW_LEVEL) 
			{
				if (!this.areInnerBlocksOpen(this.level)) {
				    this.worldObj.createExplosion(null, this.pos.getX(), this.level - 1, this.pos.getZ(), 5, true);
				}
				this.stage = VolcanoStage.BUILDING_INNER;
			} 
			else if (this.stage == VolcanoStage.BUILDING_INNER) 
			{
				if (this.buildLevel <= this.pos.getY() || this.buildLevel > this.level) {
					this.buildLevel = this.pos.getY() + 1;
				}
				Useful.fill2dCircleInPlaneXZ(this.worldObj, this.pos.getX(), this.buildLevel, this.pos.getZ(), 3,
						Volcano.blockVolcanicLava.getDefaultState());
				if (this.buildLevel < this.level) {
					++this.buildLevel;
				} else {
					this.buildLevel = 0;
					this.stage = VolcanoStage.TESTING_OUTER;
				}
			}
			else if (this.stage == VolcanoStage.TESTING_OUTER)
			{
				if (this.areOuterBlocksOpen(this.level))
				{
					this.stage = VolcanoStage.BUILDING_INNER;
				}
				else if (this.levelsTilDormant == 0)
				{
					this.stage = VolcanoStage.DORMANT;
					if (this.level >= VolcanoManager.VolcanoNode.MAX_VOLCANO_HEIGHT) 
	                {
	                    this.weight = 0;
	                    this.stage = VolcanoStage.DEAD;
	                }
					this.node.deActivate();
				}
				else 
				{
					++this.level;
					--this.levelsTilDormant;
					this.stage = VolcanoStage.NEW_LEVEL;
				}
			}
		}
        
        node.updateWorldState(this.weight, this.level);
	}
	
    @Override
	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		this.stage = VolcanoStage.values()[tagCompound.getInteger("stage")];
		this.level = tagCompound.getInteger("level");
		this.weight= tagCompound.getInteger("weight");
		this.buildLevel = tagCompound.getInteger("buildLevel");
		this.levelsTilDormant = tagCompound.getInteger("levelsTilDormant");
		int nodeId = tagCompound.getInteger("nodeId");
		
		if(nodeId != 0)
		{
		    this.node = Simulator.instance.getVolcanoManager().findNode(nodeId);
		    if(this.node == null)
		    {
		        Adversity.log.warn("Unable to load volcano simulation node for volcano at " + this.pos.toString()
		        + ". Created new simulation node.  Simulation state was lost.");
		    }
		}
		
		if(nodeId == 0 || this.node == null)
		{
		    this.node = Simulator.instance.getVolcanoManager().createNode();
		    this.markDirty();
		}

		this.hazeMaker.readFromNBT(tagCompound);
	}

	@Override
	public void writeToNBT(NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		tagCompound.setInteger("stage", this.stage.ordinal());
		tagCompound.setInteger("level", this.level);
	    tagCompound.setInteger("weight", this.weight);
		tagCompound.setInteger("buildLevel", this.buildLevel);
		tagCompound.setInteger("levelsTilDormant", this.levelsTilDormant);
		tagCompound.setInteger("nodeId", this.node.getID());

		this.hazeMaker.writeToNBT(tagCompound);

	}
}