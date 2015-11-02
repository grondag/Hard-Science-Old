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
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.Properties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import grondag.adversity.Adversity;

import java.util.List;

public class BlockBasalt extends Block {
	
	public static final PropertyEnum PROP_STYLE = PropertyEnum.create("style", EnumStyle.class);
	public static final PropertyBool PROP_PLATE_UP = PropertyBool.create("plate_up");
	public static final PropertyBool PROP_PLATE_DOWN = PropertyBool.create("plate_down");
	public static final PropertyInteger	 PROP_FACE_NORTH = PropertyInteger.create("face_north", 0, 2);
	public static final PropertyInteger	 PROP_FACE_SOUTH = PropertyInteger.create("face_south", 0, 2);
	public static final PropertyInteger	 PROP_FACE_EAST = PropertyInteger.create("face_east", 0, 2);
	public static final PropertyInteger	 PROP_FACE_WEST = PropertyInteger.create("face_west", 0, 2);
	
	private static final int FACE_NONE = 0;
	private static final int FACE_SPLINES = 1;
	private static final int FACE_Y_PLATE = 2;
	

	public BlockBasalt() {
		super(Material.rock);
		this.setHarvestLevel("pickaxe", 2);
		this.setStepSound(soundTypeStone);
		this.setHardness(2);
		this.setResistance(10);
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
		  
		  boolean plate_up = false;
		  boolean plate_down = false;
		  int face_north = FACE_NONE;
		  int face_south = FACE_NONE;
		  int face_east = FACE_NONE;
		  int face_west = FACE_NONE;
		  
		  IBlockState test;
		  
		  switch(style){
		  case COLUMN_Y:
			  
			  // Add top or bottom plates if this at the top or bottom of the column
			  // Otherwise will "see through" gaps between splines.
			  // Want to leave this block non-opaque for rendering efficiency.
			  test = worldIn.getBlockState(pos.up()); 
			  //plate_up = !(test.getBlock() == this && test.getValue(PROP_STYLE) == EnumStyle.COLUMN_Y);
			  test = worldIn.getBlockState(pos.down()); 
			  //plate_down = !(test.getBlock() == this && test.getValue(PROP_STYLE) == EnumStyle.COLUMN_Y);
			  
			  test = worldIn.getBlockState(pos.north()); 
			  face_north = (test.getBlock() == this && test.getValue(PROP_STYLE) == EnumStyle.COLUMN_Y) ? FACE_Y_PLATE : FACE_SPLINES;
		
			  test = worldIn.getBlockState(pos.south()); 
			  face_south = (test.getBlock() == this && test.getValue(PROP_STYLE) == EnumStyle.COLUMN_Y) ? FACE_Y_PLATE : FACE_SPLINES;

			  test = worldIn.getBlockState(pos.east()); 
			  face_east = (test.getBlock() == this && test.getValue(PROP_STYLE) == EnumStyle.COLUMN_Y) ? FACE_Y_PLATE : FACE_SPLINES;

			  test = worldIn.getBlockState(pos.west()); 
			  face_west = (test.getBlock() == this && test.getValue(PROP_STYLE) == EnumStyle.COLUMN_Y) ? FACE_Y_PLATE : FACE_SPLINES;

			  break;
		  default:
			  
		  }

		  return state.withProperty(PROP_PLATE_UP, plate_up).withProperty(PROP_PLATE_DOWN, plate_down)
				  .withProperty(PROP_FACE_NORTH, face_north).withProperty(PROP_FACE_SOUTH, face_south)
				  .withProperty(PROP_FACE_EAST, face_east).withProperty(PROP_FACE_WEST, face_west);

	    
		  // TODO: May need to handle some texture rotation here.
		  // See MathHelper.getPositionRandom(Vec3i pos)
		  // but note intended for client only.

	  }

	  @Override
	  protected BlockState createBlockState()
	  {
//	    return new BlockState(this, new IProperty[] {PROPERTY_STYLE, PROPERTY_TEXTURE_ID, PROPERTY_TEXTURE_ROT});
	    return new BlockState(this, new IProperty[] {PROP_STYLE, PROP_PLATE_UP, PROP_PLATE_DOWN,
	    		PROP_FACE_NORTH, PROP_FACE_SOUTH, PROP_FACE_EAST, PROP_FACE_WEST});
	  }


	  public static enum EnumStyle implements IStringSerializable
	  {
	    ROUGH(0, "rough"),
	    SMOOTH(1, "smooth"),

	    COLUMN_Y(2, "column_y");
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
}
