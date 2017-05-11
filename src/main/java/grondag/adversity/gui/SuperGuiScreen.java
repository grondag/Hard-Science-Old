package grondag.adversity.gui;


import java.io.IOException;

import org.lwjgl.opengl.GL11;

import grondag.adversity.network.AdversityMessages;
import grondag.adversity.network.PacketUpdateSuperModelBlock;
import grondag.adversity.niceblock.color.BlockColorMapProvider;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;
import grondag.adversity.superblock.block.SuperItemBlock;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public class SuperGuiScreen extends GuiScreen
{
//    /** The X size of the window in pixels. */
//    protected int xSize = 360;
//    /** The Y size of the window in pixels. */
//    protected int ySize = 46;
    
    private final float MARGIN_FACTOR = 0.2F;
    private int xStart;
    private int yStart;
    private int xSize;
    private int ySize;

    private ColorPicker colorPicker;
    
    private int meta = 0;
    private ModelState modelState = null;
    private ItemStack heldItem;
    
    private boolean hasUpdates = false;
    
    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int clickedMouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, clickedMouseButton);
        colorPicker.handleMouseInput(mouseX, mouseY);
        if(this.modelState != null && this.modelState.getColorMap(0).ordinal != colorPicker.getColorMapID())
        {
            this.modelState.setColorMap(0, BlockColorMapProvider.INSTANCE.getColorMap(colorPicker.getColorMapID()));
            SuperItemBlock.setModelState(heldItem, modelState);
            this.hasUpdates = true;
        }
    }
    
    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        colorPicker.handleMouseInput(mouseX, mouseY);
        if(this.modelState != null && this.modelState.getColorMap(0).ordinal != colorPicker.getColorMapID())
        {
            this.modelState.setColorMap(0, BlockColorMapProvider.INSTANCE.getColorMap(colorPicker.getColorMapID()));
            SuperItemBlock.setModelState(heldItem, modelState);
            this.hasUpdates = true;
        }
    }
    
    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) 
    {
        super.mouseReleased(mouseX, mouseY, state);
        colorPicker.handleMouseInput(mouseX, mouseY);
        
        // todo - put into shared routine
        if(this.modelState != null && this.modelState.getColorMap(0).ordinal != colorPicker.getColorMapID())
        {
            this.modelState.setColorMap(0, BlockColorMapProvider.INSTANCE.getColorMap(colorPicker.getColorMapID()));
            SuperItemBlock.setModelState(heldItem, modelState);
            this.hasUpdates = true;
        }
        
        // todo - create an apply button - so can escape out and not save changes

        if(this.hasUpdates)
        {
            AdversityMessages.INSTANCE.sendToServer(new PacketUpdateSuperModelBlock(this.meta, this.modelState));
            this.hasUpdates = false;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);
    }
    
    @Override
    public void initGui()
    {
        super.initGui();
        
        this.heldItem = mc.player.getHeldItem(EnumHand.MAIN_HAND).copy();
                
        int margin = (int) (this.height * MARGIN_FACTOR);
        this.xStart = margin;
        this.yStart = margin;
        this.xSize = this.width - (margin - 1) * 2;
        this.ySize = this.height - (margin - 1) * 2;
        
        if(colorPicker == null)
        {
            
            if (this.heldItem == null || !(this.heldItem.getItem() instanceof SuperItemBlock)) 
            {
                // Cannot happen!
                return;
            }
            this.meta = this.heldItem.getMetadata();
            this.modelState = SuperItemBlock.getModelState(heldItem);
            
            colorPicker = new ColorPicker(this.xStart + 80, this.yStart + 10, 40);
            colorPicker.setColorMapID(this.modelState.getColorMap(0).ordinal);
        }
        else
        {
            colorPicker.resize(this.xStart + 80, this.yStart + 10, 40);
        }
        

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawGradientRect(this.xStart, this.yStart, this.xStart + this.xSize, this.yStart + this.ySize, -1072689136, -804253680);

        this.colorPicker.drawControl(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRenderer, Integer.toString(BlockColorMapProvider.INSTANCE.getColorMapCount()), this.width / 2, this.yStart + 20, 16777215);
        
        if(this.heldItem != null)
        {
            GuiUtil.renderItemAndEffectIntoGui(mc, itemRender, heldItem, (this.xStart + 10), (this.yStart + 10), 4);
//            GL11.glPushMatrix();
//            GL11.glScalef(2, 2, 1);
//            this.itemRender.renderItemAndEffectIntoGUI(this.heldItem, left + this.ySize / 2, top + this.ySize / 2);
//            GL11.glPopMatrix();
        }
        
//        drawRect(left, top , left + this.ySize / 2, top + this.ySize / 2, 
//                BlockColorMapProvider.INSTANCE.getColorMap(this.colorPicker.getColorMapID()).getColor(EnumColorMap.BASE));
        
        super.drawScreen(mouseX, mouseY, partialTicks);
 
    }
//    private static void drawLine(int x1, int y1, int x2, int y2, int color) {
//        float f3 = (color >> 24 & 255) / 255.0F;
//        float f = (color >> 16 & 255) / 255.0F;
//        float f1 = (color >> 8 & 255) / 255.0F;
//        float f2 = (color & 255) / 255.0F;
//        Tessellator tessellator = Tessellator.getInstance();
//        VertexBuffer buffer = tessellator.getBuffer();
//
//        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
//        GlStateManager.enableBlend();
//        GlStateManager.disableTexture2D();
//        GlStateManager.disableDepth();
//        GL11.glLineWidth(2.0f);
//        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
//        GlStateManager.color(f, f1, f2, f3);
//        buffer.pos(x1, y1, 0.0D).endVertex();
//        buffer.pos(x2, y2, 0.0D).endVertex();
//        tessellator.draw();
//        GlStateManager.enableTexture2D();
//        GlStateManager.enableDepth();
//        GlStateManager.disableBlend();
//    }

    @Override
    public void drawBackground(int tint)
    {
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        this.mc.getTextureManager().bindTexture(OPTIONS_BACKGROUND);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
//        float f = 32.0F;
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        vertexbuffer.pos(0.0D, (double)this.height, 0.0D).tex(0.0D, (double)((float)this.height / 32.0F + (float)tint)).color(64, 64, 64, 255).endVertex();
        vertexbuffer.pos((double)this.width, (double)this.height, 0.0D).tex((double)((float)this.width / 32.0F), (double)((float)this.height / 32.0F + (float)tint)).color(64, 64, 64, 255).endVertex();
        vertexbuffer.pos((double)this.width, 0.0D, 0.0D).tex((double)((float)this.width / 32.0F), (double)tint).color(64, 64, 64, 255).endVertex();
        vertexbuffer.pos(0.0D, 0.0D, 0.0D).tex(0.0D, (double)tint).color(64, 64, 64, 255).endVertex();
        tessellator.draw();
    }
}
