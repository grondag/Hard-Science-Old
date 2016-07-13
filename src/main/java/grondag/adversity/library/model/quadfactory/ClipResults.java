package grondag.adversity.library.model.quadfactory;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;

public class ClipResults
{
    public List<RawQuad> facePatches = new ArrayList<RawQuad>(4);
    public List<RawQuad> clippedQuads = new ArrayList<RawQuad>(4);
    
    public List<BakedQuad> createNormalQuads(boolean includePatches)
    {
        ArrayList<BakedQuad> retVal = new ArrayList<BakedQuad>(8);
        
        clippedQuads.forEach((quad) -> retVal.add(quad.createBakedQuad()));

        if (includePatches) facePatches.forEach((quad) -> retVal.add(quad.createBakedQuad()));
        
        return retVal;
        
    }
}