package grondag.hard_science.machines;

import grondag.hard_science.Configurator;
import grondag.hard_science.init.ModBlocks;
import grondag.hard_science.init.ModSuperModelBlocks;
import grondag.hard_science.machines.WeightedIngredientList.WeightedIngredient;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.block.SuperModelBlock;
import grondag.hard_science.superblock.block.SuperModelTileEntity;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.virtualblock.VirtualBlock;
import grondag.hard_science.virtualblock.VirtualBlockTracker;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class BasicBuilderTileEntity extends MachineTileEntity implements ITickable
{
    private int xChunkOffset = 0;
    private int zChunkOffset = 0;
    private BlockPos checkPos;

    private int tickCounter = 0;
    
    
    private static final WeightedIngredientList WOOD_INGREDIENTS = new WeightedIngredientList(
            new WeightedIngredient("logWood", MaterialBuffer.UNITS_PER_ITEM * 4), 
            new WeightedIngredient("plankWood", MaterialBuffer.UNITS_PER_ITEM), 
            new WeightedIngredient("slabWood", MaterialBuffer.UNITS_PER_ITEM / 2),
            new WeightedIngredient("stairWood", MaterialBuffer.UNITS_PER_ITEM * 6 / 4),
            new WeightedIngredient("stickWood", MaterialBuffer.UNITS_PER_ITEM / 2));
    private final MaterialBuffer WOOD_BUFFER = new MaterialBuffer(WOOD_INGREDIENTS, 64, "wood");
    
    private static final WeightedIngredientList STONE_INGREDIENTS = new WeightedIngredientList(
            new WeightedIngredient("stone", MaterialBuffer.UNITS_PER_ITEM),
            new WeightedIngredient("cobblestone", MaterialBuffer.UNITS_PER_ITEM),
            new WeightedIngredient("stoneGranite", MaterialBuffer.UNITS_PER_ITEM),
            new WeightedIngredient("stoneGranitePolished", MaterialBuffer.UNITS_PER_ITEM),
            new WeightedIngredient("stoneDiorite", MaterialBuffer.UNITS_PER_ITEM),
            new WeightedIngredient("stoneDioritePolished", MaterialBuffer.UNITS_PER_ITEM),
            new WeightedIngredient("stoneAndesite", MaterialBuffer.UNITS_PER_ITEM),
            new WeightedIngredient("stoneAndesitePolished", MaterialBuffer.UNITS_PER_ITEM),
            new WeightedIngredient("stone", MaterialBuffer.UNITS_PER_ITEM),
            new WeightedIngredient("stone", MaterialBuffer.UNITS_PER_ITEM),
            new WeightedIngredient("stone", MaterialBuffer.UNITS_PER_ITEM));
    private final MaterialBuffer STONE_BUFFER = new MaterialBuffer(STONE_INGREDIENTS, 64, "wood");
    
    private static final WeightedIngredientList GLASS_INGREDIENTS = new WeightedIngredientList(
            new WeightedIngredient("logWood", MaterialBuffer.UNITS_PER_ITEM * 4), 
            new WeightedIngredient("plankWood", MaterialBuffer.UNITS_PER_ITEM), 
            new WeightedIngredient("slabWood", MaterialBuffer.UNITS_PER_ITEM / 2),
            new WeightedIngredient("stairWood", MaterialBuffer.UNITS_PER_ITEM * 6 / 4),
            new WeightedIngredient("stickWood", MaterialBuffer.UNITS_PER_ITEM / 2));
    private final MaterialBuffer GLASS_BUFFER = new MaterialBuffer(GLASS_INGREDIENTS, 64, "wood");
    
    private static final WeightedIngredientList GLOWSTONE_INGREDIENTS = new WeightedIngredientList(
            new WeightedIngredient("dustGlowstone", MaterialBuffer.UNITS_PER_ITEM), 
            new WeightedIngredient("glowstone", MaterialBuffer.UNITS_PER_ITEM * 4));
    private final MaterialBuffer GLOWSTONE_BUFFER = new MaterialBuffer(GLOWSTONE_INGREDIENTS, 64, "wood");
    
    private static final WeightedIngredientList CYAN_INGREDIENTS = new WeightedIngredientList(
            new WeightedIngredient("dyeCyan", MaterialBuffer.UNITS_PER_ITEM));
    private final MaterialBuffer CYAN_BUFFER = new MaterialBuffer(CYAN_INGREDIENTS, 64, "wood");
    
    private static final WeightedIngredientList MAGENTA_INGREDIENTS = new WeightedIngredientList(
            new WeightedIngredient("dyeMagenta", MaterialBuffer.UNITS_PER_ITEM));
    private final MaterialBuffer MAGENTA_BUFFER = new MaterialBuffer(MAGENTA_INGREDIENTS, 64, "wood");
    
    private static final WeightedIngredientList YELLOW_INGREDIENTS = new WeightedIngredientList(
            new WeightedIngredient("dyeYellow", MaterialBuffer.UNITS_PER_ITEM));
    private final MaterialBuffer YELLOW_BUFFER = new MaterialBuffer(YELLOW_INGREDIENTS, 64, "wood");
    
    private static final WeightedIngredientList BLACK_INGREDIENTS = new WeightedIngredientList(
            new WeightedIngredient("dyeBlack", MaterialBuffer.UNITS_PER_ITEM));
    private final MaterialBuffer BLACK_BUFFER = new MaterialBuffer(BLACK_INGREDIENTS, 64, "wood");
    
    private final MaterialBufferManager bufferManager = new MaterialBufferManager(WOOD_BUFFER, STONE_BUFFER, GLASS_BUFFER, GLOWSTONE_BUFFER,
            CYAN_BUFFER, MAGENTA_BUFFER, YELLOW_BUFFER, BLACK_BUFFER);
    
    @Override
    public void update()
    {
        if(world.isRemote || !this.isOn()) return;
        
        if(checkPos == null) checkPos = this.pos;
        
        if(++tickCounter == 20)
        {
            tickCounter = 0;
            this.pullResources();
        }
        
        // look for a virtual block in current target chunk
        BlockPos targetPos = VirtualBlockTracker.INSTANCE.get(this.world).dequeue(checkPos);
        
        // if nothing found in this chunk, move to next chunk and exit
        if(targetPos == null)
        {
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
        SuperModelBlock newBlock = ModSuperModelBlocks.findAppropriateSuperModelBlock(oldTE.getSubstance(), modelState);

        world.setBlockState(targetPos, newBlock.getDefaultState().withProperty(SuperBlock.META, oldState.getValue(SuperBlock.META)));
        
        SuperModelTileEntity newTE = (SuperModelTileEntity)world.getTileEntity(targetPos);
        if (newTE == null) return;
        
        newTE.setLightValue(oldTE.getLightValue());
        newTE.setSubstance(oldTE.getSubstance());
        newTE.setModelState(modelState);
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
                bufferManager.restock(capability);
                if(!bufferManager.canRestock()) return;
            }
        }
    }
}
