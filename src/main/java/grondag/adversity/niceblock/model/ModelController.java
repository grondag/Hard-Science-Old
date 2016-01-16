package grondag.adversity.niceblock.model;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;
import grondag.adversity.library.Alternator;
import grondag.adversity.library.IAlternator;
import grondag.adversity.niceblock.NiceSubstance;
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
	 * Index of the first texture to be used. The controller will
	 * assume all textures are offset from this index.
	 */
	public final int textureIndex;

	/**
	 * How many versions of textures are provided in the atlas. (count includes
	 * the first texture) Does not include rotations.
	 */
	public final int alternateTextureCount;
	
	/**
	 * If true, textures on each face can be rotated. Cookbook must still
	 * handle selection of specific textures to match the rotations if the
	 * faces have a visible orientation.
	 */
	public final boolean useRotatedTexturesAsAlternates;

	/**
	 * Total alternate face variants, including rotations.
	 */
	public final int expandedAlternateCount;
	
	/**
	 * Layer in which block faces should render.
	 */
	public final EnumWorldBlockLayer renderLayer;
	
	/**
	 * If false, faces of the block are not shaded according to light levels.
	 */
	public final boolean isShaded;
	
	/**
	 * If true, will use the overlay textures for the substance.
	 * In this case, textureIndex gives the starting offset for overlay textures.
	 */
	public final boolean useOverlayTextures;
	
	protected ModelController(int textureIndex, int alternateCount, boolean useOverlayTextures, EnumWorldBlockLayer renderLayer, boolean isShaded, boolean useRotations){
		this.textureIndex = textureIndex;
		this.alternateTextureCount = Math.max(1, alternateCount);
		this.renderLayer = renderLayer;
		this.isShaded = isShaded;
		this.useOverlayTextures = useOverlayTextures;
		this.useRotatedTexturesAsAlternates = useRotations;
		this.expandedAlternateCount = calcExpanded();
		alternator = Alternator.getAlternator((byte) expandedAlternateCount);
	}
	
	@Override
	public String getParticleTextureName(NiceSubstance substance) {
		return getTextureName(substance, textureIndex);
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
	public boolean canRenderInLayer(EnumWorldBlockLayer layer) {
		return layer == renderLayer;
	}
	
	/**
	 * Looks up and returns the appropriate textures for offset.
	 * For convenience, accepts expanded alternate index values
	 * that occur when using rotations as alternate textures.
	 */
	public ImmutableMap<String, String> getTexturesForExpandedAlternate(NiceSubstance substance, int expanded) {
		
		Map<String, String> textures = Maps.newHashMap();
		textures.put("all", getTextureName(substance, calcAlternate(expanded) + textureIndex));
				
		return ImmutableMap.copyOf(textures);
	}
	
	protected String getTextureName(NiceSubstance substance, int offset) {
		
		String textureName = this.useOverlayTextures && substance.overlayTexture != null
				? substance.overlayTexture : substance.baseTexture;

		int position = this.textureIndex + offset;
				
		return "adversity:blocks/" + textureName + "/" + textureName + "_" + (position >> 3) + "_" + (position & 7);
	}
	
	public String[] getAllTextures(NiceSubstance substance){
		
		final String retVal[] = new String[alternateTextureCount];
		
		for(int alt = 0 ; alt < alternateTextureCount ; alt++){
			retVal[alt] = getTextureName(substance, alt);
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
	
	/**
	 * Texture rotations. Used mainly when rotated textures are used as
	 * alternate textures.
	 */
	protected static enum Rotation {
		ROTATE_NONE(0, 0),
		ROTATE_90(1, 90),
		ROTATE_180(2, 180),
		ROTATE_270(3, 270);

		/**
		 * May be useful for dynamic manipulations.
		 */
		public final int index;

		/**
		 * Useful for locating model file names that use degrees as a suffix.
		 */
		public final int degrees;

		Rotation(int index, int degrees) {
			this.index = index;
			this.degrees = degrees;
		}

	}
}
