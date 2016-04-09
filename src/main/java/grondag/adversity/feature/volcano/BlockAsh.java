// TODO: Fix or remove

//package com.grondag.adversity.feature.volcano;
//
//import java.util.Random;
//
//import net.minecraft.block.Block;
//import net.minecraft.block.material.Material;
//import net.minecraft.client.renderer.texture.IIconRegister;
//import net.minecraft.entity.player.EntityPlayer;
//import net.minecraft.init.Items;
//import net.minecraft.item.Item;
//import net.minecraft.util.AxisAlignedBB;
//import net.minecraft.util.IIcon;
//import net.minecraft.world.IBlockAccess;
//import net.minecraft.world.World;
//
//import com.grondag.adversity.Adversity;
//import com.grondag.adversity.client.AdversityBlockRenderer;
//import com.grondag.adversity.client.FlippableIcon;
//import com.grondag.adversity.client.TextureMixer;
//
//import cpw.mods.fml.relauncher.Side;
//import cpw.mods.fml.relauncher.SideOnly;
//
//public class BlockAsh extends Block {
//
//	private static int	renderID	= -1;
//
//	static TextureMixer	mixer		= new TextureMixer();
////
////	@Override
////	public int getRenderType() {
////
////		if (renderID == -1) {
////			renderID = new AdversityBlockRenderer().getRenderId();
////		}
////		return renderID;
////	}
//
//	public IIcon[]	icons	= new IIcon[6];
//
//	public BlockAsh(String unlocalizedName, Material material) {
//		super(material);
//		this.setBlockName(unlocalizedName);
//		this.setCreativeTab(Adversity.tabAdversity);
//		this.setHarvestLevel("shovel", 0);
//		this.setStepSound(soundTypeSnow);
//		this.setHardness(0.1F);
//		this.setResistance(2);
//		this.setBlockTextureName(Adversity.MODID + ":ash0");
//	}
//
//	@Override
//	public void registerBlockIcons(IIconRegister reg) {
//		this.icons[0] = reg.registerIcon(Adversity.MODID + ":ash3");
//		FlippableIcon f = new FlippableIcon(icons[0]);
//		f.setFlip(true, false);
//		this.icons[1] = f;
//		
//		f = new FlippableIcon(icons[0]);
//		f.setFlip(false, true);
//		this.icons[2] = f;
//		
//		f = new FlippableIcon(icons[0]);
//		f.setFlip(true, true);
//		this.icons[3] = f;		
//		
////		this.icons[0] = reg.registerIcon(Adversity.MODID + ":ash0");
////		this.icons[1] = reg.registerIcon(Adversity.MODID + ":ash1");
////		this.icons[2] = reg.registerIcon(Adversity.MODID + ":ash2");
////		this.icons[3] = reg.registerIcon(Adversity.MODID + ":ash3");
//		// only the first four are unique and the last two are not expected to be used,
//		// but populating as a failsafe against run-time errors.
//		this.icons[4] = reg.registerIcon(Adversity.MODID + ":ash0");
//		this.icons[5] = reg.registerIcon(Adversity.MODID + ":ash1");
//	}
//
//	@Override
//	public AxisAlignedBB getCollisionBoundingBoxFromPool(World p_149668_1_, int p_149668_2_, int p_149668_3_,
//			int p_149668_4_) {
//		int l = p_149668_1_.getBlockMetadata(p_149668_2_, p_149668_3_, p_149668_4_) & 7;
//		float f = 0.125F;
//		return AxisAlignedBB.getBoundingBox(p_149668_2_ + this.minX, p_149668_3_ + this.minY, p_149668_4_ + this.minZ,
//				p_149668_2_ + this.maxX, p_149668_3_ + l * f, p_149668_4_ + this.maxZ);
//	}
//
//	@Override
//	public boolean isOpaqueCube() {
//		return false;
//	}
//
//	@Override
//	public boolean renderAsNormalBlock() {
//		return false;
//	}
//
//	@Override
//	public void setBlockBoundsForItemRender() {
//		this.calculateBlockBounds(0);
//	}
//
//	@Override
//	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
//		this.calculateBlockBounds(world.getBlockMetadata(x, y, z));
//	}
//
//	protected void calculateBlockBounds(int meta) {
//		int j = meta & 7;
//		float f = 2 * (1 + j) / 16.0F;
//		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, f, 1.0F);
//	}
//
//	@Override
//	public boolean canPlaceBlockAt(World world, int x, int y, int z) {
//		Block block = world.getBlock(x, y - 1, z);
//		return block.isBlockNormalCube();
//	}
//
//	/**
//	 * Lets the block know when one of its neighbor changes. Doesn't know which neighbor changed (coordinates passed are
//	 * their own) Args: x, y, z, neighbor Block
//	 */
//	@Override
//	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
//		this.validateLocation(world, x, y, z);
//		// TODO: should make this drop ash piles
//	}
//
//	private boolean validateLocation(World world, int x, int y, int z) {
//		if (!this.canPlaceBlockAt(world, x, y, z)) {
//			world.setBlockToAir(x, y, z);
//			return false;
//		} else
//			return true;
//	}
//
//	 /**
//	 * Called when the player destroys a block with an item that can harvest it. (i, j, k) are the coordinates of the
//	 * block and l is the block's subtype/damage.
//	 */
//	@Override
//	public void harvestBlock(World p_149636_1_, EntityPlayer p_149636_2_, int p_149636_3_, int p_149636_4_,
//			int p_149636_5_, int p_149636_6_) {
//		super.harvestBlock(p_149636_1_, p_149636_2_, p_149636_3_, p_149636_4_, p_149636_5_, p_149636_6_);
//		p_149636_1_.setBlockToAir(p_149636_3_, p_149636_4_, p_149636_5_);
//	}
//
//	@Override
//	public Item getItemDropped(int p_149650_1_, Random p_149650_2_, int p_149650_3_) {
//		return Items.snowball;
//	}
//
//	/**
//	 * Returns the quantity of items to drop on block destruction.
//	 */
//	@Override
//	public int quantityDropped(Random p_149745_1_) {
//		return 1;
//	}
//
//	@Override
//	@SideOnly(Side.CLIENT)
//	public boolean shouldSideBeRendered(IBlockAccess p_149646_1_, int p_149646_2_, int p_149646_3_, int p_149646_4_,
//			int p_149646_5_) {
//		return p_149646_5_ == 1 ? true : super.shouldSideBeRendered(p_149646_1_, p_149646_2_, p_149646_3_, p_149646_4_,
//				p_149646_5_);
//	}
//
//	@Override
//	public int quantityDropped(int meta, int fortune, Random random) {
//		return (meta & 7) + 1;
//	}
//
//	@Override
//	public IIcon getIcon(IBlockAccess iba, int x, int y, int z, int side) {
//		mixer.setToPosition(x, y, z);
//		return this.icons[mixer.mix.index[side]];
//	}
//
//	@Override
//	public IIcon getIcon(int side, int meta) {
//		return this.icons[side];
//	}
//
//}
