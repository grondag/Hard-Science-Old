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

	private static  Integer[][][][][][] col_x_lookup = new Integer[2][2][2][2][2][2];
	private static  Integer[][][][][][] col_y_lookup = new Integer[2][2][2][2][2][2];
	private static  Integer[][][][][][] col_z_lookup = new Integer[2][2][2][2][2][2];

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
		  
		  int neighbor_up;
		  int neighbor_down;
		  int neighbor_east;
		  int neighbor_west;
		  int neighbor_north;
		  int neighbor_south;

		  
		  IBlockState test;
		  
		  switch(style){
		  
		  case ROUGH:
			  return state.withProperty(PROP_DETAILS, 0);

		  case SMOOTH:
			  return state.withProperty(PROP_DETAILS, 0);
			  
		  case COLUMN_Y:
			  
			  // Add top or bottom plates if this at the top or bottom of the column
			  // Otherwise will "see through" gaps between splines.
			  // Want to leave this block non-opaque for rendering efficiency.
			  test = worldIn.getBlockState(pos.up()); 
			  neighbor_up = (test.getBlock() == this && test.getValue(PROP_STYLE) == EnumStyle.COLUMN_Y) ? 1 : 0;
			  test = worldIn.getBlockState(pos.down()); 
			  neighbor_down = (test.getBlock() == this && test.getValue(PROP_STYLE) == EnumStyle.COLUMN_Y) ? 1 : 0;
			  test = worldIn.getBlockState(pos.east()); 
			  neighbor_east = (test.getBlock() == this && test.getValue(PROP_STYLE) == EnumStyle.COLUMN_Y) ? 1 : 0;
			  test = worldIn.getBlockState(pos.west()); 
			  neighbor_west = (test.getBlock() == this && test.getValue(PROP_STYLE) == EnumStyle.COLUMN_Y) ? 1 : 0;
			  test = worldIn.getBlockState(pos.north()); 
			  neighbor_north = (test.getBlock() == this && test.getValue(PROP_STYLE) == EnumStyle.COLUMN_Y) ? 1 : 0;
			  test = worldIn.getBlockState(pos.south()); 
			  neighbor_south = (test.getBlock() == this && test.getValue(PROP_STYLE) == EnumStyle.COLUMN_Y) ? 1 : 0;
			
			  return this.getDefaultState().withProperty(PROP_STYLE, EnumStyle.COLUMN_Y).withProperty(PROP_DETAILS, col_y_lookup[neighbor_up][neighbor_down][neighbor_east][neighbor_west][neighbor_north][neighbor_south]);			  
			  
		  case COLUMN_X:
			  
			  // Add top or bottom plates if this at the top or bottom of the column
			  // Otherwise will "see through" gaps between splines.
			  // Want to leave this block non-opaque for rendering efficiency.
			  test = worldIn.getBlockState(pos.up()); 
			  neighbor_up = (test.getBlock() == this && test.getValue(PROP_STYLE) == EnumStyle.COLUMN_X) ? 1 : 0;
			  test = worldIn.getBlockState(pos.down()); 
			  neighbor_down = (test.getBlock() == this && test.getValue(PROP_STYLE) == EnumStyle.COLUMN_X) ? 1 : 0;
			  test = worldIn.getBlockState(pos.east()); 
			  neighbor_east = (test.getBlock() == this && test.getValue(PROP_STYLE) == EnumStyle.COLUMN_X) ? 1 : 0;
			  test = worldIn.getBlockState(pos.west()); 
			  neighbor_west = (test.getBlock() == this && test.getValue(PROP_STYLE) == EnumStyle.COLUMN_X) ? 1 : 0;
			  test = worldIn.getBlockState(pos.north()); 
			  neighbor_north = (test.getBlock() == this && test.getValue(PROP_STYLE) == EnumStyle.COLUMN_X) ? 1 : 0;
			  test = worldIn.getBlockState(pos.south()); 
			  neighbor_south = (test.getBlock() == this && test.getValue(PROP_STYLE) == EnumStyle.COLUMN_X) ? 1 : 0;
			  
			  return this.getDefaultState().withProperty(PROP_STYLE, EnumStyle.COLUMN_X).withProperty(PROP_DETAILS, col_x_lookup[neighbor_up][neighbor_down][neighbor_east][neighbor_west][neighbor_north][neighbor_south]);			  

		  case COLUMN_Z:
			  
			  // Add top or bottom plates if this at the top or bottom of the column
			  // Otherwise will "see through" gaps between splines.
			  // Want to leave this block non-opaque for rendering efficiency.
			  test = worldIn.getBlockState(pos.up()); 
			  neighbor_up = (test.getBlock() == this && test.getValue(PROP_STYLE) == EnumStyle.COLUMN_Z) ? 1 : 0;
			  test = worldIn.getBlockState(pos.down()); 
			  neighbor_down = (test.getBlock() == this && test.getValue(PROP_STYLE) == EnumStyle.COLUMN_Z) ? 1 : 0;
			  test = worldIn.getBlockState(pos.east()); 
			  neighbor_east = (test.getBlock() == this && test.getValue(PROP_STYLE) == EnumStyle.COLUMN_Z) ? 1 : 0;
			  test = worldIn.getBlockState(pos.west()); 
			  neighbor_west = (test.getBlock() == this && test.getValue(PROP_STYLE) == EnumStyle.COLUMN_Z) ? 1 : 0;
			  test = worldIn.getBlockState(pos.north()); 
			  neighbor_north = (test.getBlock() == this && test.getValue(PROP_STYLE) == EnumStyle.COLUMN_Z) ? 1 : 0;
			  test = worldIn.getBlockState(pos.south()); 
			  neighbor_south = (test.getBlock() == this && test.getValue(PROP_STYLE) == EnumStyle.COLUMN_Z) ? 1 : 0;
			  
			  return this.getDefaultState().withProperty(PROP_STYLE, EnumStyle.COLUMN_Z).withProperty(PROP_DETAILS, col_z_lookup[neighbor_up][neighbor_down][neighbor_east][neighbor_west][neighbor_north][neighbor_south]);			  

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


	  public static enum EnumStyle implements IStringSerializable
	  {
	    ROUGH(0, "rough", 0),
	    SMOOTH(1, "smooth", 0),
	    COLUMN_Y(2, "column_y", 63),
	    COLUMN_X(3, "column_x", 63),	    
	    COLUMN_Z(4, "column_z", 63);
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
		
		  col_y_lookup[1][1][0][1][1][1]=0;
		  col_y_lookup[1][1][1][0][1][1]=1;
		  col_y_lookup[1][1][1][1][0][1]=2;
		  col_y_lookup[1][1][1][1][1][0]=3;
		  col_y_lookup[1][1][0][1][0][1]=4;
		  col_y_lookup[1][1][1][0][0][1]=5;
		  col_y_lookup[1][1][0][1][1][0]=6;
		  col_y_lookup[1][1][1][0][1][0]=7;
		  col_y_lookup[1][1][0][0][1][1]=8;
		  col_y_lookup[1][1][1][1][0][0]=9;
		  col_y_lookup[1][1][0][0][0][1]=10;
		  col_y_lookup[1][1][0][0][1][0]=11;
		  col_y_lookup[1][1][0][1][0][0]=12;
		  col_y_lookup[1][1][1][0][0][0]=13;
		  col_y_lookup[1][1][0][0][0][0]=14;
		  col_y_lookup[1][1][1][1][1][1]=15;
		  col_y_lookup[0][1][0][1][1][1]=16;
		  col_y_lookup[0][1][1][0][1][1]=17;
		  col_y_lookup[0][1][1][1][0][1]=18;
		  col_y_lookup[0][1][1][1][1][0]=19;
		  col_y_lookup[0][1][0][1][0][1]=20;
		  col_y_lookup[0][1][1][0][0][1]=21;
		  col_y_lookup[0][1][0][1][1][0]=22;
		  col_y_lookup[0][1][1][0][1][0]=23;
		  col_y_lookup[0][1][0][0][1][1]=24;
		  col_y_lookup[0][1][1][1][0][0]=25;
		  col_y_lookup[0][1][0][0][0][1]=26;
		  col_y_lookup[0][1][0][0][1][0]=27;
		  col_y_lookup[0][1][0][1][0][0]=28;
		  col_y_lookup[0][1][1][0][0][0]=29;
		  col_y_lookup[0][1][0][0][0][0]=30;
		  col_y_lookup[0][1][1][1][1][1]=31;
		  col_y_lookup[1][0][0][1][1][1]=32;
		  col_y_lookup[1][0][1][0][1][1]=33;
		  col_y_lookup[1][0][1][1][0][1]=34;
		  col_y_lookup[1][0][1][1][1][0]=35;
		  col_y_lookup[1][0][0][1][0][1]=36;
		  col_y_lookup[1][0][1][0][0][1]=37;
		  col_y_lookup[1][0][0][1][1][0]=38;
		  col_y_lookup[1][0][1][0][1][0]=39;
		  col_y_lookup[1][0][0][0][1][1]=40;
		  col_y_lookup[1][0][1][1][0][0]=41;
		  col_y_lookup[1][0][0][0][0][1]=42;
		  col_y_lookup[1][0][0][0][1][0]=43;
		  col_y_lookup[1][0][0][1][0][0]=44;
		  col_y_lookup[1][0][1][0][0][0]=45;
		  col_y_lookup[1][0][0][0][0][0]=46;
		  col_y_lookup[1][0][1][1][1][1]=47;
		  col_y_lookup[0][0][0][1][1][1]=48;
		  col_y_lookup[0][0][1][0][1][1]=49;
		  col_y_lookup[0][0][1][1][0][1]=50;
		  col_y_lookup[0][0][1][1][1][0]=51;
		  col_y_lookup[0][0][0][1][0][1]=52;
		  col_y_lookup[0][0][1][0][0][1]=53;
		  col_y_lookup[0][0][0][1][1][0]=54;
		  col_y_lookup[0][0][1][0][1][0]=55;
		  col_y_lookup[0][0][0][0][1][1]=56;
		  col_y_lookup[0][0][1][1][0][0]=57;
		  col_y_lookup[0][0][0][0][0][1]=58;
		  col_y_lookup[0][0][0][0][1][0]=59;
		  col_y_lookup[0][0][0][1][0][0]=60;
		  col_y_lookup[0][0][1][0][0][0]=61;
		  col_y_lookup[0][0][0][0][0][0]=62;
		  col_y_lookup[0][0][1][1][1][1]=63;
		  
		  col_x_lookup[0][1][1][1][1][1]=0;
		  col_x_lookup[1][0][1][1][1][1]=1;
		  col_x_lookup[1][1][1][1][0][1]=2;
		  col_x_lookup[1][1][1][1][1][0]=3;
		  col_x_lookup[0][1][1][1][0][1]=4;
		  col_x_lookup[1][0][1][1][0][1]=5;
		  col_x_lookup[0][1][1][1][1][0]=6;
		  col_x_lookup[1][0][1][1][1][0]=7;
		  col_x_lookup[0][0][1][1][1][1]=8;
		  col_x_lookup[1][1][1][1][0][0]=9;
		  col_x_lookup[0][0][1][1][0][1]=10;
		  col_x_lookup[0][0][1][1][1][0]=11;
		  col_x_lookup[0][1][1][1][0][0]=12;
		  col_x_lookup[1][0][1][1][0][0]=13;
		  col_x_lookup[0][0][1][1][0][0]=14;
		  col_x_lookup[1][1][1][1][1][1]=15;
		  col_x_lookup[0][1][1][0][1][1]=16;
		  col_x_lookup[1][0][1][0][1][1]=17;
		  col_x_lookup[1][1][1][0][0][1]=18;
		  col_x_lookup[1][1][1][0][1][0]=19;
		  col_x_lookup[0][1][1][0][0][1]=20;
		  col_x_lookup[1][0][1][0][0][1]=21;
		  col_x_lookup[0][1][1][0][1][0]=22;
		  col_x_lookup[1][0][1][0][1][0]=23;
		  col_x_lookup[0][0][1][0][1][1]=24;
		  col_x_lookup[1][1][1][0][0][0]=25;
		  col_x_lookup[0][0][1][0][0][1]=26;
		  col_x_lookup[0][0][1][0][1][0]=27;
		  col_x_lookup[0][1][1][0][0][0]=28;
		  col_x_lookup[1][0][1][0][0][0]=29;
		  col_x_lookup[0][0][1][0][0][0]=30;
		  col_x_lookup[1][1][1][0][1][1]=31;
		  col_x_lookup[0][1][0][1][1][1]=32;
		  col_x_lookup[1][0][0][1][1][1]=33;
		  col_x_lookup[1][1][0][1][0][1]=34;
		  col_x_lookup[1][1][0][1][1][0]=35;
		  col_x_lookup[0][1][0][1][0][1]=36;
		  col_x_lookup[1][0][0][1][0][1]=37;
		  col_x_lookup[0][1][0][1][1][0]=38;
		  col_x_lookup[1][0][0][1][1][0]=39;
		  col_x_lookup[0][0][0][1][1][1]=40;
		  col_x_lookup[1][1][0][1][0][0]=41;
		  col_x_lookup[0][0][0][1][0][1]=42;
		  col_x_lookup[0][0][0][1][1][0]=43;
		  col_x_lookup[0][1][0][1][0][0]=44;
		  col_x_lookup[1][0][0][1][0][0]=45;
		  col_x_lookup[0][0][0][1][0][0]=46;
		  col_x_lookup[1][1][0][1][1][1]=47;
		  col_x_lookup[0][1][0][0][1][1]=48;
		  col_x_lookup[1][0][0][0][1][1]=49;
		  col_x_lookup[1][1][0][0][0][1]=50;
		  col_x_lookup[1][1][0][0][1][0]=51;
		  col_x_lookup[0][1][0][0][0][1]=52;
		  col_x_lookup[1][0][0][0][0][1]=53;
		  col_x_lookup[0][1][0][0][1][0]=54;
		  col_x_lookup[1][0][0][0][1][0]=55;
		  col_x_lookup[0][0][0][0][1][1]=56;
		  col_x_lookup[1][1][0][0][0][0]=57;
		  col_x_lookup[0][0][0][0][0][1]=58;
		  col_x_lookup[0][0][0][0][1][0]=59;
		  col_x_lookup[0][1][0][0][0][0]=60;
		  col_x_lookup[1][0][0][0][0][0]=61;
		  col_x_lookup[0][0][0][0][0][0]=62;
		  col_x_lookup[1][1][0][0][1][1]=63;
		  
		  col_z_lookup[1][1][1][0][1][1]=0;
		  col_z_lookup[1][1][0][1][1][1]=1;
		  col_z_lookup[1][0][1][1][1][1]=2;
		  col_z_lookup[0][1][1][1][1][1]=3;
		  col_z_lookup[1][0][1][0][1][1]=4;
		  col_z_lookup[1][0][0][1][1][1]=5;
		  col_z_lookup[0][1][1][0][1][1]=6;
		  col_z_lookup[0][1][0][1][1][1]=7;
		  col_z_lookup[1][1][0][0][1][1]=8;
		  col_z_lookup[0][0][1][1][1][1]=9;
		  col_z_lookup[1][0][0][0][1][1]=10;
		  col_z_lookup[0][1][0][0][1][1]=11;
		  col_z_lookup[0][0][1][0][1][1]=12;
		  col_z_lookup[0][0][0][1][1][1]=13;
		  col_z_lookup[0][0][0][0][1][1]=14;
		  col_z_lookup[1][1][1][1][1][1]=15;
		  col_z_lookup[1][1][1][0][1][0]=16;
		  col_z_lookup[1][1][0][1][1][0]=17;
		  col_z_lookup[1][0][1][1][1][0]=18;
		  col_z_lookup[0][1][1][1][1][0]=19;
		  col_z_lookup[1][0][1][0][1][0]=20;
		  col_z_lookup[1][0][0][1][1][0]=21;
		  col_z_lookup[0][1][1][0][1][0]=22;
		  col_z_lookup[0][1][0][1][1][0]=23;
		  col_z_lookup[1][1][0][0][1][0]=24;
		  col_z_lookup[0][0][1][1][1][0]=25;
		  col_z_lookup[1][0][0][0][1][0]=26;
		  col_z_lookup[0][1][0][0][1][0]=27;
		  col_z_lookup[0][0][1][0][1][0]=28;
		  col_z_lookup[0][0][0][1][1][0]=29;
		  col_z_lookup[0][0][0][0][1][0]=30;
		  col_z_lookup[1][1][1][1][1][0]=31;
		  col_z_lookup[1][1][1][0][0][1]=32;
		  col_z_lookup[1][1][0][1][0][1]=33;
		  col_z_lookup[1][0][1][1][0][1]=34;
		  col_z_lookup[0][1][1][1][0][1]=35;
		  col_z_lookup[1][0][1][0][0][1]=36;
		  col_z_lookup[1][0][0][1][0][1]=37;
		  col_z_lookup[0][1][1][0][0][1]=38;
		  col_z_lookup[0][1][0][1][0][1]=39;
		  col_z_lookup[1][1][0][0][0][1]=40;
		  col_z_lookup[0][0][1][1][0][1]=41;
		  col_z_lookup[1][0][0][0][0][1]=42;
		  col_z_lookup[0][1][0][0][0][1]=43;
		  col_z_lookup[0][0][1][0][0][1]=44;
		  col_z_lookup[0][0][0][1][0][1]=45;
		  col_z_lookup[0][0][0][0][0][1]=46;
		  col_z_lookup[1][1][1][1][0][1]=47;
		  col_z_lookup[1][1][1][0][0][0]=48;
		  col_z_lookup[1][1][0][1][0][0]=49;
		  col_z_lookup[1][0][1][1][0][0]=50;
		  col_z_lookup[0][1][1][1][0][0]=51;
		  col_z_lookup[1][0][1][0][0][0]=52;
		  col_z_lookup[1][0][0][1][0][0]=53;
		  col_z_lookup[0][1][1][0][0][0]=54;
		  col_z_lookup[0][1][0][1][0][0]=55;
		  col_z_lookup[1][1][0][0][0][0]=56;
		  col_z_lookup[0][0][1][1][0][0]=57;
		  col_z_lookup[1][0][0][0][0][0]=58;
		  col_z_lookup[0][1][0][0][0][0]=59;
		  col_z_lookup[0][0][1][0][0][0]=60;
		  col_z_lookup[0][0][0][1][0][0]=61;
		  col_z_lookup[0][0][0][0][0][0]=62;
		  col_z_lookup[1][1][1][1][0][0]=63;
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
