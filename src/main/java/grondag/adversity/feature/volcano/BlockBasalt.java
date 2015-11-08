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
	public static final PropertyInteger	 PROP_DETAILS = PropertyInteger.create("details", 0, 193);

	private static int[] detailParents = new int[194];
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
	    ROUGH(0, "rough"),
	    SMOOTH(1, "smooth"),
	    COLUMN_Y(2, "column_y"),
	    COLUMN_X(3, "column_x"),	    
	    COLUMN_Z(4, "column_z");
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
		  
		  detailParents[66] = 3;
		  detailParents[67] = 3;
		  detailParents[68] = 3;
		  detailParents[69] = 3;
		  detailParents[70] = 3;
		  detailParents[71] = 3;
		  detailParents[72] = 3;
		  detailParents[73] = 3;
		  detailParents[74] = 3;
		  detailParents[75] = 3;
		  detailParents[76] = 3;
		  detailParents[77] = 3;
		  detailParents[78] = 3;
		  detailParents[79] = 3;
		  detailParents[80] = 3;
		  detailParents[81] = 3;
		  detailParents[82] = 3;
		  detailParents[83] = 3;
		  detailParents[84] = 3;
		  detailParents[85] = 3;
		  detailParents[86] = 3;
		  detailParents[87] = 3;
		  detailParents[88] = 3;
		  detailParents[89] = 3;
		  detailParents[90] = 3;
		  detailParents[91] = 3;
		  detailParents[92] = 3;
		  detailParents[93] = 3;
		  detailParents[94] = 3;
		  detailParents[95] = 3;
		  detailParents[96] = 3;
		  detailParents[97] = 3;
		  detailParents[98] = 3;
		  detailParents[99] = 3;
		  detailParents[100] = 3;
		  detailParents[101] = 3;
		  detailParents[102] = 3;
		  detailParents[103] = 3;
		  detailParents[104] = 3;
		  detailParents[105] = 3;
		  detailParents[106] = 3;
		  detailParents[107] = 3;
		  detailParents[108] = 3;
		  detailParents[109] = 3;
		  detailParents[110] = 3;
		  detailParents[111] = 3;
		  detailParents[112] = 3;
		  detailParents[113] = 3;
		  detailParents[114] = 3;
		  detailParents[115] = 3;
		  detailParents[116] = 3;
		  detailParents[117] = 3;
		  detailParents[118] = 3;
		  detailParents[119] = 3;
		  detailParents[120] = 3;
		  detailParents[121] = 3;
		  detailParents[122] = 3;
		  detailParents[123] = 3;
		  detailParents[124] = 3;
		  detailParents[125] = 3;
		  detailParents[126] = 3;
		  detailParents[127] = 3;
		  detailParents[128] = 3;
		  detailParents[129] = 3;
		  
		  detailParents[130] = 4;
		  detailParents[131] = 4;
		  detailParents[132] = 4;
		  detailParents[133] = 4;
		  detailParents[134] = 4;
		  detailParents[135] = 4;
		  detailParents[136] = 4;
		  detailParents[137] = 4;
		  detailParents[138] = 4;
		  detailParents[139] = 4;
		  detailParents[140] = 4;
		  detailParents[141] = 4;
		  detailParents[142] = 4;
		  detailParents[143] = 4;
		  detailParents[144] = 4;
		  detailParents[145] = 4;
		  detailParents[146] = 4;
		  detailParents[147] = 4;
		  detailParents[148] = 4;
		  detailParents[149] = 4;
		  detailParents[150] = 4;
		  detailParents[151] = 4;
		  detailParents[152] = 4;
		  detailParents[153] = 4;
		  detailParents[154] = 4;
		  detailParents[155] = 4;
		  detailParents[156] = 4;
		  detailParents[157] = 4;
		  detailParents[158] = 4;
		  detailParents[159] = 4;
		  detailParents[160] = 4;
		  detailParents[161] = 4;
		  detailParents[162] = 4;
		  detailParents[163] = 4;
		  detailParents[164] = 4;
		  detailParents[165] = 4;
		  detailParents[166] = 4;
		  detailParents[167] = 4;
		  detailParents[168] = 4;
		  detailParents[169] = 4;
		  detailParents[170] = 4;
		  detailParents[171] = 4;
		  detailParents[172] = 4;
		  detailParents[173] = 4;
		  detailParents[174] = 4;
		  detailParents[175] = 4;
		  detailParents[176] = 4;
		  detailParents[177] = 4;
		  detailParents[178] = 4;
		  detailParents[179] = 4;
		  detailParents[180] = 4;
		  detailParents[181] = 4;
		  detailParents[182] = 4;
		  detailParents[183] = 4;
		  detailParents[184] = 4;
		  detailParents[185] = 4;
		  detailParents[186] = 4;
		  detailParents[187] = 4;
		  detailParents[188] = 4;
		  detailParents[189] = 4;
		  detailParents[190] = 4;
		  detailParents[191] = 4;
		  detailParents[192] = 4;
		  detailParents[193] = 4;
		  
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
		  
		  col_x_lookup[0][1][1][1][1][1]=66;
		  col_x_lookup[1][0][1][1][1][1]=67;
		  col_x_lookup[1][1][1][1][0][1]=68;
		  col_x_lookup[1][1][1][1][1][0]=69;
		  col_x_lookup[0][1][1][1][0][1]=70;
		  col_x_lookup[1][0][1][1][0][1]=71;
		  col_x_lookup[0][1][1][1][1][0]=72;
		  col_x_lookup[1][0][1][1][1][0]=73;
		  col_x_lookup[0][0][1][1][1][1]=74;
		  col_x_lookup[1][1][1][1][0][0]=75;
		  col_x_lookup[0][0][1][1][0][1]=76;
		  col_x_lookup[0][0][1][1][1][0]=77;
		  col_x_lookup[0][1][1][1][0][0]=78;
		  col_x_lookup[1][0][1][1][0][0]=79;
		  col_x_lookup[0][0][1][1][0][0]=80;
		  col_x_lookup[1][1][1][1][1][1]=81;
		  col_x_lookup[0][1][1][0][1][1]=82;
		  col_x_lookup[1][0][1][0][1][1]=83;
		  col_x_lookup[1][1][1][0][0][1]=84;
		  col_x_lookup[1][1][1][0][1][0]=85;
		  col_x_lookup[0][1][1][0][0][1]=86;
		  col_x_lookup[1][0][1][0][0][1]=87;
		  col_x_lookup[0][1][1][0][1][0]=88;
		  col_x_lookup[1][0][1][0][1][0]=89;
		  col_x_lookup[0][0][1][0][1][1]=90;
		  col_x_lookup[1][1][1][0][0][0]=91;
		  col_x_lookup[0][0][1][0][0][1]=92;
		  col_x_lookup[0][0][1][0][1][0]=93;
		  col_x_lookup[0][1][1][0][0][0]=94;
		  col_x_lookup[1][0][1][0][0][0]=95;
		  col_x_lookup[0][0][1][0][0][0]=96;
		  col_x_lookup[1][1][1][0][1][1]=97;
		  col_x_lookup[0][1][0][1][1][1]=98;
		  col_x_lookup[1][0][0][1][1][1]=99;
		  col_x_lookup[1][1][0][1][0][1]=100;
		  col_x_lookup[1][1][0][1][1][0]=101;
		  col_x_lookup[0][1][0][1][0][1]=102;
		  col_x_lookup[1][0][0][1][0][1]=103;
		  col_x_lookup[0][1][0][1][1][0]=104;
		  col_x_lookup[1][0][0][1][1][0]=105;
		  col_x_lookup[0][0][0][1][1][1]=106;
		  col_x_lookup[1][1][0][1][0][0]=107;
		  col_x_lookup[0][0][0][1][0][1]=108;
		  col_x_lookup[0][0][0][1][1][0]=109;
		  col_x_lookup[0][1][0][1][0][0]=110;
		  col_x_lookup[1][0][0][1][0][0]=111;
		  col_x_lookup[0][0][0][1][0][0]=112;
		  col_x_lookup[1][1][0][1][1][1]=113;
		  col_x_lookup[0][1][0][0][1][1]=114;
		  col_x_lookup[1][0][0][0][1][1]=115;
		  col_x_lookup[1][1][0][0][0][1]=116;
		  col_x_lookup[1][1][0][0][1][0]=117;
		  col_x_lookup[0][1][0][0][0][1]=118;
		  col_x_lookup[1][0][0][0][0][1]=119;
		  col_x_lookup[0][1][0][0][1][0]=120;
		  col_x_lookup[1][0][0][0][1][0]=121;
		  col_x_lookup[0][0][0][0][1][1]=122;
		  col_x_lookup[1][1][0][0][0][0]=123;
		  col_x_lookup[0][0][0][0][0][1]=124;
		  col_x_lookup[0][0][0][0][1][0]=125;
		  col_x_lookup[0][1][0][0][0][0]=126;
		  col_x_lookup[1][0][0][0][0][0]=127;
		  col_x_lookup[0][0][0][0][0][0]=128;
		  col_x_lookup[1][1][0][0][1][1]=129;
		  
		  col_z_lookup[1][1][1][0][1][1]=130;
		  col_z_lookup[1][1][0][1][1][1]=131;
		  col_z_lookup[1][0][1][1][1][1]=132;
		  col_z_lookup[0][1][1][1][1][1]=133;
		  col_z_lookup[1][0][1][0][1][1]=134;
		  col_z_lookup[1][0][0][1][1][1]=135;
		  col_z_lookup[0][1][1][0][1][1]=136;
		  col_z_lookup[0][1][0][1][1][1]=137;
		  col_z_lookup[1][1][0][0][1][1]=138;
		  col_z_lookup[0][0][1][1][1][1]=139;
		  col_z_lookup[1][0][0][0][1][1]=140;
		  col_z_lookup[0][1][0][0][1][1]=141;
		  col_z_lookup[0][0][1][0][1][1]=142;
		  col_z_lookup[0][0][0][1][1][1]=143;
		  col_z_lookup[0][0][0][0][1][1]=144;
		  col_z_lookup[1][1][1][1][1][1]=145;
		  col_z_lookup[1][1][1][0][1][0]=146;
		  col_z_lookup[1][1][0][1][1][0]=147;
		  col_z_lookup[1][0][1][1][1][0]=148;
		  col_z_lookup[0][1][1][1][1][0]=149;
		  col_z_lookup[1][0][1][0][1][0]=150;
		  col_z_lookup[1][0][0][1][1][0]=151;
		  col_z_lookup[0][1][1][0][1][0]=152;
		  col_z_lookup[0][1][0][1][1][0]=153;
		  col_z_lookup[1][1][0][0][1][0]=154;
		  col_z_lookup[0][0][1][1][1][0]=155;
		  col_z_lookup[1][0][0][0][1][0]=156;
		  col_z_lookup[0][1][0][0][1][0]=157;
		  col_z_lookup[0][0][1][0][1][0]=158;
		  col_z_lookup[0][0][0][1][1][0]=159;
		  col_z_lookup[0][0][0][0][1][0]=160;
		  col_z_lookup[1][1][1][1][1][0]=161;
		  col_z_lookup[1][1][1][0][0][1]=162;
		  col_z_lookup[1][1][0][1][0][1]=163;
		  col_z_lookup[1][0][1][1][0][1]=164;
		  col_z_lookup[0][1][1][1][0][1]=165;
		  col_z_lookup[1][0][1][0][0][1]=166;
		  col_z_lookup[1][0][0][1][0][1]=167;
		  col_z_lookup[0][1][1][0][0][1]=168;
		  col_z_lookup[0][1][0][1][0][1]=169;
		  col_z_lookup[1][1][0][0][0][1]=170;
		  col_z_lookup[0][0][1][1][0][1]=171;
		  col_z_lookup[1][0][0][0][0][1]=172;
		  col_z_lookup[0][1][0][0][0][1]=173;
		  col_z_lookup[0][0][1][0][0][1]=174;
		  col_z_lookup[0][0][0][1][0][1]=175;
		  col_z_lookup[0][0][0][0][0][1]=176;
		  col_z_lookup[1][1][1][1][0][1]=177;
		  col_z_lookup[1][1][1][0][0][0]=178;
		  col_z_lookup[1][1][0][1][0][0]=179;
		  col_z_lookup[1][0][1][1][0][0]=180;
		  col_z_lookup[0][1][1][1][0][0]=181;
		  col_z_lookup[1][0][1][0][0][0]=182;
		  col_z_lookup[1][0][0][1][0][0]=183;
		  col_z_lookup[0][1][1][0][0][0]=184;
		  col_z_lookup[0][1][0][1][0][0]=185;
		  col_z_lookup[1][1][0][0][0][0]=186;
		  col_z_lookup[0][0][1][1][0][0]=187;
		  col_z_lookup[1][0][0][0][0][0]=188;
		  col_z_lookup[0][1][0][0][0][0]=189;
		  col_z_lookup[0][0][1][0][0][0]=190;
		  col_z_lookup[0][0][0][1][0][0]=191;
		  col_z_lookup[0][0][0][0][0][0]=192;
		  col_z_lookup[1][1][1][1][0][0]=193;
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
		            	
		            if(childID == 0 || parentID.meta == detailParents[childID] ){
		            	this.mapStateModelLocations.put(iblockstate, this.getModelResourceLocation(iblockstate));
		            }

		        }

		        return this.mapStateModelLocations;
		    }
	}
	  
}
