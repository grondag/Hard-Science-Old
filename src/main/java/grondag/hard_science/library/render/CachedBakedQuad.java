package grondag.hard_science.library.render;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
class CachedBakedQuad extends BakedQuad
{
    public CachedBakedQuad(int[] vertexDataIn, EnumFacing faceIn, TextureAtlasSprite spriteIn, boolean applyDiffuseLighting, VertexFormat format)
    {
        super(vertexDataIn, -1, faceIn, spriteIn, applyDiffuseLighting, format);
    }

    @Override
    public int hashCode()
    {
        int hash = 1;
        for (int i = 0; i < this.vertexData.length; i++) {
            hash = 31 * hash + this.vertexData[i];
        }
        hash = 31 * hash + (this.format == null ? 0 : this.format.hashCode());
        hash = 31 * hash + (this.sprite == null ? 0 : this.sprite.hashCode());

        int otherStuff = this.applyDiffuseLighting ? 1 : 0;
        otherStuff |= this.face == null ? 0 : this.face.ordinal() << 1;
        otherStuff |= this.tintIndex << 4;

        hash = 31 * hash + otherStuff;
        
        return hash;
    }
    
    @Override
    public boolean equals(Object other)
    {
        if(other instanceof CachedBakedQuad)
        {
            CachedBakedQuad otherQuad = (CachedBakedQuad)other;
            
            int[] v1 = this.vertexData;
            int[] v2 = otherQuad.vertexData;
            if(v1.length != v2.length) return false;
            
            for (int i = 0; i < v1.length; i++) 
            {
                if(v1[i] != v2[i]) return false;
            }
            
            return      this.format == otherQuad.format
                    &&  this.sprite == otherQuad.sprite
                    &&  this.face == otherQuad.face
                    &&  this.applyDiffuseLighting == otherQuad.applyDiffuseLighting
                    &&  this.tintIndex == otherQuad.tintIndex;
        }
        else
        {
            return false;
        }
    }
}
