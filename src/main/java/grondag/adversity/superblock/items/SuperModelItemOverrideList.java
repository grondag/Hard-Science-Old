package grondag.adversity.superblock.items;

import com.google.common.collect.Lists;

import grondag.adversity.superblock.block.SuperDispatcher;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class SuperModelItemOverrideList extends ItemOverrideList
{
	private final SuperDispatcher dispatcher;
	
	public SuperModelItemOverrideList(SuperDispatcher dispatcher) {
		super(Lists.<ItemOverride>newArrayList());
		this.dispatcher = dispatcher;
	}

	@Override
	public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity)
	{
		return dispatcher.handleItemState(originalModel, stack, world, entity);
	}
}
