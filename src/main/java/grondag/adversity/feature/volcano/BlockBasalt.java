package grondag.adversity.feature.volcano;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Cartesian;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.MapPopulator;
import net.minecraft.util.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.Properties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import grondag.adversity.Adversity;
import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.NeighborBlocks.INeighborTest;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class BlockBasalt extends Block {
	
	public static final PropertyEnum PROP_STYLE = PropertyEnum.create("style", EnumStyle.class);
	public static final PropertyInteger	 PROP_DETAILS = PropertyInteger.create("details", 0, 63);

	private static  Integer[][][][][][] COL_X_LOOKUP = new Integer[2][2][2][2][2][2];
	private static  Integer[][][][][][] COL_Y_LOOKUP = new Integer[2][2][2][2][2][2];
	private static  Integer[][][][][][] COL_Z_LOOKUP = new Integer[2][2][2][2][2][2];
	private static  Integer[][][][][][] JOIN_LOOKUP = new Integer[2][2][2][2][2][2];
	
	static {
		setupLookupArrays();
	}
	
	public BlockBasalt() {
		super(Material.rock);
		this.setHarvestLevel("pickaxe", 2);
		this.setStepSound(soundTypeStone);
		this.setHardness(2);
		this.setResistance(10);
	}	
	
	  @Override
	public EnumWorldBlockLayer getBlockLayer() {
		return EnumWorldBlockLayer.CUTOUT_MIPPED;
	}

	@Override
	  public int damageDropped(IBlockState state)
	  {
	    EnumStyle enumStyle = (EnumStyle)state.getValue(PROP_STYLE);
	    return enumStyle.getMetadata();
	  }

	  // for columns, set the appropriate facing direction based on which way the player is looking
	  @Override
	  public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing blockFaceClickedOn, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
	  {
	    EnumStyle style = EnumStyle.byMetadata(meta);
	    IBlockState bs = super.onBlockPlaced(worldIn, pos, blockFaceClickedOn, hitX, hitY, hitZ, meta, placer);
	    
	    if(style == EnumStyle.COLUMN_Y || style == EnumStyle.COLUMN_X || style == EnumStyle.COLUMN_Z){
		    
		    switch(blockFaceClickedOn.getAxis()){
		    case X:
		    	return bs.withProperty(PROP_STYLE, EnumStyle.COLUMN_X);

		    case Y:
		    	return bs.withProperty(PROP_STYLE, EnumStyle.COLUMN_Y);

		    case Z:
		    	return bs.withProperty(PROP_STYLE, EnumStyle.COLUMN_Z);
		    }

		    
	    } else if(style == EnumStyle.BRICK_BIG_A || style == EnumStyle.BRICK_BIG_B){
	    	
	    	return bs.withProperty(PROP_STYLE, findBestStyleForPlacedBrick( worldIn, pos));

	    }	
	    return bs;
	    
	  }
	  
	  private EnumStyle findBestStyleForPlacedBrick(World worldIn, BlockPos pos){
		  
		  // ROUGH is default because it will not match any of the brick styles
		  EnumStyle styleNorth = EnumStyle.ROUGH;
		  EnumStyle styleSouth = EnumStyle.ROUGH;
		  EnumStyle styleEast = EnumStyle.ROUGH;
		  EnumStyle styleWest = EnumStyle.ROUGH;
		  
		  NeighborBlocks centerBlocks = new NeighborBlocks(worldIn, pos);
		  NeighborTestResults testBigBricks = centerBlocks.getNeighborTestResults(new TestForBigBrick(this));

		  if(testBigBricks.north) styleNorth = (EnumStyle) centerBlocks.north.getValue(PROP_STYLE);
		  if(testBigBricks.south) styleSouth = (EnumStyle) centerBlocks.south.getValue(PROP_STYLE);
		  if(testBigBricks.east) styleEast = (EnumStyle) centerBlocks.east.getValue(PROP_STYLE);
		  if(testBigBricks.west) styleWest = (EnumStyle) centerBlocks.west.getValue(PROP_STYLE);
		  
		  if(testBigBricks.north ){
			  if(!doesBrickHaveMatches(centerBlocks.north, worldIn, pos.north())
				  && styleNorth != styleEast && styleNorth != styleSouth && styleNorth != styleWest){
				  return styleNorth;
			  }
		  }
		  if(testBigBricks.south ){
			  if(!doesBrickHaveMatches(centerBlocks.south, worldIn, pos.south())
				  && styleSouth != styleEast && styleSouth != styleNorth && styleSouth != styleWest){
				  return styleSouth;
			  }
		  }
		  if(testBigBricks.east ){
			  if(!doesBrickHaveMatches(centerBlocks.east, worldIn, pos.east())
				  && styleEast != styleNorth && styleEast != styleSouth && styleEast != styleWest){
				  return styleEast;
			  }
		  }
		  if(testBigBricks.west ){
			  if(!doesBrickHaveMatches(centerBlocks.west, worldIn, pos.west())
				  && styleWest != styleEast && styleWest != styleSouth && styleWest != styleNorth){
				  return styleWest;
			  }
		  }
		  
		  // if no available mates, try to choose a style that will not connect to what is surrounding
		  NeighborTestResults testA = centerBlocks.getNeighborTestResults(new TestForStyle(this, EnumStyle.BRICK_BIG_A));
		  NeighborTestResults testB = centerBlocks.getNeighborTestResults(new TestForStyle(this, EnumStyle.BRICK_BIG_B));
		  NeighborTestResults testC = centerBlocks.getNeighborTestResults(new TestForStyle(this, EnumStyle.BRICK_BIG_C));
		  
		  boolean hasA = testA.north || testA.south || testA.east || testA.west;
		  boolean hasB = testB.north || testB.south || testB.east || testB.west;
		  boolean hasC = testC.north || testC.south || testC.east || testC.west;
		  
		  if(hasA && !hasB){
			  return EnumStyle.BRICK_BIG_B;
		  } else if (hasB && !hasC) {
			  return EnumStyle.BRICK_BIG_C;
		  } else {
			  return EnumStyle.BRICK_BIG_A;
		  }
	  }
	  
	 // True if block at this location is already matched with at least one brick of the same style
	 private boolean doesBrickHaveMatches(IBlockState ibs, World worldIn, BlockPos pos){

		 EnumStyle style = (EnumStyle) ibs.getValue(PROP_STYLE);
		 NeighborTestResults candidates = new NeighborBlocks(worldIn, pos).getNeighborTestResults(new TestForStyle(this, style));
		 return candidates.east || candidates.west || candidates.north || candidates.south;		  
	 }
	  
	  @Override
	  @SideOnly(Side.CLIENT)
	  public void getSubBlocks(Item itemIn, CreativeTabs tab, List list)
	  {
	    EnumStyle[] allStyles = EnumStyle.values();
	    for (EnumStyle style : allStyles) {
	      list.add(new ItemStack(itemIn, 1, style.getMetadata()));
	    }
	  }

	  @Override
	  public IBlockState getStateFromMeta(int meta)
	  {
	    EnumStyle style = EnumStyle.byMetadata(meta);
		return this.getDefaultState().withProperty(PROP_STYLE, style).withProperty(PROP_DETAILS, 0);
	  }

	  @Override
	  public int getMetaFromState(IBlockState state)
	  {
	    EnumStyle style = (EnumStyle)state.getValue(PROP_STYLE);
	    return style.getMetadata();
	  }

	  @Override
	  public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
	  {
		  EnumStyle style = (EnumStyle) state.getValue(PROP_STYLE);

		  switch(style){
		  
		  case ROUGH:
			  return state.withProperty(PROP_DETAILS, 0);

		  case SMOOTH:
			  return state.withProperty(PROP_DETAILS, 0);
			  
		  case COLUMN_Y:
		  case COLUMN_X:			  
		  case COLUMN_Z:
			  // N.B.  Using state.withProperty here caused an NPE for center blocks.
			  // Was never able to track down why.Hence the use of getDefaultState.
			  // Was conspicuous that the detailID was the max value. 
			  // This was before I re-factored quite a bit, maybe it would work now.
			  return GetColumnState(style, state, worldIn, pos);

		  case BRICK_BIG_A:
		  case BRICK_BIG_B:
		  case BRICK_BIG_C:
			  
			  return GetBigBrickState(style, state, worldIn, pos);
			  
		  default:
			  
		  }

		  return state; 
    
		  // TODO: May need to handle some texture rotation here.
		  // See MathHelper.getPositionRandom(Vec3i pos)
		  // but note intended for client only.

	  }

	  private IBlockState GetColumnState(EnumStyle style, IBlockState state, IBlockAccess worldIn, BlockPos pos){


		  NeighborTestResults tests = new NeighborBlocks(worldIn, pos).getNeighborTestResults(new TestForStyle(this, style));
		  int detailID = 0;
		  
		  //TODO: need a cleaner way to do this
		  switch (style){
		  case COLUMN_X:
			  detailID = COL_X_LOOKUP[tests.up?1:0][tests.down?1:0][tests.east?1:0][tests.west?1:0][tests.north?1:0][tests.south?1:0];
			  break;
		  case COLUMN_Y:
			  detailID = COL_Y_LOOKUP[tests.up?1:0][tests.down?1:0][tests.east?1:0][tests.west?1:0][tests.north?1:0][tests.south?1:0];
			  break;
		  case COLUMN_Z:
			  detailID = COL_Z_LOOKUP[tests.up?1:0][tests.down?1:0][tests.east?1:0][tests.west?1:0][tests.north?1:0][tests.south?1:0];
			  break;
		  default:
			  // should never get here			  			  
		  }
		  
		  return this.getDefaultState().withProperty(PROP_STYLE, style).withProperty(PROP_DETAILS, detailID);			   
		  
	  }
	  
	  private IBlockState GetBigBrickState(EnumStyle style, IBlockState state, IBlockAccess worldIn, BlockPos pos){
		  
		  NeighborBlocks neighbors = new NeighborBlocks(worldIn, pos);
		  NeighborTestResults mates = neighbors.getNeighborTestResults(new TestForStyle(this, style));
		  NeighborTestResults bigBricks = neighbors.getNeighborTestResults(new TestForBigBrick(this));
		  NeighborTestResults thisBlock = neighbors.getNeighborTestResults(new TestForThisBlock(this));
		  
		  int detailID = JOIN_LOOKUP[0][thisBlock.down?1:0]  //UP DOWN
				  [(thisBlock.east && !bigBricks.east) || (bigBricks.east && !mates.east)?1:0] 	// EAST
				  [thisBlock.west && !bigBricks.west?1:0]  											// WEST
				  [(thisBlock.north && !bigBricks.north) || (bigBricks.north && !mates.north)?1:0]									// NORTH
				  [thisBlock.south && !bigBricks.south?1:0]; 								// SOUTH
		  
		  return this.getDefaultState().withProperty(PROP_STYLE, style).withProperty(PROP_DETAILS, detailID);			  
		  
	  }
	  
	  @Override
	  protected BlockState createBlockState()
	  {
	    return new BlockState(this, new IProperty[] {PROP_STYLE, PROP_DETAILS}); 
	  }

	  private class TestForStyle implements INeighborTest{

		private final Block block;
		private final EnumStyle style ;
		
		public TestForStyle(Block block, EnumStyle style){
			this.block = block;
			this.style = style;
		}
		  
		@Override
		public boolean TestNeighbor(IBlockState ibs) {
			return (ibs.getBlock() == block && ibs.getValue(PROP_STYLE) == style);
		}	  
	  }
	  
	  private class TestForBigBrick implements INeighborTest{

		private final Block block;
		
		public TestForBigBrick(Block block){
			this.block = block;
		}
		  
		@Override
		public boolean TestNeighbor(IBlockState ibs) {
			boolean result = false;
			if (ibs.getBlock() == block) {
				EnumStyle style = (EnumStyle) ibs.getValue(PROP_STYLE);
				result = style.isBigBrick();
			}
			return result;
		}	  
	  }

	  private class TestForThisBlock implements INeighborTest{

		private final Block block;
		
		public TestForThisBlock(Block block){
			this.block = block;
		}
		  
		@Override
		public boolean TestNeighbor(IBlockState ibs) {
			return ibs.getBlock() == block;
		}	  
	  }
	  
	  public static enum EnumStyle implements IStringSerializable
	  {
	    ROUGH(0, "rough", 0),
	    SMOOTH(1, "smooth", 0),
	    COLUMN_Y(2, "column_y", 63),
	    COLUMN_X(3, "column_x", 63),	    
	    COLUMN_Z(4, "column_z", 63),
	    BRICK_BIG_A(5, "brick_big_a", 63),
	    BRICK_BIG_B(6, "brick_big_b", 63),
	    BRICK_BIG_C(7, "brick_big_c", 63);
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
	    private final int maxDetailID;
	    
	    private static final EnumStyle[] META_LOOKUP = new EnumStyle[values().length];
	    
	    static
	    {
	      for (EnumStyle style : values()) {
	        META_LOOKUP[style.getMetadata()] = style;
	      }
	      

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
			switch(this){
	    	case BRICK_BIG_A:
			case BRICK_BIG_B:
			case BRICK_BIG_C:
				return true;
			default:
	    		return false;
	    	}
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
	    
		public int getMaxDetailID()
	    {
	      return this.maxDetailID;
	    }

	    private EnumStyle(int i_meta, String i_name, int i_maxDetailID)
	    {
	      this.meta = i_meta;
	      this.name = i_name;
	      this.maxDetailID = i_maxDetailID;
	    }

	  }
	  
	  private static void setupLookupArrays(){


		  // U D E W N S
		  // 1 means has adjacent block of same style
		
		  COL_Y_LOOKUP[1][1][0][1][1][1]=0;
		  COL_Y_LOOKUP[1][1][1][0][1][1]=1;
		  COL_Y_LOOKUP[1][1][1][1][0][1]=2;
		  COL_Y_LOOKUP[1][1][1][1][1][0]=3;
		  COL_Y_LOOKUP[1][1][0][1][0][1]=4;
		  COL_Y_LOOKUP[1][1][1][0][0][1]=5;
		  COL_Y_LOOKUP[1][1][0][1][1][0]=6;
		  COL_Y_LOOKUP[1][1][1][0][1][0]=7;
		  COL_Y_LOOKUP[1][1][0][0][1][1]=8;
		  COL_Y_LOOKUP[1][1][1][1][0][0]=9;
		  COL_Y_LOOKUP[1][1][0][0][0][1]=10;
		  COL_Y_LOOKUP[1][1][0][0][1][0]=11;
		  COL_Y_LOOKUP[1][1][0][1][0][0]=12;
		  COL_Y_LOOKUP[1][1][1][0][0][0]=13;
		  COL_Y_LOOKUP[1][1][0][0][0][0]=14;
		  COL_Y_LOOKUP[1][1][1][1][1][1]=15;
		  COL_Y_LOOKUP[0][1][0][1][1][1]=16;
		  COL_Y_LOOKUP[0][1][1][0][1][1]=17;
		  COL_Y_LOOKUP[0][1][1][1][0][1]=18;
		  COL_Y_LOOKUP[0][1][1][1][1][0]=19;
		  COL_Y_LOOKUP[0][1][0][1][0][1]=20;
		  COL_Y_LOOKUP[0][1][1][0][0][1]=21;
		  COL_Y_LOOKUP[0][1][0][1][1][0]=22;
		  COL_Y_LOOKUP[0][1][1][0][1][0]=23;
		  COL_Y_LOOKUP[0][1][0][0][1][1]=24;
		  COL_Y_LOOKUP[0][1][1][1][0][0]=25;
		  COL_Y_LOOKUP[0][1][0][0][0][1]=26;
		  COL_Y_LOOKUP[0][1][0][0][1][0]=27;
		  COL_Y_LOOKUP[0][1][0][1][0][0]=28;
		  COL_Y_LOOKUP[0][1][1][0][0][0]=29;
		  COL_Y_LOOKUP[0][1][0][0][0][0]=30;
		  COL_Y_LOOKUP[0][1][1][1][1][1]=31;
		  COL_Y_LOOKUP[1][0][0][1][1][1]=32;
		  COL_Y_LOOKUP[1][0][1][0][1][1]=33;
		  COL_Y_LOOKUP[1][0][1][1][0][1]=34;
		  COL_Y_LOOKUP[1][0][1][1][1][0]=35;
		  COL_Y_LOOKUP[1][0][0][1][0][1]=36;
		  COL_Y_LOOKUP[1][0][1][0][0][1]=37;
		  COL_Y_LOOKUP[1][0][0][1][1][0]=38;
		  COL_Y_LOOKUP[1][0][1][0][1][0]=39;
		  COL_Y_LOOKUP[1][0][0][0][1][1]=40;
		  COL_Y_LOOKUP[1][0][1][1][0][0]=41;
		  COL_Y_LOOKUP[1][0][0][0][0][1]=42;
		  COL_Y_LOOKUP[1][0][0][0][1][0]=43;
		  COL_Y_LOOKUP[1][0][0][1][0][0]=44;
		  COL_Y_LOOKUP[1][0][1][0][0][0]=45;
		  COL_Y_LOOKUP[1][0][0][0][0][0]=46;
		  COL_Y_LOOKUP[1][0][1][1][1][1]=47;
		  COL_Y_LOOKUP[0][0][0][1][1][1]=48;
		  COL_Y_LOOKUP[0][0][1][0][1][1]=49;
		  COL_Y_LOOKUP[0][0][1][1][0][1]=50;
		  COL_Y_LOOKUP[0][0][1][1][1][0]=51;
		  COL_Y_LOOKUP[0][0][0][1][0][1]=52;
		  COL_Y_LOOKUP[0][0][1][0][0][1]=53;
		  COL_Y_LOOKUP[0][0][0][1][1][0]=54;
		  COL_Y_LOOKUP[0][0][1][0][1][0]=55;
		  COL_Y_LOOKUP[0][0][0][0][1][1]=56;
		  COL_Y_LOOKUP[0][0][1][1][0][0]=57;
		  COL_Y_LOOKUP[0][0][0][0][0][1]=58;
		  COL_Y_LOOKUP[0][0][0][0][1][0]=59;
		  COL_Y_LOOKUP[0][0][0][1][0][0]=60;
		  COL_Y_LOOKUP[0][0][1][0][0][0]=61;
		  COL_Y_LOOKUP[0][0][0][0][0][0]=62;
		  COL_Y_LOOKUP[0][0][1][1][1][1]=63;
		  
		  COL_X_LOOKUP[0][1][1][1][1][1]=0;
		  COL_X_LOOKUP[1][0][1][1][1][1]=1;
		  COL_X_LOOKUP[1][1][1][1][0][1]=2;
		  COL_X_LOOKUP[1][1][1][1][1][0]=3;
		  COL_X_LOOKUP[0][1][1][1][0][1]=4;
		  COL_X_LOOKUP[1][0][1][1][0][1]=5;
		  COL_X_LOOKUP[0][1][1][1][1][0]=6;
		  COL_X_LOOKUP[1][0][1][1][1][0]=7;
		  COL_X_LOOKUP[0][0][1][1][1][1]=8;
		  COL_X_LOOKUP[1][1][1][1][0][0]=9;
		  COL_X_LOOKUP[0][0][1][1][0][1]=10;
		  COL_X_LOOKUP[0][0][1][1][1][0]=11;
		  COL_X_LOOKUP[0][1][1][1][0][0]=12;
		  COL_X_LOOKUP[1][0][1][1][0][0]=13;
		  COL_X_LOOKUP[0][0][1][1][0][0]=14;
		  COL_X_LOOKUP[1][1][1][1][1][1]=15;
		  COL_X_LOOKUP[0][1][1][0][1][1]=16;
		  COL_X_LOOKUP[1][0][1][0][1][1]=17;
		  COL_X_LOOKUP[1][1][1][0][0][1]=18;
		  COL_X_LOOKUP[1][1][1][0][1][0]=19;
		  COL_X_LOOKUP[0][1][1][0][0][1]=20;
		  COL_X_LOOKUP[1][0][1][0][0][1]=21;
		  COL_X_LOOKUP[0][1][1][0][1][0]=22;
		  COL_X_LOOKUP[1][0][1][0][1][0]=23;
		  COL_X_LOOKUP[0][0][1][0][1][1]=24;
		  COL_X_LOOKUP[1][1][1][0][0][0]=25;
		  COL_X_LOOKUP[0][0][1][0][0][1]=26;
		  COL_X_LOOKUP[0][0][1][0][1][0]=27;
		  COL_X_LOOKUP[0][1][1][0][0][0]=28;
		  COL_X_LOOKUP[1][0][1][0][0][0]=29;
		  COL_X_LOOKUP[0][0][1][0][0][0]=30;
		  COL_X_LOOKUP[1][1][1][0][1][1]=31;
		  COL_X_LOOKUP[0][1][0][1][1][1]=32;
		  COL_X_LOOKUP[1][0][0][1][1][1]=33;
		  COL_X_LOOKUP[1][1][0][1][0][1]=34;
		  COL_X_LOOKUP[1][1][0][1][1][0]=35;
		  COL_X_LOOKUP[0][1][0][1][0][1]=36;
		  COL_X_LOOKUP[1][0][0][1][0][1]=37;
		  COL_X_LOOKUP[0][1][0][1][1][0]=38;
		  COL_X_LOOKUP[1][0][0][1][1][0]=39;
		  COL_X_LOOKUP[0][0][0][1][1][1]=40;
		  COL_X_LOOKUP[1][1][0][1][0][0]=41;
		  COL_X_LOOKUP[0][0][0][1][0][1]=42;
		  COL_X_LOOKUP[0][0][0][1][1][0]=43;
		  COL_X_LOOKUP[0][1][0][1][0][0]=44;
		  COL_X_LOOKUP[1][0][0][1][0][0]=45;
		  COL_X_LOOKUP[0][0][0][1][0][0]=46;
		  COL_X_LOOKUP[1][1][0][1][1][1]=47;
		  COL_X_LOOKUP[0][1][0][0][1][1]=48;
		  COL_X_LOOKUP[1][0][0][0][1][1]=49;
		  COL_X_LOOKUP[1][1][0][0][0][1]=50;
		  COL_X_LOOKUP[1][1][0][0][1][0]=51;
		  COL_X_LOOKUP[0][1][0][0][0][1]=52;
		  COL_X_LOOKUP[1][0][0][0][0][1]=53;
		  COL_X_LOOKUP[0][1][0][0][1][0]=54;
		  COL_X_LOOKUP[1][0][0][0][1][0]=55;
		  COL_X_LOOKUP[0][0][0][0][1][1]=56;
		  COL_X_LOOKUP[1][1][0][0][0][0]=57;
		  COL_X_LOOKUP[0][0][0][0][0][1]=58;
		  COL_X_LOOKUP[0][0][0][0][1][0]=59;
		  COL_X_LOOKUP[0][1][0][0][0][0]=60;
		  COL_X_LOOKUP[1][0][0][0][0][0]=61;
		  COL_X_LOOKUP[0][0][0][0][0][0]=62;
		  COL_X_LOOKUP[1][1][0][0][1][1]=63;
		  
		  COL_Z_LOOKUP[1][1][1][0][1][1]=0;
		  COL_Z_LOOKUP[1][1][0][1][1][1]=1;
		  COL_Z_LOOKUP[1][0][1][1][1][1]=2;
		  COL_Z_LOOKUP[0][1][1][1][1][1]=3;
		  COL_Z_LOOKUP[1][0][1][0][1][1]=4;
		  COL_Z_LOOKUP[1][0][0][1][1][1]=5;
		  COL_Z_LOOKUP[0][1][1][0][1][1]=6;
		  COL_Z_LOOKUP[0][1][0][1][1][1]=7;
		  COL_Z_LOOKUP[1][1][0][0][1][1]=8;
		  COL_Z_LOOKUP[0][0][1][1][1][1]=9;
		  COL_Z_LOOKUP[1][0][0][0][1][1]=10;
		  COL_Z_LOOKUP[0][1][0][0][1][1]=11;
		  COL_Z_LOOKUP[0][0][1][0][1][1]=12;
		  COL_Z_LOOKUP[0][0][0][1][1][1]=13;
		  COL_Z_LOOKUP[0][0][0][0][1][1]=14;
		  COL_Z_LOOKUP[1][1][1][1][1][1]=15;
		  COL_Z_LOOKUP[1][1][1][0][1][0]=16;
		  COL_Z_LOOKUP[1][1][0][1][1][0]=17;
		  COL_Z_LOOKUP[1][0][1][1][1][0]=18;
		  COL_Z_LOOKUP[0][1][1][1][1][0]=19;
		  COL_Z_LOOKUP[1][0][1][0][1][0]=20;
		  COL_Z_LOOKUP[1][0][0][1][1][0]=21;
		  COL_Z_LOOKUP[0][1][1][0][1][0]=22;
		  COL_Z_LOOKUP[0][1][0][1][1][0]=23;
		  COL_Z_LOOKUP[1][1][0][0][1][0]=24;
		  COL_Z_LOOKUP[0][0][1][1][1][0]=25;
		  COL_Z_LOOKUP[1][0][0][0][1][0]=26;
		  COL_Z_LOOKUP[0][1][0][0][1][0]=27;
		  COL_Z_LOOKUP[0][0][1][0][1][0]=28;
		  COL_Z_LOOKUP[0][0][0][1][1][0]=29;
		  COL_Z_LOOKUP[0][0][0][0][1][0]=30;
		  COL_Z_LOOKUP[1][1][1][1][1][0]=31;
		  COL_Z_LOOKUP[1][1][1][0][0][1]=32;
		  COL_Z_LOOKUP[1][1][0][1][0][1]=33;
		  COL_Z_LOOKUP[1][0][1][1][0][1]=34;
		  COL_Z_LOOKUP[0][1][1][1][0][1]=35;
		  COL_Z_LOOKUP[1][0][1][0][0][1]=36;
		  COL_Z_LOOKUP[1][0][0][1][0][1]=37;
		  COL_Z_LOOKUP[0][1][1][0][0][1]=38;
		  COL_Z_LOOKUP[0][1][0][1][0][1]=39;
		  COL_Z_LOOKUP[1][1][0][0][0][1]=40;
		  COL_Z_LOOKUP[0][0][1][1][0][1]=41;
		  COL_Z_LOOKUP[1][0][0][0][0][1]=42;
		  COL_Z_LOOKUP[0][1][0][0][0][1]=43;
		  COL_Z_LOOKUP[0][0][1][0][0][1]=44;
		  COL_Z_LOOKUP[0][0][0][1][0][1]=45;
		  COL_Z_LOOKUP[0][0][0][0][0][1]=46;
		  COL_Z_LOOKUP[1][1][1][1][0][1]=47;
		  COL_Z_LOOKUP[1][1][1][0][0][0]=48;
		  COL_Z_LOOKUP[1][1][0][1][0][0]=49;
		  COL_Z_LOOKUP[1][0][1][1][0][0]=50;
		  COL_Z_LOOKUP[0][1][1][1][0][0]=51;
		  COL_Z_LOOKUP[1][0][1][0][0][0]=52;
		  COL_Z_LOOKUP[1][0][0][1][0][0]=53;
		  COL_Z_LOOKUP[0][1][1][0][0][0]=54;
		  COL_Z_LOOKUP[0][1][0][1][0][0]=55;
		  COL_Z_LOOKUP[1][1][0][0][0][0]=56;
		  COL_Z_LOOKUP[0][0][1][1][0][0]=57;
		  COL_Z_LOOKUP[1][0][0][0][0][0]=58;
		  COL_Z_LOOKUP[0][1][0][0][0][0]=59;
		  COL_Z_LOOKUP[0][0][1][0][0][0]=60;
		  COL_Z_LOOKUP[0][0][0][1][0][0]=61;
		  COL_Z_LOOKUP[0][0][0][0][0][0]=62;
		  COL_Z_LOOKUP[1][1][1][1][0][0]=63;
		  
		  JOIN_LOOKUP[0][0][0][0][0][0]=0;
		  JOIN_LOOKUP[1][0][0][0][0][0]=1;
		  JOIN_LOOKUP[0][1][0][0][0][0]=2;
		  JOIN_LOOKUP[0][0][1][0][0][0]=3;
		  JOIN_LOOKUP[0][0][0][1][0][0]=4;
		  JOIN_LOOKUP[0][0][0][0][1][0]=5;
		  JOIN_LOOKUP[0][0][0][0][0][1]=6;
		  JOIN_LOOKUP[1][1][0][0][0][0]=7;
		  JOIN_LOOKUP[0][0][1][1][0][0]=8;
		  JOIN_LOOKUP[0][0][0][0][1][1]=9;
		  JOIN_LOOKUP[1][0][1][0][0][0]=10;
		  JOIN_LOOKUP[1][0][0][1][0][0]=11;
		  JOIN_LOOKUP[1][0][0][0][1][0]=12;
		  JOIN_LOOKUP[1][0][0][0][0][1]=13;
		  JOIN_LOOKUP[0][1][1][0][0][0]=14;
		  JOIN_LOOKUP[0][1][0][1][0][0]=15;
		  JOIN_LOOKUP[0][1][0][0][1][0]=16;
		  JOIN_LOOKUP[0][1][0][0][0][1]=17;
		  JOIN_LOOKUP[0][0][1][0][1][0]=18;
		  JOIN_LOOKUP[0][0][1][0][0][1]=19;
		  JOIN_LOOKUP[0][0][0][1][1][0]=20;
		  JOIN_LOOKUP[0][0][0][1][0][1]=21;
		  JOIN_LOOKUP[1][0][1][1][0][0]=22;
		  JOIN_LOOKUP[1][0][0][0][1][1]=23;
		  JOIN_LOOKUP[0][1][1][1][0][0]=24;
		  JOIN_LOOKUP[0][1][0][0][1][1]=25;
		  JOIN_LOOKUP[0][0][1][1][1][0]=26;
		  JOIN_LOOKUP[1][1][0][0][1][0]=27;
		  JOIN_LOOKUP[0][0][1][1][0][1]=28;
		  JOIN_LOOKUP[1][1][0][0][0][1]=29;
		  JOIN_LOOKUP[1][1][1][0][0][0]=30;
		  JOIN_LOOKUP[0][0][1][0][1][1]=31;
		  JOIN_LOOKUP[1][1][0][1][0][0]=32;
		  JOIN_LOOKUP[0][0][0][1][1][1]=33;
		  JOIN_LOOKUP[1][0][1][0][1][0]=34;
		  JOIN_LOOKUP[1][0][1][0][0][1]=35;
		  JOIN_LOOKUP[1][0][0][1][1][0]=36;
		  JOIN_LOOKUP[1][0][0][1][0][1]=37;
		  JOIN_LOOKUP[0][1][1][0][1][0]=38;
		  JOIN_LOOKUP[0][1][1][0][0][1]=39;
		  JOIN_LOOKUP[0][1][0][1][1][0]=40;
		  JOIN_LOOKUP[0][1][0][1][0][1]=41;
		  JOIN_LOOKUP[0][0][1][1][1][1]=42;
		  JOIN_LOOKUP[1][1][1][1][0][0]=43;
		  JOIN_LOOKUP[1][1][0][0][1][1]=44;
		  JOIN_LOOKUP[1][0][0][1][1][1]=45;
		  JOIN_LOOKUP[1][0][1][0][1][1]=46;
		  JOIN_LOOKUP[1][0][1][1][0][1]=47;
		  JOIN_LOOKUP[1][0][1][1][1][0]=48;
		  JOIN_LOOKUP[0][1][0][1][1][1]=49;
		  JOIN_LOOKUP[0][1][1][0][1][1]=50;
		  JOIN_LOOKUP[0][1][1][1][0][1]=51;
		  JOIN_LOOKUP[0][1][1][1][1][0]=52;
		  JOIN_LOOKUP[1][1][1][0][1][0]=53;
		  JOIN_LOOKUP[1][1][1][0][0][1]=54;
		  JOIN_LOOKUP[1][1][0][1][1][0]=55;
		  JOIN_LOOKUP[1][1][0][1][0][1]=56;
		  JOIN_LOOKUP[0][1][1][1][1][1]=57;
		  JOIN_LOOKUP[1][0][1][1][1][1]=58;
		  JOIN_LOOKUP[1][1][0][1][1][1]=59;
		  JOIN_LOOKUP[1][1][1][0][1][1]=60;
		  JOIN_LOOKUP[1][1][1][1][0][1]=61;
		  JOIN_LOOKUP[1][1][1][1][1][0]=62;
		  JOIN_LOOKUP[1][1][1][1][1][1]=63;
	  }

	  public class CustomStateMapper extends DefaultStateMapper{

		  @Override  
		  public Map putStateModelLocations(Block block)
		  {
		        Iterator iterator = block.getBlockState().getValidStates().iterator();
		        
		        while (iterator.hasNext())
		        {
		        	IBlockState iblockstate = (IBlockState)iterator.next();

		        	EnumStyle parentID = (EnumStyle) iblockstate.getValue(BlockBasalt.PROP_STYLE);
		            int childID = (Integer) iblockstate.getValue(BlockBasalt.PROP_DETAILS);
		            	
		            if(childID <= parentID.getMaxDetailID()){
		            	this.mapStateModelLocations.put(iblockstate, this.getModelResourceLocation(iblockstate));
		            }

		        }

		        return this.mapStateModelLocations;
		    }
	}
	  
}
