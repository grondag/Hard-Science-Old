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
	private static  Integer[][][][][][] BRICK_BIG_LOOKUP = new Integer[2][2][2][2][2][2];
	
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
	    
	    if(style == EnumStyle.COLUMN_Y || style == EnumStyle.COLUMN_X || style == EnumStyle.COLUMN_Z){
		    
		    switch(blockFaceClickedOn.getAxis()){
		    case X:
		    	style = EnumStyle.COLUMN_X;
		    	break;
		    case Y:
		    	style = EnumStyle.COLUMN_Y;
		    	break;
		    case Z:
		    	style = EnumStyle.COLUMN_Z;
		    	break;
		    }
	
		    return this.getDefaultState().withProperty(PROP_STYLE, style);
		    
	    } else {
	    	return super.onBlockPlaced(worldIn, pos, blockFaceClickedOn, hitX, hitY, hitZ, meta, placer);
	    }
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

	  // this method isn't required if your properties only depend on the stored metadata.
	  // it is required if:
	  // 1) you are making a multiblock which stores information in other blocks eg BlockBed, BlockDoor
	  // 2) your block's state depends on other neighbours (eg BlockFence)
	  @Override
	  public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
	  {
		  EnumStyle style = (EnumStyle) state.getValue(PROP_STYLE);
		  
		  NeighborTestResults tests;
		  int detailID ;

		  switch(style){
		  
		  case ROUGH:
			  return state.withProperty(PROP_DETAILS, 0);

		  case SMOOTH:
			  return state.withProperty(PROP_DETAILS, 0);
			  
		  case COLUMN_Y:
			  
			  tests = new NeighborBlocks(worldIn, pos).getNeighborTestResults(new TestForSameStyle(this, EnumStyle.COLUMN_Y));
			  detailID = COL_Y_LOOKUP[tests.up?1:0][tests.down?1:0][tests.east?1:0][tests.west?1:0][tests.north?1:0][tests.south?1:0];
			  return this.getDefaultState().withProperty(PROP_STYLE, EnumStyle.COLUMN_Y).withProperty(PROP_DETAILS, detailID);			   
			  
		  case COLUMN_X:			  

			  tests = new NeighborBlocks(worldIn, pos).getNeighborTestResults(new TestForSameStyle(this, EnumStyle.COLUMN_X));
			  detailID = COL_X_LOOKUP[tests.up?1:0][tests.down?1:0][tests.east?1:0][tests.west?1:0][tests.north?1:0][tests.south?1:0];
			  return this.getDefaultState().withProperty(PROP_STYLE, EnumStyle.COLUMN_X).withProperty(PROP_DETAILS, detailID);			  
			  
		  case COLUMN_Z:
			  
			  tests = new NeighborBlocks(worldIn, pos).getNeighborTestResults(new TestForSameStyle(this, EnumStyle.COLUMN_Z));
			  detailID = COL_Z_LOOKUP[tests.up?1:0][tests.down?1:0][tests.east?1:0][tests.west?1:0][tests.north?1:0][tests.south?1:0];
			  // N.B.  Using state.withProperty here caused an NPE for center blocks.
			  // Was never able to track down why.Hence the use of getDefaultState.
			  // Was conspicuous that the detailID was the max value. 
			  // This was before I re-factored the detail ID declarations, maybe it would work now.
			  return this.getDefaultState().withProperty(PROP_STYLE, EnumStyle.COLUMN_Z).withProperty(PROP_DETAILS, detailID);			  

		  default:
			  
		  }

		  return state; 

	    
		  // TODO: May need to handle some texture rotation here.
		  // See MathHelper.getPositionRandom(Vec3i pos)
		  // but note intended for client only.

	  }

	  @Override
	  protected BlockState createBlockState()
	  {
	    return new BlockState(this, new IProperty[] {PROP_STYLE, PROP_DETAILS}); 
	  }

	  private class TestForSameStyle implements INeighborTest{

		private final Block block;
		private final EnumStyle style ;
		
		public TestForSameStyle(Block block, EnumStyle style){
			this.block = block;
			this.style = style;
		}
		  
		@Override
		public boolean TestNeighbor(IBlockState ibs) {
			return (ibs.getBlock() == block && ibs.getValue(PROP_STYLE) == style);
		}	  
	  }

	  public static enum EnumStyle implements IStringSerializable
	  {
	    ROUGH(0, "rough", 0),
	    SMOOTH(1, "smooth", 0),
	    COLUMN_Y(2, "column_y", 63),
	    COLUMN_X(3, "column_x", 63),	    
	    COLUMN_Z(4, "column_z", 63),
	    BRICK_BIG(5, "brick_big", 63);
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
