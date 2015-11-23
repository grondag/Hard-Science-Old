package grondag.adversity.library;


import grondag.adversity.Adversity;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;

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

	private static  Integer[][][][][][] SIMPLE_JOIN_LOOKUP = new Integer[2][2][2][2][2][2];
	private static  CornerJoin[][][][][][] CORNER_JOIN_LOOKUP = new CornerJoin[2][2][2][2][2][2];

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

		int detailID = SIMPLE_JOIN_LOOKUP[0][thisBlock.down && !mates.down?1:0]  					// UP DOWN
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

		CornerJoin join = CORNER_JOIN_LOOKUP[mates.up?1:0][mates.down?1:0]
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



	protected static class CornerJoin{

		private final int ID;
		private BlockPos[] offsets;
		public final boolean hasTests;

		// tests identifiers should be provided in LSB-first order
		// e.g. the first test adds 1 to ID if true, second adds 2, etc.
		public CornerJoin(int ID, String... corners){
			this.ID = ID;
			hasTests = corners.length > 0;

			offsets = new BlockPos[corners.length];
			for(int i = 0; i < corners.length; i++){

				if(corners[i] == "NE") 
					offsets[i] = new BlockPos(0,0,0).north().east();					
				else if(corners[i] == "NW") 
					offsets[i] = new BlockPos(0,0,0).north().west();					
				else if(corners[i] == "SE") 
					offsets[i] = new BlockPos(0,0,0).south().east();					
				else if(corners[i] == "SW") 
					offsets[i] = new BlockPos(0,0,0).south().west();					
				else if(corners[i] == "UE") 
					offsets[i] = new BlockPos(0,0,0).up().east();					
				else if(corners[i] == "UW") 
					offsets[i] = new BlockPos(0,0,0).up().west();					
				else if(corners[i] == "UN") 
					offsets[i] = new BlockPos(0,0,0).up().north();					
				else if(corners[i] == "US") 
					offsets[i] = new BlockPos(0,0,0).up().south();					
				else if(corners[i] == "DE") 
					offsets[i] = new BlockPos(0,0,0).down().east();					
				else if(corners[i] == "DW") 
					offsets[i] = new BlockPos(0,0,0).down().west();					
				else if(corners[i] == "DN") 
					offsets[i] = new BlockPos(0,0,0).down().north();					
				else if (corners[i] == "DS") 
					offsets[i] = new BlockPos(0,0,0).down().south();
				else {
					offsets[i] = new BlockPos(0,0,0);
					Adversity.log.warn("Unrecognized offset ID string when setting up corner lookups. This should never happen.");
				}
			}
		}
		public int getOffsetID(IBlockTest test, IBlockAccess worldIn, BlockPos pos){

			int result = ID;

			if(hasTests){
				for(int i = 0; i < offsets.length ; i++){
					// corner block not found means we need to show corner texture
					if(!test.testBlock(worldIn.getBlockState(pos.add(offsets[i])))){
						result += (1 << i);
					}
				}
			} 

			return result;			
		}		
	}

	protected static void setupLookupArrays(){


		// U D E W N S
		// 1 means has adjacent block of same style

		SIMPLE_JOIN_LOOKUP[0][0][0][0][0][0]=0;
		SIMPLE_JOIN_LOOKUP[1][0][0][0][0][0]=1;
		SIMPLE_JOIN_LOOKUP[0][1][0][0][0][0]=2;
		SIMPLE_JOIN_LOOKUP[0][0][1][0][0][0]=3;
		SIMPLE_JOIN_LOOKUP[0][0][0][1][0][0]=4;
		SIMPLE_JOIN_LOOKUP[0][0][0][0][1][0]=5;
		SIMPLE_JOIN_LOOKUP[0][0][0][0][0][1]=6;
		SIMPLE_JOIN_LOOKUP[1][1][0][0][0][0]=7;
		SIMPLE_JOIN_LOOKUP[0][0][1][1][0][0]=8;
		SIMPLE_JOIN_LOOKUP[0][0][0][0][1][1]=9;
		SIMPLE_JOIN_LOOKUP[1][0][1][0][0][0]=10;
		SIMPLE_JOIN_LOOKUP[1][0][0][1][0][0]=11;
		SIMPLE_JOIN_LOOKUP[1][0][0][0][1][0]=12;
		SIMPLE_JOIN_LOOKUP[1][0][0][0][0][1]=13;
		SIMPLE_JOIN_LOOKUP[0][1][1][0][0][0]=14;
		SIMPLE_JOIN_LOOKUP[0][1][0][1][0][0]=15;
		SIMPLE_JOIN_LOOKUP[0][1][0][0][1][0]=16;
		SIMPLE_JOIN_LOOKUP[0][1][0][0][0][1]=17;
		SIMPLE_JOIN_LOOKUP[0][0][1][0][1][0]=18;
		SIMPLE_JOIN_LOOKUP[0][0][1][0][0][1]=19;
		SIMPLE_JOIN_LOOKUP[0][0][0][1][1][0]=20;
		SIMPLE_JOIN_LOOKUP[0][0][0][1][0][1]=21;
		SIMPLE_JOIN_LOOKUP[1][0][1][1][0][0]=22;
		SIMPLE_JOIN_LOOKUP[1][0][0][0][1][1]=23;
		SIMPLE_JOIN_LOOKUP[0][1][1][1][0][0]=24;
		SIMPLE_JOIN_LOOKUP[0][1][0][0][1][1]=25;
		SIMPLE_JOIN_LOOKUP[0][0][1][1][1][0]=26;
		SIMPLE_JOIN_LOOKUP[1][1][0][0][1][0]=27;
		SIMPLE_JOIN_LOOKUP[0][0][1][1][0][1]=28;
		SIMPLE_JOIN_LOOKUP[1][1][0][0][0][1]=29;
		SIMPLE_JOIN_LOOKUP[1][1][1][0][0][0]=30;
		SIMPLE_JOIN_LOOKUP[0][0][1][0][1][1]=31;
		SIMPLE_JOIN_LOOKUP[1][1][0][1][0][0]=32;
		SIMPLE_JOIN_LOOKUP[0][0][0][1][1][1]=33;
		SIMPLE_JOIN_LOOKUP[1][0][1][0][1][0]=34;
		SIMPLE_JOIN_LOOKUP[1][0][1][0][0][1]=35;
		SIMPLE_JOIN_LOOKUP[1][0][0][1][1][0]=36;
		SIMPLE_JOIN_LOOKUP[1][0][0][1][0][1]=37;
		SIMPLE_JOIN_LOOKUP[0][1][1][0][1][0]=38;
		SIMPLE_JOIN_LOOKUP[0][1][1][0][0][1]=39;
		SIMPLE_JOIN_LOOKUP[0][1][0][1][1][0]=40;
		SIMPLE_JOIN_LOOKUP[0][1][0][1][0][1]=41;
		SIMPLE_JOIN_LOOKUP[0][0][1][1][1][1]=42;
		SIMPLE_JOIN_LOOKUP[1][1][1][1][0][0]=43;
		SIMPLE_JOIN_LOOKUP[1][1][0][0][1][1]=44;
		SIMPLE_JOIN_LOOKUP[1][0][0][1][1][1]=45;
		SIMPLE_JOIN_LOOKUP[1][0][1][0][1][1]=46;
		SIMPLE_JOIN_LOOKUP[1][0][1][1][0][1]=47;
		SIMPLE_JOIN_LOOKUP[1][0][1][1][1][0]=48;
		SIMPLE_JOIN_LOOKUP[0][1][0][1][1][1]=49;
		SIMPLE_JOIN_LOOKUP[0][1][1][0][1][1]=50;
		SIMPLE_JOIN_LOOKUP[0][1][1][1][0][1]=51;
		SIMPLE_JOIN_LOOKUP[0][1][1][1][1][0]=52;
		SIMPLE_JOIN_LOOKUP[1][1][1][0][1][0]=53;
		SIMPLE_JOIN_LOOKUP[1][1][1][0][0][1]=54;
		SIMPLE_JOIN_LOOKUP[1][1][0][1][1][0]=55;
		SIMPLE_JOIN_LOOKUP[1][1][0][1][0][1]=56;
		SIMPLE_JOIN_LOOKUP[0][1][1][1][1][1]=57;
		SIMPLE_JOIN_LOOKUP[1][0][1][1][1][1]=58;
		SIMPLE_JOIN_LOOKUP[1][1][0][1][1][1]=59;
		SIMPLE_JOIN_LOOKUP[1][1][1][0][1][1]=60;
		SIMPLE_JOIN_LOOKUP[1][1][1][1][0][1]=61;
		SIMPLE_JOIN_LOOKUP[1][1][1][1][1][0]=62;
		SIMPLE_JOIN_LOOKUP[1][1][1][1][1][1]=63;

		CORNER_JOIN_LOOKUP[1][1][1][1][1][1]=new CornerJoin( 0 );
		CORNER_JOIN_LOOKUP[1][1][1][1][1][0]=new CornerJoin( 162, "UE", "UW", "DE", "DW" );
		CORNER_JOIN_LOOKUP[1][1][1][1][0][1]=new CornerJoin( 162, "UE", "UW", "DE", "DW" );
		CORNER_JOIN_LOOKUP[1][1][1][1][0][0]=new CornerJoin( 162, "UE", "UW", "DE", "DW" );
		CORNER_JOIN_LOOKUP[1][1][1][0][1][1]=new CornerJoin( 178, "UN", "US", "DN", "DS" );
		CORNER_JOIN_LOOKUP[1][1][1][0][1][0]=new CornerJoin( 322, "UN", "UE", "DN", "DE" );
		CORNER_JOIN_LOOKUP[1][1][1][0][0][1]=new CornerJoin( 338, "UE", "US", "DE", "DS" );
		CORNER_JOIN_LOOKUP[1][1][1][0][0][0]=new CornerJoin( 66, "UE", "DE" );
		CORNER_JOIN_LOOKUP[1][1][0][1][1][1]=new CornerJoin( 178, "UN", "US", "DN", "DS" );
		CORNER_JOIN_LOOKUP[1][1][0][1][1][0]=new CornerJoin( 354, "UN", "UW", "DN", "DW" );
		CORNER_JOIN_LOOKUP[1][1][0][1][0][1]=new CornerJoin( 370, "US", "UW", "DS", "DW" );
		CORNER_JOIN_LOOKUP[1][1][0][1][0][0]=new CornerJoin( 74, "UW", "DW" );
		CORNER_JOIN_LOOKUP[1][1][0][0][1][1]=new CornerJoin( 178, "UN", "US", "DN", "DS" );
		CORNER_JOIN_LOOKUP[1][1][0][0][1][0]=new CornerJoin( 54, "UN", "DN" );
		CORNER_JOIN_LOOKUP[1][1][0][0][0][1]=new CornerJoin( 62, "US", "DS" );
		CORNER_JOIN_LOOKUP[1][1][0][0][0][0]=new CornerJoin( 7 );
		CORNER_JOIN_LOOKUP[1][0][1][1][1][1]=new CornerJoin( 146, "NE", "SE", "SW", "NW" );
		CORNER_JOIN_LOOKUP[1][0][1][1][1][0]=new CornerJoin( 242, "UE", "UW", "NE", "NW" );
		CORNER_JOIN_LOOKUP[1][0][1][1][0][1]=new CornerJoin( 226, "UE", "UW", "SE", "SW" );
		CORNER_JOIN_LOOKUP[1][0][1][1][0][0]=new CornerJoin( 34, "UE", "UW" );
		CORNER_JOIN_LOOKUP[1][0][1][0][1][1]=new CornerJoin( 210, "UN", "US", "NE", "SE" );
		CORNER_JOIN_LOOKUP[1][0][1][0][1][0]=new CornerJoin( 82, "UN", "UE", "NE" );
		CORNER_JOIN_LOOKUP[1][0][1][0][0][1]=new CornerJoin( 90, "UE", "US", "SE" );
		CORNER_JOIN_LOOKUP[1][0][1][0][0][0]=new CornerJoin( 10, "UE" );
		CORNER_JOIN_LOOKUP[1][0][0][1][1][1]=new CornerJoin( 194, "UN", "US", "SW", "NW" );
		CORNER_JOIN_LOOKUP[1][0][0][1][1][0]=new CornerJoin( 98, "UN", "UW", "NW" );
		CORNER_JOIN_LOOKUP[1][0][0][1][0][1]=new CornerJoin( 106, "US", "UW", "SW" );
		CORNER_JOIN_LOOKUP[1][0][0][1][0][0]=new CornerJoin( 12, "UW" );
		CORNER_JOIN_LOOKUP[1][0][0][0][1][1]=new CornerJoin( 38, "UN", "US" );
		CORNER_JOIN_LOOKUP[1][0][0][0][1][0]=new CornerJoin( 14, "UN" );
		CORNER_JOIN_LOOKUP[1][0][0][0][0][1]=new CornerJoin( 16, "US" );
		CORNER_JOIN_LOOKUP[1][0][0][0][0][0]=new CornerJoin( 1 );
		CORNER_JOIN_LOOKUP[0][1][1][1][1][1]=new CornerJoin( 146, "NE", "SE", "SW", "NW" );
		CORNER_JOIN_LOOKUP[0][1][1][1][1][0]=new CornerJoin( 306, "DE", "DW", "NE", "NW" );
		CORNER_JOIN_LOOKUP[0][1][1][1][0][1]=new CornerJoin( 290, "DE", "DW", "SE", "SW" );
		CORNER_JOIN_LOOKUP[0][1][1][1][0][0]=new CornerJoin( 42, "DE", "DW" );
		CORNER_JOIN_LOOKUP[0][1][1][0][1][1]=new CornerJoin( 274, "DN", "DS", "NE", "SE" );
		CORNER_JOIN_LOOKUP[0][1][1][0][1][0]=new CornerJoin( 114, "DN", "DE", "NE" );
		CORNER_JOIN_LOOKUP[0][1][1][0][0][1]=new CornerJoin( 122, "DE", "DS", "SE" );
		CORNER_JOIN_LOOKUP[0][1][1][0][0][0]=new CornerJoin( 18, "DE" );
		CORNER_JOIN_LOOKUP[0][1][0][1][1][1]=new CornerJoin( 258, "DN", "DS", "SW", "NW" );
		CORNER_JOIN_LOOKUP[0][1][0][1][1][0]=new CornerJoin( 130, "DN", "DW", "NW" );
		CORNER_JOIN_LOOKUP[0][1][0][1][0][1]=new CornerJoin( 138, "DS", "DW", "SW" );
		CORNER_JOIN_LOOKUP[0][1][0][1][0][0]=new CornerJoin( 20, "DW" );
		CORNER_JOIN_LOOKUP[0][1][0][0][1][1]=new CornerJoin( 46, "DN", "DS" );
		CORNER_JOIN_LOOKUP[0][1][0][0][1][0]=new CornerJoin( 22, "DN" );
		CORNER_JOIN_LOOKUP[0][1][0][0][0][1]=new CornerJoin( 24, "DS" );
		CORNER_JOIN_LOOKUP[0][1][0][0][0][0]=new CornerJoin( 2 );
		CORNER_JOIN_LOOKUP[0][0][1][1][1][1]=new CornerJoin( 146, "NE", "SE", "SW", "NW" );
		CORNER_JOIN_LOOKUP[0][0][1][1][1][0]=new CornerJoin( 50, "NE", "NW" );
		CORNER_JOIN_LOOKUP[0][0][1][1][0][1]=new CornerJoin( 58, "SE", "SW" );
		CORNER_JOIN_LOOKUP[0][0][1][1][0][0]=new CornerJoin( 8 );
		CORNER_JOIN_LOOKUP[0][0][1][0][1][1]=new CornerJoin( 70, "NE", "SE" );
		CORNER_JOIN_LOOKUP[0][0][1][0][1][0]=new CornerJoin( 26, "NE" );
		CORNER_JOIN_LOOKUP[0][0][1][0][0][1]=new CornerJoin( 28, "SE" );
		CORNER_JOIN_LOOKUP[0][0][1][0][0][0]=new CornerJoin( 3 );
		CORNER_JOIN_LOOKUP[0][0][0][1][1][1]=new CornerJoin( 78, "SW", "NW" );
		CORNER_JOIN_LOOKUP[0][0][0][1][1][0]=new CornerJoin( 30, "NW" );
		CORNER_JOIN_LOOKUP[0][0][0][1][0][1]=new CornerJoin( 32, "SW" );
		CORNER_JOIN_LOOKUP[0][0][0][1][0][0]=new CornerJoin( 4 );
		CORNER_JOIN_LOOKUP[0][0][0][0][1][1]=new CornerJoin( 9 );
		CORNER_JOIN_LOOKUP[0][0][0][0][1][0]=new CornerJoin( 5 );
		CORNER_JOIN_LOOKUP[0][0][0][0][0][1]=new CornerJoin( 6 );
		CORNER_JOIN_LOOKUP[0][0][0][0][0][0]=new CornerJoin( 0 );

	}
}
