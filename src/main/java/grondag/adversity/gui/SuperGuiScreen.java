package grondag.adversity.gui;


import static grondag.adversity.gui.base.GuiControl.*;

import java.io.IOException;
import java.util.ArrayList;

import grondag.adversity.gui.base.TabBar;
import grondag.adversity.gui.control.Button;
import grondag.adversity.gui.control.ColorPicker;
import grondag.adversity.gui.control.ItemPreview;
import grondag.adversity.gui.control.TexturePicker;
import grondag.adversity.network.AdversityMessages;
import grondag.adversity.network.PacketUpdateSuperModelBlock;
import grondag.adversity.niceblock.color.BlockColorMapProvider;
import grondag.adversity.superblock.block.SuperItemBlock;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import grondag.adversity.superblock.texture.TextureLayout;
import grondag.adversity.superblock.texture.TexturePalletteProvider.TexturePallette;
import grondag.adversity.superblock.texture.Textures;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumHand;

public class SuperGuiScreen extends GuiScreen
{
//    /** The X size of the window in pixels. */
//    protected int xSize = 360;
//    /** The Y size of the window in pixels. */
//    protected int ySize = 46;
    

    private static final float MARGIN_FACTOR = 0.2F;

    
    private static final int BUTTON_ID_CANCEL = 0;
    private static final int BUTTON_ID_ACCEPT = 1;
    
    //TODO: localize
    private static final String STR_ACCEPT = "Accept";
    private static final String STR_CANCEL = "Cancel";
    
    private int xStart;
    private int yStart;
    private int xSize;
    private int ySize;

    private ColorPicker colorPicker;
    private ItemPreview itemPreview;
    private TexturePicker textureTabBar;
    
    private int meta = 0;
    private ModelState modelState = null;
    
    private boolean hasUpdates = false;
    
    private int buttonWidth;
    private int buttonHeight;
    
    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    private void updateItemPreviewState()
    {
        // abort on strangeness
        if(this.modelState == null) return;

        if(this.modelState.getColorMap(0).ordinal != colorPicker.getColorMapID())
        {
            this.modelState.setColorMap(0, BlockColorMapProvider.INSTANCE.getColorMap(colorPicker.getColorMapID()));
            this.textureTabBar.colorMap = BlockColorMapProvider.INSTANCE.getColorMap(colorPicker.getColorMapID());
            SuperItemBlock.setModelState(this.itemPreview.previewItem, modelState);
            this.hasUpdates = true;
        }
        
        if(this.modelState.getTexture(0) != this.textureTabBar.getSelected())
        {
            this.modelState.setTexture(0, this.textureTabBar.getSelected());
            SuperItemBlock.setModelState(this.itemPreview.previewItem, modelState);
            this.hasUpdates = true;
        }
    }
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int clickedMouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, clickedMouseButton);
        colorPicker.handleMouseClick(this.mc, mouseX, mouseY);
        this.textureTabBar.handleMouseClick(this.mc, mouseX, mouseY);
        updateItemPreviewState();
    }
    
    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        colorPicker.handleMouseDrag(this.mc, mouseX, mouseY);
        this.textureTabBar.handleMouseDrag(this.mc, mouseX, mouseY);
        updateItemPreviewState();
    }
    
//    @Override
//    protected void mouseReleased(int mouseX, int mouseY, int state) 
//    {
//        super.mouseReleased(mouseX, mouseY, state);
//
//        updateItemPreviewState();
//    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if(this.hasUpdates && button.id == BUTTON_ID_ACCEPT)
        {
            AdversityMessages.INSTANCE.sendToServer(new PacketUpdateSuperModelBlock(this.meta, this.modelState));
            this.hasUpdates = false;
        }
        this.mc.displayGuiScreen((GuiScreen)null);
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
        
        FontRenderer fr = this.mc.fontRenderer;
        this.buttonWidth = Math.max(fr.getStringWidth(STR_ACCEPT), fr.getStringWidth(STR_CANCEL)) + CONTROL_INTERNAL_MARGIN + CONTROL_INTERNAL_MARGIN;
        this.buttonHeight = fr.FONT_HEIGHT + CONTROL_INTERNAL_MARGIN + CONTROL_INTERNAL_MARGIN;
        
        int buttonTop = this.yStart + this.ySize - this.buttonHeight - CONTROL_EXTERNAL_MARGIN;
       
        // buttons are cleared by super each time
        this.addButton(new Button(BUTTON_ID_ACCEPT, this.xStart + CONTROL_EXTERNAL_MARGIN, buttonTop, this.buttonWidth, this.buttonHeight, STR_ACCEPT));
        this.addButton(new Button(BUTTON_ID_CANCEL, this.xStart + CONTROL_EXTERNAL_MARGIN * 2 + this.buttonWidth, buttonTop, this.buttonWidth, this.buttonHeight, STR_CANCEL));
        
        if(this.itemPreview == null)
        {
            this.itemPreview = new ItemPreview(this.xStart, this.yStart, 80);
//            this.itemPreview.setBackgroundColor(CONTROL_BACKGROUND);
//            this.itemPreview.setInnerMargin(CONTROL_INTERNAL_MARGIN);
//            this.itemPreview.setOuterMargin(CONTROL_EXTERNAL_MARGIN);
            this.itemPreview.previewItem = mc.player.getHeldItem(EnumHand.MAIN_HAND).copy();
            
            if (this.itemPreview.previewItem == null || !(this.itemPreview.previewItem.getItem() instanceof SuperItemBlock)) 
            {
                // Abort on strangeness
                return;
            }
            this.meta = this.itemPreview.previewItem.getMetadata();
            this.modelState = SuperItemBlock.getModelState(itemPreview.previewItem);
        }
        else
        {
            this.itemPreview.resize(this.xStart, this.yStart, 80);
        }
        
        // abort on strangeness
        if(this.modelState == null) return;
        
        if(this.textureTabBar == null)
        {
            ArrayList<TexturePallette> textures = new ArrayList<TexturePallette>();
            for(TexturePallette tex : Textures.ALL_TEXTURES)
            {
                if(tex.textureLayout == TextureLayout.BIGTEX || tex.textureLayout == TextureLayout.SPLIT_X_8)
                {
                    textures.add(tex);
                }
            }
            
            this.textureTabBar = new TexturePicker(textures, this.xStart + CONTROL_EXTERNAL_MARGIN, this.yStart + 100, 200, 48 );
            this.textureTabBar.setSelected(this.modelState.getTexture(0));
            this.textureTabBar.showSelected();
            this.textureTabBar.colorMap = this.modelState.getColorMap(0);
        }
        else
        {
            this.textureTabBar.resize(this.xStart + CONTROL_EXTERNAL_MARGIN, this.yStart + 100, 200, 48);
        }
        
        if(this.colorPicker == null)
        {
            
            this.colorPicker = new ColorPicker(this.xStart + 80, this.yStart + 10, 40);
            this.colorPicker.setColorMapID(this.modelState.getColorMap(0).ordinal);
//            this.colorPicker.setBackgroundColor(CONTROL_BACKGROUND);
//            this.colorPicker.setInnerMargin(CONTROL_INTERNAL_MARGIN);
//            this.colorPicker.setOuterMargin(CONTROL_EXTERNAL_MARGIN);
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

        this.colorPicker.drawControl(this.mc, this.itemRender, mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRenderer, Integer.toString(BlockColorMapProvider.INSTANCE.getColorMapCount()), this.width / 2, this.yStart + 20, 16777215);
        
//        drawRect(left, top , left + this.ySize / 2, top + this.ySize / 2, 
//                BlockColorMapProvider.INSTANCE.getColorMap(this.colorPicker.getColorMapID()).getColor(EnumColorMap.BASE));
        
        this.itemPreview.drawControl(this.mc, this.itemRender, mouseX, mouseY, partialTicks);
        
        this.textureTabBar.drawControl(mc, itemRender, mouseX, mouseY, partialTicks);
        
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
