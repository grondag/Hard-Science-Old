package grondag.adversity.niceblock.modelstate;

import java.util.EnumMap;
import java.util.EnumSet;

import grondag.adversity.niceblock.modelstate.ModelAxisFactory.ModelAxis;
import grondag.adversity.niceblock.modelstate.AbstractModelStateComponentFactory.ModelStateComponent;
import grondag.adversity.niceblock.support.BlockTests;
import grondag.adversity.niceblock.support.BlockTests.TestForBigBlockMatch;
import net.minecraft.util.EnumFacing;

/**
 * axis
 * alternate texture index - could be different for diff layers
 * corner join state
 * simple join state
 * flow join state
 * height state
 * big texture model index
 * simple alternate index (hot basalt, for example)
 * base color
 * glow color
 * border color
 * highlight color
 * primitive offset

 * underlying primitive (probably determined by the block)
 * 
 * 
 * views of model state
 * controller - will use a subset, but can have access to whole state
 * dispatcher - needs a cache key based on visual appearance - get from state or controller?
 * niceblock/plus - needs to persist the parts of the state that should be persisted
 *         - can vary based on type of block with same controller (flowing vs. static lava)
 *         - persistence options are meta, world-derived, NBT, cached)
 * multipart/CSG - will have need to persist full state to NBT even if originally not
 * StateProvider - obtains state instance from a key or from appropriate persistence locale
 * 
 * column - axis (meta), color, altTex, cornerJoin
 * bigTex - color, metaVariant (optional), bigTex index
 * border - color, altTex*2, cornerJoin, (meta used to derive cornerjoin but not part of state)
 * color - color, altTex
 * flow - altTex, flowState (meta is used/implied by flowState), color?
 * height - altTex, height (meta), color?
 * masonry - color, altTex*2, simpleJoin
 * cylinder - color, offset, radius, length, cornerOrCenter
 * 
 * some blocks could have different alternate textures per controller
 */
public class ModelStateContainer
{

    public static void testDongle()
    {
        ModelStateSet set = new ModelStateSet(ModelStateComponentType.AXIS, ModelStateComponentType.CORNER_JOIN);
        ModelStateSetValue value = new ModelStateSetValue(set);
        EnumFacing.Axis axis = value.getValue(ModelAxisFactory.INSTANCE);
    }
}
