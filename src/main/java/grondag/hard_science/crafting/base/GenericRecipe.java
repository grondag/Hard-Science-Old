package grondag.hard_science.crafting.base;

import java.util.Collection;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.external.jei.HardScienceJEIPlugIn;
import grondag.hard_science.external.jei.RecipeLayout;
import grondag.hard_science.gui.GuiUtil;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.HorizontalAlignment;
import grondag.hard_science.library.varia.VerticalAlignment;
import grondag.hard_science.machines.energy.MachinePower;
import grondag.hard_science.matter.VolumeUnits;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.FluidResource;
import grondag.hard_science.simulator.resource.FluidResourceWithQuantity;
import grondag.hard_science.simulator.resource.ItemResource;
import grondag.hard_science.simulator.resource.ItemResourceWithQuantity;
import grondag.hard_science.simulator.resource.PowerResource;
import grondag.hard_science.simulator.resource.StorageType;
import mezz.jei.api.gui.IDrawable;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;

public class GenericRecipe implements IHardScienceRecipe
{
    public static final GenericRecipe EMPTY_RECIPE = new GenericRecipe(
            ImmutableList.of(), 
            ImmutableList.of(), 
            0) {};
    
    private final ImmutableList<FluidResourceWithQuantity> fluidInputs;
    private final ImmutableList<ItemResourceWithQuantity> itemInputs;
    private final ImmutableList<FluidResourceWithQuantity> fluidOutputs;
    private final ImmutableList<ItemResourceWithQuantity> itemOutputs;
    private final long energyInputJoules;
    private final long energyOutputJoules;
    private final int ticksDuration;
    
    /**
     * Lazy creation - do not reference directly.
     */
    private RecipeLayout layout;
    
    protected GenericRecipe(
            Collection<AbstractResourceWithQuantity<?>> inputs,
            Collection<AbstractResourceWithQuantity<?>> outputs,
            int ticksDuration
            )
    {
        ImmutableList.Builder<FluidResourceWithQuantity> fluidInputs = ImmutableList.builder();
        ImmutableList.Builder<FluidResourceWithQuantity> fluidOutputs = ImmutableList.builder();
        ImmutableList.Builder<ItemResourceWithQuantity> itemInputs = ImmutableList.builder();
        ImmutableList.Builder<ItemResourceWithQuantity> itemOutputs = ImmutableList.builder();
        long energyInputJoules = 0;
        long energyOutputJoules = 0;
        
        for(AbstractResourceWithQuantity<?> rwq : inputs)
        {
            switch(rwq.resource().storageType().enumType)
            {
            case FLUID:
                fluidInputs.add((FluidResourceWithQuantity) rwq);
                break;
                
            case ITEM:
                itemInputs.add((ItemResourceWithQuantity) rwq);
                break;
                
            case POWER:
                energyInputJoules = rwq.getQuantity();
                break;

            case PRIVATE:
            default:
                break;
            
            }
        }
        
        for(AbstractResourceWithQuantity<?> rwq : outputs)
        {
            switch(rwq.resource().storageType().enumType)
            {
            case FLUID:
                fluidOutputs.add((FluidResourceWithQuantity) rwq);
                break;
                
            case ITEM:
                itemOutputs.add((ItemResourceWithQuantity) rwq);
                break;
                
            case POWER:
                energyOutputJoules = rwq.getQuantity();
                break;

            case PRIVATE:
            default:
                break;
            
            }
        }
        
        this.fluidInputs = fluidInputs.build();
        this.itemInputs = itemInputs.build();
        this.fluidOutputs = fluidOutputs.build();
        this.itemOutputs = itemOutputs.build();
        this.energyInputJoules = energyInputJoules;
        this.energyOutputJoules = energyOutputJoules;
        this.ticksDuration = ticksDuration;
    }
    
    public GenericRecipe(
            AbstractCraftingProcess<?> process,
            SingleParameterModel.Result result,
            int ticksDuration
            )
    {
        if(process.fluidInputs().isEmpty())
        {
            this.fluidInputs = ImmutableList.of();
        }
        else
        {
            ImmutableList.Builder<FluidResourceWithQuantity> builder = ImmutableList.builder();
            for(FluidResource r : process.fluidInputs())
            {
                builder.add(r.withQuantity(result.inputValueDiscrete(r)));
            }
            this.fluidInputs = builder.build();
        }

        if(process.itemInputs().isEmpty())
        {
            this.itemInputs = ImmutableList.of();
        }
        else
        {
            ImmutableList.Builder<ItemResourceWithQuantity> builder = ImmutableList.builder();
            for(ItemResource r : process.itemInputs())
            {
                builder.add(r.withQuantity(result.inputValueDiscrete(r)));
            }
            this.itemInputs = builder.build();
        }
        
        if(process.fluidOutputs().isEmpty())
        {
            this.fluidOutputs = ImmutableList.of();
        }
        else
        {
            ImmutableList.Builder<FluidResourceWithQuantity> builder = ImmutableList.builder();
            for(FluidResource r : process.fluidOutputs())
            {
                builder.add(r.withQuantity(result.outputValueDiscrete(r)));
            }
            this.fluidOutputs = builder.build();
        }
        
        if(process.itemOutputs().isEmpty())
        {
            this.itemOutputs = ImmutableList.of();
        }
        else
        {
            ImmutableList.Builder<ItemResourceWithQuantity> builder = ImmutableList.builder();
            for(ItemResource r : process.itemOutputs())
            {
                builder.add(r.withQuantity(result.outputValueDiscrete(r)));
            }
            this.itemOutputs = builder.build();
        }
        
        this.energyInputJoules = process.consumesEnergy()
                ? Math.round(result.inputValue(PowerResource.JOULES))
                : 0;
                
        this.energyOutputJoules = process.producesEnergy()
                ? Math.round(result.outputValue(PowerResource.JOULES))
                : 0;
        
        this.ticksDuration = ticksDuration;
    }
    
    public GenericRecipe(NBTTagCompound tag)
    {
        this.energyInputJoules = tag.getLong(ModNBTTag.RECIPE_INPUT_JOULES);
        this.energyOutputJoules = tag.getLong(ModNBTTag.RECIPE_OUTPUT_JOULES);
        this.ticksDuration = tag.getInteger(ModNBTTag.RECIPE_TICKS_DURATION);

        if(tag.hasKey(ModNBTTag.RECIPE_FLUID_INPUTS))
        {
            NBTTagList list = tag.getTagList(ModNBTTag.RECIPE_FLUID_INPUTS, 10);
            ImmutableList.Builder<FluidResourceWithQuantity> builder = ImmutableList.builder();
            list.forEach(t -> builder.add((FluidResourceWithQuantity) StorageType.FLUID.fromNBTWithQty((NBTTagCompound) t)));
            this.fluidInputs = builder.build();
        }
        else this.fluidInputs = ImmutableList.of();
       
        if(tag.hasKey(ModNBTTag.RECIPE_ITEM_INPUTS))
        {
            NBTTagList list = tag.getTagList(ModNBTTag.RECIPE_ITEM_INPUTS, 10);
            ImmutableList.Builder<ItemResourceWithQuantity> builder = ImmutableList.builder();
            list.forEach(t -> builder.add((ItemResourceWithQuantity) StorageType.ITEM.fromNBTWithQty((NBTTagCompound) t)));
            this.itemInputs = builder.build();
        }
        else this.itemInputs = ImmutableList.of();
        
        if(tag.hasKey(ModNBTTag.RECIPE_FLUID_OUTPUTS))
        {
            NBTTagList list = tag.getTagList(ModNBTTag.RECIPE_FLUID_OUTPUTS, 10);
            ImmutableList.Builder<FluidResourceWithQuantity> builder = ImmutableList.builder();
            list.forEach(t -> builder.add((FluidResourceWithQuantity) StorageType.FLUID.fromNBTWithQty((NBTTagCompound) t)));
            this.fluidOutputs = builder.build();
        }
        else this.fluidOutputs = ImmutableList.of();
       
        if(tag.hasKey(ModNBTTag.RECIPE_ITEM_OUTPUTS))
        {
            NBTTagList list = tag.getTagList(ModNBTTag.RECIPE_ITEM_OUTPUTS, 10);
            ImmutableList.Builder<ItemResourceWithQuantity> builder = ImmutableList.builder();
            list.forEach(t -> builder.add((ItemResourceWithQuantity) StorageType.ITEM.fromNBTWithQty((NBTTagCompound) t)));
            this.itemOutputs = builder.build();
        }
        else this.itemOutputs = ImmutableList.of();
    }
    
    public GenericRecipe(PacketBuffer pBuff)
    {
        this.energyInputJoules = pBuff.readVarLong();
        this.energyOutputJoules = pBuff.readVarLong();
        this.ticksDuration = pBuff.readVarInt();

        int size = pBuff.readByte();
        if(size == 0)
        {
            this.fluidInputs = ImmutableList.of();
        }
        else
        {
            ImmutableList.Builder<FluidResourceWithQuantity> builder = ImmutableList.builder();
            for(int i = 0; i < size; i++)
            {
                builder.add((FluidResourceWithQuantity) StorageType.FLUID.fromBytesWithQty(pBuff));
            }
            this.fluidInputs = builder.build();
        }
       
        size = pBuff.readByte();
        if(size == 0)
        {
            this.itemInputs = ImmutableList.of();
        }
        else
        {
            ImmutableList.Builder<ItemResourceWithQuantity> builder = ImmutableList.builder();
            for(int i = 0; i < size; i++)
            {
                builder.add((ItemResourceWithQuantity) StorageType.ITEM.fromBytesWithQty(pBuff));
            }
            this.itemInputs = builder.build();
        }
        
        size = pBuff.readByte();
        if(size == 0)
        {
            this.fluidOutputs = ImmutableList.of();
        }
        else
        {
            ImmutableList.Builder<FluidResourceWithQuantity> builder = ImmutableList.builder();
            for(int i = 0; i < size; i++)
            {
                builder.add((FluidResourceWithQuantity) StorageType.FLUID.fromBytesWithQty(pBuff));
            }
            this.fluidOutputs = builder.build();
        }
       
        size = pBuff.readByte();
        if(size == 0)
        {
            this.itemOutputs = ImmutableList.of();
        }
        else
        {
            ImmutableList.Builder<ItemResourceWithQuantity> builder = ImmutableList.builder();
            for(int i = 0; i < size; i++)
            {
                builder.add((ItemResourceWithQuantity) StorageType.ITEM.fromBytesWithQty(pBuff));
            }
            this.itemOutputs = builder.build();
        }
    }
    
    @Override
    public ImmutableList<FluidResourceWithQuantity> fluidInputs()
    {
        return this.fluidInputs;
    }

    @Override
    public ImmutableList<ItemResourceWithQuantity> itemInputs()
    {
        return this.itemInputs;
    }

    @Override
    public ImmutableList<FluidResourceWithQuantity> fluidOutputs()
    {
        return this.fluidOutputs;
    }

    @Override
    public ImmutableList<ItemResourceWithQuantity> itemOutputs()
    {
        return this.itemOutputs;
    }

    @Override
    public long energyInputJoules()
    {
        return this.energyInputJoules;
    }

    @Override
    public long energyOutputJoules()
    {
        return this.energyOutputJoules;
    }

    @Override
    public int ticksDuration()
    {
        return this.ticksDuration;
    }

    public RecipeLayout layout()
    {
        if(this.layout == null)
        {
            this.layout = new RecipeLayout(
                    this.itemInputs.size() + this.fluidInputs.size(),
                    this.itemOutputs.size() + this.fluidOutputs.size());
        }
        return this.layout;
    }
    
    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY)
    {
        IDrawable slot = HardScienceJEIPlugIn.registry().getJeiHelpers().getGuiHelper().getSlotDrawable();
        RecipeLayout layout = this.layout();
        
        if(layout.inputCount > 0)
        {
            for(int i : layout.inputY)
            {
                slot.draw(minecraft, RecipeLayout.LEFT, i);
            }
            
            int inputIndex = 0;
            if(!this.itemInputs.isEmpty())
            {
                for(ItemResourceWithQuantity rwq : this.itemInputs)
                {
                    GuiUtil.drawAlignedStringNoShadow(
                            minecraft.fontRenderer, 
                            Long.toString(rwq.getQuantity()), 
                            RecipeLayout.LEFT, 
                            layout.inputY[inputIndex] + 20, 
                            20, 
                            8, 0xFF000000, HorizontalAlignment.CENTER, VerticalAlignment.TOP);
                    inputIndex++;
                }
            }
            
            if(!this.fluidInputs.isEmpty())
            {
                for(FluidResourceWithQuantity rwq : this.fluidInputs)
                {
                    GuiUtil.drawAlignedStringNoShadow(
                            minecraft.fontRenderer,
                            VolumeUnits.formatVolume(rwq.getQuantity(), false),
                            RecipeLayout.LEFT, 
                            layout.inputY[inputIndex] + 20, 
                            20, 8, 0xFF000000, HorizontalAlignment.CENTER, VerticalAlignment.TOP);
                    inputIndex++;
                }
            }
        }
        
        if(layout.outputCount > 0)
        {
            for(int i : layout.outputY)
            {
                slot.draw(minecraft, RecipeLayout.RIGHT, i);
            }
            
            int outputIndex = 0;
            if(!this.itemOutputs.isEmpty())
            {
                for(ItemResourceWithQuantity rwq : this.itemOutputs)
                {
                    GuiUtil.drawAlignedStringNoShadow(
                            minecraft.fontRenderer, 
                            Long.toString(rwq.getQuantity()), 
                            RecipeLayout.RIGHT, 
                            layout.outputY[outputIndex] + 20, 
                            20, 8, 0xFF000000, HorizontalAlignment.CENTER, VerticalAlignment.TOP);
                    outputIndex++;
                }
            }
            
            if(!this.fluidOutputs.isEmpty())
            {
                for(FluidResourceWithQuantity rwq : this.fluidOutputs)
                {
                    GuiUtil.drawAlignedStringNoShadow(
                            minecraft.fontRenderer,
                            VolumeUnits.formatVolume(rwq.getQuantity(), false),
                            RecipeLayout.RIGHT, 
                            layout.outputY[outputIndex] + 20, 
                            20, 8, 0xFF000000, HorizontalAlignment.CENTER, VerticalAlignment.TOP);
                    outputIndex++;
                }
            }
        }
        
        IHardScienceRecipe.super.drawInfo(minecraft, recipeWidth, recipeHeight, mouseX, mouseY);
        if(this.energyInputJoules != 0)
        {
            GuiUtil.drawAlignedStringNoShadow(
                    minecraft.fontRenderer,
                    MachinePower.formatEnergy(energyInputJoules, false),
                    0, 
                    4,
                    layout.width, 
                    8, 
                    0xFF000000, HorizontalAlignment.CENTER, VerticalAlignment.TOP);

        }
        
        if(this.energyOutputJoules != 0)
        {
            GuiUtil.drawAlignedStringNoShadow(
                    minecraft.fontRenderer,
                    MachinePower.formatEnergy(energyOutputJoules, false),
                    0, 
                    layout.height -4,
                    layout.width, 
                    8, 0xFF000000, HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM);
        }
    }

    public void serializeNBT(NBTTagCompound tag)
    {
        tag.setLong(ModNBTTag.RECIPE_INPUT_JOULES, energyInputJoules);
        tag.setLong(ModNBTTag.RECIPE_OUTPUT_JOULES, energyOutputJoules);
        tag.setInteger(ModNBTTag.RECIPE_TICKS_DURATION, ticksDuration);

        if(!this.fluidInputs.isEmpty())
        {
            NBTTagList list = new NBTTagList();
            this.fluidInputs.stream().forEach(r -> list.appendTag(r.toNBT()));
            tag.setTag(ModNBTTag.RECIPE_FLUID_INPUTS, list);
        }
       
        if(!this.itemInputs.isEmpty())
        {
            NBTTagList list = new NBTTagList();
            this.itemInputs.stream().forEach(r -> list.appendTag(r.toNBT()));
            tag.setTag(ModNBTTag.RECIPE_ITEM_INPUTS, list);
        }
        
        if(!this.fluidOutputs.isEmpty())
        {
            NBTTagList list = new NBTTagList();
            this.fluidOutputs.stream().forEach(r -> list.appendTag(r.toNBT()));
            tag.setTag(ModNBTTag.RECIPE_FLUID_OUTPUTS, list);
        }
       
        if(!this.itemOutputs.isEmpty())
        {
            NBTTagList list = new NBTTagList();
            this.itemOutputs.stream().forEach(r -> list.appendTag(r.toNBT()));
            tag.setTag(ModNBTTag.RECIPE_ITEM_OUTPUTS, list);
        }
    }

    public void toBytes(PacketBuffer pBuff)
    {
        pBuff.writeVarLong(energyInputJoules);
        pBuff.writeVarLong(energyOutputJoules);
        pBuff.writeVarInt(ticksDuration);

        pBuff.writeByte(this.fluidInputs.size());
        if(!this.fluidInputs.isEmpty()) 
            this.fluidInputs.stream().forEach(r -> StorageType.FLUID.toBytes(r, pBuff));
       
        pBuff.writeByte(this.itemInputs.size());
        if(!this.itemInputs.isEmpty()) 
            this.itemInputs.stream().forEach(r -> StorageType.ITEM.toBytes(r, pBuff));

        pBuff.writeByte(this.fluidOutputs.size());
        if(!this.fluidOutputs.isEmpty()) 
            this.fluidOutputs.stream().forEach(r -> StorageType.FLUID.toBytes(r, pBuff));
       
        pBuff.writeByte(this.itemOutputs.size());
        if(!this.itemOutputs.isEmpty()) 
            this.itemOutputs.stream().forEach(r -> StorageType.ITEM.toBytes(r, pBuff));
    }
}
