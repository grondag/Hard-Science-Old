package grondag.hard_science.machines;

import java.util.List;

import grondag.hard_science.Configurator;
import grondag.hard_science.Log;
import grondag.hard_science.init.ModBlocks;
import grondag.hard_science.init.ModItems;
import grondag.hard_science.init.ModModels;
import grondag.hard_science.init.ModSuperModelBlocks;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.library.world.PackedBlockPos;
import grondag.hard_science.machines.base.MachineContainerTileEntity;
import grondag.hard_science.machines.support.MachineControlState.MachineState;
import grondag.hard_science.machines.support.MachineFuelCell;
import grondag.hard_science.machines.support.MachinePower;
import grondag.hard_science.machines.support.MaterialBufferManager;
import grondag.hard_science.machines.support.MaterialBufferManager.DemandManager;
import grondag.hard_science.machines.support.MaterialBufferManager.MaterialBufferDelegate;
import grondag.hard_science.machines.support.MaterialBufferManager.VolumetricBufferSpec;
import grondag.hard_science.machines.support.StandardUnits;
import grondag.hard_science.machines.support.VolumeUnits;
import grondag.hard_science.machines.support.VolumetricIngredientList;
import grondag.hard_science.machines.support.VolumetricIngredientList.VolumetricIngredient;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.block.SuperModelBlock;
import grondag.hard_science.superblock.block.SuperModelTileEntity;
import grondag.hard_science.superblock.items.SuperItemBlock;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
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
    
    private static final VolumetricIngredientList HDPE_INGREDIENTS = new VolumetricIngredientList(
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
    
    private static final VolumetricIngredientList TiO2_INGREDIENTS = new VolumetricIngredientList(
            new VolumetricIngredient("dyeWhite", StandardUnits.nL_HS_CUBE_TWO));
    
    private static final VolumetricIngredientList CARBON_INGREDIENTS = new VolumetricIngredientList(
            new VolumetricIngredient("charcoal", StandardUnits.nL_HS_CUBE_TWO));
    
    private static final VolumetricBufferSpec[] BUFFER_SPECS =
        {
            new VolumetricBufferSpec(HDPE_INGREDIENTS, StandardUnits.nL_HS_CUBE_THREE, ModNBTTag.MATERIAL_HDPE),
            new VolumetricBufferSpec(FILLER_INGREDIENTS, StandardUnits.nL_FULL_STACK_OF_BLOCKS_nL, ModNBTTag.MATERIAL_MINERAL_FILLER),
            new VolumetricBufferSpec(RESIN_A_INGREDIENTS, StandardUnits.nL_HS_CUBE_ONE, ModNBTTag.MATERIAL_RESIN_A),
            new VolumetricBufferSpec(RESIN_B_INGREDIENTS, StandardUnits.nL_HS_CUBE_ONE, ModNBTTag.MATERIAL_RESIN_B),
            new VolumetricBufferSpec(NANOLIGHT_INGREDIENTS, StandardUnits.nL_HS_CUBE_THREE, ModNBTTag.MATERIAL_NANO_LIGHTS),
            new VolumetricBufferSpec(CYAN_INGREDIENTS, StandardUnits.nL_HS_CUBE_TWO, ModNBTTag.MATERIAL_DYE_CYAN),
            new VolumetricBufferSpec(MAGENTA_INGREDIENTS, StandardUnits.nL_HS_CUBE_TWO, ModNBTTag.MATERIAL_DYE_MAGENTA),
            new VolumetricBufferSpec(YELLOW_INGREDIENTS, StandardUnits.nL_HS_CUBE_TWO, ModNBTTag.MATERIAL_DYE_YELLOW),
            new VolumetricBufferSpec(TiO2_INGREDIENTS, StandardUnits.nL_HS_CUBE_ONE, ModNBTTag.MATERIAL_TiO2)
        };
    
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
    private final MaterialBufferDelegate bufferFiller;
    private final MaterialBufferDelegate bufferResinA;
    private final MaterialBufferDelegate bufferResinB;
    private final MaterialBufferDelegate bufferNanoLights;
    private final MaterialBufferDelegate bufferCyan;
    private final MaterialBufferDelegate bufferMagenta;
    private final MaterialBufferDelegate bufferYellow;
    private final MaterialBufferDelegate bufferTiO2;   
    
 
   
    public BasicBuilderTileEntity()
    {
        super();
        MaterialBufferManager bufferManager = new MaterialBufferManager(BUFFER_SPECS);
        this.setBufferManager(bufferManager);
        
        // note that order has to match array declaration
        this.bufferHDPE = bufferManager.getBuffer(0);
        this.bufferFiller = bufferManager.getBuffer(1);
        this.bufferResinA = bufferManager.getBuffer(2);
        this.bufferResinB = bufferManager.getBuffer(3);
        this.bufferNanoLights = bufferManager.getBuffer(4);
        this.bufferCyan = bufferManager.getBuffer(5);
        this.bufferMagenta = bufferManager.getBuffer(6);
        this.bufferYellow = bufferManager.getBuffer(7);
        this.bufferTiO2 = bufferManager.getBuffer(9);
        
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
            // setup job duration
            this.getControlState().startJobTicks((short) (this.getBufferManager().demandManager().totalDemandNanoLiters() * TICKS_PER_FULL_BLOCK / VolumeUnits.KILOLITER.nL));

            // consume resources
            this.getBufferManager().demandManager().consumeAllDemandsAndClear();

            // save placement info
            this.getControlState().setModelState(modelState);
            this.getControlState().setTargetPos(targetPos);
            this.getControlState().setLightValue(oldTE.getLightValue());
            this.getControlState().setSubstance(substance);
            this.getControlState().setMeta(oldState.getValue(SuperBlock.META));
            
            // indicate progress for work searching
            this.completionsThisChunk++;
            
            this.getControlState().setMachineState(MachineState.FABRICATING);
            
            this.markDirty();
            // we want to send an immediate update when job starts
            this.markPlayerUpdateDirty(true);
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
        DemandManager demand = this.getBufferManager().demandManager();
        
        demand.clearAllDemand();
        
        SuperBlockMaterialCalculator needs = new SuperBlockMaterialCalculator(modelState, oldTE.getSubstance(), oldTE.getLightValue());
        
        if(needs.filler_nL > 0) this.bufferFiller.addDemand(needs.filler_nL);
        if(needs.resinA_nL > 0) this.bufferResinA.addDemand(needs.resinA_nL);
        if(needs.resinB_nL > 0) this.bufferResinB.addDemand(needs.resinB_nL);
        if(needs.nanoLights_nL > 0) this.bufferNanoLights.addDemand(needs.nanoLights_nL);
        if(needs.cyan_nL > 0) this.bufferCyan.addDemand(needs.cyan_nL);
        if(needs.magenta_nL > 0) this.bufferMagenta.addDemand(needs.magenta_nL);
        if(needs.yellow_nL > 0) this.bufferYellow.addDemand(needs.yellow_nL);
        if(needs.TiO2_nL > 0) this.bufferTiO2.addDemand(needs.TiO2_nL);
    
        
        if(demand.canAllDemandsBeMetAndBlameIfNot())
        {
            // As soon as we are able to make any block, forget that we have material shortages.
            // Maybe some other builder will handle.
            this.getBufferManager().forgiveAll();
            return needs.actualSubtance;
        }
        else
        {
            return null;
        }
    }
    
    private void pullResources()
    {
        MaterialBufferManager bufferManager = this.getBufferManager();
        
        if(!bufferManager.canRestockAny()) return;
        
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
                if(!bufferManager.canRestockAny()) return;
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
