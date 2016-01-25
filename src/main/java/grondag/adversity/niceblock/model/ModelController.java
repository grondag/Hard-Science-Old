package grondag.adversity.niceblock.model;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;
import grondag.adversity.library.Alternator;
import grondag.adversity.library.IAlternator;
import grondag.adversity.niceblock.NiceBlock;
import grondag.adversity.niceblock.support.ICollisionHandler;

public abstract class ModelController implements IModelController{

	/**
	 * Randomizer for texture alternates. These are cached in Alternator class, so
	 * no significant cost to keeping a reference in each controller. Necessary
	 * because Vanilla alternate function apparently uses block state as input,
	 * causing textures to vary for the same position as extended state changes,
	 * which gives a jarring effect in some cases. Alternator only uses BlockPos
	 * as input.
	 */
	protected final IAlternator alternator;
	
	/**
	 * Folder and prefix for textures.  Will use first texture as starting point
	 */
	protected final String textureName;
	
	
	/** How many texture are in a complete set of textures used by controller.
	 * Total number of textures to be loaded will be textureCount * alternateTextureCount
	 */
	protected int textureCount = 1;
	
	/**
	 * How many versions of textures are provided in the atlas. (count includes
	 * the first texture) Does not include rotations.
	 */
	protected final int alternateTextureCount;
	
	/**
	 * If true, textures on each face can be rotated. Cookbook must still
	 * handle selection of specific textures to match the rotations if the
	 * faces have a visible orientation.
	 */
	protected final boolean useRotatedTexturesAsAlternates;

	/**
	 * Total alternate face variants, including rotations.
	 */
	protected final int expandedAlternateCount;
	
	/**
	 * Layer in which block faces should render.
	 */
	protected final EnumWorldBlockLayer renderLayer;
	
	/**
	 * If false, faces of the block are not shaded according to light levels.
	 */
	public final boolean isShaded;
			
	protected ModelController(String textureName, int alternateCount, EnumWorldBlockLayer renderLayer, boolean isShaded, boolean useRotations){
		this.textureName = textureName;
		this.alternateTextureCount = Math.max(1, alternateCount);
		this.renderLayer = renderLayer;
		this.isShaded = isShaded;
		this.useRotatedTexturesAsAlternates = useRotations;
		this.expandedAlternateCount = calcExpanded();
		alternator = Alternator.getAlternator((byte) expandedAlternateCount);
	}
	
	@Override
	public String getFirstTextureName(int meta) {
		return this.getTextureName(meta, 0);
	}
  
	@Override
	public ICollisionHandler getCollisionHandler() {
		return null;
	}

	/**
	 * Used to provide extended state to NiceBlock.
	 * Separate and public to enable compound controllers 
	 * that aggregate state from multiple controllers.
	 */
	public int getVariantID(IExtendedBlockState state, IBlockAccess worldIn, BlockPos pos) {
		return alternator.getAlternate(pos);
	}
	
	@Override
	public ModelRenderState getRenderState(IExtendedBlockState state, IBlockAccess world, BlockPos pos) {
		return new ModelRenderState(getVariantID((IExtendedBlockState) state, world, pos), -1);
	}

	
	@Override
	public boolean canRenderInLayer(EnumWorldBlockLayer layer) {
		return layer == renderLayer;
	}
	
	/**
	 * Looks up and returns the appropriate textures for offset.
	 * For convenience, accepts expanded alternate index values
	 * that occur when using rotations as alternate textures.
	 */
	public ImmutableMap<String, String> getTexturesForExpandedAlternate(int meta, int expanded) {
		
		Map<String, String> textures = Maps.newHashMap();
		textures.put("all", this.getTextureName(meta, calcAlternate(expanded)));
				
		return ImmutableMap.copyOf(textures);
	}
	
	protected String getTextureName(int meta, int offset) {
		return "adversity:blocks/" + textureName + "_" + (offset >> 3) + "_" + (offset & 7);
	}
	
	@Override
	public String[] getAllTextures(int meta){
		
		final String retVal[] = new String[alternateTextureCount * textureCount];
		
		for(int i = 0 ; i < alternateTextureCount * textureCount ; i++){
			retVal[i] = this.getTextureName(meta, i);
		}
		return retVal;
	}
	
	/**
	 * Creates an expanded alternate count when we use rotations as alternates
	 * pairs with calcRotation and calcAlternate
	 * */
	protected final int calcExpanded() {
		if (useRotatedTexturesAsAlternates) {
			return alternateTextureCount * 4;
		} else {
			return alternateTextureCount;
		}
	}

	/**
	 * retrieves alternate index from expanded value see calcExpanded
	 */
	protected final int calcAlternate(int expanded) {
		if (useRotatedTexturesAsAlternates) {
			return expanded / 4;
		} else {
			return expanded;
		}
	}

	/**
	 * retrieves rotation from expanded value see calcExpanded
	 */
	protected final Rotation calcRotation(int expanded) {
		if (useRotatedTexturesAsAlternates) {
			return Rotation.values()[expanded & 3];
		} else {
			return Rotation.ROTATE_NONE;
		}
	}

}
