package grondag.adversity.superblock.block;

import javax.annotation.Nullable;

import grondag.adversity.Output;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;
import grondag.adversity.superblock.model.layout.PaintLayer;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import grondag.adversity.superblock.texture.TexturePalletteProvider.TexturePallette;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ParticleDiggingSuperBlock extends ParticleDigging
{

    /** multiply resulting UVs by this factor to limit samples within a 1-box area of larger textures */
    protected float uvScale;
    
    protected ParticleDiggingSuperBlock(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, IBlockState state, ModelState modelState)
    {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, state);
        int color = modelState.getColorMap(PaintLayer.BASE).getColor(EnumColorMap.BASE);
        this.particleRed = ((color >> 16) & 0xFF) / 255f;
        this.particleGreen = ((color >> 8) & 0xFF) / 255f;
        this.particleBlue = (color & 0xFF) / 255f;
        
        SuperBlock block = (SuperBlock)state.getBlock();
        this.particleAlpha = block.isTranslucent(state) ? modelState.getTranslucency().alpha : 1f;
        TexturePallette tex = modelState.getTexture(PaintLayer.BASE);
        this.particleTexture = tex.getSampleSprite();
        this.uvScale = 1f / tex.textureScale.sliceCount;
    }

    protected void multiplyColor(@Nullable BlockPos p_187154_1_)
    {
        // ignore
    }
    
    /** same as vanilla except for alpha and uvScale*/
    @Override
    public void renderParticle(VertexBuffer buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
    {
        // This code should never matter because values all get replaced below unless particle texture is null
        // Leaving in because modded weirdness...
        float f = ((float)this.particleTextureIndexX + this.particleTextureJitterX / 4.0F) / 16.0F;
        float f1 = f + 0.015609375F;
        float f2 = ((float)this.particleTextureIndexY + this.particleTextureJitterY / 4.0F) / 16.0F;
        float f3 = f2 + 0.015609375F;
        float f4 = 0.1F * this.particleScale;
        
        if (this.particleTexture == null)
        {
            if(Output.DEBUG_MODE)
                Output.warn("Missing particle texture in ParticleDiggingSuperBlock.renderParticle");
        }
        else
        {
            f = this.particleTexture.getInterpolatedU((double)(this.particleTextureJitterX / 4.0F * 16.0F * this.uvScale));
            f1 = this.particleTexture.getInterpolatedU((double)((this.particleTextureJitterX + 1.0F) / 4.0F * 16.0F * this.uvScale));
            f2 = this.particleTexture.getInterpolatedV((double)(this.particleTextureJitterY / 4.0F * 16.0F * this.uvScale));
            f3 = this.particleTexture.getInterpolatedV((double)((this.particleTextureJitterY + 1.0F) / 4.0F * 16.0F * this.uvScale));
            
            if(Output.DEBUG_MODE && (f < this.particleTexture.getMinU() || f1 > this.particleTexture.getMaxU() || f2 < this.particleTexture.getMinV() || f3 > this.particleTexture.getMaxV()))
            {
                Output.warn("UV out of range in ParticleDiggingSuperBlock.renderParticle");
            }
        }

        float f5 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
        float f6 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
        float f7 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);
        int i = this.getBrightnessForRender(partialTicks);
        int j = i >> 16 & 65535;
        int k = i & 65535;
        buffer.pos((double)(f5 - rotationX * f4 - rotationXY * f4), (double)(f6 - rotationZ * f4), (double)(f7 - rotationYZ * f4 - rotationXZ * f4)).tex((double)f, (double)f3).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos((double)(f5 - rotationX * f4 + rotationXY * f4), (double)(f6 + rotationZ * f4), (double)(f7 - rotationYZ * f4 + rotationXZ * f4)).tex((double)f, (double)f2).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos((double)(f5 + rotationX * f4 + rotationXY * f4), (double)(f6 + rotationZ * f4), (double)(f7 + rotationYZ * f4 + rotationXZ * f4)).tex((double)f1, (double)f2).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos((double)(f5 + rotationX * f4 - rotationXY * f4), (double)(f6 - rotationZ * f4), (double)(f7 + rotationYZ * f4 - rotationXZ * f4)).tex((double)f1, (double)f3).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
    }
}
