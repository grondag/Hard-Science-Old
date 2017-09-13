package grondag.hard_science.machines;

import java.util.List;

import grondag.hard_science.Configurator;
import grondag.hard_science.Log;
import grondag.hard_science.init.ModBlocks;
import grondag.hard_science.init.ModItems;
import grondag.hard_science.init.ModModels;
import grondag.hard_science.init.ModSuperModelBlocks;
import grondag.hard_science.library.varia.ColorHelper;
import grondag.hard_science.library.varia.ColorHelper.CMYK;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.library.world.PackedBlockPos;
import grondag.hard_science.machines.base.MachineContainerTileEntity;
import grondag.hard_science.machines.support.MachineControlState.MachineState;
import grondag.hard_science.machines.support.MachineFuelCell;
import grondag.hard_science.machines.support.MachinePower;
import grondag.hard_science.machines.support.MaterialBuffer;
import grondag.hard_science.machines.support.MaterialBufferManager;
import grondag.hard_science.machines.support.StandardUnits;
import grondag.hard_science.machines.support.VolumeUnits;
import grondag.hard_science.machines.support.VolumetricIngredientList;
import grondag.hard_science.machines.support.VolumetricIngredientList.VolumetricIngredient;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.block.SuperModelBlock;
import grondag.hard_science.superblock.block.SuperModelTileEntity;
import grondag.hard_science.superblock.color.ColorMap.EnumColorMap;
import grondag.hard_science.superblock.items.SuperItemBlock;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.model.state.PaintLayer;
import grondag.hard_science.superblock.varia.BlockSubstance;
import grondag.hard_science.virtualblock.VirtualBlock;
import grondag.hard_science.virtualblock.VirtualBlockTracker;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;


public class BasicBuilderTileEntity extends MachineContainerTileEntity implements ITickable
{

    ////////////////////////////////////////////////////////////////////////
    //  STATIC MEMBERS
    ////////////////////////////////////////////////////////////////////////
    
    private static final VolumetricIngredientList POLYETHYLENE_INGREDIENTS = new VolumetricIngredientList(
            new VolumetricIngredient(ModItems.hdpe_cube_3.getRegistryName().getResourcePath(), StandardUnits.nL_HS_CUBE_THREE),
            new VolumetricIngredient(ModItems.hdpe_cube_4.getRegistryName().getResourcePath(), StandardUnits.nL_HS_CUBE_FOUR),
            new VolumetricIngredient(ModItems.hdpe_cube_5.getRegistryName().getResourcePath(), StandardUnits.nL_HS_CUBE_FIVE),
            new VolumetricIngredient(ModItems.hdpe_cube_6.getRegistryName().getResourcePath(), StandardUnits.nL_HS_CUBE_SIX),
            new VolumetricIngredient(ModItems.hdpe_wafer_6.getRegistryName().getResourcePath(), StandardUnits.nL_HS_CUBE_SIX));
    
    private static final VolumetricIngredientList FILLER_INGREDIENTS = new VolumetricIngredientList(
            new VolumetricIngredient("sand", StandardUnits.nL_ONE_BLOCK),
            new VolumetricIngredient("red_sand", StandardUnits.nL_ONE_BLOCK));
    
    private static final VolumetricIngredientList RESIN_A_INGREDIENTS = new VolumetricIngredientList(
            new VolumetricIngredient("slime_ball", StandardUnits.nL_HS_CUBE_ONE));
    
    private static final VolumetricIngredientList RESIN_B_INGREDIENTS = new VolumetricIngredientList(
            new VolumetricIngredient("beef", StandardUnits.nL_HS_CUBE_ONE));
    
    private static final VolumetricIngredientList NANOLIGHT_INGREDIENTS = new VolumetricIngredientList(
            new VolumetricIngredient("dustGlowstone", StandardUnits.nL_HS_CUBE_THREE));
    
    private static final VolumetricIngredientList CYAN_INGREDIENTS = new VolumetricIngredientList(
            new VolumetricIngredient("dyeCyan", StandardUnits.nL_HS_CUBE_TWO));
    
    private static final VolumetricIngredientList MAGENTA_INGREDIENTS = new VolumetricIngredientList(
            new VolumetricIngredient("dyeMagenta", StandardUnits.nL_HS_CUBE_TWO));
    
    private static final VolumetricIngredientList YELLOW_INGREDIENTS = new VolumetricIngredientList(
            new VolumetricIngredient("dyeYellow", StandardUnits.nL_HS_CUBE_TWO));
    
    private static final VolumetricIngredientList WHITE_INGREDIENTS = new VolumetricIngredientList(
            new VolumetricIngredient("dyeWhite", StandardUnits.nL_HS_CUBE_TWO));
    
    private static final VolumetricIngredientList CARBON_INGREDIENTS = new VolumetricIngredientList(
            new VolumetricIngredient("charcoal", StandardUnits.nL_HS_CUBE_TWO));
    
    private static final long DYE_USAGE_PER_SATURATED_FULL_BLOCK = StandardUnits.nL_LITER / 4;
    private static final long DYE_USAGE_PER_SATURATED_FULL_BLOCK_OVERLAY = DYE_USAGE_PER_SATURATED_FULL_BLOCK / 4;
    private static final long TICKS_PER_FULL_BLOCK = 40;

    
    ////////////////////////////////////////////////////////////////////////
    //  INSTANCE MEMBERS
    ////////////////////////////////////////////////////////////////////////
    
    // All of these hold transient state for processing - none are persisted.
    private int offsetIndex = 0;
    private BlockPos checkPos; 
    private LongOpenHashSet failuresThisChunk = new LongOpenHashSet();
    
    /**
     * Number of blocks built in current chunk.  If > 0 will go back to origin when moving to next chunk.
     * Otherwise will keep searching to full radius until can build something.
     */
    private int completionsThisChunk = 0;
    
    // Buffer setup - all persisted
    private final MaterialBuffer bufferPolyEthylene = new MaterialBuffer(POLYETHYLENE_INGREDIENTS, StandardUnits.nL_HS_CUBE_THREE, "HDPE");
    private final MaterialBuffer bufferFiller = new MaterialBuffer(FILLER_INGREDIENTS, 64, "filler");
    private final MaterialBuffer bufferResinA = new MaterialBuffer(RESIN_A_INGREDIENTS, 64, "resinA");
    private final MaterialBuffer bufferResinB = new MaterialBuffer(RESIN_B_INGREDIENTS, 64, "resinB");
    private final MaterialBuffer bufferNanoLights = new MaterialBuffer(NANOLIGHT_INGREDIENTS, 64, "lights");
    private final MaterialBuffer bufferCyan = new MaterialBuffer(CYAN_INGREDIENTS, 64, "cyan");
    private final MaterialBuffer bufferMagenta = new MaterialBuffer(MAGENTA_INGREDIENTS, 64, "magenta");
    private final MaterialBuffer bufferYellow = new MaterialBuffer(YELLOW_INGREDIENTS, 64, "yellow");
    private final MaterialBuffer bufferCarbon = new MaterialBuffer(CARBON_INGREDIENTS, 64, "carbon");
    private final MaterialBuffer bufferTiO2 = new MaterialBuffer(CARBON_INGREDIENTS, 64, "TiO2");   
    
    private long stoneNeeded;
    private long woodNeeded;
    private long glassNeeded;
    private long glowstoneNeeded;
    private long cyanNeeded;
    private long magentaNeeded;
    private long yellowNeeded;
    private long blackNeeded;
    private boolean isFabricationReady = false;
   
    public BasicBuilderTileEntity()
    {
        super();
        this.setBufferManager(new MaterialBufferManager(bufferPolyEthylene, bufferFiller, bufferResinA, bufferResinB, 
                bufferNanoLights, bufferCyan, bufferMagenta, bufferYellow, bufferCarbon, bufferTiO2));
        
        this.setPowerProvider(new MachineFuelCell(MachinePower.FuelCellSpec.STANDARD_INTEGRATED));
        
        this.statusState.setHasBacklog(true);
    }
    
    
    
    @Override
    public boolean togglePower(EntityPlayerMP player)
    {
        boolean result = super.togglePower(player);
        if(result)
        {
            // reset to origin chunk on power toggle
            this.checkPos =null;
            this.failuresThisChunk.clear();
        }
        return result;
    }

    @Override
    public void update()
    {
        super.update();
        
        if(world.isRemote)
        {
            if(!this.isOn()) return;
            
            // approximate more granular job progress on client side
            if(this.getControlState().getMachineState() == MachineState.FABRICATING)
            {
                this.getControlState().progressJob((short) 1);
            }
        }
        else
        {
            long tick = this.world.getTotalWorldTime();
            
            if((tick & 0xF) == 0xF)
            {
                if(this.getPowerProvider().provideEnergy(1, false, false) != 1)
                {
                    this.getPowerProvider().setFailureCause(true);
                    return;
                }
                
                if(!this.isOn()) return;
                
                this.pullResources();
                
                if((tick & 0x1F) == 0x1F)
                {
                    this.setCurrentBacklog(VirtualBlockTracker.INSTANCE.get(this.world).sizeInChunksNear(pos, Configurator.MACHINES.basicBuilderChunkRadius));
                }
            }
            
            if(!this.isOn()) return;
            
            switch(this.getControlState().getMachineState())
            {
            case FABRICATING:
                this.progressFabrication();
                break;
                
            case TRANSPORTING:
                this.placeFabricatedBlock();
                break;
                
            default:
                this.searchForWork();
                break;
                
            }
        }
        
    }

    private void searchForWork()
    {
        if(this.getPowerProvider().provideEnergy(2, false, false) != 2) return;
        
        if(checkPos == null) checkPos = this.pos;
        
        // look for a virtual block in current target chunk
        BlockPos targetPos = VirtualBlockTracker.INSTANCE.get(this.world).dequeue(checkPos);
        
        // if nothing found in this chunk, or if we have cycled through queue to failures, move to next chunk and exit
        if(targetPos == null || this.failuresThisChunk.contains(PackedBlockPos.pack(targetPos)))
        {
            
            // if we grabbed a position from the queue, put it back
            if(targetPos != null) 
                VirtualBlockTracker.INSTANCE.get(this.world).enqueue(targetPos);
            
            // clear the cycle tracking for the queue
            this.failuresThisChunk.clear();
            
            // if we actually built something, go back to origin and start search again so that we are
            // always building the nearest chunks
            if(this.completionsThisChunk > 0)
            {
                this.completionsThisChunk = 0;
                this.offsetIndex = -1;
            }
            
            Vec3i nextOffset = Useful.getDistanceSortedCircularOffset(++this.offsetIndex);
            if(nextOffset == null || nextOffset.getY() > Configurator.MACHINES.basicBuilderChunkRadius)
            {
                nextOffset = Useful.getDistanceSortedCircularOffset(0);
                this.offsetIndex = 0;
            }
            this.checkPos = this.pos.add(nextOffset.getX() * 16, 0, nextOffset.getZ() * 16);
            return;
        }
        
        // look for a virtual block at target position, get state if found
        IBlockState oldState = this.world.getBlockState(targetPos);
        
        if(oldState.getBlock() != ModBlocks.virtual_block) return;
        
        TileEntity rawTE = world.getTileEntity(targetPos);
        if(rawTE == null || !(rawTE instanceof SuperModelTileEntity)) return;
        SuperModelTileEntity oldTE = (SuperModelTileEntity)rawTE;
        
        ModelState modelState = ((VirtualBlock)ModBlocks.virtual_block).getModelStateAssumeStateIsCurrent(oldState, world, targetPos, true);

        BlockSubstance substance = this.prepareFabrication(modelState, oldTE);
        
        if(substance == null)
        {
            // put block back in queue and abort
            VirtualBlockTracker.INSTANCE.get(this.world).enqueue(targetPos);
            
            // track failure
            this.failuresThisChunk.add(PackedBlockPos.pack(targetPos));
        }
        else
        {
            // consume resources
            this.beginFabrication();

            // save placement info
            this.getControlState().setModelState(modelState);
            this.getControlState().setTargetPos(targetPos);
            this.getControlState().setLightValue(oldTE.getLightValue());
            this.getControlState().setSubstance(substance);
            this.getControlState().setMeta(oldState.getValue(SuperBlock.META));
            
            long totalUnits = this.stoneNeeded + this.woodNeeded + this.glassNeeded
                    + this.glowstoneNeeded + this.cyanNeeded + this.magentaNeeded 
                    + this.yellowNeeded + this.blackNeeded;
            
            // setup job duration
            this.getControlState().startJobTicks((short) (totalUnits * TICKS_PER_FULL_BLOCK / VolumeUnits.KILOLITER.nL));
            
            // indicate progress for work searching
            this.completionsThisChunk++;
            
            this.getControlState().setMachineState(MachineState.FABRICATING);
            
            this.markDirty();
            this.markPlayerUpdateDirty(true);
            // we want to send an immediate update when job starts
        }
    }
    
    private void progressFabrication()
    {
        if(this.getPowerProvider().provideEnergy(100, false, false) == 100 && this.getControlState().progressJob((short) 1))
        {
            this.getControlState().clearJobTicks();
            this.getControlState().setMachineState(MachineState.TRANSPORTING);
        }
        this.markDirty();
    }
    
    private void placeFabricatedBlock()
    {
        
        ModelState newModelState = this.getControlState().getModelState();
        int newLightValue = this.getControlState().getLightValue();
        int newMeta = this.getControlState().getMeta();
        BlockSubstance newSubstance = this.getControlState().getSubstance();
        BlockPos targetPos = this.getControlState().getTargetPos();
        
        //FIXME: ensure fuel cell max output can support max configurable range
        int powerNeeded = (int) (1 * this.pos.getDistance(targetPos.getX(), targetPos.getY(), targetPos.getZ()));
        
        if(this.getPowerProvider().provideEnergy(powerNeeded, false, false) == powerNeeded)
        {
        
            // this all needs to happen in any case
            this.getControlState().setModelState(null);
            this.getControlState().setTargetPos(null);
            this.getControlState().setMachineState(MachineState.THINKING);
            this.markDirty();
            
            // abort on strangeness
            if(newModelState == null)
            {
                Log.warn("Unable to retrieve model state for virtual block placement.  This is probably a bug.  Block was not placed.");
                return;
            }
            
            SuperModelBlock newBlock = ModSuperModelBlocks.findAppropriateSuperModelBlock(newSubstance, newModelState);
            IBlockState newState = newBlock.getDefaultState().withProperty(SuperBlock.META, newMeta);
            
            IBlockState oldState = this.world.getBlockState(targetPos);
            
            // confirm virtual block matching our fabricated block still located at target position
            if(oldState.getBlock() == ModBlocks.virtual_block && oldState.getValue(SuperBlock.META) == newMeta) 
            {
                TileEntity rawTE = world.getTileEntity(targetPos);
                if(rawTE != null && rawTE instanceof SuperModelTileEntity)
                {
                    SuperModelTileEntity oldTE = (SuperModelTileEntity)rawTE;
                    
                    if(oldTE.getLightValue() == newLightValue && oldTE.getSubstance() == newSubstance)
                    {
                        ModelState oldModelState = ((VirtualBlock)ModBlocks.virtual_block).getModelStateAssumeStateIsCurrent(oldState, world, targetPos, true);
                        if(newModelState.equals(oldModelState))
                        {
                            // match!  place the block and exit, resume searching for work
                            world.setBlockState(targetPos, newState);
                            
                            SuperModelTileEntity newTE = (SuperModelTileEntity)world.getTileEntity(targetPos);
                            if (newTE == null) return;
                            
                            newTE.setLightValue((byte) newLightValue);
                            newTE.setSubstance(newSubstance);
                            newTE.setModelState(newModelState);
                            
                            return;
                        }
    
                    }
                }
            }
            
            // if we got to this point, we have a fabricated block with no place to be
            // put it in an adjacent container if there is one with room
            // otherwise eject it into the world
            List<ItemStack> items = newBlock.getSubItems();
            
            ItemStack stack = newMeta < items.size() ? items.get(newMeta) : items.get(0);
            stack.setItemDamage(newMeta);
            SuperItemBlock.setStackLightValue(stack, newLightValue);
            SuperItemBlock.setStackSubstance(stack, newSubstance);
            SuperItemBlock.setStackModelState(stack, newModelState);
            
            for(EnumFacing face : EnumFacing.VALUES)
            {
                TileEntity tileentity = this.world.getTileEntity(this.pos.offset(face));
                if (tileentity != null)
                {
                    IItemHandler itemHandler = tileentity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face.getOpposite());
                    if(itemHandler != null)
                    {
                        ItemStack remaining = ItemHandlerHelper.insertItem(itemHandler, stack, false);
                        if(remaining.isEmpty())
                        {
                            return;
                        }
                    }
                }
            }
            
            // no luck with containers - spawn in world
            Block.spawnAsEntity(this.world, targetPos, stack);
        }
    }
   
    /** returns substance that should be used to create the block in world if it can be successfully fabricated.  
     * Must be called prior to calling fabricate */
    private BlockSubstance prepareFabrication(ModelState modelState, SuperModelTileEntity oldTE)
    {
        this.stoneNeeded = 0;
        this.woodNeeded = 0;
        this.glassNeeded = 0;
        this.glowstoneNeeded = 0;
        this.cyanNeeded = 0;
        this.magentaNeeded = 0;
        this.yellowNeeded = 0;
        this.blackNeeded = 0;
        
        long volume = (long) (Useful.volumeAABB(modelState.collisionBoxes(BlockPos.ORIGIN)) * StandardUnits.nL_ONE_BLOCK);
        BlockSubstance substance;
        switch(oldTE.getSubstance())
        {
            case DURAWOOD:
            case FLEXWOOD:
            case HYPERWOOD:
                substance = BlockSubstance.FLEXWOOD;
                this.woodNeeded = volume;
                break;

            case DURAGLASS:
            case FLEXIGLASS:
            case HYPERGLASS:
                substance = BlockSubstance.FLEXIGLASS;
                this.glassNeeded = volume;
                break;
            
            default:
                substance = BlockSubstance.FLEXSTONE;
                this.stoneNeeded = volume;
                break;
        }
        

        int lightLevel = oldTE.getLightValue() + (modelState.getRenderPassSet().hasFlatRenderPass ? 1 : 0);
        this.glowstoneNeeded = lightLevel  / 16 * StandardUnits.nL_QUARTER_BLOCK_;
        
        CMYK cmyk = ColorHelper.cmyk(modelState.getColorMap(PaintLayer.BASE).getColor(EnumColorMap.BASE));
        if(cmyk.cyan != 0) this.cyanNeeded = (long) (cmyk.cyan * DYE_USAGE_PER_SATURATED_FULL_BLOCK);
        if(cmyk.magenta != 0) this.magentaNeeded = (long) (cmyk.magenta * DYE_USAGE_PER_SATURATED_FULL_BLOCK);
        if(cmyk.yellow != 0) this.yellowNeeded = (long) (cmyk.yellow * DYE_USAGE_PER_SATURATED_FULL_BLOCK);
        if(cmyk.keyBlack != 0) this.blackNeeded = (long) (cmyk.keyBlack * DYE_USAGE_PER_SATURATED_FULL_BLOCK);
        

        if(modelState.isMiddleLayerEnabled())
        {
            cmyk = ColorHelper.cmyk(modelState.getColorMap(PaintLayer.MIDDLE).getColor(EnumColorMap.BASE));
            if(cmyk.cyan != 0) this.cyanNeeded += (long) (cmyk.cyan * DYE_USAGE_PER_SATURATED_FULL_BLOCK_OVERLAY);
            if(cmyk.magenta != 0) this.magentaNeeded += (long) (cmyk.magenta * DYE_USAGE_PER_SATURATED_FULL_BLOCK_OVERLAY);
            if(cmyk.yellow != 0) this.yellowNeeded += (long) (cmyk.yellow * DYE_USAGE_PER_SATURATED_FULL_BLOCK_OVERLAY);
            if(cmyk.keyBlack != 0) this.blackNeeded += (long) (cmyk.keyBlack * DYE_USAGE_PER_SATURATED_FULL_BLOCK_OVERLAY);
        }
        
        if(modelState.isOuterLayerEnabled())
        {
            cmyk = ColorHelper.cmyk(modelState.getColorMap(PaintLayer.OUTER).getColor(EnumColorMap.BASE));
            if(cmyk.cyan != 0) this.cyanNeeded += (long) (cmyk.cyan * DYE_USAGE_PER_SATURATED_FULL_BLOCK_OVERLAY);
            if(cmyk.magenta != 0) this.magentaNeeded += (long) (cmyk.magenta * DYE_USAGE_PER_SATURATED_FULL_BLOCK_OVERLAY);
            if(cmyk.yellow != 0) this.yellowNeeded += (long) (cmyk.yellow * DYE_USAGE_PER_SATURATED_FULL_BLOCK_OVERLAY);
            if(cmyk.keyBlack != 0) this.blackNeeded += (long) (cmyk.keyBlack * DYE_USAGE_PER_SATURATED_FULL_BLOCK_OVERLAY);
        }
        
        if(modelState.hasLampSurface())
        {
            cmyk = ColorHelper.cmyk(modelState.getColorMap(PaintLayer.LAMP).getColor(EnumColorMap.BASE));
            if(cmyk.cyan != 0) this.cyanNeeded += (long) (cmyk.cyan * DYE_USAGE_PER_SATURATED_FULL_BLOCK_OVERLAY);
            if(cmyk.magenta != 0) this.magentaNeeded += (long) (cmyk.magenta * DYE_USAGE_PER_SATURATED_FULL_BLOCK_OVERLAY);
            if(cmyk.yellow != 0) this.yellowNeeded += (long) (cmyk.yellow * DYE_USAGE_PER_SATURATED_FULL_BLOCK_OVERLAY);
            if(cmyk.keyBlack != 0) this.blackNeeded += (long) (cmyk.keyBlack * DYE_USAGE_PER_SATURATED_FULL_BLOCK_OVERLAY);
        }
        
        boolean isReady = checkBufferAndBlameForFailure(this.stoneNeeded, this.bufferFiller);
        isReady = isReady && checkBufferAndBlameForFailure(this.woodNeeded, this.bufferPolyEthylene);
        isReady = isReady && checkBufferAndBlameForFailure(this.glassNeeded, this.bufferResinA);
        isReady = isReady && checkBufferAndBlameForFailure(this.glowstoneNeeded, this.bufferNanoLights);
        isReady = isReady && checkBufferAndBlameForFailure(this.cyanNeeded, this.bufferCyan);
        isReady = isReady && checkBufferAndBlameForFailure(this.magentaNeeded, this.bufferMagenta);
        isReady = isReady && checkBufferAndBlameForFailure(this.yellowNeeded, this.bufferYellow);
        isReady = isReady && checkBufferAndBlameForFailure(this.blackNeeded, this.bufferCarbon);
        
        this.isFabricationReady = isReady;
        
        // As soon as we are able to make any block, forget that we have material shortages.
        // Maybe some other builder will handle.
        if(isReady) this.getBufferManager().forgiveAll();
        
        return this.isFabricationReady ? substance : null;
    }
    
    private boolean checkBufferAndBlameForFailure(long levelNeeded, MaterialBuffer buffer)
    {
        if(levelNeeded > buffer.getLevel())
        {
            if(!buffer.isFailureCause())
            {
                buffer.setFailureCause(true);
                this.markPlayerUpdateDirty(true);
                this.getBufferManager().blame();
            }
            return false;
        }
        return true;
    }
    /**
     * Consumes the resources calculated during last call to prepareFabrication.
     */
    private void beginFabrication()
    {
        if(!isFabricationReady) Log.warn("Basic Builder attempted fabrication without preparation.  This is a bug");
        
        if(this.stoneNeeded > 0) this.bufferFiller.use(stoneNeeded);
        if(this.woodNeeded > 0) this.bufferPolyEthylene.use(woodNeeded);
        if(this.glassNeeded > 0) this.bufferResinA.use(glassNeeded);
        if(this.glowstoneNeeded > 0) this.bufferNanoLights.use(glowstoneNeeded);
        if(this.cyanNeeded > 0) this.bufferCyan.use(cyanNeeded);
        if(this.magentaNeeded > 0) this.bufferMagenta.use(magentaNeeded);
        if(this.yellowNeeded > 0) this.bufferYellow.use(yellowNeeded);
        if(this.blackNeeded > 0) this.bufferCarbon.use(blackNeeded);
        this.isFabricationReady = false;
    }
    
    private void pullResources()
    {
        MaterialBufferManager bufferManager = this.getBufferManager();
        
        if(!bufferManager.canRestock()) return;
        
        for(EnumFacing face : EnumFacing.VALUES)
        {
            TileEntity tileentity = this.world.getTileEntity(this.pos.offset(face));
            if (tileentity != null)
            {
                IItemHandler capability = tileentity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face.getOpposite());
                if(this.getPowerProvider().provideEnergy(2, false, true) == 2 && bufferManager.restock(capability))
                {
                    this.getPowerProvider().provideEnergy(2, false, false);
                    this.markDirty();
                }
                if(!bufferManager.canRestock()) return;
            }
        }
    }

    @Override
    public IItemHandler getItemHandler()
    {
        return this.getBufferManager();
    }
   
    @SideOnly(Side.CLIENT)
    @Override
    public int getSymbolGlTextureId()
    {
        return ModModels.TEX_SYMBOL_BUILDER;
    }

    @Override
    public void disconnect()
    {
        // Currently NOOP
    }

    @Override
    public void reconnect()
    {
        // Currently NOOP
    }
}
