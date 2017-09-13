package grondag.hard_science.gui.control.machine;

import grondag.hard_science.gui.control.machine.RenderBounds.RadialRenderBounds;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class RadialGaugeSpec extends RadialRenderBounds
{
    /**
     * Index of resource in TileEntity machine buffer manager
     */
    public final int bufferIndex;

    /**
     * Color for render of level gauge.
     */
    public final int color;

    /**
     * MC texture sprite for center of gauge
     */
    public final TextureAtlasSprite sprite;

    public final double spriteScale;
    public final double spriteLeft;
    public final double spriteTop;
    public final double spriteSize;

    /** 
     * SpriteScale is multiplied by radius to get size for rendering the sprite. 
     * Value of 0.75 is normal rendering size for a square block texture.
     * Some smaller item textures are easier to read if rendered a little bigger.
     */
    public RadialGaugeSpec(int bufferIndex, RadialRenderBounds bounds, double spriteScale, TextureAtlasSprite sprite, int color)
    {
        super(bounds.centerX, bounds.centerY, bounds.radius);
        this.spriteScale = spriteScale;
        this.spriteSize = this.radius() * spriteScale;
        this.spriteLeft = this.centerX() - this.spriteSize / 2.0;
        this.spriteTop = this.centerY() - this.spriteSize / 2.0;
        this.bufferIndex = bufferIndex;
        this.color = color;
        this.sprite = sprite;
    }

}