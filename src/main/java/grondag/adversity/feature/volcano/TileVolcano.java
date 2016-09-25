package grondag.adversity.feature.volcano;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import grondag.adversity.Adversity;
import grondag.adversity.config.Config;
import grondag.adversity.feature.volcano.BlockManager.BlockPlacement;
import grondag.adversity.feature.volcano.SpaceManager.OpenSpace;
import grondag.adversity.library.Useful;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.base.NiceBlockPlus;
import grondag.adversity.niceblock.block.FlowDynamicBlock;
import grondag.adversity.niceblock.block.FlowStaticBlock;
import grondag.adversity.niceblock.support.BaseMaterial;
import grondag.adversity.simulator.Simulator;

//TEST
//bore clearing / mounding

//TODOS
//fix model pinholes
//fix lighting normals
//water rendering
//final textures


//detection item
//volcano wand
//remove block item/item model
//simulation integration & control from simulation
//world gen
//biome
//ignite surrounding blocks
//sound effects
//smoke
//haze
//ash

//performance tuning as needed
  

public class TileVolcano extends TileEntity implements ITickable{

	// Activity cycle.
	private VolcanoStage stage = VolcanoStage.NEW;
	private int						level;
	private int						buildLevel;
	private int						groundLevel;
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
    private BlockManager topBlocks;
	private BlockManager coolingBlocks;
	
	//private final VolcanoHazeMaker	hazeMaker		= new VolcanoHazeMaker();

	// these are all derived or ephemeral - not saved to NBT
    private boolean isLoaded = false;
	private int						hazeTimer		= 60;
	
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


    
    private boolean isWithinBore(BlockPos posIn)
    {
        return posIn.distanceSq(new BlockPos(this.pos.getX(), posIn.getY(), this.pos.getZ())) <= Config.volcano().boreRadiusSquared;
    }
    
	private boolean canDisplace(BlockPos pos) 
	{

        IBlockState state = this.worldObj.getBlockState(pos);
        Material material = state.getMaterial();
        
        if(material == Material.AIR) return true;
        
        Block block = state.getBlock();
        
        if(block == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK) return false;
        
        if(pos.getY() == this.level && isWithinBore(pos)) return true;
        
        if (IFlowBlock.isBlockFlowFiller(state.getBlock())) return true;

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
            this.topBlocks = new BlockManager(this.pos, true);
            this.coolingBlocks = new BlockManager(this.pos, false);
            this.cooldownTicks = 0;
            int moundRadius = Config.volcano().moundRadius;
            this.groundLevel = Useful.getAvgHeight(this.worldObj, this.pos, moundRadius, moundRadius * moundRadius / 10);
        }
        
        if(!isLoaded) return;
        
        int blockTrackingCount = spaceManager.getCount() + lavaBlocks.getCount() + coolingBlocks.getCount() + topBlocks.getCount();
        
        if(blockTrackingCount == 0)
        {
            this.buildLevel = this.level;
            BlockPos startingPos = new BlockPos(this.getPos().getX(), this.level, this.getPos().getZ());
            placeIfPossible(startingPos, startingPos);
            backtrackLimit = level;
            
            if(spaceManager.getCount() == 0)
            {
                if(this.level < Config.volcano().maxYLevel)
                {
                    startLavaCoolingAndPause(true);
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
            else if(placementCountdown == 0 && blockTrackingCount <= Config.volcano().blockTrackingMax)
            {
                OpenSpace place = spaceManager.pollFirst();
            	int y = place.getPos().getY();
            	
            	if(y < this.backtrackLimit || y == this.level)
                {
            	    if(y == this.level)
        	        {
        	            backtrackLimit = this.level;
        	            
        	            //if back at top, enable cooling of blocks so far
        	            if(this.buildLevel < y && this.lavaBlocks.getCount() > 0)
        	            {
        	                startLavaCoolingAndPause(false);
        	            }
        	        }
                    placeIfPossible(place.getPos(), place.getOrigin());
                    placementCountdown = Config.volcano().baseTicksPerBlock;
                    if(Config.volcano().randTicksPerBlock > 0) placementCountdown += Useful.SALT_SHAKER.nextInt(Config.volcano().randTicksPerBlock);
                    this.buildLevel = Math.min(buildLevel, y);
                    this.backtrackLimit = Math.min(backtrackLimit, y + Config.volcano().backtrackIncrement);
                }
            }
            else if(placementCountdown == 0 && (lavaBlocks.getCount() + topBlocks.getCount()) >= Config.volcano().blockTrackingMax)
            {
                //Handle special (and hopefully rare) case where we have max blocks
                //and they are all lava blocks.  If this ever happens, have to enable
                //cooling so that the tracking count can go down.
                startLavaCoolingAndPause(false);
            }
        }
        else
        {
            // no more lava to place in this stream, enable cooling
            if(this.lavaBlocks.getCount() > 0)
            {
                startLavaCoolingAndPause(false);
            }
        }
         
        if(coolingBlocks.getCount() != 0)// && placedLava.lastEntry().getValue().worldTick < this.worldObj.getWorldTime())
        {
            
            BlockPlacement placement = coolingBlocks.pollFirstReadyEntry(this.ticksActive);

            while(placement != null)
            {
                BlockPos target = placement.getPos();
    
                IBlockState oldState = this.worldObj.getBlockState(target);
                
                if(oldState.getBlock() instanceof NiceBlock && !isWithinBore(target))
                {
                    NiceBlock oldBlock = (NiceBlock)oldState.getBlock();
                    
                    if(oldBlock == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK
                            || oldBlock == NiceBlockRegistrar.HOT_FLOWING_LAVA_FILLER_BLOCK)
                    {   
                        NiceBlock newBlock = oldBlock == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK
                                ? NiceBlockRegistrar.HOT_FLOWING_BASALT_3_HEIGHT_BLOCK
                                :NiceBlockRegistrar.HOT_FLOWING_BASALT_3_FILLER_BLOCK;
                    
                        int meta = oldState.getValue(NiceBlock.META);
                        IBlockState newState = newBlock.getDefaultState().withProperty(NiceBlock.META, meta);
                        this.worldObj.setBlockState(target, newState);
                        this.coolingBlocks.add(target, this.ticksActive + Config.volcano().coolingLagTicks);                      
                    }
                    else
                    {
                        if(isFullyStaticCube(target, oldState))
                        {
                            // use simple cubic basalt if not exposed and can't influence any flow blocks
                            this.worldObj.setBlockState(target, NiceBlockRegistrar.COOL_SQUARE_BASALT_BLOCK.getDefaultState());
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
                                    coolingBlocks.add(target, ticksActive + Config.volcano().coolingLagTicks);
                                }                   
                            }
                        }
                    }
                }
                placement = coolingBlocks.pollFirstReadyEntry(this.ticksActive);
            }
        }
    }
    
    private void startLavaCoolingAndPause(boolean includeTopBlocks)
    {
        this.cooldownTicks += lavaBlocks.getCount() * (Config.volcano().baseTicksPerBlock + (Config.volcano().randTicksPerBlock + 1)/2);
        this.coolingBlocks.transferWithOffset(lavaBlocks,  ticksActive, Config.volcano().baseTicksPerBlock, Config.volcano().randTicksPerBlock);

        if(includeTopBlocks)
        {
            this.cooldownTicks += (topBlocks.getCount() - Config.volcano().boreRadiusSquared * Math.PI ) * (Config.volcano().baseTicksPerBlock 
                    + (Config.volcano().randTicksPerBlock + 1)/2);
            this.coolingBlocks.transferWithOffset(topBlocks,  ticksActive, Config.volcano().baseTicksPerBlock, Config.volcano().randTicksPerBlock);
        }
    }
    
    private boolean isFullyStaticCube(BlockPos pos, IBlockState state)
    {
        
        if(!IFlowBlock.isFullCube(state, worldObj, pos)) return false;
        
        for(int x = -1; x <= 1; x++)
        {
            for(int z = -1; z <= 1; z++)
            {
                for(int y = -2; y <= 2; y++)
                {
                    if(!(x == 0 && y == 0 && z == 0))
                    {
                        if(worldObj.getBlockState(pos.add(x, y, z)) instanceof FlowDynamicBlock) return false;
                    }
                }
            }
        }
        
        return true;
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
        if(isWithinBore(pPos)) clearBore(pPos);
        
        if(this.canDisplace(pPos))
        {
            double distanceSq = pPos.distanceSq(pOrigin);
            int meta;
            if(pPos.getY() == this.level)
            {
                if(distanceSq > Config.volcano().topSpreadRadiusSquared 
                        || (distanceSq > Config.volcano().boreRadiusSquared && Useful.SALT_SHAKER.nextInt(Config.volcano().boreRadiusSquared * 3) < distanceSq)) return;
                meta = (int)Math.sqrt(distanceSq);
            }
            else 
            {
                if(distanceSq > Config.volcano().lavaSpreadRadiusSquared || Useful.SALT_SHAKER.nextInt(99) < distanceSq) return;
                meta = 2 * (int)Math.sqrt(distanceSq);
            }
            
            
            meta = Math.min(15, Math.max(1, meta - 1 + Useful.SALT_SHAKER.nextInt(3)));
            IBlockState targetState = NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK.getDefaultState()
                        .withProperty(NiceBlock.META, meta);
            
            
            this.worldObj.setBlockState(pPos, targetState);
            trackLavaBlock(pPos);
            
//            Adversity.log.info("placing " + placement.pos.toString());
            
            // don't spread sideways if can flow down or if already flowing down
            if(!addSpaceIfOpen(pPos.down(), pPos.down()))
            {
//                Adversity.log.info("skipping side placements for " + placement.pos.toString());
                
                if(isHardBasalt(this.worldObj.getBlockState(pPos.down()).getBlock()))
                {
                    this.worldObj.setBlockState(pPos.down(), NiceBlockRegistrar.HOT_FLOWING_BASALT_3_HEIGHT_BLOCK.getDefaultState()
                            .withProperty(NiceBlock.META, 15));
                    trackLavaBlock(pPos.down());
                    //this.basaltBlocks.add(pPos.down(), this.ticksActive + 200 + Useful.SALT_SHAKER.nextInt(200));
                    if(isHardBasalt(this.worldObj.getBlockState(pPos.down(2)).getBlock()))
                    {
                        this.worldObj.setBlockState(pPos.down(2), NiceBlockRegistrar.HOT_FLOWING_BASALT_3_HEIGHT_BLOCK.getDefaultState()
                                .withProperty(NiceBlock.META, 15));
                        trackLavaBlock(pPos.down(2));
                        //this.basaltBlocks.add(pPos.down(), this.ticksActive + 200 + Useful.SALT_SHAKER.nextInt(200));
                    }
                }


                addSpaceIfOpen(pPos.east(), pOrigin);
                addSpaceIfOpen(pPos.west(), pOrigin);
                addSpaceIfOpen(pPos.north(), pOrigin);
                addSpaceIfOpen(pPos.south(), pOrigin);
                
            }

            for(int x = -1; x <= 1; x++)
            {
                for(int z = -1; z <= 1; z++)
                {
                    for(int y = -3; y <= 5; y++)
                    {
                        if(!(x == 0 && y == 0 && z == 0))
                        {
                            meltExposedBasalt(pPos.add(x, y, z));
                        }
                    }
                }
            }
            
            for(int x = -1; x <= 1; x++)
            {
                for(int z = -1; z <= 1; z++)
                {
                    for(int y = -3; y <= 5; y++)
                    {
                        if(!(x == 0 && y == 0 && z == 0))
                        {
                            adjustFillIfNeeded(pPos.add(x, y, z));
                        }
                    }
                }
            }
        }
    }
    
    private void clearBore(BlockPos clearPos)
    {
         if(!this.worldObj.isAirBlock(clearPos))
        {
            this.worldObj.setBlockToAir(clearPos);
            if(clearPos.getY() < this.groundLevel)
            {
                buildMound();
            }
        }
    }
    
    private void buildMound()
    {
        BlockPos top = findMoundSpot();
        if(top == null) return;
        
        //allow drops of trees and such
        this.worldObj.destroyBlock(top.up(), true); 
        
        IBlockState state = worldObj.getBlockState(top);

        while(!canDisplace(top) && state.getBlock() != Blocks.BEDROCK
                && top.getY() >= 0)
        {
            this.worldObj.setBlockState(top.up(), state);
            top = top.down();
            state = this.worldObj.getBlockState(top);
        }
        //avoid duplication of valuable blocks by clever nerds
        this.worldObj.setBlockState(top.up(), worldObj.getBiomeGenForCoords(top).fillerBlock);
    }
    
    private BlockPos findMoundSpot()
    {
        int dx = (int) (Useful.SALT_SHAKER.nextGaussian() * Config.volcano().moundRadius);
        int dz = (int) (Useful.SALT_SHAKER.nextGaussian() * Config.volcano().moundRadius);
        double lastDistance = 0;
        BlockPos center = new BlockPos(this.pos.getX(), this.level, this.pos.getZ());
        BlockPos best = null;
        for(int i = 0; i < 20; i++)
        {
            dx = (int) (Useful.SALT_SHAKER.nextGaussian() * Config.volcano().moundRadius);
            dz = (int) (Useful.SALT_SHAKER.nextGaussian() * Config.volcano().moundRadius);
            BlockPos candidate = this.worldObj.getHeight(this.pos.east(dx).north(dz));
            while(candidate.getY() > 0 && canDisplace(candidate))
            {
                candidate = candidate.down();
            }
            Block block = worldObj.getBlockState(candidate).getBlock();
            boolean isLava = (block == NiceBlockRegistrar.HOT_FLOWING_LAVA_HEIGHT_BLOCK
                    || block == NiceBlockRegistrar.HOT_FLOWING_LAVA_FILLER_BLOCK);
            if(!isLava)
            {
                if(best == null)
                {
                    best = candidate;
                    lastDistance = candidate.distanceSq(center);
                }
                else
                {
                    double newDistance = candidate.distanceSq(center);
                    if(newDistance < lastDistance)
                    {
                        lastDistance = newDistance;
                        best = candidate;
                    }
                }
            }
        }
        return best;
    }

    private boolean isHardBasalt(Block block)
    {
        return (block instanceof NiceBlockPlus 
                && block instanceof IFlowBlock
                && ((NiceBlockPlus)(block)).material == BaseMaterial.BASALT)
                || block == NiceBlockRegistrar.COOL_SQUARE_BASALT_BLOCK;
    }
    
    private void meltExposedBasalt(BlockPos targetPos)
    {
        if(targetPos == null) return;
        
        Block block = worldObj.getBlockState(targetPos).getBlock();
        if(isHardBasalt(block))
        {
            if(block == NiceBlockRegistrar.COOL_SQUARE_BASALT_BLOCK)
            {
                this.worldObj.setBlockState(targetPos, NiceBlockRegistrar.COOL_SQUARE_BASALT_BLOCK.getDefaultState());
            }
            else
            {
                NiceBlock targetBlock = IFlowBlock.isBlockFlowHeight(block)
                        ? NiceBlockRegistrar.HOT_FLOWING_BASALT_3_HEIGHT_BLOCK
                        : NiceBlockRegistrar.HOT_FLOWING_BASALT_3_FILLER_BLOCK;
                
                this.worldObj.setBlockState(targetPos, targetBlock.getDefaultState()
                        .withProperty(NiceBlock.META, this.worldObj.getBlockState(targetPos).getValue(NiceBlock.META)));
            }
            
            trackLavaBlock(targetPos);
        }
    }
    
    private void trackLavaBlock(BlockPos lavaPos)
    {
        if(lavaPos.getY() >= level)
            this.topBlocks.add(lavaPos, ++lavaCounter);
        else
            this.lavaBlocks.add(lavaPos, ++lavaCounter);
    }

    
    private void adjustFillIfNeeded(BlockPos basePos)
    {
        final int SHOULD_BE_AIR = -1;
        
        IBlockState baseState = this.worldObj.getBlockState(basePos);
        Block baseBlock = baseState.getBlock();
        Block fillBlock = null;
        
        int targetMeta = SHOULD_BE_AIR;
        
        /**
         * If space is occupied with a non-displaceable block, will be ignored.
         * Otherwise, possible target states: air, fill +1, fill +2
         * 
         * Should be fill +1 if block below is a heightblock and needs a fill >= 1;
         * Should be a fill +2 if block below is not a heightblock and block
         * two below needs a fill = 2;
         * Otherwise should be air.
         */
        IBlockState stateBelow = this.worldObj.getBlockState(basePos.down());
        if(IFlowBlock.isBlockFlowHeight(stateBelow.getBlock()) 
                && IFlowBlock.topFillerNeeded(stateBelow, worldObj, basePos.down()) > 0)
        {
            targetMeta = 0;
            fillBlock = stateBelow.getBlock() == NiceBlockRegistrar.HOT_FLOWING_BASALT_3_HEIGHT_BLOCK
                    ? NiceBlockRegistrar.HOT_FLOWING_BASALT_3_FILLER_BLOCK
                    : NiceBlockRegistrar.HOT_FLOWING_LAVA_FILLER_BLOCK;
        }
        else 
        {
            IBlockState stateTwoBelow = this.worldObj.getBlockState(basePos.down(2));
            if((IFlowBlock.isBlockFlowHeight(stateTwoBelow.getBlock()) 
                    && IFlowBlock.topFillerNeeded(stateTwoBelow, worldObj, basePos.down(2)) == 2))
            {
                targetMeta = 1;
                fillBlock = stateTwoBelow.getBlock() == NiceBlockRegistrar.HOT_FLOWING_BASALT_3_HEIGHT_BLOCK
                        ? NiceBlockRegistrar.HOT_FLOWING_BASALT_3_FILLER_BLOCK
                        : NiceBlockRegistrar.HOT_FLOWING_LAVA_FILLER_BLOCK;
            }
        }
        
        if(IFlowBlock.isBlockFlowFiller(baseBlock))
        {
            if(targetMeta == SHOULD_BE_AIR)
            {
                worldObj.setBlockToAir(basePos);
            }
            else if(baseState.getValue(NiceBlock.META) != targetMeta || baseBlock != fillBlock && fillBlock != null)
            {
                worldObj.setBlockState(basePos, fillBlock.getDefaultState()
                        .withProperty(NiceBlock.META, targetMeta));
                trackLavaBlock(basePos);
            }
                //confirm filler needed and adjust/remove if needed
        }
        else if(targetMeta != SHOULD_BE_AIR && canDisplace(basePos) && fillBlock != null)
        {
            worldObj.setBlockState(basePos, fillBlock.getDefaultState()
                    .withProperty(NiceBlock.META, targetMeta));
            trackLavaBlock(basePos);
        }
         
    }
    
    private boolean addSpaceIfOpen(BlockPos posIn, BlockPos origin)
    {
//        Adversity.log.info("attempting to add open space @ " + posIn.toString() + " with origin " + origin.toString());
        
        if(canDisplace(posIn))
        {
            spaceManager.add(posIn, origin);
            return true;
        }
        else
        {            
            return false;
        }
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
		this.groundLevel = tagCompound.getInteger("groundLevel");
		this.ticksActive = tagCompound.getInteger("ticksActive");
		this.backtrackLimit = tagCompound.getInteger("backtrackLimit");
		this.lavaCounter = tagCompound.getInteger("lavaCounter");
		this.cooldownTicks = tagCompound.getInteger("cooldownTicks");
		
        this.spaceManager = new SpaceManager(this.pos, tagCompound.getIntArray("spaceManager"));
        this.lavaBlocks = new BlockManager(this.pos, true, tagCompound.getIntArray("lavaBlocks"));
        this.topBlocks = new BlockManager(this.pos, true, tagCompound.getIntArray("topBlocks"));
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
		tagCompound.setInteger("groundLevel", this.groundLevel);
		tagCompound.setInteger("ticksActive", this.ticksActive);
		tagCompound.setInteger("backtrackLimit", this.backtrackLimit);
		tagCompound.setInteger("lavaCounter", this.lavaCounter);
		tagCompound.setInteger("cooldownTicks", cooldownTicks);
		
		tagCompound.setIntArray("spaceManager", this.spaceManager.getArray());
		tagCompound.setIntArray("lavaBlocks", this.lavaBlocks.getArray());
	    tagCompound.setIntArray("topBlocks", this.topBlocks.getArray());
		tagCompound.setIntArray("basaltBlocks", this.coolingBlocks.getArray());      
		
//		if(this.node != null) tagCompound.setInteger("nodeId", this.node.getID());
		return super.writeToNBT(tagCompound);

		//this.hazeMaker.writeToNBT(tagCompound);

	}
}