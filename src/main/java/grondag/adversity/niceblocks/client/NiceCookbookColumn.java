package grondag.adversity.niceblocks.client;

import java.util.Map;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Quat4f;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.niceblocks.NiceBlockStyle;
import grondag.adversity.niceblocks.NiceSubstance;
import grondag.adversity.niceblocks.NiceBlock.TestForCompleteMatch;
import grondag.adversity.niceblocks.NiceBlock.TestForSubstance;
import grondag.adversity.niceblocks.NiceBlock.TestForStyle;
import grondag.adversity.niceblocks.client.NiceCookbook.Ingredients;
import grondag.adversity.niceblocks.client.NiceCookbook.Rotation;
import grondag.adversity.niceblocks.client.NiceCookbook.TextureOffset;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;

public class NiceCookbookColumn extends NiceCookbook{

	private final  Integer[][][][][][] COLUMN_LOOKUP = new Integer[2][2][2][2][2][2];
	
	
	
	
	@Override
	public int getRecipeCount() {
		// TODO Auto-generated method stub
		return 64;
	}


	@Override
	public Ingredients getIngredients(NiceSubstance substance, int recipe,
			int alternate) {
		// TODO Auto-generated method stub
		return super.getIngredients(substance, recipe, alternate);
	}

	


	
	@Override
	public int getModelIndex(IExtendedBlockState state, IBlockAccess worldIn, BlockPos pos) {
		
		NeighborTestResults tests = new NeighborBlocks(worldIn, pos).getNeighborTestResults(new TestForStyle(state));
		
		return  COLUMN_LOOKUP[tests.up?1:0][tests.down?1:0][tests.east?1:0][tests.west?1:0][tests.north?1:0][tests.south?1:0];

	}

	public NiceCookbookColumn (NiceBlockStyle style, EnumFacing.Axis axis){
		super(style);
		
		switch (axis){
		case X:
			COLUMN_LOOKUP[0][1][1][1][1][1]=0;
			COLUMN_LOOKUP[1][0][1][1][1][1]=1;
			COLUMN_LOOKUP[1][1][1][1][0][1]=2;
			COLUMN_LOOKUP[1][1][1][1][1][0]=3;
			COLUMN_LOOKUP[0][1][1][1][0][1]=4;
			COLUMN_LOOKUP[1][0][1][1][0][1]=5;
			COLUMN_LOOKUP[0][1][1][1][1][0]=6;
			COLUMN_LOOKUP[1][0][1][1][1][0]=7;
			COLUMN_LOOKUP[0][0][1][1][1][1]=8;
			COLUMN_LOOKUP[1][1][1][1][0][0]=9;
			COLUMN_LOOKUP[0][0][1][1][0][1]=10;
			COLUMN_LOOKUP[0][0][1][1][1][0]=11;
			COLUMN_LOOKUP[0][1][1][1][0][0]=12;
			COLUMN_LOOKUP[1][0][1][1][0][0]=13;
			COLUMN_LOOKUP[0][0][1][1][0][0]=14;
			COLUMN_LOOKUP[1][1][1][1][1][1]=15;
			COLUMN_LOOKUP[0][1][1][0][1][1]=16;
			COLUMN_LOOKUP[1][0][1][0][1][1]=17;
			COLUMN_LOOKUP[1][1][1][0][0][1]=18;
			COLUMN_LOOKUP[1][1][1][0][1][0]=19;
			COLUMN_LOOKUP[0][1][1][0][0][1]=20;
			COLUMN_LOOKUP[1][0][1][0][0][1]=21;
			COLUMN_LOOKUP[0][1][1][0][1][0]=22;
			COLUMN_LOOKUP[1][0][1][0][1][0]=23;
			COLUMN_LOOKUP[0][0][1][0][1][1]=24;
			COLUMN_LOOKUP[1][1][1][0][0][0]=25;
			COLUMN_LOOKUP[0][0][1][0][0][1]=26;
			COLUMN_LOOKUP[0][0][1][0][1][0]=27;
			COLUMN_LOOKUP[0][1][1][0][0][0]=28;
			COLUMN_LOOKUP[1][0][1][0][0][0]=29;
			COLUMN_LOOKUP[0][0][1][0][0][0]=30;
			COLUMN_LOOKUP[1][1][1][0][1][1]=31;
			COLUMN_LOOKUP[0][1][0][1][1][1]=32;
			COLUMN_LOOKUP[1][0][0][1][1][1]=33;
			COLUMN_LOOKUP[1][1][0][1][0][1]=34;
			COLUMN_LOOKUP[1][1][0][1][1][0]=35;
			COLUMN_LOOKUP[0][1][0][1][0][1]=36;
			COLUMN_LOOKUP[1][0][0][1][0][1]=37;
			COLUMN_LOOKUP[0][1][0][1][1][0]=38;
			COLUMN_LOOKUP[1][0][0][1][1][0]=39;
			COLUMN_LOOKUP[0][0][0][1][1][1]=40;
			COLUMN_LOOKUP[1][1][0][1][0][0]=41;
			COLUMN_LOOKUP[0][0][0][1][0][1]=42;
			COLUMN_LOOKUP[0][0][0][1][1][0]=43;
			COLUMN_LOOKUP[0][1][0][1][0][0]=44;
			COLUMN_LOOKUP[1][0][0][1][0][0]=45;
			COLUMN_LOOKUP[0][0][0][1][0][0]=46;
			COLUMN_LOOKUP[1][1][0][1][1][1]=47;
			COLUMN_LOOKUP[0][1][0][0][1][1]=48;
			COLUMN_LOOKUP[1][0][0][0][1][1]=49;
			COLUMN_LOOKUP[1][1][0][0][0][1]=50;
			COLUMN_LOOKUP[1][1][0][0][1][0]=51;
			COLUMN_LOOKUP[0][1][0][0][0][1]=52;
			COLUMN_LOOKUP[1][0][0][0][0][1]=53;
			COLUMN_LOOKUP[0][1][0][0][1][0]=54;
			COLUMN_LOOKUP[1][0][0][0][1][0]=55;
			COLUMN_LOOKUP[0][0][0][0][1][1]=56;
			COLUMN_LOOKUP[1][1][0][0][0][0]=57;
			COLUMN_LOOKUP[0][0][0][0][0][1]=58;
			COLUMN_LOOKUP[0][0][0][0][1][0]=59;
			COLUMN_LOOKUP[0][1][0][0][0][0]=60;
			COLUMN_LOOKUP[1][0][0][0][0][0]=61;
			COLUMN_LOOKUP[0][0][0][0][0][0]=62;
			COLUMN_LOOKUP[1][1][0][0][1][1]=63;
			break;
			
		case Y:
			COLUMN_LOOKUP[1][1][0][1][1][1]=0;
			COLUMN_LOOKUP[1][1][1][0][1][1]=1;
			COLUMN_LOOKUP[1][1][1][1][0][1]=2;
			COLUMN_LOOKUP[1][1][1][1][1][0]=3;
			COLUMN_LOOKUP[1][1][0][1][0][1]=4;
			COLUMN_LOOKUP[1][1][1][0][0][1]=5;
			COLUMN_LOOKUP[1][1][0][1][1][0]=6;
			COLUMN_LOOKUP[1][1][1][0][1][0]=7;
			COLUMN_LOOKUP[1][1][0][0][1][1]=8;
			COLUMN_LOOKUP[1][1][1][1][0][0]=9;
			COLUMN_LOOKUP[1][1][0][0][0][1]=10;
			COLUMN_LOOKUP[1][1][0][0][1][0]=11;
			COLUMN_LOOKUP[1][1][0][1][0][0]=12;
			COLUMN_LOOKUP[1][1][1][0][0][0]=13;
			COLUMN_LOOKUP[1][1][0][0][0][0]=14;
			COLUMN_LOOKUP[1][1][1][1][1][1]=15;
			COLUMN_LOOKUP[0][1][0][1][1][1]=16;
			COLUMN_LOOKUP[0][1][1][0][1][1]=17;
			COLUMN_LOOKUP[0][1][1][1][0][1]=18;
			COLUMN_LOOKUP[0][1][1][1][1][0]=19;
			COLUMN_LOOKUP[0][1][0][1][0][1]=20;
			COLUMN_LOOKUP[0][1][1][0][0][1]=21;
			COLUMN_LOOKUP[0][1][0][1][1][0]=22;
			COLUMN_LOOKUP[0][1][1][0][1][0]=23;
			COLUMN_LOOKUP[0][1][0][0][1][1]=24;
			COLUMN_LOOKUP[0][1][1][1][0][0]=25;
			COLUMN_LOOKUP[0][1][0][0][0][1]=26;
			COLUMN_LOOKUP[0][1][0][0][1][0]=27;
			COLUMN_LOOKUP[0][1][0][1][0][0]=28;
			COLUMN_LOOKUP[0][1][1][0][0][0]=29;
			COLUMN_LOOKUP[0][1][0][0][0][0]=30;
			COLUMN_LOOKUP[0][1][1][1][1][1]=31;
			COLUMN_LOOKUP[1][0][0][1][1][1]=32;
			COLUMN_LOOKUP[1][0][1][0][1][1]=33;
			COLUMN_LOOKUP[1][0][1][1][0][1]=34;
			COLUMN_LOOKUP[1][0][1][1][1][0]=35;
			COLUMN_LOOKUP[1][0][0][1][0][1]=36;
			COLUMN_LOOKUP[1][0][1][0][0][1]=37;
			COLUMN_LOOKUP[1][0][0][1][1][0]=38;
			COLUMN_LOOKUP[1][0][1][0][1][0]=39;
			COLUMN_LOOKUP[1][0][0][0][1][1]=40;
			COLUMN_LOOKUP[1][0][1][1][0][0]=41;
			COLUMN_LOOKUP[1][0][0][0][0][1]=42;
			COLUMN_LOOKUP[1][0][0][0][1][0]=43;
			COLUMN_LOOKUP[1][0][0][1][0][0]=44;
			COLUMN_LOOKUP[1][0][1][0][0][0]=45;
			COLUMN_LOOKUP[1][0][0][0][0][0]=46;
			COLUMN_LOOKUP[1][0][1][1][1][1]=47;
			COLUMN_LOOKUP[0][0][0][1][1][1]=48;
			COLUMN_LOOKUP[0][0][1][0][1][1]=49;
			COLUMN_LOOKUP[0][0][1][1][0][1]=50;
			COLUMN_LOOKUP[0][0][1][1][1][0]=51;
			COLUMN_LOOKUP[0][0][0][1][0][1]=52;
			COLUMN_LOOKUP[0][0][1][0][0][1]=53;
			COLUMN_LOOKUP[0][0][0][1][1][0]=54;
			COLUMN_LOOKUP[0][0][1][0][1][0]=55;
			COLUMN_LOOKUP[0][0][0][0][1][1]=56;
			COLUMN_LOOKUP[0][0][1][1][0][0]=57;
			COLUMN_LOOKUP[0][0][0][0][0][1]=58;
			COLUMN_LOOKUP[0][0][0][0][1][0]=59;
			COLUMN_LOOKUP[0][0][0][1][0][0]=60;
			COLUMN_LOOKUP[0][0][1][0][0][0]=61;
			COLUMN_LOOKUP[0][0][0][0][0][0]=62;
			COLUMN_LOOKUP[0][0][1][1][1][1]=63;
			break;
			
		case Z:
			COLUMN_LOOKUP[1][1][1][0][1][1]=0;
			COLUMN_LOOKUP[1][1][0][1][1][1]=1;
			COLUMN_LOOKUP[1][0][1][1][1][1]=2;
			COLUMN_LOOKUP[0][1][1][1][1][1]=3;
			COLUMN_LOOKUP[1][0][1][0][1][1]=4;
			COLUMN_LOOKUP[1][0][0][1][1][1]=5;
			COLUMN_LOOKUP[0][1][1][0][1][1]=6;
			COLUMN_LOOKUP[0][1][0][1][1][1]=7;
			COLUMN_LOOKUP[1][1][0][0][1][1]=8;
			COLUMN_LOOKUP[0][0][1][1][1][1]=9;
			COLUMN_LOOKUP[1][0][0][0][1][1]=10;
			COLUMN_LOOKUP[0][1][0][0][1][1]=11;
			COLUMN_LOOKUP[0][0][1][0][1][1]=12;
			COLUMN_LOOKUP[0][0][0][1][1][1]=13;
			COLUMN_LOOKUP[0][0][0][0][1][1]=14;
			COLUMN_LOOKUP[1][1][1][1][1][1]=15;
			COLUMN_LOOKUP[1][1][1][0][1][0]=16;
			COLUMN_LOOKUP[1][1][0][1][1][0]=17;
			COLUMN_LOOKUP[1][0][1][1][1][0]=18;
			COLUMN_LOOKUP[0][1][1][1][1][0]=19;
			COLUMN_LOOKUP[1][0][1][0][1][0]=20;
			COLUMN_LOOKUP[1][0][0][1][1][0]=21;
			COLUMN_LOOKUP[0][1][1][0][1][0]=22;
			COLUMN_LOOKUP[0][1][0][1][1][0]=23;
			COLUMN_LOOKUP[1][1][0][0][1][0]=24;
			COLUMN_LOOKUP[0][0][1][1][1][0]=25;
			COLUMN_LOOKUP[1][0][0][0][1][0]=26;
			COLUMN_LOOKUP[0][1][0][0][1][0]=27;
			COLUMN_LOOKUP[0][0][1][0][1][0]=28;
			COLUMN_LOOKUP[0][0][0][1][1][0]=29;
			COLUMN_LOOKUP[0][0][0][0][1][0]=30;
			COLUMN_LOOKUP[1][1][1][1][1][0]=31;
			COLUMN_LOOKUP[1][1][1][0][0][1]=32;
			COLUMN_LOOKUP[1][1][0][1][0][1]=33;
			COLUMN_LOOKUP[1][0][1][1][0][1]=34;
			COLUMN_LOOKUP[0][1][1][1][0][1]=35;
			COLUMN_LOOKUP[1][0][1][0][0][1]=36;
			COLUMN_LOOKUP[1][0][0][1][0][1]=37;
			COLUMN_LOOKUP[0][1][1][0][0][1]=38;
			COLUMN_LOOKUP[0][1][0][1][0][1]=39;
			COLUMN_LOOKUP[1][1][0][0][0][1]=40;
			COLUMN_LOOKUP[0][0][1][1][0][1]=41;
			COLUMN_LOOKUP[1][0][0][0][0][1]=42;
			COLUMN_LOOKUP[0][1][0][0][0][1]=43;
			COLUMN_LOOKUP[0][0][1][0][0][1]=44;
			COLUMN_LOOKUP[0][0][0][1][0][1]=45;
			COLUMN_LOOKUP[0][0][0][0][0][1]=46;
			COLUMN_LOOKUP[1][1][1][1][0][1]=47;
			COLUMN_LOOKUP[1][1][1][0][0][0]=48;
			COLUMN_LOOKUP[1][1][0][1][0][0]=49;
			COLUMN_LOOKUP[1][0][1][1][0][0]=50;
			COLUMN_LOOKUP[0][1][1][1][0][0]=51;
			COLUMN_LOOKUP[1][0][1][0][0][0]=52;
			COLUMN_LOOKUP[1][0][0][1][0][0]=53;
			COLUMN_LOOKUP[0][1][1][0][0][0]=54;
			COLUMN_LOOKUP[0][1][0][1][0][0]=55;
			COLUMN_LOOKUP[1][1][0][0][0][0]=56;
			COLUMN_LOOKUP[0][0][1][1][0][0]=57;
			COLUMN_LOOKUP[1][0][0][0][0][0]=58;
			COLUMN_LOOKUP[0][1][0][0][0][0]=59;
			COLUMN_LOOKUP[0][0][1][0][0][0]=60;
			COLUMN_LOOKUP[0][0][0][1][0][0]=61;
			COLUMN_LOOKUP[0][0][0][0][0][0]=62;
			COLUMN_LOOKUP[1][1][1][1][0][0]=63;
			break;
		}
	}
	
}
