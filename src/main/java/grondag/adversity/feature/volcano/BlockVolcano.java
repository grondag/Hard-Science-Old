// TODO: Fix or remove

//package com.grondag.adversity.feature.volcano;
//
//import java.util.Random;
//
//import net.minecraft.block.Block;
//import net.minecraft.block.ITileEntityProvider;
//import net.minecraft.block.material.Material;
//import net.minecraft.client.renderer.texture.IIconRegister;
//import net.minecraft.tileentity.TileEntity;
//import net.minecraft.util.IIcon;
//import net.minecraft.world.IBlockAccess;
//import net.minecraft.world.World;
//
//import com.grondag.adversity.Adversity;
//
//public class BlockVolcano extends Block implements ITileEntityProvider {
//
//	private IIcon	icon;
//
//	public BlockVolcano() {
//		super(Material.rock);
//		this.setBlockName("blockVolcano");
//		this.setCreativeTab(Adversity.tabAdversity);
//		this.setHardness(2000.0F);
//		this.setResistance(2000.0F);
//		this.setStepSound(soundTypePiston);
//		this.setHarvestLevel("pickaxe", 5);
//	}
//
//	@Override
//	public boolean isBurning(IBlockAccess world, int x, int y, int z) {
//		return true;
//	}
//
//	@Override
//	public int getLightValue() {
//		return 8;
//	}
//
//	@Override
//	public TileEntity createNewTileEntity(World world, int i) {
//		return null;
//	}
//
//	@Override
//	public TileEntity createTileEntity(World world, int metadata) {
//		return new TileVolcano();
//	}
//
//	@Override
//	public void registerBlockIcons(IIconRegister iconRegister) {
//		this.icon = iconRegister.registerIcon(Adversity.MODID + ":volcano");
//	}
//
//	@Override
//	public int quantityDropped(Random random) {
//		return 0;
//	}
//
//	@Override
//	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
//		return this.icon;
//	}
//
//	@Override
//	public IIcon getIcon(int side, int meta) {
//		return this.icon;
//	}
//}