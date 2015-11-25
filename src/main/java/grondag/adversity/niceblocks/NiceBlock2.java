package grondag.adversity.niceblocks;


import grondag.adversity.Adversity;
import grondag.adversity.library.IBlockTest;
import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.ShapeValidatorCubic;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.niceblocks.client.NiceBlockData;
import grondag.adversity.niceblocks.client.NiceBlockData.CornerJoin;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.Properties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class NiceBlock2 extends Block {

	public static final PropertyEnum PROP_STYLE = PropertyEnum.create("style", EnumStyle.class);
	public static final IUnlistedProperty PROP_SCENARIO = Properties.toUnlisted(PropertyInteger.create("scenario", 0, 385));
	public static final IUnlistedProperty PROP_ALTERNATE = Properties.toUnlisted(PropertyInteger.create("alternate", 0, 15));


	static {
		setupLookupArrays();
	}

	public NiceBlock2(Material stuff) {
		super(stuff);
	}	

	@Override
	public int damageDropped(IBlockState state)
	{
		EnumStyle enumStyle = (EnumStyle)state.getValue(PROP_STYLE);
		return enumStyle.getMetadata();
	}

	@Override
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing blockFaceClickedOn, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
	{
		EnumStyle style = EnumStyle.byMetadata(meta);
		IBlockState bs = super.onBlockPlaced(worldIn, pos, blockFaceClickedOn, hitX, hitY, hitZ, meta, placer);

		if(style.isBigBrick()){

			return bs.withProperty(PROP_STYLE, findBestStyleForPlacedBrick( worldIn, pos, EnumStyle.bigBrickStyles));

		}	else if(style.isBigBlock()){

			return bs.withProperty(PROP_STYLE, findBestStyleForPlacedBrick( worldIn, pos, EnumStyle.bigBlockStyles ));

		}
		return bs;

	}

	private EnumStyle findBestStyleForPlacedBrick(World worldIn, BlockPos pos, EnumStyle... styles){

		ShapeValidatorCubic shape = new ShapeValidatorCubic(3, 3, 3);

		NeighborBlocks centerBlocks = new NeighborBlocks(worldIn, pos);
		NeighborTestResults testStyles = centerBlocks.getNeighborTestResults(new TestForStyle(this, styles));

		EnumStyle candidateStyle;

		if(testStyles.east){
			candidateStyle = (EnumStyle) centerBlocks.east.getValue(PROP_STYLE);
			if(shape.isValidShape(worldIn, pos, new TestForStyle(this, candidateStyle), true)){
				return candidateStyle;
			};
		} 
		if (testStyles.west){
			candidateStyle = (EnumStyle) centerBlocks.west.getValue(PROP_STYLE);
			if(shape.isValidShape(worldIn, pos, new TestForStyle(this, candidateStyle), true)){
				return candidateStyle;		  
			}
		}	
		if (testStyles.north){
			candidateStyle = (EnumStyle) centerBlocks.north.getValue(PROP_STYLE);
			if(shape.isValidShape(worldIn, pos, new TestForStyle(this, candidateStyle), true)){
				return candidateStyle;		  
			}
		} 
		if (testStyles.south){
			candidateStyle = (EnumStyle) centerBlocks.south.getValue(PROP_STYLE);
			if(shape.isValidShape(worldIn, pos, new TestForStyle(this, candidateStyle), true)){
				return candidateStyle;		  
			}
		} 
		if (testStyles.up){
			candidateStyle = (EnumStyle) centerBlocks.up.getValue(PROP_STYLE);
			if(shape.isValidShape(worldIn, pos, new TestForStyle(this, candidateStyle), true)){
				return candidateStyle;		  
			}
		}  
		if (testStyles.down){
			candidateStyle = (EnumStyle) centerBlocks.down.getValue(PROP_STYLE);
			if(shape.isValidShape(worldIn, pos, new TestForStyle(this, candidateStyle), true)){
				return candidateStyle;		  
			}
		}


		// if no available mates, try to choose a style that will not connect to what is surrounding
		NeighborTestResults tests[] = new NeighborTestResults[styles.length];
		boolean hasStyle[] = new boolean[styles.length];

		for( int n = 0 ; n < styles.length; n++){
			tests[n] = centerBlocks.getNeighborTestResults(new TestForStyle(this, styles[n]));
			hasStyle[n] = tests[n].north || tests[n].south || tests[n].east || tests[n].west || tests[n].up || tests[n].down;
		}

		for( int n = 0 ; n < styles.length - 1; n++){
			if(hasStyle[n] && !hasStyle[n+1]){
				return styles[n+1];
			}
		}

		return styles[0];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item itemIn, CreativeTabs tab, List list)
	{
		list.add(new ItemStack(itemIn, 1, EnumStyle.ROUGH.getMetadata()));
		list.add(new ItemStack(itemIn, 1, EnumStyle.SMOOTH.getMetadata()));
		list.add(new ItemStack(itemIn, 1, EnumStyle.BRICK_BIG_A.getMetadata()));
		list.add(new ItemStack(itemIn, 1, EnumStyle.BLOCK_BIG_A.getMetadata()));
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		EnumStyle style = EnumStyle.byMetadata(meta);
		return this.getDefaultState().withProperty(PROP_STYLE, style);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		EnumStyle style = (EnumStyle)state.getValue(PROP_STYLE);
		return style.getMetadata();
	}

	@Override
	public EnumWorldBlockLayer getBlockLayer() {
		return EnumWorldBlockLayer.SOLID;
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
	{
		return state; 

	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {

		if (state instanceof IExtendedBlockState) {  // avoid crash in case of mismatch
			IExtendedBlockState retval = (IExtendedBlockState)state;

			EnumStyle style = (EnumStyle) state.getValue(PROP_STYLE);

			switch(style){

			case ROUGH:
				return retval;//.withProperty(PROP_TEXTURE, 0);

			case SMOOTH:
				return retval;//.withProperty(PROP_TEXTURE, 0);

			case BRICK_BIG_A:
			case BRICK_BIG_B:
			case BRICK_BIG_C:
			case BRICK_BIG_D:
			case BRICK_BIG_E:

				return GetBigBrickState(style, retval, worldIn, pos);

			case BLOCK_BIG_A:
			case BLOCK_BIG_B:
			case BLOCK_BIG_C:
			case BLOCK_BIG_D:
			case BLOCK_BIG_E:

				return GetBigBlockState(style, retval, worldIn, pos);
			default:

			}
		}
		return state;
	}

	protected IExtendedBlockState GetBigBrickState(EnumStyle style, IExtendedBlockState state, IBlockAccess worldIn, BlockPos pos){

		NeighborBlocks neighbors = new NeighborBlocks(worldIn, pos);
		NeighborTestResults mates = neighbors.getNeighborTestResults(new TestForStyle(this, style));
		NeighborTestResults bigBricks = neighbors.getNeighborTestResults(new TestForStyle(this, EnumStyle.bigBrickStyles));
		NeighborTestResults thisBlock = neighbors.getNeighborTestResults(new TestForThisBlock(this));

		int detailID = NiceBlockData.SIMPLE_JOIN_LOOKUP[0][thisBlock.down && !mates.down?1:0]  					// UP DOWN
				[(thisBlock.east && !bigBricks.east) || (bigBricks.east && !mates.east)?1:0] 		// EAST
				[thisBlock.west && !bigBricks.west?1:0]  											// WEST
				[(thisBlock.north && !bigBricks.north) || (bigBricks.north && !mates.north)?1:0]	// NORTH								// NORTH
				[thisBlock.south && !bigBricks.south?1:0]; 											// SOUTH

		return state.withProperty(PROP_SCENARIO, detailID);			  

	}

	protected IExtendedBlockState GetBigBlockState(EnumStyle style, IExtendedBlockState state, IBlockAccess worldIn, BlockPos pos){

		TestForStyle test = new TestForStyle(this, style);

		NeighborBlocks neighbors = new NeighborBlocks(worldIn, pos);
		NeighborTestResults mates = neighbors.getNeighborTestResults(test);

		CornerJoin join = NiceBlockData.CORNER_JOIN_LOOKUP[mates.up?1:0][mates.down?1:0]
				[mates.east?1:0][mates.west?1:0] 
						[mates.north?1:0][mates.south?1:0];
		
		int alt = NiceBlockData.textureMix12[pos.getX() & 15][pos.getY() & 15][pos.getZ() & 15];
		
		if(join.hasTests){
			return state.withProperty(PROP_SCENARIO, join.getOffsetID(test, worldIn, pos)).withProperty(PROP_ALTERNATE, alt);
		} else {
			return state.withProperty(PROP_SCENARIO, join.ID).withProperty(PROP_ALTERNATE, alt);
		}

	}


	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		EnumStyle style = (EnumStyle) state.getValue(PROP_STYLE);
		IBlockState returnState = state;
		if(style.isBigBrick()){
			returnState = this.getDefaultState().withProperty(PROP_STYLE, EnumStyle.BRICK_BIG_A);
		} else if (style.isBigBlock()){
			returnState = this.getDefaultState().withProperty(PROP_STYLE, EnumStyle.BLOCK_BIG_A);
		}

		return super.getItemDropped(returnState, rand, fortune);

	}

	@Override
	protected BlockState createBlockState()
	{
		return new ExtendedBlockState(this, new IProperty[] {PROP_STYLE}, new IUnlistedProperty[] {PROP_SCENARIO, PROP_ALTERNATE});
	}

	private class TestForStyle implements IBlockTest{

		private final Block block;
		private EnumStyle styles[] = new EnumStyle[1];

		public TestForStyle(Block block, EnumStyle style){
			this.block = block;
			this.styles[0] = style;
		}

		public TestForStyle(Block block, EnumStyle... styles){
			this.block = block;
			this.styles = styles;
		}

		@Override
		public boolean testBlock(IBlockState ibs) {
			boolean foundIt = false;
			for (EnumStyle style: styles){
				if((ibs.getBlock() == block && ibs.getValue(PROP_STYLE) == style)) foundIt = true;
			}
			return foundIt;
		}	  
	}

	protected class TestForThisBlock implements IBlockTest{

		private final Block block;

		public TestForThisBlock(Block block){
			this.block = block;
		}

		@Override
		public boolean testBlock(IBlockState ibs) {
			return ibs.getBlock() == block;
		}	  
	}


	public static enum EnumStyle implements IStringSerializable
	{
		ROUGH(0, "rough"),
		SMOOTH(1, "smooth"),
		BRICK_BIG_A(2, "brick_big_a"),
		BRICK_BIG_B(3, "brick_big_b"),
		BRICK_BIG_C(4, "brick_big_c"),
		BRICK_BIG_D(5, "brick_big_d"),
		BRICK_BIG_E(6, "brick_big_e"),
		BLOCK_BIG_A(7, "block_big_a"),
		BLOCK_BIG_B(8, "block_big_b"),
		BLOCK_BIG_C(9, "block_big_c"),
		BLOCK_BIG_D(10, "block_big_d"),
		BLOCK_BIG_E(11, "block_big_e");
		//	    PLATE(5, "plate"),	    
		//	    BRICK1(6, "brick1"),
		//	    BRICK2(7, "brick2"),
		//	    TILE1(8, "tile1"),
		//	    TILE2(9, "tile2"),
		//	    TILE3(10, "tile3"),
		//	    DESIGN1(11, "design1"),
		//	    DESIGN2(12, "design2"),
		//	    DESIGN3(13, "design3"),
		//	    CAP(14, "cap"),
		//	    BASE(15, "base");

		private final int meta;
		private final String name;
		private final static EnumStyle[] bigBrickStyles = new EnumStyle[5];
		private final static EnumStyle[] bigBlockStyles = new EnumStyle[5];

		private static final EnumStyle[] META_LOOKUP = new EnumStyle[values().length];

		static
		{
			for (EnumStyle style : values()) {
				META_LOOKUP[style.getMetadata()] = style;
			}

			bigBrickStyles[0] = EnumStyle.BRICK_BIG_A;
			bigBrickStyles[1] = EnumStyle.BRICK_BIG_B;
			bigBrickStyles[2] = EnumStyle.BRICK_BIG_C;
			bigBrickStyles[3] = EnumStyle.BRICK_BIG_D;
			bigBrickStyles[4] = EnumStyle.BRICK_BIG_E;

			bigBlockStyles[0] = EnumStyle.BLOCK_BIG_A;
			bigBlockStyles[1] = EnumStyle.BLOCK_BIG_B;
			bigBlockStyles[2] = EnumStyle.BLOCK_BIG_C;
			bigBlockStyles[3] = EnumStyle.BLOCK_BIG_D;
			bigBlockStyles[4] = EnumStyle.BLOCK_BIG_E;


		}

		public int getMetadata()
		{
			return this.meta;
		}

		@Override
		public String toString()
		{
			return this.name;
		}


		public boolean isBigBrick(){
			for(EnumStyle style: bigBrickStyles){
				if(this == style) return true;
			}
			return false;
		}

		public boolean isBigBlock(){
			for(EnumStyle style: bigBlockStyles){
				if(this == style) return true;
			}
			return false;
		}


		public static EnumStyle byMetadata(int meta)
		{
			if (meta < 0 || meta >= META_LOOKUP.length)
			{
				meta = 0;
			}

			return META_LOOKUP[meta];
		}

		@Override
		public String getName()
		{
			return this.name;
		}


		private EnumStyle(int i_meta, String i_name)
		{
			this.meta = i_meta;
			this.name = i_name;

		}

	}





	protected static void setupLookupArrays(){


	}
}
