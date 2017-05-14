package grondag.adversity.gui;


import java.io.IOException;

import grondag.adversity.gui.control.ColorPicker;
import grondag.adversity.network.AdversityMessages;
import grondag.adversity.network.PacketUpdateNiceItemBlock;
import grondag.adversity.niceblock.base.NiceItemBlock;
import grondag.adversity.niceblock.color.BlockColorMapProvider;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public class NiceGuiScreen extends GuiScreen
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
    
    private int colorMapID = 0;
    
    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int clickedMouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, clickedMouseButton);
//        colorPicker.handleMouseInput(mouseX, mouseY);
    }
    
    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
//        colorPicker.handleMouseInput(mouseX, mouseY);
    }
    
    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) 
    {
        super.mouseReleased(mouseX, mouseY, state);
//        colorPicker.handleMouseInput(mouseX, mouseY);
        
        if(this.colorMapID != colorPicker.getColorMapID())
        {
            this.colorMapID = colorPicker.getColorMapID();
            AdversityMessages.INSTANCE.sendToServer(new PacketUpdateNiceItemBlock(this.colorMapID));
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
                
        int margin = (int) (this.height * MARGIN_FACTOR);
        this.xStart = margin;
        this.yStart = margin;
        this.xSize = this.width - (margin - 1) * 2;
        this.ySize = this.height - (margin - 1) * 2;
        
        if(colorPicker == null)
        {
            ItemStack heldItem = mc.player.getHeldItem(EnumHand.MAIN_HAND);
            if (heldItem == null || !(heldItem.getItem() instanceof NiceItemBlock)) 
            {
                // Cannot happen!
                return;
            }
            NiceItemBlock niceItem = (NiceItemBlock)heldItem.getItem();
            this.colorMapID = niceItem.getColorMapID(heldItem);
            
            colorPicker = new ColorPicker(this.xStart + 10, this.yStart + 10, this.ySize - 20);
            colorPicker.setColorMapID(this.colorMapID);
        }
        else
        {
            colorPicker.resize(this.xStart + 10, this.yStart + 10, this.ySize - 20);
        }
        

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawGradientRect(this.xStart, this.yStart, this.xStart + this.xSize, this.yStart + this.ySize, -1072689136, -804253680);

        this.colorPicker.drawControl(this.mc, this.itemRender, mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRenderer, Integer.toString(BlockColorMapProvider.INSTANCE.getColorMapCount()), this.width / 2, this.yStart + 20, 16777215);
        
        int left = this.xStart + this.xSize / 2;
        int top = this.yStart + this.ySize / 4;
        drawRect(left, top , left + this.ySize / 2, top + this.ySize / 2, 
                BlockColorMapProvider.INSTANCE.getColorMap(this.colorPicker.getColorMapID()).getColor(EnumColorMap.BASE));
        
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
