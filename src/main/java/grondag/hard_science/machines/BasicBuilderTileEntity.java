package grondag.hard_science.machines;

import static grondag.hard_science.machines.support.MaterialBuffer.UNITS_PER_ITEM;

import grondag.hard_science.Configurator;
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
import grondag.hard_science.machines.support.WeightedIngredientList.WeightedIngredient;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.block.SuperModelBlock;
import grondag.hard_science.superblock.block.SuperModelTileEntity;
import grondag.hard_science.superblock.color.ColorMap.EnumColorMap;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.model.state.PaintLayer;
import grondag.hard_science.superblock.varia.BlockSubstance;
import grondag.hard_science.virtualblock.VirtualBlock;
import grondag.hard_science.virtualblock.VirtualBlockTracker;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import jline.internal.Log;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;


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
    private int xChunkOffset = 0;
    private int zChunkOffset = 0;
    private BlockPos checkPos;
    private int tickCounter = 0;    
    private LongOpenHashSet failuresThisChunk = new LongOpenHashSet();
    

    // Buffer setup - all persisted
    private final MaterialBuffer WOOD_BUFFER = new MaterialBuffer(WOOD_INGREDIENTS, 64, "mbl_wood");
    private final MaterialBuffer STONE_BUFFER = new MaterialBuffer(STONE_INGREDIENTS, 64, "mbl_stone");
    private final MaterialBuffer GLASS_BUFFER = new MaterialBuffer(GLASS_INGREDIENTS, 64, "mbl_glass");
    private final MaterialBuffer GLOWSTONE_BUFFER = new MaterialBuffer(GLOWSTONE_INGREDIENTS, 64, "mbl_glowstone");
    private final MaterialBuffer CYAN_BUFFER = new MaterialBuffer(CYAN_INGREDIENTS, 64, "mbl_cyan");
    private final MaterialBuffer MAGENTA_BUFFER = new MaterialBuffer(MAGENTA_INGREDIENTS, 64, "mbl_magenta");
    private final MaterialBuffer YELLOW_BUFFER = new MaterialBuffer(YELLOW_INGREDIENTS, 64, "mbl_yellow");
    private final MaterialBuffer BLACK_BUFFER = new MaterialBuffer(BLACK_INGREDIENTS, 64, "mbl_black");   
    private final MaterialBufferManager bufferManager = new MaterialBufferManager(WOOD_BUFFER, STONE_BUFFER, GLASS_BUFFER, GLOWSTONE_BUFFER,
            CYAN_BUFFER, MAGENTA_BUFFER, YELLOW_BUFFER, BLACK_BUFFER);
    
    public BasicBuilderTileEntity()
    {
        super();
        this.setBufferManager(bufferManager);
    }
    
    @Override
    public void update()
    {
        super.update();
        if(world.isRemote || !this.isOn()) return;
        
        if(checkPos == null) checkPos = this.pos;
        
        if(++tickCounter == 20)
        {
            tickCounter = 0;
            this.pullResources();
        }
        
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
            
            if(++xChunkOffset >= Configurator.MACHINES.basicBuilderChunkRadius)
            {
                xChunkOffset = -Configurator.MACHINES.basicBuilderChunkRadius;
                if(++zChunkOffset > Configurator.MACHINES.basicBuilderChunkRadius)
                {
                    zChunkOffset = -Configurator.MACHINES.basicBuilderChunkRadius;
                }
            }
            this.checkPos = this.pos.add(this.xChunkOffset * 16, 0, this.zChunkOffset * 16);
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
            this.fabricate();     

            SuperModelBlock newBlock = ModSuperModelBlocks.findAppropriateSuperModelBlock(substance, modelState);
            IBlockState newState = newBlock.getDefaultState().withProperty(SuperBlock.META, oldState.getValue(SuperBlock.META));
            
            world.setBlockState(targetPos, newState);
            
            SuperModelTileEntity newTE = (SuperModelTileEntity)world.getTileEntity(targetPos);
            if (newTE == null) return;
            
            newTE.setLightValue(oldTE.getLightValue());
            newTE.setSubstance(substance);
            newTE.setModelState(modelState);
        }
        
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
        
        this.isFabricationReady =
                this.stoneNeeded <= this.STONE_BUFFER.getLevel()
             && this.woodNeeded <= this.WOOD_BUFFER.getLevel()
             && this.glassNeeded <= this.GLASS_BUFFER.getLevel()
             && this.glowstoneNeeded <= this.GLOWSTONE_BUFFER.getLevel()
             && this.cyanNeeded <= this.CYAN_BUFFER.getLevel()
             && this.magentaNeeded <= this.MAGENTA_BUFFER.getLevel()
             && this.yellowNeeded <= this.YELLOW_BUFFER.getLevel()
             && this.blackNeeded <= this.BLACK_BUFFER.getLevel();
        
        return this.isFabricationReady ? substance : null;
    }
    
    /**
     * Consumes the resources calculated during last call to prepareFabrication.
     */
    private void fabricate()
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
        this.markDirty();
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
