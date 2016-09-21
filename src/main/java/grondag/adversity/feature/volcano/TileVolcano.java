package grondag.adversity.feature.volcano;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.TreeMap;

import grondag.adversity.Adversity;
import grondag.adversity.feature.volcano.BlockManager.BlockPlacement;
import grondag.adversity.feature.volcano.SpaceManager.OpenSpace;
//import grondag.adversity.Adversity;
import grondag.adversity.library.RelativeBlockPos;
import grondag.adversity.library.Useful;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.simulator.Simulator;
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
	private int backtrackLimit = 0;
	private int ticksActive = 0;
	private int lavaCounter = 0;
	private int lavaCoolingIndex = 0;
	
//	private VolcanoNode             node;
    
	private SpaceManager spaceManager;
	private BlockManager lavaBlocks;
	
	//private final VolcanoHazeMaker	hazeMaker		= new VolcanoHazeMaker();

	// these are all derived or ephemeral - not saved to NBT
    private boolean isLoaded = false;
	private int						hazeTimer		= 60;

	
	private static enum VolcanoStage
	{
	    NEW,
	    DORMANT,
	    ACTIVE,
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

//	private boolean isBlockOpen(BlockPos pos, boolean allowSourceLava) {
//	    final IBlockState state = this.worldObj.getBlockState(pos);
//		final Block block = state.getBlock();
//		return block.isAir(state, this.worldObj, pos) 
//		        ||  (block instanceof BlockVolcanicLava && (allowSourceLava || !((BlockVolcanicLava)block).isSourceBlock(this.worldObj, pos)));
//	}

	// true if relatively few nearby blocks at this level would stop lava
//	private boolean areOuterBlocksOpen(int y) {
//
//		int closedCount = 0;
//		final int THRESHOLD = 16;
//
//		for (int x = this.pos.getX() - 7; x <= this.pos.getX() + 7; ++x) {
//			for (int z = this.pos.getZ() - 7; z <= this.pos.getZ() + 7; ++z) {
//				if (!(this.worldObj.getBlockState(pos).getBlock() == Volcano.blockVolcanicLava 
//				        || Volcano.blockVolcanicLava.canDisplace(this.worldObj, pos))) {
//					++closedCount;
//					if (closedCount >= THRESHOLD) {
//						break;
//					}
//				}
//			}
//		}
//
//		return closedCount < THRESHOLD;
//
//	}

//	private boolean areInnerBlocksOpen(int y) {
//		return this.isBlockOpen(new BlockPos(this.pos.getX(), y, this.pos.getZ()), true)
//				&& this.isBlockOpen(new BlockPos(this.pos.getX() - 2, y, this.pos.getZ() - 1), true)
//				&& this.isBlockOpen(new BlockPos(this.pos.getX() - 2, y, this.pos.getZ()), true)
//				&& this.isBlockOpen(new BlockPos(this.pos.getX() - 2, y, this.pos.getZ() + 1), true) &&
//
//				this.isBlockOpen(new BlockPos(this.pos.getX() - 1, y, this.pos.getZ() - 2), true)
//				&& this.isBlockOpen(new BlockPos(this.pos.getX() - 1, y, this.pos.getZ() - 1), true)
//				&& this.isBlockOpen(new BlockPos(this.pos.getX() - 1, y, this.pos.getZ()), true)
//				&& this.isBlockOpen(new BlockPos(this.pos.getX() - 1, y, this.pos.getZ() + 1), true)
//				&& this.isBlockOpen(new BlockPos(this.pos.getX() - 1, y, this.pos.getZ() + 2), true) &&
//
//				this.isBlockOpen(new BlockPos(this.pos.getX(), y, this.pos.getZ() - 2), true)
//				&& this.isBlockOpen(new BlockPos(this.pos.getX(), y, this.pos.getZ() - 1), true)
//				&& this.isBlockOpen(new BlockPos(this.pos.getX(), y, this.pos.getZ() + 1), true)
//				&& this.isBlockOpen(new BlockPos(this.pos.getX(), y, this.pos.getZ() + 2), true) &&
//
//				this.isBlockOpen(new BlockPos(this.pos.getX() + 1, y, this.pos.getZ() - 2), true)
//				&& this.isBlockOpen(new BlockPos(this.pos.getX() + 1, y, this.pos.getZ() - 1), true)
//				&& this.isBlockOpen(new BlockPos(this.pos.getX() + 1, y, this.pos.getZ()), true)
//				&& this.isBlockOpen(new BlockPos(this.pos.getX() + 1, y, this.pos.getZ() + 1), true)
//				&& this.isBlockOpen(new BlockPos(this.pos.getX() + 1, y, this.pos.getZ() + 2), true) &&
//
//				this.isBlockOpen(new BlockPos(this.pos.getX() + 2, y, this.pos.getZ() - 1), true)
//				&& this.isBlockOpen(new BlockPos(this.pos.getX() + 2, y, this.pos.getZ()), true)
//				&& this.isBlockOpen(new BlockPos(this.pos.getX() + 2, y, this.pos.getZ() + 1), true);
//	}

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
		Useful.fill2dCircleInPlaneXZ(this.worldObj, x, this.level - 2, z, blastRadius, NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK.getDefaultState());
	}

	private void makeHaze() {
		if (this.hazeTimer > 0) {
			--this.hazeTimer;
		} else {
			this.hazeTimer = 5;
			//this.hazeMaker.update(this.worldObj, this.pos.getX(), this.pos.getZ());
			
			// if(worldObj.rand.nextInt(3)==0){
			// worldObj.setBlock(xCoord+2-worldObj.rand.nextInt(5), level+2, zCoord+2-worldObj.rand.nextInt(5),
			// Adversity.blockHazeRising, 0, 2);
			// worldObj.scheduleBlockUpdate(xCoord, level+2, zCoord, Adversity.blockHazeRising, 15);
			// }
		}
	}


    
    private boolean canDisplace(BlockPos pos) {

        IBlockState state = this.worldObj.getBlockState(pos);
        
        if (state.getBlock() == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK)
        {
            return false;
        }

        Material material = state.getMaterial();
        if (material == Material.CLAY) return false;
        if (material == Material.DRAGON_EGG ) return false;
        if (material == Material.GROUND ) return false;
        if (material == Material.IRON ) return false;
        if (material == Material.SAND ) return false;
        if (material == Material.PORTAL ) return false;
        if (material == Material.ROCK ) return false;
        if (material == Material.ANVIL ) return false;
        if (material == Material.GRASS ) return false;

        // Volcanic lava don't give no shits.
        return true;        

    };
	
    public void update() 
    {
        if(this.worldObj.isRemote || this.stage == VolcanoStage.DORMANT) return;
        
        ticksActive++;
        this.markDirty();
                
        if(this.stage == VolcanoStage.NEW && Simulator.instance.getVolcanoManager() != null)
        {
//            this.node = Simulator.instance.getVolcanoManager().createNode();
            this.isLoaded = true;
            this.stage = VolcanoStage.ACTIVE;
            this.spaceManager = new SpaceManager(this.pos);
            this.lavaBlocks = new BlockManager(this.pos);
        }
        
        if(!isLoaded) return;
        
        if(spaceManager.getCount() == 0 && lavaBlocks.getCount() == 0)
        {
            placeIfPossible(this.getPos().up(), this.getPos().up());
            if(spaceManager.getCount() == 0)
            {
                this.stage = VolcanoStage.DORMANT;
                //TODO: remove
                Adversity.log.info("Volcano DORMANT");
            }
            else
            {
                backtrackLimit = this.pos.getY() + 3;
            }
        } 
        else if(spaceManager.getCount() != 0)
        {
        	OpenSpace place = spaceManager.pollFirstEntry();
//        	Adversity.log.info("found open spot @ " + place.getPos().toString() + " with origin " + place.getOrigin().toString());
        	if(place.getPos().getY() < this.backtrackLimit)
            {
                placeIfPossible(place.getPos(), place.getOrigin());
                this.backtrackLimit = Math.min(backtrackLimit, place.getPos().getY() + 3);
            }
        	else
        	{
//                Adversity.log.info("skipping spot due to backtrackLimit " + this.backtrackLimit);
        	}
        }
        else
        {
            // no more lava to place in this stream, enable cooling
            this.lavaCoolingIndex = this.lavaCounter;
        }
        
        // this should always be true if we get to this point - really just for clarity
        if(lavaBlocks.getCount() != 0)// && placedLava.lastEntry().getValue().worldTick < this.worldObj.getWorldTime())
        {
            
            BlockPlacement placement = lavaBlocks.pollLastReadyEntry(this.lavaCoolingIndex);

            if(placement != null)
            {
                BlockPos target = placement.getPos();
    
                IBlockState state = this.worldObj.getBlockState(target);
                if(this.worldObj.getBlockState(target).getBlock() == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK)
                {
                    int meta = state.getValue(NiceBlock.META);
                    long modelStateKey = NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK.getModelStateKey(state, this.worldObj, target);
                    state = NiceBlockRegistrar.HOT_FLOWING_BASALT_3_HEIGHT_BLOCK.getDefaultState().withProperty(NiceBlock.META, meta);
                    this.worldObj.setBlockState(target, state);
//                    NiceBlockRegistrar.HOT_FLOWING_BASALT_3_HEIGHT_BLOCK.setModelStateKey(state, this.worldObj, target, modelStateKey);
    //                Adversity.log.info("place basalt meta=" + meta 
    //                        + " priorKey=" + modelStateKey
    //                        + " newKey=" + NiceBlockRegistrar.HOT_FLOWING_BASALT_3_HEIGHT_BLOCK.getModelStateKey(state, worldObj, target));
                }
                else if(this.worldObj.getBlockState(target).getBlock() == NiceBlockRegistrar.HOT_FLOWING_LAVA_FILLER_BLOCK)
                {
                    int meta = state.getValue(NiceBlock.META);
                    long modelStateKey = NiceBlockRegistrar.HOT_FLOWING_LAVA_FILLER_BLOCK.getModelStateKey(state, this.worldObj, target);
                    state = NiceBlockRegistrar.HOT_FLOWING_BASALT_3_FILLER_BLOCK.getDefaultState().withProperty(NiceBlock.META, meta);
                    this.worldObj.setBlockState(target, state);
//                    NiceBlockRegistrar.HOT_FLOWING_BASALT_3_FILLER_BLOCK.setModelStateKey(state, this.worldObj, target, modelStateKey);
                }
            }
        }
    }
    
    private void placeIfPossible(BlockPos pPos, BlockPos pOrigin)
    {
//        Adversity.log.info("attempting to place @ " + pPos.toString() + " with origin " + pOrigin.toString());
        
        if(this.canDisplace(pPos))
        {
            double distanceSq = pPos.distanceSq(pOrigin);
            if(distanceSq > 49 || Useful.SALT_SHAKER.nextInt(99) < distanceSq) return;
            
            
            this.worldObj.setBlockState(pPos, NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK.getDefaultState()
                    .withProperty(NiceBlock.META, 2 * (int)Math.sqrt(distanceSq)));
            this.lavaBlocks.add(pPos, ++lavaCounter);
            
//            Adversity.log.info("placing " + placement.pos.toString());
            
            // don't spread sideways if can flow down or if already flowing down
            if(!(addSpaceIfOpen(pPos.down(), pPos.down())
                    || this.worldObj.getBlockState(pPos.down()).getBlock() == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK))
            {
//                Adversity.log.info("skipping side placements for " + placement.pos.toString());
                
                addSpaceIfOpen(pPos.east(), pOrigin);
                addSpaceIfOpen(pPos.west(), pOrigin);
                addSpaceIfOpen(pPos.north(), pOrigin);
                addSpaceIfOpen(pPos.south(), pOrigin);
            }
            
            fillIfNeeded(pPos.up());
            
            fillIfNeeded(pPos.east());
            fillIfNeeded(pPos.west());
            fillIfNeeded(pPos.north());
            fillIfNeeded(pPos.south());
            
            fillIfNeeded(pPos.east().north());
            fillIfNeeded(pPos.west().north());
            fillIfNeeded(pPos.east().south());
            fillIfNeeded(pPos.west().south());
        }
        else
        {
//            Adversity.log.info("skipping placement: not displaceable");
        }
    }
    
    private void fillIfNeeded(BlockPos spaceAbove)
    {
        if(this.canDisplace(spaceAbove) && IFlowBlock.needsTopFiller(this.worldObj.getBlockState(spaceAbove.down()), this.worldObj, spaceAbove.down()))
        {
            this.worldObj.setBlockState(spaceAbove, NiceBlockRegistrar.HOT_FLOWING_LAVA_FILLER_BLOCK.getDefaultState()
                    .withProperty(NiceBlock.META, 3));
            this.lavaBlocks.add(spaceAbove, ++lavaCounter);
        }
    }
    
    private boolean addSpaceIfOpen(BlockPos posIn, BlockPos origin)
    {
//        Adversity.log.info("attempting to add open space @ " + posIn.toString() + " with origin " + origin.toString());
        
        if(!canDisplace(posIn))
        {
//            Adversity.log.info("space not added: not displacable");
            return false;
        }
        
        spaceManager.add(posIn, origin);
        return true;

    }
	
//	@Override
//	public void updateOld() {
//	    if(this.worldObj.isRemote) return;
//	    
//	    
//	    // dead volcanoes don't do anything
//	    if(stage == VolcanoStage.DEAD) return;
//        
//        if(weight < Integer.MAX_VALUE) weight++;
//	    
//        // everything after this point only happens 1x every 16 ticks.
//        if ((this.worldObj.getTotalWorldTime() & 15) != 15) return;
//
//        this.markDirty();
//
// //       if (node.isActive()) {
//
//            this.markDirty();
//
//			if ((this.worldObj.getTotalWorldTime() & 255) == 255) {
//				Adversity.log.info("Volcanot State @" + this.pos.toString() + " = " + this.stage);
//			}
//
//			this.makeHaze();
//			
//			if (this.stage == VolcanoStage.NEW) 
//			{
//				this.level = this.getPos().getY();
//
//				++this.level;
//
//				this.levelsTilDormant = 80 - this.level;
//				this.stage = VolcanoStage.NEW_LEVEL;
//			}
//			else if (this.stage == VolcanoStage.DORMANT) 
//			{
//
//			    // Simulation shouldn't reactivate a volcano that is already at build limit
//			    // but deactivate if it somehow does.
//				if (this.level >= VolcanoManager.VolcanoNode.MAX_VOLCANO_HEIGHT) 
//				{
//				    this.weight = 0;
//				    this.stage = VolcanoStage.DEAD;
//					this.node.deActivate();
//					return;
//				}
//				
//				int window = VolcanoManager.VolcanoNode.MAX_VOLCANO_HEIGHT - this.level;
//				int interval = Math.min(1, window / 10);
//				
//                // always grow at least 10% of the available window
//                // plus 0 to 36% with a total average around 28%
//				this.levelsTilDormant = Math.min(window,
//				        interval
//				        + this.worldObj.rand.nextInt(interval)
//				        + this.worldObj.rand.nextInt(interval)
//				        + this.worldObj.rand.nextInt(interval)
//				        + this.worldObj.rand.nextInt(interval));
//
//				if (this.level >= 70) {
//					this.blowOut(4, 5);
//				}
//				this.stage = VolcanoStage.BUILDING_INNER;
//			} 
//			else if (this.stage == VolcanoStage.NEW_LEVEL) 
//			{
//				if (!this.areInnerBlocksOpen(this.level)) {
//				    this.worldObj.createExplosion(null, this.pos.getX(), this.level - 1, this.pos.getZ(), 5, true);
//				}
//				this.stage = VolcanoStage.BUILDING_INNER;
//			} 
//			else if (this.stage == VolcanoStage.BUILDING_INNER) 
//			{
//				if (this.buildLevel <= this.pos.getY() || this.buildLevel > this.level) {
//					this.buildLevel = this.pos.getY() + 1;
//				}
//				Useful.fill2dCircleInPlaneXZ(this.worldObj, this.pos.getX(), this.buildLevel, this.pos.getZ(), 3,
//						Volcano.blockVolcanicLava.getDefaultState());
//				if (this.buildLevel < this.level) {
//					++this.buildLevel;
//				} else {
//					this.buildLevel = 0;
//					this.stage = VolcanoStage.TESTING_OUTER;
//				}
//			}
//			else if (this.stage == VolcanoStage.TESTING_OUTER)
//			{
//				if (this.areOuterBlocksOpen(this.level))
//				{
//					this.stage = VolcanoStage.BUILDING_INNER;
//				}
//				else if (this.levelsTilDormant == 0)
//				{
//					this.stage = VolcanoStage.DORMANT;
//					if (this.level >= VolcanoManager.VolcanoNode.MAX_VOLCANO_HEIGHT) 
//	                {
//	                    this.weight = 0;
//	                    this.stage = VolcanoStage.DEAD;
//	                }
//					this.node.deActivate();
//				}
//				else 
//				{
//					++this.level;
//					--this.levelsTilDormant;
//					this.stage = VolcanoStage.NEW_LEVEL;
//				}
//			}
////		}
//        
//        node.updateWorldState(this.weight, this.level);
//	}
	
    @Override
	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		this.stage = VolcanoStage.values()[tagCompound.getInteger("stage")];
		this.level = tagCompound.getInteger("level");
		this.weight= tagCompound.getInteger("weight");
		this.buildLevel = tagCompound.getInteger("buildLevel");
		this.levelsTilDormant = tagCompound.getInteger("levelsTilDormant");
		this.ticksActive = tagCompound.getInteger("ticksActive");
		this.backtrackLimit = tagCompound.getInteger("backtrackLimit");
		this.lavaCounter = tagCompound.getInteger("lavaCounter");
		this.lavaCoolingIndex = tagCompound.getInteger("lavaCoolingIndex");
		
        this.spaceManager = new SpaceManager(this.pos, tagCompound.getIntArray("spaceManager"));
        this.lavaBlocks = new BlockManager(this.pos, tagCompound.getIntArray("lavaBlocks"));
		
//		int nodeId = tagCompound.getInteger("nodeId");
//		
//		if(nodeId != 0)
//		{
//		    this.node = Simulator.instance.getVolcanoManager().findNode(nodeId);
//		    if(this.node == null)
//		    {
//		        Adversity.log.warn("Unable to load volcano simulation node for volcano at " + this.pos.toString()
//		        + ". Created new simulation node.  Simulation state was lost.");
//		    }
//		}
//		
//		if(nodeId == 0 || this.node == null)
//		{
//		    this.node = Simulator.instance.getVolcanoManager().createNode();
//		    this.markDirty();
//		}

		this.isLoaded = true;
		//this.hazeMaker.readFromNBT(tagCompound);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
		tagCompound.setInteger("stage", this.stage.ordinal());
		tagCompound.setInteger("level", this.level);
	    tagCompound.setInteger("weight", this.weight);
		tagCompound.setInteger("buildLevel", this.buildLevel);
		tagCompound.setInteger("levelsTilDormant", this.levelsTilDormant);
		tagCompound.setInteger("ticksActive", this.ticksActive);
		tagCompound.setInteger("backtrackLimit", this.backtrackLimit);
		tagCompound.setInteger("lavaCounter", this.lavaCounter);
		tagCompound.setInteger("lavaCoolingIndex", lavaCoolingIndex);
		
		tagCompound.setIntArray("spaceManager", this.spaceManager.getArray());
		tagCompound.setIntArray("lavaBlocks", this.lavaBlocks.getArray());
	      
		
//		if(this.node != null) tagCompound.setInteger("nodeId", this.node.getID());
		return super.writeToNBT(tagCompound);

		//this.hazeMaker.writeToNBT(tagCompound);

	}
}