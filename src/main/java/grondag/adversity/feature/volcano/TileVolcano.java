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
import grondag.adversity.library.NeighborBlocks.HorizontalCorner;
import grondag.adversity.library.Useful;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.base.NiceBlockPlus;
import grondag.adversity.niceblock.block.FlowStaticBlock;
import grondag.adversity.niceblock.modelstate.FlowHeightState;
import grondag.adversity.simulator.Simulator;

//TEST
//random heights
//leveling
//fix incomplete fillers
//adjust fill 2 up

//TODOS
//replace fully enclosed blocks with regular cube blocks
//bore clearing / mounding

//fix block item model
//fix model pinholes
//fix lighting normals
//final textures

//simulation integration & control from simulation
//detection item
//volcano wand
//world gen
//biome
//sound effects
//smoke
//haze
//ash



//performance tuning as needed
//configurability      

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
	private int cooldownTicks = 0;
	private int placementCountdown = 0;
	
//	private VolcanoNode             node;
    
	private SpaceManager spaceManager;
	private BlockManager lavaBlocks;
	private BlockManager coolingBlocks;
	
	//private final VolcanoHazeMaker	hazeMaker		= new VolcanoHazeMaker();

	// these are all derived or ephemeral - not saved to NBT
    private boolean isLoaded = false;
	private int						hazeTimer		= 60;

	private static final int BACKTRACK_INCREMENT = 2;
	private static final int BASE_TICKS_PER_BLOCK = 1;
	private static final int RAND_TICKS_PER_BLOCK = 0;
	private static final int COOLING_LAG_TICKS = 100;
	private static final int BLOCK_TRACKING_MAX = 4000;
	private static final int MAX_Y_LEVEL = 213;
	
	private static enum VolcanoStage
	{
	    NEW,
	    FLOWING,
	    /** Flow temporarily stopped to allow for cooling. */
	    COOLING,
	    DORMANT,
//	    ACTIVE,
//	    NEW_LEVEL,
//	    BUILDING_INNER,
//	    BUILDING_LOWER,
//	    TESTING_OUTER,
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

        // Volcanic lava don't give no shits about your stuff.
        return true;        

    };
	
    public void update() 
    {
        if(this.worldObj.isRemote || this.stage == VolcanoStage.DORMANT) return;
        
        ticksActive++;
        if(placementCountdown > 0) placementCountdown--;
        
        this.markDirty();
                
        if(this.stage == VolcanoStage.NEW && Simulator.instance.getVolcanoManager() != null)
        {
//            this.node = Simulator.instance.getVolcanoManager().createNode();
            this.isLoaded = true;
            this.stage = VolcanoStage.FLOWING;
            this.level = this.pos.up().getY();
            this.spaceManager = new SpaceManager(this.pos);
            this.lavaBlocks = new BlockManager(this.pos, true);
            this.coolingBlocks = new BlockManager(this.pos, false);
            this.cooldownTicks = 0;
        }
        
        if(!isLoaded) return;
        
        int blockTrackingCount = spaceManager.getCount() + lavaBlocks.getCount() + coolingBlocks.getCount();
        
        if(blockTrackingCount == 0)
        {
            this.buildLevel = this.level;
            BlockPos startingPos = new BlockPos(this.getPos().getX(), this.level, this.getPos().getZ());
            placeIfPossible(startingPos, startingPos);
            backtrackLimit = level;
            
            if(spaceManager.getCount() == 0)
            {
                if(this.level < MAX_Y_LEVEL)
                {
                    this.level++;
                }
                else
                {
                    this.stage = VolcanoStage.DORMANT;
                    //TODO: remove
                    Adversity.log.info("Volcano DORMANT");
                }
            }
        } 
        else if(spaceManager.getCount() != 0)
        {
            if(cooldownTicks > 0)
            {
                cooldownTicks--;
            }
            else if(placementCountdown == 0 && blockTrackingCount <= BLOCK_TRACKING_MAX)
            {
                OpenSpace place = spaceManager.pollFirstEntry();
            	int y = place.getPos().getY();
            	
            	if(y < this.backtrackLimit || y == this.level)
                {
            	    if(y == this.level)
        	        {
        	            backtrackLimit = this.level;
        	            
        	            //if back at top, enable cooling of blocks so far
        	            if(this.buildLevel < y && this.lavaBlocks.getCount() > 0)
        	            {
        	                startLavaCoolingAndPause();
        	            }
        	        }
                    placeIfPossible(place.getPos(), place.getOrigin());
                    placementCountdown = BASE_TICKS_PER_BLOCK;
                    if(RAND_TICKS_PER_BLOCK > 0) placementCountdown += Useful.SALT_SHAKER.nextInt(RAND_TICKS_PER_BLOCK);
                    this.buildLevel = Math.min(buildLevel, y);
                    this.backtrackLimit = Math.min(backtrackLimit, y + BACKTRACK_INCREMENT);
                }
            }
            else if(placementCountdown == 0 && lavaBlocks.getCount() >= BLOCK_TRACKING_MAX)
            {
                //Handle special (and hopefully rare) case where we have max blocks
                //and they are all lava blocks.  If this ever happens, have to enable
                //cooling so that the tracking count can go down.
                startLavaCoolingAndPause();
            }
        }
        else
        {
            // no more lava to place in this stream, enable cooling
            if(this.lavaBlocks.getCount() > 0)
            {
                startLavaCoolingAndPause();
            }
        }
         
        if(coolingBlocks.getCount() != 0)// && placedLava.lastEntry().getValue().worldTick < this.worldObj.getWorldTime())
        {
            
            BlockPlacement placement = coolingBlocks.pollFirstReadyEntry(this.ticksActive);

            while(placement != null)
            {
                BlockPos target = placement.getPos();
    
                IBlockState oldState = this.worldObj.getBlockState(target);
                
                if(oldState.getBlock() instanceof NiceBlock)
                {
                    NiceBlock oldBlock = (NiceBlock)oldState.getBlock();
                    
                    if(oldBlock == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK
                            || oldBlock == NiceBlockRegistrar.HOT_FLOWING_LAVA_FILLER_BLOCK)
                    {   
                        if(target.getY() < level || target.distanceSq(new BlockPos(this.pos.getX(), level, this.pos.getZ())) > 49)
                        {
                    
                            NiceBlock newBlock = oldBlock == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK
                                    ? NiceBlockRegistrar.HOT_FLOWING_BASALT_3_HEIGHT_BLOCK
                                    :NiceBlockRegistrar.HOT_FLOWING_BASALT_3_FILLER_BLOCK;
                        
                            int meta = oldState.getValue(NiceBlock.META);
                            IBlockState newState = newBlock.getDefaultState().withProperty(NiceBlock.META, meta);
                            this.worldObj.setBlockState(target, newState);
                            this.coolingBlocks.add(target, this.ticksActive + COOLING_LAG_TICKS);
                        }
                    }
                    else
                    {
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
                                coolingBlocks.add(target, ticksActive + COOLING_LAG_TICKS);
                            }                   
                        }
                    }
                }
                placement = coolingBlocks.pollFirstReadyEntry(this.ticksActive);
            }
        }
    }
    
    private void startLavaCoolingAndPause()
    {
        this.cooldownTicks = lavaBlocks.getCount() * (BASE_TICKS_PER_BLOCK + (RAND_TICKS_PER_BLOCK + 1)/2);
        this.coolingBlocks.transferWithOffset(lavaBlocks,  ticksActive, BASE_TICKS_PER_BLOCK, RAND_TICKS_PER_BLOCK);
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
            
            int meta = 2 * (int)Math.sqrt(distanceSq);
            meta = Math.min(15, Math.max(1, meta - 1 + Useful.SALT_SHAKER.nextInt(3)));
            IBlockState targetState = NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK.getDefaultState()
                        .withProperty(NiceBlock.META, meta);
            
            
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

            BlockPos[] neighbors = new BlockPos[HorizontalCorner.values().length];
            for(int i = 0; i < neighbors.length; i++)
            {
                neighbors[i] = findTopFlowBlock(pPos.add(HorizontalCorner.values()[i].directionVector));
                if(neighbors[i] != null) meltExposedBasalt(neighbors[i]);
            }
            
            for(int i = 0; i < neighbors.length; i++)
            {
                if(neighbors[i] != null) adjustFillIfNeeded(neighbors[i]);

            }
            adjustFillIfNeeded(pPos);
        }
    }
    /** 
     * Searches from two blocks above to two blocks below and returns
     * Position of upper-most flow height block, or null if none found.
     */
    private BlockPos findTopFlowBlock(BlockPos startingPos)
    {
        BlockPos testPos = startingPos.up().up();
        
        for(int i = 0; i < 5; i++)
        {
            if(IFlowBlock.isBlockFlowHeight(this.worldObj.getBlockState(testPos).getBlock())) return testPos;
            testPos = testPos.down();
        }
        
        return null;
    }
    
    private boolean isHardBasalt(IBlockState state)
    {
        return state.getBlock() == NiceBlockRegistrar.HOT_FLOWING_BASALT_2_HEIGHT_BLOCK
                || state.getBlock() == NiceBlockRegistrar.HOT_FLOWING_BASALT_1_HEIGHT_BLOCK
                || state.getBlock() == NiceBlockRegistrar.HOT_FLOWING_BASALT_0_HEIGHT_BLOCK
                || state.getBlock() == NiceBlockRegistrar.COOL_FLOWING_BASALT_HEIGHT_BLOCK;
    }
    
    private void meltExposedBasalt(BlockPos targetPos)
    {
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
    }
    
    private void adjustFillIfNeeded(BlockPos basePos)
    {
        IBlockState baseState = this.worldObj.getBlockState(basePos);
        int fillerNeeded = IFlowBlock.topFillerNeeded(baseState, this.worldObj, basePos);
        Block fillBlock = baseState.getBlock() == NiceBlockRegistrar.HOT_FLOWING_BASALT_3_HEIGHT_BLOCK
                ? NiceBlockRegistrar.HOT_FLOWING_BASALT_3_FILLER_BLOCK
                : NiceBlockRegistrar.HOT_FLOWING_LAVA_FILLER_BLOCK;
        
        BlockPos spaceAbove = basePos.up();
        Block blockAbove = this.worldObj.getBlockState(spaceAbove).getBlock();
        
        BlockPos spaceTwoAbove = spaceAbove.up();
        Block blockTwoAbove = this.worldObj.getBlockState(spaceTwoAbove).getBlock();
        
        if(IFlowBlock.isBlockFlowFiller(blockAbove) && fillerNeeded == 0)
        {
            this.worldObj.setBlockToAir(spaceAbove);
        }
        else if(fillerNeeded > 0 && this.canDisplace(spaceAbove))
        {
            this.worldObj.setBlockState(spaceAbove, fillBlock.getDefaultState()
                    .withProperty(NiceBlock.META, 3));
            this.lavaBlocks.add(spaceAbove, ++lavaCounter);
            if(IFlowBlock.isFullCube(worldObj.getBlockState(spaceAbove), worldObj, spaceAbove))
                Adversity.log.info("wut?");
        }
        
        if(IFlowBlock.isBlockFlowFiller(blockTwoAbove) && fillerNeeded < 2)
        {
            this.worldObj.setBlockToAir(spaceTwoAbove);
        }
        else if(fillerNeeded == 2 && this.canDisplace(spaceTwoAbove))
        {
            this.worldObj.setBlockState(spaceTwoAbove, fillBlock.getDefaultState()
                    .withProperty(NiceBlock.META, 4));
            this.lavaBlocks.add(spaceTwoAbove, ++lavaCounter);
            if(IFlowBlock.isFullCube(worldObj.getBlockState(spaceTwoAbove), worldObj, spaceTwoAbove))
                Adversity.log.info("wut?");

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
		this.cooldownTicks = tagCompound.getInteger("cooldownTicks");
		
        this.spaceManager = new SpaceManager(this.pos, tagCompound.getIntArray("spaceManager"));
        this.lavaBlocks = new BlockManager(this.pos, true, tagCompound.getIntArray("lavaBlocks"));
        this.coolingBlocks = new BlockManager(this.pos, false, tagCompound.getIntArray("basaltBlocks"));
        
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
		tagCompound.setInteger("cooldownTicks", cooldownTicks);
		
		tagCompound.setIntArray("spaceManager", this.spaceManager.getArray());
		tagCompound.setIntArray("lavaBlocks", this.lavaBlocks.getArray());
		tagCompound.setIntArray("basaltBlocks", this.coolingBlocks.getArray());      
		
//		if(this.node != null) tagCompound.setInteger("nodeId", this.node.getID());
		return super.writeToNBT(tagCompound);

		//this.hazeMaker.writeToNBT(tagCompound);

	}
}