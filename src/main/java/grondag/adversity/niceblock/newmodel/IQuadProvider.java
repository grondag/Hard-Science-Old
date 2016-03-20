package grondag.adversity.niceblock.newmodel;

import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;

public interface IQuadProvider {
	public List<BakedQuad> getQuads(EnumFacing face);
}
