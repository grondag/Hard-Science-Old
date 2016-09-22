package grondag.adversity.feature.volcano;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import grondag.adversity.Adversity;
import grondag.adversity.feature.volcano.BlockManager.BlockPlacement;
import grondag.adversity.feature.volcano.SpaceManager.OpenSpace;
import grondag.adversity.library.Useful;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.base.NiceBlockPlus;
import grondag.adversity.niceblock.block.FlowStaticBlock;
import grondag.adversity.niceblock.modelstate.FlowHeightState;
import grondag.adversity.simulator.Simulator;


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
	private BlockManager basaltBlocks;
	
	//private final VolcanoHazeMaker	hazeMaker		= new VolcanoHazeMaker();

	// these are all derived or ephemeral - not saved to NBT
    private boolean isLoaded = false;
	private int						hazeTimer		= 60;

	private static final int BACKTRACK_INCREMENT = 2;
	
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
        
        if (IFlowBlock.isBlockFlowFiller(state.getBlock()))
        {
            return true;
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
        
        //TODOS
        //adjust fill 2 up
        //sustain update for multiple passes until level is filled
        //leveling
        //final textures
        //fix model pinholes
        //find and fix straggle fillers (if still happening)
        //find and fix failure to flow down (if still happening)
        //performance tuning as needed
        //configurability
        //volcano wand
        //bore clearing / mounding
        //simulation integration & control from simulation
        //world gen
        //detection item
        
        if(this.worldObj.isRemote || this.stage == VolcanoStage.DORMANT) return;
        
        ticksActive++;
        this.markDirty();
                
        if(this.stage == VolcanoStage.NEW && Simulator.instance.getVolcanoManager() != null)
        {
//            this.node = Simulator.instance.getVolcanoManager().createNode();
            this.isLoaded = true;
            this.stage = VolcanoStage.ACTIVE;
            this.spaceManager = new SpaceManager(this.pos);
            this.lavaBlocks = new BlockManager(this.pos, true);
            this.basaltBlocks = new BlockManager(this.pos, false);
        }
        
        if(!isLoaded) return;
        
        if(spaceManager.getCount() == 0 && lavaBlocks.getCount() == 0 
                && basaltBlocks.getCount() == 0)
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
                backtrackLimit = this.pos.getY() + BACKTRACK_INCREMENT;
            }
        } 
        else if(spaceManager.getCount() != 0)
        {
        	OpenSpace place = spaceManager.pollFirstEntry();
//        	Adversity.log.info("found open spot @ " + place.getPos().toString() + " with origin " + place.getOrigin().toString());
        	if(place.getPos().getY() < this.backtrackLimit)
            {
                placeIfPossible(place.getPos(), place.getOrigin());
                this.backtrackLimit = Math.min(backtrackLimit, place.getPos().getY() + BACKTRACK_INCREMENT);
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
            
            BlockPlacement placement = lavaBlocks.pollFarthestReadyEntry(this.lavaCoolingIndex);

            if(placement != null)
            {
                BlockPos target = placement.getPos();
    
                IBlockState oldState = this.worldObj.getBlockState(target);
                if(oldState.getBlock() == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK)
                {
                    int meta = oldState.getValue(NiceBlock.META);
//                    long modelStateKey = NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK.getModelStateKey(state, this.worldObj, target);
                    IBlockState newState = NiceBlockRegistrar.HOT_FLOWING_BASALT_3_HEIGHT_BLOCK.getDefaultState().withProperty(NiceBlock.META, meta);
                    this.worldObj.setBlockState(target, newState);
                    this.basaltBlocks.add(target, this.ticksActive + 60 + Useful.SALT_SHAKER.nextInt(30));

    //                Adversity.log.info("place basalt meta=" + meta 
    //                        + " priorKey=" + modelStateKey
    //                        + " newKey=" + NiceBlockRegistrar.HOT_FLOWING_BASALT_3_HEIGHT_BLOCK.getModelStateKey(state, worldObj, target));
                }
                else if(oldState.getBlock() == NiceBlockRegistrar.HOT_FLOWING_LAVA_FILLER_BLOCK)
                {
                    int meta = oldState.getValue(NiceBlock.META);
//                    long modelStateKey = NiceBlockRegistrar.HOT_FLOWING_LAVA_FILLER_BLOCK.getModelStateKey(state, this.worldObj, target);
                    IBlockState newState = NiceBlockRegistrar.HOT_FLOWING_BASALT_3_FILLER_BLOCK.getDefaultState().withProperty(NiceBlock.META, meta);
                    this.worldObj.setBlockState(target, newState);
                    this.basaltBlocks.add(target, this.ticksActive + 60 + Useful.SALT_SHAKER.nextInt(30));
                }
                else if(oldState.getBlock() == NiceBlockRegistrar.HOT_FLOWING_BASALT_3_HEIGHT_BLOCK 
                        || oldState.getBlock() == NiceBlockRegistrar.HOT_FLOWING_BASALT_3_FILLER_BLOCK)
                {
                    basaltBlocks.add(placement.getPos(), this.ticksActive + 30 +  Useful.SALT_SHAKER.nextInt(30));
                }
            }
        }
        
        if(basaltBlocks.getCount() != 0)// && placedLava.lastEntry().getValue().worldTick < this.worldObj.getWorldTime())
        {
            
            BlockPlacement placement = basaltBlocks.pollFirstReadyEntry(this.ticksActive);

            if(placement != null)
            {
                BlockPos target = placement.getPos();
    
                IBlockState oldState = this.worldObj.getBlockState(target);
                
                if(oldState.getBlock() instanceof NiceBlock)
                {
                    NiceBlock oldBlock = (NiceBlock)oldState.getBlock();
                    FlowStaticBlock newBlock = getNextCoolingBlock(oldBlock);
                    
                    if(newBlock != null)
                    {
                        int meta = oldState.getValue(NiceBlock.META);
                        long modelStateKey = oldBlock.getModelStateKey(oldState, this.worldObj, target);
                        IBlockState newState = newBlock.getDefaultState().withProperty(NiceBlock.META, meta);
                        this.worldObj.setBlockState(target, newState);
                        newBlock.setModelStateKey(newState, this.worldObj, target, modelStateKey);
                        
                        if(!(newBlock == NiceBlockRegistrar.COOL_FLOWING_BASALT_FILLER_BLOCK
                                || newBlock == NiceBlockRegistrar.COOL_FLOWING_BASALT_FILLER_BLOCK))
                        {
                            basaltBlocks.add(target, ticksActive + 60 + Useful.SALT_SHAKER.nextInt(30));
                        }                   
                    }
                }
            }
        }
    }
    
    private FlowStaticBlock getNextCoolingBlock(Block blockIn)
    {
        if(blockIn == NiceBlockRegistrar.HOT_FLOWING_BASALT_3_HEIGHT_BLOCK)
            return NiceBlockRegistrar.HOT_FLOWING_BASALT_2_HEIGHT_BLOCK;
        else if(blockIn == NiceBlockRegistrar.HOT_FLOWING_BASALT_2_HEIGHT_BLOCK)
            return NiceBlockRegistrar.HOT_FLOWING_BASALT_1_HEIGHT_BLOCK;
        else if(blockIn == NiceBlockRegistrar.HOT_FLOWING_BASALT_1_HEIGHT_BLOCK)
            return NiceBlockRegistrar.HOT_FLOWING_BASALT_0_HEIGHT_BLOCK;
        else if(blockIn == NiceBlockRegistrar.HOT_FLOWING_BASALT_0_HEIGHT_BLOCK)
            return NiceBlockRegistrar.COOL_FLOWING_BASALT_HEIGHT_BLOCK;
        
        else if(blockIn == NiceBlockRegistrar.HOT_FLOWING_BASALT_3_FILLER_BLOCK)
            return NiceBlockRegistrar.HOT_FLOWING_BASALT_2_FILLER_BLOCK;
        else if(blockIn == NiceBlockRegistrar.HOT_FLOWING_BASALT_2_FILLER_BLOCK)
            return NiceBlockRegistrar.HOT_FLOWING_BASALT_1_FILLER_BLOCK;
        else if(blockIn == NiceBlockRegistrar.HOT_FLOWING_BASALT_1_FILLER_BLOCK)
            return NiceBlockRegistrar.HOT_FLOWING_BASALT_0_FILLER_BLOCK;
        else if(blockIn == NiceBlockRegistrar.HOT_FLOWING_BASALT_0_FILLER_BLOCK)
            return NiceBlockRegistrar.COOL_FLOWING_BASALT_FILLER_BLOCK;
        
        else return null;
      
    }
    
    private void placeIfPossible(BlockPos pPos, BlockPos pOrigin)
    {
//        Adversity.log.info("attempting to place @ " + pPos.toString() + " with origin " + pOrigin.toString());
        
        if(this.canDisplace(pPos))
        {
            double distanceSq = pPos.distanceSq(pOrigin);
            if(distanceSq > 49 || Useful.SALT_SHAKER.nextInt(99) < distanceSq) return;
            
            
            IBlockState targetState = NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK.getDefaultState()
                        .withProperty(NiceBlock.META, 2 * (int)Math.sqrt(distanceSq));
            
            
            this.worldObj.setBlockState(pPos, targetState);
            this.lavaBlocks.add(pPos, ++lavaCounter);
            
//            Adversity.log.info("placing " + placement.pos.toString());
            
            // don't spread sideways if can flow down or if already flowing down
            if(!addSpaceIfOpen(pPos.down(), pPos.down()))
            {
//                Adversity.log.info("skipping side placements for " + placement.pos.toString());
                
                if(isHardBasalt(this.worldObj.getBlockState(pPos.down())))
                {
                    this.worldObj.setBlockState(pPos.down(), NiceBlockRegistrar.HOT_FLOWING_BASALT_3_HEIGHT_BLOCK.getDefaultState()
                            .withProperty(NiceBlock.META, 15));
                    this.lavaBlocks.add(pPos.down(), ++lavaCounter);
                    //this.basaltBlocks.add(pPos.down(), this.ticksActive + 200 + Useful.SALT_SHAKER.nextInt(200));
                    if(isHardBasalt(this.worldObj.getBlockState(pPos.down().down())))
                    {
                        this.worldObj.setBlockState(pPos.down().down(), NiceBlockRegistrar.HOT_FLOWING_BASALT_3_HEIGHT_BLOCK.getDefaultState()
                                .withProperty(NiceBlock.META, 15));
                        this.lavaBlocks.add(pPos.down(), ++lavaCounter);
                        //this.basaltBlocks.add(pPos.down(), this.ticksActive + 200 + Useful.SALT_SHAKER.nextInt(200));
                    }
                }

                addSpaceIfOpen(pPos.east(), pOrigin);
                addSpaceIfOpen(pPos.west(), pOrigin);
                addSpaceIfOpen(pPos.north(), pOrigin);
                addSpaceIfOpen(pPos.south(), pOrigin);
            }

            meltExposedBasaltAndAdjustFill(pPos.east());
            meltExposedBasaltAndAdjustFill(pPos.west());
            meltExposedBasaltAndAdjustFill(pPos.north());
            meltExposedBasaltAndAdjustFill(pPos.south());
            
            meltExposedBasaltAndAdjustFill(pPos.east().north());
            meltExposedBasaltAndAdjustFill(pPos.west().north());
            meltExposedBasaltAndAdjustFill(pPos.east().south());
            meltExposedBasaltAndAdjustFill(pPos.west().south());
            
            adjustFillIfNeeded(pPos.up());
            
        }
        else  
        {
//            Adversity.log.info("skipping placement: not displaceable");
        }
    }
    /** 
     * Searches from two blocks above to two blocks below and returns
     * Position of upper-most flow height block, or null if none found.
     */
    private BlockPos findTopFlowBlock(BlockPos startingPos)
    {
        if(IFlowBlock.isBlockFlowHeight(this.worldObj.getBlockState(startingPos.up().up()).getBlock()))
            return startingPos.up().up();
        
        if(IFlowBlock.isBlockFlowHeight(this.worldObj.getBlockState(startingPos.up()).getBlock()))
            return startingPos.up();
        
        if(IFlowBlock.isBlockFlowHeight(this.worldObj.getBlockState(startingPos).getBlock()))
            return startingPos;
        
        if(IFlowBlock.isBlockFlowHeight(this.worldObj.getBlockState(startingPos.down()).getBlock()))
            return startingPos.down();
        
        if(IFlowBlock.isBlockFlowHeight(this.worldObj.getBlockState(startingPos.down().down()).getBlock()))
            return startingPos.down().down();
        
        return null;
    }
    
    private boolean isHardBasalt(IBlockState state)
    {
        return state.getBlock() == NiceBlockRegistrar.HOT_FLOWING_BASALT_2_HEIGHT_BLOCK
                || state.getBlock() == NiceBlockRegistrar.HOT_FLOWING_BASALT_1_HEIGHT_BLOCK
                || state.getBlock() == NiceBlockRegistrar.HOT_FLOWING_BASALT_0_HEIGHT_BLOCK
                || state.getBlock() == NiceBlockRegistrar.COOL_FLOWING_BASALT_HEIGHT_BLOCK;
    }
    
    private void meltExposedBasaltAndAdjustFill(BlockPos pos)
    {
        BlockPos targetPos = findTopFlowBlock(pos);
        
        if(targetPos == null) return;
        
        if(isHardBasalt(this.worldObj.getBlockState(targetPos)))
        {
            this.worldObj.setBlockState(targetPos, NiceBlockRegistrar.HOT_FLOWING_BASALT_3_HEIGHT_BLOCK.getDefaultState()
                    .withProperty(NiceBlock.META, this.worldObj.getBlockState(targetPos).getValue(NiceBlock.META)));
            
            this.lavaBlocks.add(targetPos, ++lavaCounter);
            
            targetPos = targetPos.down();
            
            if(isHardBasalt(this.worldObj.getBlockState(targetPos)))
            {
                this.worldObj.setBlockState(targetPos, NiceBlockRegistrar.HOT_FLOWING_BASALT_3_HEIGHT_BLOCK.getDefaultState()
                        .withProperty(NiceBlock.META, this.worldObj.getBlockState(targetPos).getValue(NiceBlock.META)));
                
                this.lavaBlocks.add(targetPos, ++lavaCounter);
            }
        }
        
        if(targetPos != null)
        {
            adjustFillIfNeeded(targetPos.up());
        }
    }
    
    private void adjustFillIfNeeded(BlockPos spaceAbove)
    {
        Block blockAbove = this.worldObj.getBlockState(spaceAbove).getBlock();
        IBlockState baseState = this.worldObj.getBlockState(spaceAbove.down());
        boolean needsFiller = IFlowBlock.needsTopFiller(baseState, this.worldObj, spaceAbove.down());
        if(blockAbove instanceof IFlowBlock && ((IFlowBlock)blockAbove).isFiller()
                && !needsFiller)
        {
            this.worldObj.setBlockToAir(spaceAbove);
        }
        else if(this.canDisplace(spaceAbove) && needsFiller)
        {
            Block fillBlock = baseState.getBlock() == NiceBlockRegistrar.HOT_FLOWING_BASALT_3_HEIGHT_BLOCK
                    ? NiceBlockRegistrar.HOT_FLOWING_BASALT_3_FILLER_BLOCK
                    : NiceBlockRegistrar.HOT_FLOWING_LAVA_FILLER_BLOCK;
            
            this.worldObj.setBlockState(spaceAbove, fillBlock.getDefaultState()
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
        this.lavaBlocks = new BlockManager(this.pos, true, tagCompound.getIntArray("lavaBlocks"));
        this.basaltBlocks = new BlockManager(this.pos, false, tagCompound.getIntArray("basaltBlocks"));
        
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
		tagCompound.setIntArray("basaltBlocks", this.basaltBlocks.getArray());      
		
//		if(this.node != null) tagCompound.setInteger("nodeId", this.node.getID());
		return super.writeToNBT(tagCompound);

		//this.hazeMaker.writeToNBT(tagCompound);

	}
}