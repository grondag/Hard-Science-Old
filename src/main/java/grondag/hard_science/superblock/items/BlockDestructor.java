//package grondag.hard_science.superblock.items;
//
//import grondag.hard_science.library.varia.Useful;
//import net.minecraft.entity.player.EntityPlayer;
//import net.minecraft.item.Item;
//import net.minecraft.item.ItemStack;
//import net.minecraft.nbt.NBTTagCompound;
//import net.minecraft.util.ActionResult;
//import net.minecraft.util.EnumActionResult;
//import net.minecraft.util.EnumFacing;
//import net.minecraft.util.EnumHand;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.text.TextComponentString;
//import net.minecraft.util.text.translation.I18n;
//import net.minecraft.world.World;
//
///**
// * Tool to rotate & connect/disconnect blocks
// */
//public class BlockDestructor extends Item
//{
//    private static final String MODE_TAG = "mode";
//
//    private enum WandMode
//    {
//        X221(2, 2, 1),
//        X222(2, 2, 2),
//        X331(3, 3, 1),
//        X333(3, 3, 3),
//        X441(4, 4, 1),
//        X444(4, 4, 4),
//        X551(5, 5, 1),
//        X555(5, 5, 5);
//        
//        private WandMode(int h, int w, int d)
//        {
//            this.h = h;
//            this.w = w;
//            this.d = d;
//        }
//        
//        private final int h;
//        private final int w;
//        private final int d;
//    }
//    
//    public BlockDestructor()
//    {
//        setRegistryName("block_wand"); 
//        setUnlocalizedName("block_wand");
//        this.setMaxStackSize(1);
//    }
//    
//    @Override
//    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand)
//    {
//        ItemStack stack = playerIn.getHeldItem(hand);
//        
//        if(!worldIn.isRemote)
//        {
//            WandMode newMode = WandMode.X331;
//            NBTTagCompound tag;
//
//            if(stack.hasTagCompound())
//            {
//                tag = stack.getTagCompound();
//                newMode = Useful.nextEnumValue(Useful.safeEnumFromOrdinal(tag.getInteger(MODE_TAG), WandMode.X222));
//                
//            }
//            else
//            {
//                tag = new NBTTagCompound();
//
//            }
//
//            tag.setInteger(MODE_TAG, newMode.ordinal());
//            stack.setTagCompound(tag);
//
//            @SuppressWarnings("deprecation")
//            String message = I18n.translateToLocalFormatted("misc.mode_set",  I18n.translateToLocal("misc." + newMode.toString().toLowerCase()));
//            playerIn.sendMessage(new TextComponentString(message));
//
//        }
//        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
//    }    
//
//    public WandMode getMode(ItemStack itemStackIn)
//    {
//        return itemStackIn.hasTagCompound() ? 
//                Useful.safeEnumFromOrdinal(itemStackIn.getTagCompound().getInteger(MODE_TAG), WandMode.X331) 
//                : WandMode.X331;
//    }
//
//    @Override
//    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
//    {
//        return EnumActionResult.SUCCESS;
//        
////        TODO
////        if(worldIn.isRemote) return EnumActionResult.SUCCESS;
////        
////        ItemStack stack = playerIn.getHeldItem(hand);
////        
////        for()
//    }
//
//   
//}
