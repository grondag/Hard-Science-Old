package grondag.adversity.gui.control;

import grondag.adversity.gui.GuiUtil;
import grondag.adversity.gui.GuiUtil.HorizontalAlignment;
import grondag.adversity.gui.GuiUtil.VerticalAlignment;
import grondag.adversity.gui.base.GuiControl;
import grondag.adversity.init.ModBlocks;
import grondag.adversity.superblock.block.SuperBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class MaterialPicker extends GuiControl
{
    /** dimensions are material and toughness */
    private static SuperBlock[][]blocks = new SuperBlock[3][3];
    
    //TODO: localize
    private static final String MATERIAL_LABEL = "Material";
    private static final String TOUGHNESS_LABEL = "Toughness";

    private double boxSize;
    private double spacing;
    
    private int materialIndex = NO_SELECTION;
    private int toughnessIndex = NO_SELECTION;

    static
    {
        blocks[0][0] = (SuperBlock) ModBlocks.supermodel_flexstone;
        blocks[0][1] = (SuperBlock) ModBlocks.supermodel_durastone;
        blocks[0][2] = (SuperBlock) ModBlocks.supermodel_hyperstone;

        blocks[1][0] = (SuperBlock) ModBlocks.supermodel_flexiglass;
        blocks[1][1] = (SuperBlock) ModBlocks.supermodel_duraglass;
        blocks[1][2] = (SuperBlock) ModBlocks.supermodel_hyperglass;
        
        blocks[2][0] = (SuperBlock) ModBlocks.supermodel_flexwood;
        blocks[2][1] = (SuperBlock) ModBlocks.supermodel_durawood;
        blocks[2][2] = (SuperBlock) ModBlocks.supermodel_hyperwood;
    }
    
    public MaterialPicker()
    {
        this.setAspectRatio(2.0 / 7.0);
    }
    
    public void setBlock(SuperBlock block)
    {
        this.materialIndex = NO_SELECTION;
        this.toughnessIndex = NO_SELECTION;
        
        for(int i = 0; i < 3; i++)
        {
            for(int j = 0; j < 3; j++)
            {
                if(block == blocks[i][j])
                {
                    this.materialIndex = i;
                    this.toughnessIndex = j;
                    return;
                }
            }
        }
    }
    
    public SuperBlock getBlock()
    {
        if(this.materialIndex == NO_SELECTION || this.toughnessIndex == NO_SELECTION) return null;
        return blocks[this.materialIndex][this.toughnessIndex];
    }
    
    private int getMouseIndex(int mouseX, int mouseY)
    {
        if(mouseX < this.left || mouseX > this.right || mouseY < (this.top + this.height / 2) || mouseY > this.bottom) return NO_SELECTION;
        
        int x = (int) (this.left + boxSize);
        if( mouseX < x) return 0;
        
        x += this.spacing;
        if(mouseX < x) return NO_SELECTION;
        
        x += this.boxSize;
        if(mouseX < x) return 1;
        
        x += this.spacing;
        if(mouseX < x) return NO_SELECTION;
        
        x += this.boxSize;
        if(mouseX < x) return 2;
        
        x += this.spacing + this.spacing;
        if(mouseX < x) return NO_SELECTION;
        
        x += this.boxSize;
        if(mouseX < x) return 3;
        
        x += this.spacing;
        if(mouseX < x) return NO_SELECTION;
        
        x += this.boxSize;
        if(mouseX < x) return 4;
        
        x += this.spacing;
        if(mouseX < x) return NO_SELECTION;
        
        return 5;
    }
    
    @Override
    protected void drawContent(Minecraft mc, RenderItem itemRender, int mouseX, int mouseY, float partialTicks)
    {
        double halfWidth = this.width / 2;
        double halfHeight = this.height / 2;
        double xMiddle = this.left + halfWidth;
        double yMiddle = this.top + halfHeight;
        
        GuiUtil.drawAlignedStringNoShadow(mc.fontRenderer, MATERIAL_LABEL, (float) this.left, (float) this.top, 
                (float) (halfWidth - this.spacing), (float) halfHeight, TEXT_COLOR_LABEL, HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE);
        
        GuiUtil.drawAlignedStringNoShadow(mc.fontRenderer, TOUGHNESS_LABEL, (float) (xMiddle + spacing), (float) this.top, 
                (float) (halfWidth - this.spacing), (float) halfHeight, TEXT_COLOR_LABEL, HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE);
        
        int mouseIndex = this.getMouseIndex(mouseX, mouseY);
        if(mouseIndex != NO_SELECTION)
        {
            double highlightX = this.left + mouseIndex * (this.boxSize + this.spacing);
            if(mouseIndex > 2) highlightX += this.spacing;
            
            GuiUtil.drawRect(highlightX + 1, yMiddle + 1, highlightX + this.boxSize - 1, this.bottom - 1, GuiControl.BUTTON_COLOR_FOCUS);
        }
        
        if(this.materialIndex != NO_SELECTION)
        {
            GuiUtil.drawBoxWidthHeight(this.left + this.materialIndex * (this.boxSize + this.spacing), yMiddle, boxSize, boxSize, 1, GuiControl.BUTTON_COLOR_ACTIVE);
        }
        
        if(this.toughnessIndex != NO_SELECTION)
        {
            GuiUtil.drawBoxWidthHeight(xMiddle + this.spacing + this.toughnessIndex * (this.boxSize + this.spacing), yMiddle, boxSize, boxSize, 1, GuiControl.BUTTON_COLOR_ACTIVE);
        }

        
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
        
        double x = this.left + 1;
        double y = yMiddle + 1;
        double size = this.boxSize - 2;
        
        GuiUtil.renderItemAndEffectIntoGui(mc, itemRender, new ItemStack(Item.getItemFromBlock(Blocks.STONE)), x , y, size);
        x += (this.boxSize + this.spacing);
        
        GuiUtil.renderItemAndEffectIntoGui(mc, itemRender, new ItemStack(Item.getItemFromBlock(Blocks.GLASS)), x , y, size);
        x += (this.boxSize + this.spacing);

        GuiUtil.renderItemAndEffectIntoGui(mc, itemRender, new ItemStack(Item.getItemFromBlock(Blocks.LOG)), x , y, size);
        x += (this.boxSize + this.spacing + this.spacing);

        GuiUtil.renderItemAndEffectIntoGui(mc, itemRender, new ItemStack(Items.STONE_PICKAXE), x , y, size);
        x += (this.boxSize + this.spacing);
        
        GuiUtil.renderItemAndEffectIntoGui(mc, itemRender, new ItemStack(Items.IRON_PICKAXE), x , y, size);
        x += (this.boxSize + this.spacing);

        GuiUtil.renderItemAndEffectIntoGui(mc, itemRender, new ItemStack(Items.DIAMOND_PICKAXE), x , y, size);
    }

    @Override
    protected void handleCoordinateUpdate()
    {
        this.boxSize = this.height / 2;
        this.spacing = (this.width - (boxSize * 6)) / 6;
        
    }

    @Override
    protected void handleMouseClick(Minecraft mc, int mouseX, int mouseY)
    {
        int mouseIndex = this.getMouseIndex(mouseX, mouseY);
        if(mouseIndex == NO_SELECTION) return;
        
        if(mouseIndex < 3)
        {
            this.materialIndex = mouseIndex;
        }
        else
        {
            this.toughnessIndex = mouseIndex - 3;
        }
    }

    @Override
    protected void handleMouseDrag(Minecraft mc, int mouseX, int mouseY)
    {
        this.handleMouseClick(mc, mouseX, mouseY);
    }

    @Override
    protected void handleMouseScroll(int mouseX, int mouseY, int scrollDelta)
    {
        // ignore
    }

}
