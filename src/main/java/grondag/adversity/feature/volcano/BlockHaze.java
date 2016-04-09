// TODO: Fix or remove

//package com.grondag.adversity.feature.volcano;
//
//import java.util.ArrayList;
//import java.util.Random;
//
//import net.minecraft.block.Block;
//import net.minecraft.block.material.Material;
//import net.minecraft.item.Item;
//import net.minecraft.item.ItemStack;
//import net.minecraft.util.AxisAlignedBB;
//import net.minecraft.util.IIcon;
//import net.minecraft.world.IBlockAccess;
//import net.minecraft.world.World;
//
//import com.grondag.adversity.Adversity;
//
//public class BlockHaze extends Block {
//
//	public BlockHaze(Material material) {
//		super(material);
//		this.setBlockName("haze");
//		this.setCreativeTab(Adversity.tabAdversity);
//		this.setBlockTextureName(Adversity.MODID + ":hazey_air");
//		this.setLightOpacity(8);
//	}
//
//	@Override
//	public boolean isCollidable() {
//		return false;
//	}
//
//	// @Override
//	// public void registerBlockIcons(IIconRegister reg)
//	// {
//	// this.blockIcon = reg.registerIcon(Adversity.MODID + ":hazey_air");
//	// }
//	//
//	// public IIcon getBlockTexture(IBlockAccess iba, int x, int y, int z) {
//	// return this.getIcon(0, 0);
//	// }
//
//	@Override
//	public IIcon getIcon(int s, int meta) {
//		// return Blocks.wool.getIcon(s, meta);
//		return this.blockIcon;
//	}
//
//	@Override
//	public boolean canBeReplacedByLeaves(IBlockAccess world, int x, int y, int z) {
//		return true;
//	}
//
//	// @Override
//	// public int getRenderBlockPass()
//	// {
//	// return 1;
//	// }
//
//	@Override
//	public boolean renderAsNormalBlock() {
//		return false;
//	}
//
//	@Override
//	public int quantityDropped(Random p_149745_1_) {
//		return 0;
//	}
//
//	@Override
//	public boolean isAir(IBlockAccess world, int x, int y, int z) {
//		return true;
//	}
//
//	@Override
//	public Item getItemDropped(int id, Random r, int fortune) {
//		return null;
//	}
//
//	@Override
//	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int meta, int fortune) {
//		return new ArrayList();
//	}
//
//	@Override
//	protected void dropBlockAsItem(World world, int x, int y, int z, ItemStack is) {
//
//	}
//
//	@Override
//	public boolean canSilkHarvest() {
//		return false;
//	}
//
//	@Override
//	public boolean canCollideCheck(int par1, boolean par2) {
//		return false;
//	}
//
//	@Override
//	public boolean isOpaqueCube() {
//		return false;
//	}
//
//	@Override
//	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
//		return null;
//	}
//
//	//
//	// @Override
//	// public boolean shouldSideBeRendered(IBlockAccess iba, int x, int y, int z, int side) {
//	// if (iba.getBlock(x, y, z) == this){
//	// return false;
//	// } else {
//	// return super.shouldSideBeRendered(iba, x, y, z, 1-side);
//	// }
//	// }
//
//	@Override
//	public boolean isReplaceable(IBlockAccess world, int x, int y, int z) {
//		return true;
//	}
//
//	// @Override
//	// public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity e) {
//	// if (!(e instanceof EntityItem || e instanceof EntityXPOrb)) {
//	// e.attackEntityFrom(DamageSource.onFire, 1);
//	// int meta = world.getBlockMetadata(x, y, z);
//	// if ((meta&4) != 0) {
//	// if (e instanceof EntityLivingBase)
//	// ((EntityLivingBase)e).addPotionEffect(new PotionEffect(Potion.poison.id, 200, 0));
//	// }
//	// }
//	// }
//
//}
