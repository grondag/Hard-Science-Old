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
	public static final PropertyInteger	 PROP_DETAILS = PropertyInteger.create("details", 0, 65);

	private static int[] detailParents = new int[66];
	private static  int[][][][][][] col_y_lookup = new int[2][2][2][2][2][2];

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
	    
	    if(style == EnumStyle.COLUMN_Y ){
		//if(style == EnumStyle.COLUMN_X || style == EnumStyle.COLUMN_Y || style == EnumStyle.COLUMN_Z){
		    
		    switch(blockFaceClickedOn.getAxis()){
		    case X:
//		    	style = EnumStyle.COLUMN_X;
		    	break;
		    case Y:
		    	style = EnumStyle.COLUMN_Y;
		    	break;
		    case Z:
//		    	style = EnumStyle.COLUMN_Z;
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
	    return this.getDefaultState().withProperty(PROP_STYLE, style);
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
			  return state.withProperty(PROP_DETAILS, 1);
			  
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
			  
			  return state.withProperty(PROP_DETAILS, col_y_lookup[neighbor_up][neighbor_down][neighbor_east][neighbor_west][neighbor_north][neighbor_south]);

		  default:
			  
		  }

		  return state; //.withProperty(PROP_PLATE_UP, plate_up).withProperty(PROP_PLATE_DOWN, plate_down)
				  //.withProperty(PROP_FACE_NORTH, face_north).withProperty(PROP_FACE_SOUTH, face_south)
				  //.withProperty(PROP_FACE_EAST, face_east).withProperty(PROP_FACE_WEST, face_west);

	    
		  // TODO: May need to handle some texture rotation here.
		  // See MathHelper.getPositionRandom(Vec3i pos)
		  // but note intended for client only.

	  }

	  @Override
	  protected BlockState createBlockState()
	  {
	    return new BlockState(this, new IProperty[] {PROP_STYLE, PROP_DETAILS}); //, PROP_SHAPE, PROP_ROTATION_Y, PROP_ROTATION_X});
	  }


	  public static enum EnumStyle implements IStringSerializable
	  {
	    ROUGH(0, "rough"),
	    SMOOTH(1, "smooth"),
	    COLUMN_Y(2, "column_y");//,
//	    COLUMN_X(3, "column_x"),	    
//	    COLUMN_Z(4, "column_z"),
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

	    private EnumStyle(int i_meta, String i_name)
	    {
	      this.meta = i_meta;
	      this.name = i_name;
	    }

	  }
	  
	  private static void setupLookupArrays(){
		  detailParents[0] = 0;
		  detailParents[1] = 1;
		  detailParents[2] = 2;
		  detailParents[3] = 2;
		  detailParents[4] = 2;
		  detailParents[5] = 2;
		  detailParents[6] = 2;
		  detailParents[7] = 2;
		  detailParents[8] = 2;
		  detailParents[9] = 2;
		  detailParents[10] = 2;
		  detailParents[11] = 2;
		  detailParents[12] = 2;
		  detailParents[13] = 2;
		  detailParents[14] = 2;
		  detailParents[15] = 2;
		  detailParents[16] = 2;
		  detailParents[17] = 2;
		  detailParents[18] = 2;
		  detailParents[19] = 2;
		  detailParents[20] = 2;
		  detailParents[21] = 2;
		  detailParents[22] = 2;
		  detailParents[23] = 2;
		  detailParents[24] = 2;
		  detailParents[25] = 2;
		  detailParents[26] = 2;
		  detailParents[27] = 2;
		  detailParents[28] = 2;
		  detailParents[29] = 2;
		  detailParents[30] = 2;
		  detailParents[31] = 2;
		  detailParents[32] = 2;
		  detailParents[33] = 2;
		  detailParents[34] = 2;
		  detailParents[35] = 2;
		  detailParents[36] = 2;
		  detailParents[37] = 2;
		  detailParents[38] = 2;
		  detailParents[39] = 2;
		  detailParents[40] = 2;
		  detailParents[41] = 2;
		  detailParents[42] = 2;
		  detailParents[43] = 2;
		  detailParents[44] = 2;
		  detailParents[45] = 2;
		  detailParents[46] = 2;
		  detailParents[47] = 2;
		  detailParents[48] = 2;
		  detailParents[49] = 2;
		  detailParents[50] = 2;
		  detailParents[51] = 2;
		  detailParents[52] = 2;
		  detailParents[53] = 2;
		  detailParents[54] = 2;
		  detailParents[55] = 2;
		  detailParents[56] = 2;
		  detailParents[57] = 2;
		  detailParents[58] = 2;
		  detailParents[59] = 2;
		  detailParents[60] = 2;
		  detailParents[61] = 2;
		  detailParents[62] = 2;
		  detailParents[63] = 2;
		  detailParents[64] = 2;
		  detailParents[65] = 2;
		  
		  // U D E W N S
		  // 1 means has adjacent block of same style
		
		  col_y_lookup[1][1][0][1][1][1]=2;
		  col_y_lookup[1][1][1][0][1][1]=3;
		  col_y_lookup[1][1][1][1][0][1]=4;
		  col_y_lookup[1][1][1][1][1][0]=5;
		  col_y_lookup[1][1][0][1][0][1]=6;
		  col_y_lookup[1][1][1][0][0][1]=7;
		  col_y_lookup[1][1][0][1][1][0]=8;
		  col_y_lookup[1][1][1][0][1][0]=9;
		  col_y_lookup[1][1][0][0][1][1]=10;
		  col_y_lookup[1][1][1][1][0][0]=11;
		  col_y_lookup[1][1][0][0][0][1]=12;
		  col_y_lookup[1][1][0][0][1][0]=13;
		  col_y_lookup[1][1][0][1][0][0]=14;
		  col_y_lookup[1][1][1][0][0][0]=15;
		  col_y_lookup[1][1][0][0][0][0]=16;
		  col_y_lookup[1][1][1][1][1][1]=17;
		  col_y_lookup[0][1][0][1][1][1]=18;
		  col_y_lookup[0][1][1][0][1][1]=19;
		  col_y_lookup[0][1][1][1][0][1]=20;
		  col_y_lookup[0][1][1][1][1][0]=21;
		  col_y_lookup[0][1][0][1][0][1]=22;
		  col_y_lookup[0][1][1][0][0][1]=23;
		  col_y_lookup[0][1][0][1][1][0]=24;
		  col_y_lookup[0][1][1][0][1][0]=25;
		  col_y_lookup[0][1][0][0][1][1]=26;
		  col_y_lookup[0][1][1][1][0][0]=27;
		  col_y_lookup[0][1][0][0][0][1]=28;
		  col_y_lookup[0][1][0][0][1][0]=29;
		  col_y_lookup[0][1][0][1][0][0]=30;
		  col_y_lookup[0][1][1][0][0][0]=31;
		  col_y_lookup[0][1][0][0][0][0]=32;
		  col_y_lookup[0][1][1][1][1][1]=33;
		  col_y_lookup[1][0][0][1][1][1]=34;
		  col_y_lookup[1][0][1][0][1][1]=35;
		  col_y_lookup[1][0][1][1][0][1]=36;
		  col_y_lookup[1][0][1][1][1][0]=37;
		  col_y_lookup[1][0][0][1][0][1]=38;
		  col_y_lookup[1][0][1][0][0][1]=39;
		  col_y_lookup[1][0][0][1][1][0]=40;
		  col_y_lookup[1][0][1][0][1][0]=41;
		  col_y_lookup[1][0][0][0][1][1]=42;
		  col_y_lookup[1][0][1][1][0][0]=43;
		  col_y_lookup[1][0][0][0][0][1]=44;
		  col_y_lookup[1][0][0][0][1][0]=45;
		  col_y_lookup[1][0][0][1][0][0]=46;
		  col_y_lookup[1][0][1][0][0][0]=47;
		  col_y_lookup[1][0][0][0][0][0]=48;
		  col_y_lookup[1][0][1][1][1][1]=49;
		  col_y_lookup[0][0][0][1][1][1]=50;
		  col_y_lookup[0][0][1][0][1][1]=51;
		  col_y_lookup[0][0][1][1][0][1]=52;
		  col_y_lookup[0][0][1][1][1][0]=53;
		  col_y_lookup[0][0][0][1][0][1]=54;
		  col_y_lookup[0][0][1][0][0][1]=55;
		  col_y_lookup[0][0][0][1][1][0]=56;
		  col_y_lookup[0][0][1][0][1][0]=57;
		  col_y_lookup[0][0][0][0][1][1]=58;
		  col_y_lookup[0][0][1][1][0][0]=59;
		  col_y_lookup[0][0][0][0][0][1]=60;
		  col_y_lookup[0][0][0][0][1][0]=61;
		  col_y_lookup[0][0][0][1][0][0]=62;
		  col_y_lookup[0][0][1][0][0][0]=63;
		  col_y_lookup[0][0][0][0][0][0]=64;
		  col_y_lookup[0][0][1][1][1][1]=65;
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
		            	
		            if(parentID.meta == detailParents[childID]){
		            	this.mapStateModelLocations.put(iblockstate, this.getModelResourceLocation(iblockstate));
		            }

		        }

		        return this.mapStateModelLocations;
		    }
	}
	  
}
