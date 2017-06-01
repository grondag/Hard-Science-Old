package grondag.adversity.gui;


import static grondag.adversity.gui.base.GuiControl.*;

import java.io.IOException;
import java.util.ArrayList;

import org.lwjgl.input.Mouse;

import grondag.adversity.Configurator;
import grondag.adversity.gui.base.GuiControl;
import grondag.adversity.gui.control.BrightnessSlider;
import grondag.adversity.gui.control.Button;
import grondag.adversity.gui.control.ColorPicker;
import grondag.adversity.gui.control.ItemPreview;
import grondag.adversity.gui.control.MaterialPicker;
import grondag.adversity.gui.control.Panel;
import grondag.adversity.gui.control.ShapePicker;
import grondag.adversity.gui.control.TexturePicker;
import grondag.adversity.gui.control.Toggle;
import grondag.adversity.gui.control.TranslucencyPicker;
import grondag.adversity.gui.control.VisibilityPanel;
import grondag.adversity.gui.control.VisiblitySelector;
import grondag.adversity.init.ModSuperModelBlocks;
import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.network.AdversityMessages;
import grondag.adversity.network.PacketReplaceHeldItem;
import grondag.adversity.niceblock.color.BlockColorMapProvider;
import grondag.adversity.superblock.block.SuperBlock;
import grondag.adversity.superblock.block.SuperItemBlock;
import grondag.adversity.superblock.model.layout.PaintLayer;
import grondag.adversity.superblock.model.shape.ModelShape;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import grondag.adversity.superblock.model.state.Translucency;
import grondag.adversity.superblock.texture.TexturePalletteProvider.TexturePallette;
import grondag.adversity.superblock.texture.Textures;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;

public class SuperGuiScreen extends GuiScreen
{
    
    private static final int BUTTON_ID_CANCEL = 0;
    private static final int BUTTON_ID_ACCEPT = 1;
    
    //TODO: localize
    private static final String STR_ACCEPT = "Accept";
    private static final String STR_CANCEL = "Cancel";
    
    private int xStart;
    private int yStart;
    private int xSize;
    private int ySize;

    private MaterialPicker materialPicker;
    private TranslucencyPicker translucencyPicker;
    private ColorPicker[] colorPicker;
    private TexturePicker[] textureTabBar;
    private ShapePicker shapePicker;
    private Toggle[] fullBrightToggle;
    private Toggle overlayToggle;
    private Toggle detailToggle;
    private Toggle baseTranslucentToggle;
    private Toggle lampTranslucentToggle;
    private BrightnessSlider brightnessSlider;
    
    private ItemPreview itemPreview;
    
//    private int meta = 0;
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

        if(this.shapePicker.getSelected() != this.modelState.getShape())
        {
            this.modelState.setShape(this.shapePicker.getSelected());
            this.hasUpdates = true;
        }
        
        if(this.brightnessSlider.getBrightness() != SuperItemBlock.getStackLightValue(this.itemPreview.previewItem))
        {
            SuperItemBlock.setStackLightValue(this.itemPreview.previewItem, this.brightnessSlider.getBrightness());
            this.hasUpdates = true;
        }
        
        if(this.materialPicker.getSubstance() != SuperItemBlock.getStackSubstance(this.itemPreview.previewItem))
        {
            SuperItemBlock.setStackSubstance(this.itemPreview.previewItem, this.materialPicker.getSubstance());
            this.baseTranslucentToggle.setVisible(this.materialPicker.getSubstance().isTranslucent);
            this.lampTranslucentToggle.setVisible(this.materialPicker.getSubstance().isTranslucent);
            this.translucencyPicker.setVisible(this.materialPicker.getSubstance().isTranslucent);
            this.hasUpdates = true;
        }
        
        Translucency newTrans = this.materialPicker.getSubstance().isTranslucent
                ? this.translucencyPicker.getTranslucency()
                : Translucency.CLEAR;
        if(newTrans == null) newTrans = Translucency.CLEAR;
        if(newTrans != this.modelState.getTranslucency() )
        {
            this.modelState.setTranslucency(newTrans);
            this.hasUpdates = true;
        }
        
        for(PaintLayer layer : PaintLayer.DYNAMIC_VALUES)
        {
            updateItemPreviewSub(layer);
        }
        
        if(this.overlayToggle.isOn() != this.modelState.isOverlayLayerEnabled())
        {
            this.modelState.setOverlayLayerEnabled(this.overlayToggle.isOn());
            this.hasUpdates = true;
        }

        if(this.detailToggle.isOn() != this.modelState.isDetailLayerEnabled())
        {
            this.modelState.setDetailLayerEnabled(this.detailToggle.isOn());
            this.hasUpdates = true;
        }

        BlockRenderLayer renderLayer = this.baseTranslucentToggle.isOn() ? BlockRenderLayer.TRANSLUCENT : BlockRenderLayer.SOLID;
        if(renderLayer != this.modelState.getRenderLayer(PaintLayer.BASE))
        {
            this.modelState.setRenderLayer(PaintLayer.BASE, renderLayer);
            this.hasUpdates = true;
        }
        
        renderLayer = this.lampTranslucentToggle.isOn() ? BlockRenderLayer.TRANSLUCENT : BlockRenderLayer.SOLID;
        if(renderLayer != this.modelState.getRenderLayer(PaintLayer.LAMP))
        {
            this.modelState.setRenderLayer(PaintLayer.LAMP, renderLayer);
            this.hasUpdates = true;
        }
        
        SuperBlock currentBlock = (SuperBlock) ((ItemBlock)(this.itemPreview.previewItem.getItem())).block;
        SuperBlock newBlock = ModSuperModelBlocks.findAppropriateSuperModelBlock(this.materialPicker.getSubstance(), this.modelState);
        
        if(currentBlock != newBlock && newBlock != null)
        {
            ItemStack newStack = new ItemStack(newBlock);
            newStack.setItemDamage(this.itemPreview.previewItem.getItemDamage());
            newStack.setTagCompound(this.itemPreview.previewItem.getTagCompound());
            this.itemPreview.previewItem = newStack;
            this.hasUpdates = true;
            
        }
        if(this.hasUpdates)
        {
            // see notes in SuperBlock for canRenderInLayer()
//            this.meta = this.modelState.getCanRenderInLayerFlags();
            
//            this.itemPreview.previewItem.setItemDamage(this.meta);
            SuperItemBlock.setModelState(this.itemPreview.previewItem, this.modelState);
        }
    }
    
    private void updateItemPreviewSub(PaintLayer layer)
    {
        if(this.modelState.getColorMap(layer).ordinal != colorPicker[layer.dynamicIndex].getColorMapID())
        {
            this.modelState.setColorMap(layer, BlockColorMapProvider.INSTANCE.getColorMap(colorPicker[layer.dynamicIndex].getColorMapID()));
            this.textureTabBar[layer.dynamicIndex].colorMap = BlockColorMapProvider.INSTANCE.getColorMap(colorPicker[layer.dynamicIndex].getColorMapID());
            this.hasUpdates = true;
        }
        
        TexturePallette tex = this.textureTabBar[layer.dynamicIndex].getSelected();
        if(this.modelState.getTexture(layer) != tex)
        {
            this.modelState.setTexture(layer, tex);
            this.hasUpdates = true;
        }
        
        if(!((this.modelState.getLightingMode(layer) == LightingMode.FULLBRIGHT) && this.fullBrightToggle[layer.dynamicIndex].isOn()))
        {
            this.modelState.setLightingMode(layer, this.fullBrightToggle[layer.dynamicIndex].isOn() ? LightingMode.FULLBRIGHT : LightingMode.SHADED);
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
            AdversityMessages.INSTANCE.sendToServer(new PacketReplaceHeldItem(this.itemPreview.previewItem));
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
        
        
        ySize = MathHelper.clamp(this.height * 3 / 5, this.fontRenderer.FONT_HEIGHT * 28, this.height);
        this.yStart = (this.height - ySize) / 2;
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
//            this.meta = this.itemPreview.previewItem.getMetadata();
            this.modelState = SuperItemBlock.getModelState(itemPreview.previewItem);
        }
    
        // abort on strangeness
        if(this.modelState == null) return;
        
        if(this.textureTabBar == null)
        {
            this.materialPicker = new MaterialPicker();
            this.shapePicker = new ShapePicker();
            this.translucencyPicker = new TranslucencyPicker();
            this.textureTabBar = new TexturePicker[PaintLayer.DYNAMIC_SIZE];
            this.colorPicker = new ColorPicker[PaintLayer.DYNAMIC_SIZE];
            
            this.overlayToggle = new Toggle().setLabel("Enabled");
            this.detailToggle = new Toggle().setLabel("Enabled");
            this.baseTranslucentToggle = new Toggle().setLabel("Translucent");
            this.lampTranslucentToggle = new Toggle().setLabel("Translucent");
            this.fullBrightToggle = new Toggle[PaintLayer.DYNAMIC_SIZE];
            this.brightnessSlider = new BrightnessSlider(this.mc);
            
            for(int i = 0; i < PaintLayer.DYNAMIC_SIZE; i++)
            {
                TexturePicker t = (TexturePicker) new TexturePicker(new ArrayList<TexturePallette>(), this.xStart + CONTROL_EXTERNAL_MARGIN, this.yStart + 100).setVerticalWeight(5);
                this.textureTabBar[i] = t;
                
                this.colorPicker[i] = (ColorPicker) new ColorPicker().setHorizontalWeight(5);
            
                this.fullBrightToggle[i] = new Toggle().setLabel("Glowing");
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
            GuiControl tempV = new Panel(true).addAll(this.fullBrightToggle[PaintLayer.BASE.ordinal()], this.baseTranslucentToggle)
                    .setHorizontalWeight(2);
            GuiControl tempH = new Panel(false).addAll(tempV, this.colorPicker[PaintLayer.BASE.ordinal()]).setVerticalWeight(2);
            rightPanel.addAll(GROUP_BASE, tempH, this.textureTabBar[PaintLayer.BASE.ordinal()]);
            rightPanel.setVisiblityIndex(GROUP_BASE);
            
            int GROUP_BORDER = rightPanel.createVisiblityGroup("Overlay"); 
            tempV = new Panel(true).addAll(this.overlayToggle, this.fullBrightToggle[PaintLayer.OVERLAY.ordinal()])
                    .setHorizontalWeight(2);
            tempH = new Panel(false).addAll(tempV, this.colorPicker[PaintLayer.OVERLAY.ordinal()]).setVerticalWeight(2);
            rightPanel.addAll(GROUP_BORDER, tempH,this.textureTabBar[PaintLayer.OVERLAY.ordinal()]);

            int GROUP_DECO = rightPanel.createVisiblityGroup("Decoration");
            tempV = new Panel(true).addAll(this.detailToggle, this.fullBrightToggle[PaintLayer.DETAIL.ordinal()])
                    .setHorizontalWeight(2);
            tempH = new Panel(false).addAll(tempV, this.colorPicker[PaintLayer.DETAIL.ordinal()]).setVerticalWeight(2);
            rightPanel.addAll(GROUP_DECO, tempH, this.textureTabBar[PaintLayer.DETAIL.ordinal()]);

            int GROUP_LAMP = rightPanel.createVisiblityGroup("Lamp");            
            tempV = new Panel(true).addAll(this.fullBrightToggle[PaintLayer.LAMP.ordinal()], this.lampTranslucentToggle)
                    .setHorizontalWeight(2);
            tempH = new Panel(false).addAll(tempV, this.colorPicker[PaintLayer.LAMP.ordinal()]).setVerticalWeight(2);
            rightPanel.addAll(GROUP_LAMP, tempH, this.textureTabBar[PaintLayer.LAMP.ordinal()]);

            int GROUP_SHAPE = rightPanel.createVisiblityGroup("Shape");
            rightPanel.add(GROUP_SHAPE, this.shapePicker);
            
            int GROUP_MATERIAL = rightPanel.createVisiblityGroup("Material");  
            rightPanel.add(GROUP_MATERIAL, this.materialPicker.setVerticalLayout(Layout.PROPORTIONAL));
            rightPanel.add(GROUP_MATERIAL, this.translucencyPicker.setVerticalLayout(Layout.PROPORTIONAL));
            rightPanel.add(GROUP_MATERIAL, this.brightnessSlider);
            rightPanel.setInnerMarginWidth(GuiControl.CONTROL_INTERNAL_MARGIN * 4);
            
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
            
            loadControlValuesFromModelState();

        }
        else
        {
            ((Panel)this.mainPanel.get(0)).resize( 0, 0, (this.xSize - CONTROL_EXTERNAL_MARGIN) * 2.0 / 7.0, 1);
            this.mainPanel.resize(xStart + CONTROL_EXTERNAL_MARGIN, yStart + CONTROL_EXTERNAL_MARGIN, this.xSize - CONTROL_EXTERNAL_MARGIN * 2, this.ySize - CONTROL_EXTERNAL_MARGIN * 3 - this.buttonHeight);
        }
    }
    
    private void loadControlValuesFromModelState()
    {
        this.materialPicker.setSubstance(SuperItemBlock.getStackSubstance(this.itemPreview.previewItem));
        this.shapePicker.setSelected(modelState.getShape());
        this.brightnessSlider.setBrightness(SuperItemBlock.getStackLightValue(this.itemPreview.previewItem));
        this.overlayToggle.setOn(this.modelState.isOverlayLayerEnabled());
        this.detailToggle.setOn(this.modelState.isDetailLayerEnabled());
        this.baseTranslucentToggle.setOn(this.modelState.getRenderLayer(PaintLayer.BASE) == BlockRenderLayer.TRANSLUCENT);
        this.lampTranslucentToggle.setOn(this.modelState.getRenderLayer(PaintLayer.LAMP) == BlockRenderLayer.TRANSLUCENT);

        this.baseTranslucentToggle.setVisible(this.materialPicker.getSubstance().isTranslucent);
        this.lampTranslucentToggle.setVisible(this.materialPicker.getSubstance().isTranslucent);
        
        this.translucencyPicker.setVisible(this.materialPicker.getSubstance().isTranslucent);
        this.translucencyPicker.setTranslucency(this.modelState.getTranslucency());
        
        for(PaintLayer layer : PaintLayer.DYNAMIC_VALUES)
        {
            TexturePicker t = this.textureTabBar[layer.dynamicIndex];
            
            t.clear();
            t.addAll(Textures.getTexturesForSubstanceAndPaintLayer(Configurator.SUBSTANCES.flexstone, layer));
            t.setSelected(this.modelState.getTexture(layer));
            t.showSelected();
            t.colorMap = this.modelState.getColorMap(layer);
            
            ColorPicker c = this.colorPicker[layer.dynamicIndex];
            c.setColorMapID(this.modelState.getColorMap(layer).ordinal);
            
            this.fullBrightToggle[layer.dynamicIndex].setOn(modelState.getLightingMode(layer) == LightingMode.FULLBRIGHT);
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
