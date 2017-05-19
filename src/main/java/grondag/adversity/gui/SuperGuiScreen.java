package grondag.adversity.gui;


import static grondag.adversity.gui.base.GuiControl.*;

import java.io.IOException;
import java.util.ArrayList;

import grondag.adversity.gui.base.GuiControl;
import grondag.adversity.gui.control.Button;
import grondag.adversity.gui.control.ColorPicker;
import grondag.adversity.gui.control.ItemPreview;
import grondag.adversity.gui.control.Panel;
import grondag.adversity.gui.control.TexturePicker;
import grondag.adversity.network.AdversityMessages;
import grondag.adversity.network.PacketUpdateSuperModelBlock;
import grondag.adversity.niceblock.color.BlockColorMapProvider;
import grondag.adversity.superblock.block.SuperItemBlock;
import grondag.adversity.superblock.model.painter.SurfacePainter;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import grondag.adversity.superblock.texture.TextureLayout;
import grondag.adversity.superblock.texture.TexturePalletteProvider.TexturePallette;
import grondag.adversity.superblock.texture.TextureScale;
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
    
    private Panel mainPanel;
    
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
            TexturePallette tex = this.textureTabBar.getSelected();
            //TODO: ugly, need a lookup function from texture to painter
            this.modelState.setSurfacePainter(0, tex.textureScale == TextureScale.SINGLE ? SurfacePainter.CUBIC_TILES : SurfacePainter.CUBIC_BIGTEX);
            this.modelState.setTexture(0, tex);
            SuperItemBlock.setModelState(this.itemPreview.previewItem, modelState);
            this.hasUpdates = true;
        }
    }
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int clickedMouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, clickedMouseButton);
        colorPicker.mouseClick(this.mc, mouseX, mouseY);
        this.textureTabBar.mouseClick(this.mc, mouseX, mouseY);
        updateItemPreviewState();
    }
    
    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        colorPicker.mouseDrag(this.mc, mouseX, mouseY);
        this.textureTabBar.mouseDrag(this.mc, mouseX, mouseY);
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
        this.ySize = this.height - (margin - 1) * 2;
        this.yStart = margin;
        this.xSize = (int) (this.ySize * GuiUtil.GOLDEN_RATIO);
        this.xStart = (this.width - this.xSize) / 2;
        
        FontRenderer fr = this.mc.fontRenderer;
        this.buttonWidth = Math.max(fr.getStringWidth(STR_ACCEPT), fr.getStringWidth(STR_CANCEL)) + CONTROL_INTERNAL_MARGIN + CONTROL_INTERNAL_MARGIN;
        this.buttonHeight = fr.FONT_HEIGHT + CONTROL_INTERNAL_MARGIN + CONTROL_INTERNAL_MARGIN;
        
        int buttonTop = this.yStart + this.ySize - this.buttonHeight - CONTROL_EXTERNAL_MARGIN;
        int buttonLeft = this.xStart + this.xSize - CONTROL_EXTERNAL_MARGIN * 2 - this.buttonWidth * 2;
       
        // buttons are cleared by super each time
        this.addButton(new Button(BUTTON_ID_ACCEPT, buttonLeft, buttonTop, this.buttonWidth, this.buttonHeight, STR_ACCEPT));
        this.addButton(new Button(BUTTON_ID_CANCEL, buttonLeft + CONTROL_EXTERNAL_MARGIN + this.buttonWidth, buttonTop, this.buttonWidth, this.buttonHeight, STR_CANCEL));
        
        if(this.itemPreview == null)
        {
            this.itemPreview = new ItemPreview();
            this.itemPreview.previewItem = mc.player.getHeldItem(EnumHand.MAIN_HAND).copy();
            
            if (this.itemPreview.previewItem == null || !(this.itemPreview.previewItem.getItem() instanceof SuperItemBlock)) 
            {
                // Abort on strangeness
                return;
            }
            this.meta = this.itemPreview.previewItem.getMetadata();
            this.modelState = SuperItemBlock.getModelState(itemPreview.previewItem);
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
            
            this.textureTabBar = new TexturePicker(textures, this.xStart + CONTROL_EXTERNAL_MARGIN, this.yStart + 100);
            this.textureTabBar.setHeight(48);
            this.textureTabBar.setWidth(120);
            this.textureTabBar.setSelected(this.modelState.getTexture(0));
            this.textureTabBar.showSelected();
            this.textureTabBar.colorMap = this.modelState.getColorMap(0);
        }
        
        if(this.colorPicker == null)
        {
            this.colorPicker = new ColorPicker();
            this.colorPicker.setColorMapID(this.modelState.getColorMap(0).ordinal);
        }
       
        if(this.mainPanel == null)
        {
            this.mainPanel = (Panel) new Panel(false)
                    .setOuterMarginWidth(0)
                    .setInnerMarginWidth(CONTROL_EXTERNAL_MARGIN)
                    .resize(xStart + CONTROL_EXTERNAL_MARGIN, yStart + CONTROL_EXTERNAL_MARGIN, this.xSize - CONTROL_EXTERNAL_MARGIN * 2, this.ySize - CONTROL_EXTERNAL_MARGIN * 3 - this.buttonHeight);

            Panel leftPanel = (Panel) new Panel(true)
                    .setInnerMarginWidth(CONTROL_EXTERNAL_MARGIN)
                    .add(new Panel(true)
                            .setOuterMarginWidth(CONTROL_EXTERNAL_MARGIN)
                            .add(itemPreview)
                            .setBackgroundColor(GuiControl.CONTROL_BACKGROUND)
                            .setVerticalWeight(1))
                    .add(new Panel(true)
                            .setBackgroundColor(GuiControl.CONTROL_BACKGROUND)
                            .setVerticalWeight(3))
                    .setWidth(100)
                    .setHorizontalLayout(Layout.FIXED)
                    .resize(0, 0, (this.xSize - CONTROL_EXTERNAL_MARGIN) * 2.0 / 7.0, 1);

            
            Panel rightPanel = (Panel) new Panel(true)
                    .setOuterMarginWidth(CONTROL_EXTERNAL_MARGIN)
                    .setInnerMarginWidth(CONTROL_EXTERNAL_MARGIN)
                    .addAll(this.colorPicker.setVerticalWeight(2).setHorizontalLayout(Layout.PROPORTIONAL),
                            this.textureTabBar.setVerticalWeight(5))
                    .setHorizontalWeight(5)
                    .setBackgroundColor(GuiControl.CONTROL_BACKGROUND);
           
            
            
            this.mainPanel.addAll(leftPanel, rightPanel);

        }
        else
        {
            //TODO: really ugly how the sizing hints work
            ((Panel)this.mainPanel.get(0)).resize( 0, 0, (this.xSize - CONTROL_EXTERNAL_MARGIN) * 2.0 / 7.0, 1);
            this.mainPanel.resize(xStart + CONTROL_EXTERNAL_MARGIN, yStart + CONTROL_EXTERNAL_MARGIN, this.xSize - CONTROL_EXTERNAL_MARGIN * 2, this.ySize - CONTROL_EXTERNAL_MARGIN * 3 - this.buttonHeight);
        }
        
    
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawGradientRect(this.xStart, this.yStart, this.xStart + this.xSize, this.yStart + this.ySize, -1072689136, -804253680);

        this.mainPanel.drawControl(mc, itemRender, mouseX, mouseY, partialTicks);
        
        this.colorPicker.drawControl(this.mc, this.itemRender, mouseX, mouseY, partialTicks);
        
        this.itemPreview.drawControl(this.mc, this.itemRender, mouseX, mouseY, partialTicks);
        
        this.textureTabBar.drawControl(mc, itemRender, mouseX, mouseY, partialTicks);
        
        super.drawScreen(mouseX, mouseY, partialTicks);
 
    }

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
