package grondag.adversity.gui;


import static grondag.adversity.gui.base.GuiControl.*;

import java.io.IOException;
import java.util.ArrayList;

import org.lwjgl.input.Mouse;

import grondag.adversity.Configurator;
import grondag.adversity.gui.base.GuiControl;
import grondag.adversity.gui.control.Button;
import grondag.adversity.gui.control.ColorPicker;
import grondag.adversity.gui.control.ItemPreview;
import grondag.adversity.gui.control.Panel;
import grondag.adversity.gui.control.TexturePicker;
import grondag.adversity.gui.control.VisibilityPanel;
import grondag.adversity.gui.control.VisiblitySelector;
import grondag.adversity.network.AdversityMessages;
import grondag.adversity.network.PacketUpdateSuperModelBlock;
import grondag.adversity.niceblock.color.BlockColorMapProvider;
import grondag.adversity.superblock.block.SuperItemBlock;
import grondag.adversity.superblock.model.layout.PaintLayer;
import grondag.adversity.superblock.model.shape.ModelShape;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import grondag.adversity.superblock.texture.TexturePalletteProvider.TexturePallette;
import grondag.adversity.superblock.texture.Textures;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
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

    private ColorPicker[] colorPicker;
    private TexturePicker[] textureTabBar;
    private ItemPreview itemPreview;
    
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

        // TODO - set shape first
        
        ModelShape shape = modelState.getShape();
        
        for(PaintLayer layer : PaintLayer.DYNAMIC_VALUES)
        {
            updateItemPreviewSub(layer);
        }
    }
    
    private void updateItemPreviewSub(PaintLayer layer)
    {
        if(this.modelState.getColorMap(layer).ordinal != colorPicker[layer.dynamicIndex].getColorMapID())
        {
            this.modelState.setColorMap(layer, BlockColorMapProvider.INSTANCE.getColorMap(colorPicker[layer.ordinal()].getColorMapID()));
            this.textureTabBar[layer.ordinal()].colorMap = BlockColorMapProvider.INSTANCE.getColorMap(colorPicker[layer.ordinal()].getColorMapID());
            SuperItemBlock.setModelState(this.itemPreview.previewItem, modelState);
            this.hasUpdates = true;
        }
        
        if(this.modelState.getTexture(layer) != this.textureTabBar[layer.ordinal()].getSelected())
        {
            TexturePallette tex = this.textureTabBar[layer.ordinal()].getSelected();
            //TODO: ugly, need a lookup function from texture to painter
            this.modelState.setTexture(layer, tex);
            SuperItemBlock.setModelState(this.itemPreview.previewItem, modelState);
            this.hasUpdates = true;
        }
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int clickedMouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, clickedMouseButton);
        this.mainPanel.mouseClick(mc, mouseX, mouseY);
//        colorPicker.mouseClick(this.mc, mouseX, mouseY);
//        this.textureTabBar.mouseClick(this.mc, mouseX, mouseY);
        updateItemPreviewState();
    }
    
    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        this.mainPanel.mouseDrag(mc, mouseX, mouseY);
//        colorPicker.mouseDrag(this.mc, mouseX, mouseY);
//        this.textureTabBar.mouseDrag(this.mc, mouseX, mouseY);
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
            this.textureTabBar = new TexturePicker[PaintLayer.DYNAMIC_SIZE];
                    
            for(int i = 0; i < PaintLayer.DYNAMIC_SIZE; i++)
            {
                TexturePicker t = (TexturePicker) new TexturePicker(new ArrayList<TexturePallette>(), this.xStart + CONTROL_EXTERNAL_MARGIN, this.yStart + 100).setVerticalWeight(5);
                this.textureTabBar[i] = t;
            }
            
            loadTextures();
        }
        
        if(this.colorPicker == null)
        {
            this.colorPicker = new ColorPicker[PaintLayer.DYNAMIC_SIZE];
            for(int i = 0; i < PaintLayer.DYNAMIC_SIZE; i++)
            {
                this.colorPicker[i] = (ColorPicker) new ColorPicker().setVerticalWeight(2).setHorizontalLayout(Layout.PROPORTIONAL);
            }
        }
       
        if(this.mainPanel == null)
        {
            
            VisibilityPanel rightPanel = (VisibilityPanel) new VisibilityPanel(true)
                    .setOuterMarginWidth(CONTROL_EXTERNAL_MARGIN)
                    .setInnerMarginWidth(CONTROL_EXTERNAL_MARGIN)
                    .setHorizontalWeight(5)
                    .setBackgroundColor(GuiControl.CONTROL_BACKGROUND);
            
            //TODO: localize
            int GROUP_BASE = rightPanel.createVisiblityGroup("Base Layer");
            rightPanel.addAll(GROUP_BASE, this.colorPicker[PaintLayer.BASE.ordinal()],this.textureTabBar[PaintLayer.BASE.ordinal()]);
            rightPanel.setVisiblityIndex(GROUP_BASE);
            
            int GROUP_BORDER = rightPanel.createVisiblityGroup("Overlay");            
            rightPanel.addAll(GROUP_BORDER, this.colorPicker[PaintLayer.OVERLAY.ordinal()],this.textureTabBar[PaintLayer.OVERLAY.ordinal()]);

            int GROUP_DECO = rightPanel.createVisiblityGroup("Decoration");
            rightPanel.addAll(GROUP_DECO, this.colorPicker[PaintLayer.DETAIL.ordinal()],this.textureTabBar[PaintLayer.DETAIL.ordinal()]);

            int GROUP_LAMP = rightPanel.createVisiblityGroup("Lamp");            
            rightPanel.addAll(GROUP_LAMP, this.colorPicker[PaintLayer.LAMP.ordinal()],this.textureTabBar[PaintLayer.LAMP.ordinal()]);

            int GROUP_SHAPE = rightPanel.createVisiblityGroup("Shape");  
            
            int GROUP_MATERIAL = rightPanel.createVisiblityGroup("Material");  
            
            VisiblitySelector selector = new VisiblitySelector(rightPanel);
            
            Panel leftPanel = (Panel) new Panel(true)
                    .setInnerMarginWidth(CONTROL_EXTERNAL_MARGIN)
                    .add(new Panel(true)
                            .setOuterMarginWidth(CONTROL_EXTERNAL_MARGIN)
                            .add(itemPreview)
                            .setBackgroundColor(GuiControl.CONTROL_BACKGROUND)
                            .setVerticalWeight(1))
                    .add(new Panel(true)
                            .add(selector)
                            .setBackgroundColor(GuiControl.CONTROL_BACKGROUND)
                            .setVerticalWeight(3))
                    .setWidth(100)
                    .setHorizontalLayout(Layout.FIXED)
                    .resize(0, 0, (this.xSize - CONTROL_EXTERNAL_MARGIN) * 2.0 / 7.0, 1);

            this.mainPanel = (Panel) new Panel(false)
                    .setOuterMarginWidth(0)
                    .setInnerMarginWidth(CONTROL_EXTERNAL_MARGIN)
                    .resize(xStart + CONTROL_EXTERNAL_MARGIN, yStart + CONTROL_EXTERNAL_MARGIN, this.xSize - CONTROL_EXTERNAL_MARGIN * 2, this.ySize - CONTROL_EXTERNAL_MARGIN * 3 - this.buttonHeight);
            
            this.mainPanel.addAll(leftPanel, rightPanel);

        }
        else
        {
            //TODO: really ugly how the sizing hints work
            ((Panel)this.mainPanel.get(0)).resize( 0, 0, (this.xSize - CONTROL_EXTERNAL_MARGIN) * 2.0 / 7.0, 1);
            this.mainPanel.resize(xStart + CONTROL_EXTERNAL_MARGIN, yStart + CONTROL_EXTERNAL_MARGIN, this.xSize - CONTROL_EXTERNAL_MARGIN * 2, this.ySize - CONTROL_EXTERNAL_MARGIN * 3 - this.buttonHeight);
        }
        
    
    }

    private void loadTextures()
    {
        for(PaintLayer layer : PaintLayer.DYNAMIC_VALUES)
        {
            TexturePicker t = this.textureTabBar[layer.ordinal()];
            
            t.clear();
            t.addAll(Textures.getTexturesForSubstanceAndPaintLayer(Configurator.SUBSTANCES.flexstone, layer));
            t.setSelected(this.modelState.getTexture(layer));
            t.showSelected();
            t.colorMap = this.modelState.getColorMap(layer);
        }
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {

        this.drawGradientRect(this.xStart, this.yStart, this.xStart + this.xSize, this.yStart + this.ySize, -1072689136, -804253680);

        this.mainPanel.drawControl(mc, itemRender, mouseX, mouseY, partialTicks);
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void drawBackground(int tint)
    {
//        GlStateManager.disableLighting();
//        GlStateManager.disableFog();
//        Tessellator tessellator = Tessellator.getInstance();
//        VertexBuffer vertexbuffer = tessellator.getBuffer();
//        this.mc.getTextureManager().bindTexture(OPTIONS_BACKGROUND);
//        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
////        float f = 32.0F;
//        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
//        vertexbuffer.pos(0.0D, (double)this.height, 0.0D).tex(0.0D, (double)((float)this.height / 32.0F + (float)tint)).color(64, 64, 64, 255).endVertex();
//        vertexbuffer.pos((double)this.width, (double)this.height, 0.0D).tex((double)((float)this.width / 32.0F), (double)((float)this.height / 32.0F + (float)tint)).color(64, 64, 64, 255).endVertex();
//        vertexbuffer.pos((double)this.width, 0.0D, 0.0D).tex((double)((float)this.width / 32.0F), (double)tint).color(64, 64, 64, 255).endVertex();
//        vertexbuffer.pos(0.0D, 0.0D, 0.0D).tex(0.0D, (double)tint).color(64, 64, 64, 255).endVertex();
//        tessellator.draw();
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        int i = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int j = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        int scrollAmount = Mouse.getEventDWheel();
        if(scrollAmount != 0)
        {
            System.out.println(scrollAmount);
            this.mainPanel.mouseScroll(i, j, scrollAmount);
            this.updateItemPreviewState();
        }
        
    }
}
