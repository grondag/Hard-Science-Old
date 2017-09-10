package grondag.hard_science.machines;

import static grondag.hard_science.machines.support.MaterialBuffer.UNITS_PER_ITEM;

import java.util.List;

import grondag.hard_science.Configurator;
import grondag.hard_science.Log;
import grondag.hard_science.init.ModBlocks;
import grondag.hard_science.init.ModModels;
import grondag.hard_science.init.ModSuperModelBlocks;
import grondag.hard_science.library.varia.ColorHelper;
import grondag.hard_science.library.varia.ColorHelper.CMYK;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.library.world.PackedBlockPos;
import grondag.hard_science.machines.base.MachineContainerTileEntity;
import grondag.hard_science.machines.support.MaterialBuffer;
import grondag.hard_science.machines.support.MaterialBufferManager;
import grondag.hard_science.machines.support.WeightedIngredientList;
import grondag.hard_science.machines.support.MachineControlState.MachineState;
import grondag.hard_science.machines.support.WeightedIngredientList.WeightedIngredient;
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
    
    private static final WeightedIngredientList WOOD_INGREDIENTS = new WeightedIngredientList(
            new WeightedIngredient("logWood", UNITS_PER_ITEM * 4), 
            new WeightedIngredient("plankWood", UNITS_PER_ITEM), 
            new WeightedIngredient("slabWood", UNITS_PER_ITEM / 2),
            new WeightedIngredient("stairWood", UNITS_PER_ITEM * 6 / 4),
            new WeightedIngredient("stickWood", UNITS_PER_ITEM / 2));
    
    private static final WeightedIngredientList STONE_INGREDIENTS = new WeightedIngredientList(
            new WeightedIngredient("stone", UNITS_PER_ITEM),
            new WeightedIngredient("cobblestone", UNITS_PER_ITEM),
            new WeightedIngredient("stoneGranite", UNITS_PER_ITEM),
            new WeightedIngredient("stoneGranitePolished", UNITS_PER_ITEM),
            new WeightedIngredient("stoneDiorite", UNITS_PER_ITEM),
            new WeightedIngredient("stoneDioritePolished", UNITS_PER_ITEM),
            new WeightedIngredient("stoneAndesite", UNITS_PER_ITEM),
            new WeightedIngredient("stoneAndesitePolished", UNITS_PER_ITEM),
            new WeightedIngredient("stone", UNITS_PER_ITEM),
            new WeightedIngredient("stone", UNITS_PER_ITEM),
            new WeightedIngredient("stone", UNITS_PER_ITEM));
    
    private static final WeightedIngredientList GLASS_INGREDIENTS = new WeightedIngredientList(
            new WeightedIngredient("blockGlass", UNITS_PER_ITEM), 
            new WeightedIngredient("paneGlass", UNITS_PER_ITEM * 3 / 8));
    
    private static final WeightedIngredientList GLOWSTONE_INGREDIENTS = new WeightedIngredientList(
            new WeightedIngredient("dustGlowstone", UNITS_PER_ITEM), 
            new WeightedIngredient("glowstone", UNITS_PER_ITEM * 4));
    
    private static final WeightedIngredientList CYAN_INGREDIENTS = new WeightedIngredientList(
            new WeightedIngredient("dyeCyan", UNITS_PER_ITEM));
    
    private static final WeightedIngredientList MAGENTA_INGREDIENTS = new WeightedIngredientList(
            new WeightedIngredient("dyeMagenta", UNITS_PER_ITEM));
    
    private static final WeightedIngredientList YELLOW_INGREDIENTS = new WeightedIngredientList(
            new WeightedIngredient("dyeYellow", UNITS_PER_ITEM));
    
    private static final WeightedIngredientList BLACK_INGREDIENTS = new WeightedIngredientList(
            new WeightedIngredient("dyeBlack", UNITS_PER_ITEM));
    
    
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
    private final MaterialBuffer WOOD_BUFFER = new MaterialBuffer(WOOD_INGREDIENTS, 64, "wood");
    private final MaterialBuffer STONE_BUFFER = new MaterialBuffer(STONE_INGREDIENTS, 64, "stone");
    private final MaterialBuffer GLASS_BUFFER = new MaterialBuffer(GLASS_INGREDIENTS, 64, "glass");
    private final MaterialBuffer GLOWSTONE_BUFFER = new MaterialBuffer(GLOWSTONE_INGREDIENTS, 64, "glowstone");
    private final MaterialBuffer CYAN_BUFFER = new MaterialBuffer(CYAN_INGREDIENTS, 64, "cyan");
    private final MaterialBuffer MAGENTA_BUFFER = new MaterialBuffer(MAGENTA_INGREDIENTS, 64, "magenta");
    private final MaterialBuffer YELLOW_BUFFER = new MaterialBuffer(YELLOW_INGREDIENTS, 64, "yellow");
    private final MaterialBuffer BLACK_BUFFER = new MaterialBuffer(BLACK_INGREDIENTS, 64, "black");   
    private final MaterialBufferManager bufferManager = new MaterialBufferManager(WOOD_BUFFER, STONE_BUFFER, GLASS_BUFFER, GLOWSTONE_BUFFER,
            CYAN_BUFFER, MAGENTA_BUFFER, YELLOW_BUFFER, BLACK_BUFFER);
    
    public BasicBuilderTileEntity()
    {
        super();
        this.setBufferManager(bufferManager);
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
        if(!this.isOn()) return;
        
        if(world.isRemote)
        {
            // approximate more granular job progress on client side
            if(this.controlState.getMachineState() == MachineState.FABRICATING)
            {
                this.controlState.progressJob((short) 1);
            }
        }
        else
        {
            long tick = this.world.getTotalWorldTime();
            
            if((tick & 0xF) == 0xF)
            {
                this.pullResources();
                
                if((tick & 0x1F) == 0x1F)
                {
                    this.setCurrentBacklog(VirtualBlockTracker.INSTANCE.get(this.world).sizeInChunksNear(pos, Configurator.MACHINES.basicBuilderChunkRadius));
                }
            }
            
            switch(this.controlState.getMachineState())
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
            this.controlState.setModelState(modelState);
            this.controlState.setTargetPos(targetPos);
            this.controlState.setLightValue(oldTE.getLightValue());
            this.controlState.setSubstance(substance);
            this.controlState.setMeta(oldState.getValue(SuperBlock.META));
            
            int totalUnits = this.stoneNeeded + this.woodNeeded + this.glassNeeded
                    + this.glowstoneNeeded + this.cyanNeeded + this.magentaNeeded 
                    + this.yellowNeeded + this.blackNeeded;
            
            // setup job duration
            this.controlState.startJobTicks((short) (totalUnits * 40 / UNITS_PER_ITEM));
            
            // indicate progress for work searching
            this.completionsThisChunk++;
            
            this.controlState.setMachineState(MachineState.FABRICATING);
            
            this.markDirty();
            this.markPlayerUpdateDirty(true);
            // we want to send an immediate update when job starts
        }
    }
    
    private void progressFabrication()
    {
        if(this.controlState.progressJob((short) 1))
        {
            this.controlState.clearJobTicks();
            this.controlState.setMachineState(MachineState.TRANSPORTING);
        }
        this.markDirty();
    }
    
    private void placeFabricatedBlock()
    {
        
        ModelState newModelState = this.controlState.getModelState();
        int newLightValue = this.controlState.getLightValue();
        int newMeta = this.controlState.getMeta();
        BlockSubstance newSubstance = this.controlState.getSubstance();
        BlockPos targetPos = this.controlState.getTargetPos();
        
        // this all needs to happen in any case
        this.controlState.setModelState(null);
        this.controlState.setTargetPos(null);
        this.controlState.setMachineState(MachineState.THINKING);
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
    
    private int stoneNeeded;
    private int woodNeeded;
    private int glassNeeded;
    private int glowstoneNeeded;
    private int cyanNeeded;
    private int magentaNeeded;
    private int yellowNeeded;
    private int blackNeeded;
    private boolean isFabricationReady = false;
   
    
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
        
        int volume = (int) (UNITS_PER_ITEM * Useful.volumeAABB(modelState.collisionBoxes(BlockPos.ORIGIN)));
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
        
        // one glowstone dust to make a block light area at full brightness, much less (1/16) if just want glow render
        int lightLevel = oldTE.getLightValue() + (modelState.getRenderPassSet().hasFlatRenderPass ? 1 : 0);
        this.glowstoneNeeded = lightLevel  / 16;
        
        // dye usage is 1/8 of a dust per saturated, full-size block in base layer
        CMYK cmyk = ColorHelper.cmyk(modelState.getColorMap(PaintLayer.BASE).getColor(EnumColorMap.BASE));
        if(cmyk.cyan != 0) this.cyanNeeded = (int) (cmyk.cyan * UNITS_PER_ITEM / 8);
        if(cmyk.magenta != 0) this.magentaNeeded = (int) (cmyk.magenta * UNITS_PER_ITEM / 8);
        if(cmyk.yellow != 0) this.yellowNeeded = (int) (cmyk.yellow * UNITS_PER_ITEM / 8);
        if(cmyk.keyBlack != 0) this.blackNeeded = (int) (cmyk.keyBlack * UNITS_PER_ITEM / 8);
        
        // other layers count for 1/4 as much as base layer
        if(modelState.isMiddleLayerEnabled())
        {
            cmyk = ColorHelper.cmyk(modelState.getColorMap(PaintLayer.MIDDLE).getColor(EnumColorMap.BASE));
            if(cmyk.cyan != 0) this.cyanNeeded += (int) (cmyk.cyan * UNITS_PER_ITEM / 32);
            if(cmyk.magenta != 0) this.magentaNeeded += (int) (cmyk.magenta * UNITS_PER_ITEM / 32);
            if(cmyk.yellow != 0) this.yellowNeeded += (int) (cmyk.yellow * UNITS_PER_ITEM / 32);
            if(cmyk.keyBlack != 0) this.blackNeeded += (int) (cmyk.keyBlack * UNITS_PER_ITEM / 32);
        }
        
        if(modelState.isOuterLayerEnabled())
        {
            cmyk = ColorHelper.cmyk(modelState.getColorMap(PaintLayer.OUTER).getColor(EnumColorMap.BASE));
            if(cmyk.cyan != 0) this.cyanNeeded += (int) (cmyk.cyan * UNITS_PER_ITEM / 32);
            if(cmyk.magenta != 0) this.magentaNeeded += (int) (cmyk.magenta * UNITS_PER_ITEM / 32);
            if(cmyk.yellow != 0) this.yellowNeeded += (int) (cmyk.yellow * UNITS_PER_ITEM / 32);
            if(cmyk.keyBlack != 0) this.blackNeeded += (int) (cmyk.keyBlack * UNITS_PER_ITEM / 32);
        }
        
        if(modelState.hasLampSurface())
        {
            cmyk = ColorHelper.cmyk(modelState.getColorMap(PaintLayer.LAMP).getColor(EnumColorMap.BASE));
            if(cmyk.cyan != 0) this.cyanNeeded += (int) (cmyk.cyan * UNITS_PER_ITEM / 32);
            if(cmyk.magenta != 0) this.magentaNeeded += (int) (cmyk.magenta * UNITS_PER_ITEM / 32);
            if(cmyk.yellow != 0) this.yellowNeeded += (int) (cmyk.yellow * UNITS_PER_ITEM / 32);
            if(cmyk.keyBlack != 0) this.blackNeeded += (int) (cmyk.keyBlack * UNITS_PER_ITEM / 32);
        }
        
        boolean isReady = checkBufferAndBlameForFailure(this.stoneNeeded, this.STONE_BUFFER);
        isReady = isReady && checkBufferAndBlameForFailure(this.woodNeeded, this.WOOD_BUFFER);
        isReady = isReady && checkBufferAndBlameForFailure(this.glassNeeded, this.GLASS_BUFFER);
        isReady = isReady && checkBufferAndBlameForFailure(this.glowstoneNeeded, this.GLOWSTONE_BUFFER);
        isReady = isReady && checkBufferAndBlameForFailure(this.cyanNeeded, this.CYAN_BUFFER);
        isReady = isReady && checkBufferAndBlameForFailure(this.magentaNeeded, this.MAGENTA_BUFFER);
        isReady = isReady && checkBufferAndBlameForFailure(this.yellowNeeded, this.YELLOW_BUFFER);
        isReady = isReady && checkBufferAndBlameForFailure(this.blackNeeded, this.BLACK_BUFFER);
        
        this.isFabricationReady = isReady;
        
        // As soon as we are able to make any block, forget that we have material shortages.
        // Maybe some other builder will handle.
        if(isReady) this.bufferManager.forgiveAll();
        
        return this.isFabricationReady ? substance : null;
    }
    
    private boolean checkBufferAndBlameForFailure(int levelNeeded, MaterialBuffer buffer)
    {
        if(levelNeeded > buffer.getLevel())
        {
            if(!buffer.isFailureCause())
            {
                buffer.setFailureCause(true);
                this.markPlayerUpdateDirty(true);
                this.bufferManager.blame();
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
        
        if(this.stoneNeeded > 0) this.STONE_BUFFER.use(stoneNeeded);
        if(this.woodNeeded > 0) this.WOOD_BUFFER.use(woodNeeded);
        if(this.glassNeeded > 0) this.GLASS_BUFFER.use(glassNeeded);
        if(this.glowstoneNeeded > 0) this.GLOWSTONE_BUFFER.use(glowstoneNeeded);
        if(this.cyanNeeded > 0) this.CYAN_BUFFER.use(cyanNeeded);
        if(this.magentaNeeded > 0) this.MAGENTA_BUFFER.use(magentaNeeded);
        if(this.yellowNeeded > 0) this.YELLOW_BUFFER.use(yellowNeeded);
        if(this.blackNeeded > 0) this.BLACK_BUFFER.use(blackNeeded);
        this.isFabricationReady = false;
    }
    
    private void pullResources()
    {
        
        if(!bufferManager.canRestock()) return;
        
        for(EnumFacing face : EnumFacing.VALUES)
        {
            TileEntity tileentity = this.world.getTileEntity(this.pos.offset(face));
            if (tileentity != null)
            {
                IItemHandler capability = tileentity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face.getOpposite());
                if(bufferManager.restock(capability)) this.markDirty();;
                if(!bufferManager.canRestock()) return;
            }
        }
    }

    @Override
    public IItemHandler getItemHandler()
    {
        return this.bufferManager;
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
        // TODO Auto-generated method stub
        
    }

    @Override
    public void reconnect()
    {
        // TODO Auto-generated method stub
        
    }
}
