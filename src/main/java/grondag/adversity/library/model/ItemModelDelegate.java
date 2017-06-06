//package grondag.adversity.library.model;
//
//import com.google.common.collect.Lists;
//
//import net.minecraft.client.renderer.block.model.IBakedModel;
//import net.minecraft.client.renderer.block.model.ItemOverride;
//import net.minecraft.client.renderer.block.model.ItemOverrideList;
//import net.minecraft.entity.EntityLivingBase;
//import net.minecraft.item.ItemStack;
//import net.minecraft.world.World;
//
//public class ItemModelDelegate extends ItemOverrideList
//{
//	private final ModelDispatcher dispatcher;
//	
//	public ItemModelDelegate(ModelDispatcher dispatcher) {
//		super(Lists.<ItemOverride>newArrayList());
//		this.dispatcher = dispatcher;
//	}
//
//	@Override
//	public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity)
//	{
//		return dispatcher.handleItemState(originalModel, stack, world, entity);
//	}
//}
