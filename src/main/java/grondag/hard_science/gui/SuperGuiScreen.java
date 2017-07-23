package grondag.hard_science.gui;


import static grondag.hard_science.gui.control.GuiControl.CONTROL_EXTERNAL_MARGIN;
import static grondag.hard_science.gui.control.GuiControl.CONTROL_INTERNAL_MARGIN;

import java.io.IOException;
import java.util.ArrayList;

import org.lwjgl.input.Mouse;

import grondag.hard_science.Configurator;
import grondag.hard_science.gui.control.BrightnessSlider;
import grondag.hard_science.gui.control.Button;
import grondag.hard_science.gui.control.ColorPicker;
import grondag.hard_science.gui.control.GuiControl;
import grondag.hard_science.gui.control.ItemPreview;
import grondag.hard_science.gui.control.MaterialPicker;
import grondag.hard_science.gui.control.Panel;
import grondag.hard_science.gui.control.ShapePicker;
import grondag.hard_science.gui.control.TexturePicker;
import grondag.hard_science.gui.control.Toggle;
import grondag.hard_science.gui.control.TranslucencyPicker;
import grondag.hard_science.gui.control.VisibilityPanel;
import grondag.hard_science.gui.control.VisiblitySelector;
import grondag.hard_science.gui.shape.GuiShape;
import grondag.hard_science.gui.shape.GuiShapeFinder;
import grondag.hard_science.init.ModSuperModelBlocks;
import grondag.hard_science.network.ModMessages;
import grondag.hard_science.network.PacketReplaceHeldItem;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.color.BlockColorMapProvider;
import grondag.hard_science.superblock.color.ColorMap.EnumColorMap;
import grondag.hard_science.superblock.items.SuperItemBlock;
import grondag.hard_science.superblock.model.state.PaintLayer;
import grondag.hard_science.superblock.model.state.Translucency;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.texture.Textures;
import grondag.hard_science.superblock.texture.TexturePalletteRegistry.TexturePallette;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SuperGuiScreen extends GuiScreen
{

    private static final int BUTTON_ID_CANCEL = 0;
    private static final int BUTTON_ID_ACCEPT = 1;

    @SuppressWarnings("deprecation")
    private final String STR_ACCEPT = I18n.translateToLocal("label.accept");
    @SuppressWarnings("deprecation")
    private final String STR_CANCEL = I18n.translateToLocal("label.cancel");

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
    private Toggle outerToggle;
    private Toggle middleToggle;
    private Toggle baseTranslucentToggle;
    private Toggle lampTranslucentToggle;
    private BrightnessSlider brightnessSlider;
    private GuiShape shapeGui;

    private ItemPreview itemPreview;

    private ModelState modelState = null;

    private boolean hasUpdates = false;

    private int buttonWidth;
    private int buttonHeight;

    private Panel mainPanel;
    private int group_base;
    private int group_outer;
    private int group_middle;
    private int group_lamp;
    private int group_shape;
    private int group_material;
    private VisibilityPanel rightPanel;

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    private void updateItemPreviewState()
    {
        // abort on strangeness
        if(modelState == null)
        {
            return;
        }

        if(shapePicker.getSelected() != modelState.getShape())
        {
            modelState.setShape(shapePicker.getSelected());
            rightPanel.remove(group_shape, 1);
            shapeGui = GuiShapeFinder.findGuiForShape(modelState.getShape(), mc);
            rightPanel.add(group_shape, shapeGui.setVerticalWeight(2));
            // display shape defaults if any
            shapeGui.loadSettings(modelState);
            hasUpdates = true;
        }
        else
        {
            //shape is the same, so can check for shape-specific updates
            hasUpdates = shapeGui.saveSettings(modelState) || hasUpdates;
        }

        if(brightnessSlider.getBrightness() != SuperItemBlock.getStackLightValue(itemPreview.previewItem))
        {
            SuperItemBlock.setStackLightValue(itemPreview.previewItem, brightnessSlider.getBrightness());
            hasUpdates = true;
        }

        if(materialPicker.getSubstance() != SuperItemBlock.getStackSubstance(itemPreview.previewItem))
        {
            SuperItemBlock.setStackSubstance(itemPreview.previewItem, materialPicker.getSubstance());
            baseTranslucentToggle.setVisible(materialPicker.getSubstance().isTranslucent);
            lampTranslucentToggle.setVisible(materialPicker.getSubstance().isTranslucent);
            translucencyPicker.setVisible(materialPicker.getSubstance().isTranslucent);
            hasUpdates = true;
        }

        Translucency newTrans = materialPicker.getSubstance().isTranslucent
                ? translucencyPicker.getTranslucency()
                        : Translucency.CLEAR;
        if(newTrans == null)
        {
            newTrans = Translucency.CLEAR;
        }
        if(newTrans != modelState.getTranslucency() )
        {
            modelState.setTranslucency(newTrans);
            hasUpdates = true;
        }
        
        if(outerToggle.isOn() != modelState.isOuterLayerEnabled())
        {
            modelState.setOuterLayerEnabled(outerToggle.isOn());
            hasUpdates = true;

            if(!outerToggle.isOn())
            {
                textureTabBar[PaintLayer.OUTER.dynamicIndex].setSelected(null);
            }
        }
        
        if(middleToggle.isOn() != modelState.isMiddleLayerEnabled())
        {
            modelState.setMiddleLayerEnabled(middleToggle.isOn());
            hasUpdates = true;
            
            if(!middleToggle.isOn())
            {
                textureTabBar[PaintLayer.MIDDLE.dynamicIndex].setSelected(null);
            }
        }

        // needs to happen before toggle checks because it turns on
        // middle/outer toggles when a texture is selected
        for(PaintLayer layer : PaintLayer.DYNAMIC_VALUES)
        {
            updateItemPreviewSub(layer);
        }

        if(baseTranslucentToggle.isOn() != modelState.isTranslucent(PaintLayer.BASE))
        {
            modelState.setTranslucent(PaintLayer.BASE, baseTranslucentToggle.isOn());
            hasUpdates = true;
        }

        if(lampTranslucentToggle.isOn() != modelState.isTranslucent(PaintLayer.LAMP))
        {
            modelState.setTranslucent(PaintLayer.LAMP, lampTranslucentToggle.isOn());
            hasUpdates = true;
        }
       
        SuperBlock currentBlock = (SuperBlock) ((ItemBlock)(itemPreview.previewItem.getItem())).getBlock();
        SuperBlock newBlock = ModSuperModelBlocks.findAppropriateSuperModelBlock(materialPicker.getSubstance(), modelState);

        if(currentBlock != newBlock && newBlock != null)
        {
            ItemStack newStack = new ItemStack(newBlock);
            newStack.setCount(itemPreview.previewItem.getCount());
            newStack.setItemDamage(itemPreview.previewItem.getItemDamage());
            newStack.setTagCompound(itemPreview.previewItem.getTagCompound());
            itemPreview.previewItem = newStack;
            hasUpdates = true;

        }
        
        if(hasUpdates)
        {
            this.itemPreview.previewItem.setItemDamage(this.modelState.getMetaData());
            SuperItemBlock.setModelState(itemPreview.previewItem, modelState);
        }
    }

    private void updateItemPreviewSub(PaintLayer layer)
    {
        if(modelState.getColorMap(layer).ordinal != colorPicker[layer.dynamicIndex].getColorMapID())
        {
            updateColors(layer);
            hasUpdates = true;
        }

        TexturePallette tex = textureTabBar[layer.dynamicIndex].getSelected();
        if(tex != null && modelState.getTexture(layer) != tex)
        {
            modelState.setTexture(layer, tex);
            hasUpdates = true;
            
            //enable layer if user selected a texture
            if(layer == PaintLayer.OUTER && !outerToggle.isOn())
            {
                outerToggle.setOn(true);
                modelState.setOuterLayerEnabled(true);
            }
            else if(layer == PaintLayer.MIDDLE && !middleToggle.isOn())
            {
                middleToggle.setOn(true);
                modelState.setMiddleLayerEnabled(true);
            }
        }

        if(((modelState.isFullBrightness(layer)) != fullBrightToggle[layer.dynamicIndex].isOn()))
        {
            modelState.setFullBrightness(layer, fullBrightToggle[layer.dynamicIndex].isOn());
            updateColors(layer);
            this.colorPicker[layer.dynamicIndex].showLampColors = modelState.isFullBrightness(layer);
            hasUpdates = true;
        }
    }

    private void updateColors(PaintLayer layer)
    {
        modelState.setColorMap(layer, BlockColorMapProvider.INSTANCE.getColorMap(colorPicker[layer.dynamicIndex].getColorMapID()));
        textureTabBar[layer.dynamicIndex].borderColor = BlockColorMapProvider.INSTANCE
                .getColorMap(colorPicker[layer.dynamicIndex].getColorMapID())
                .getColor(modelState.isFullBrightness(layer) ? EnumColorMap.LAMP: EnumColorMap.BASE);
        
        if(layer == PaintLayer.BASE)
        {
            // refresh base color on overlay layers if it has changed
            int baseColor = modelState.isFullBrightness(PaintLayer.BASE)
                    ? modelState.getColorMap(PaintLayer.BASE).getColor(EnumColorMap.LAMP)
                    : modelState.getColorMap(PaintLayer.BASE).getColor(EnumColorMap.BASE);

            textureTabBar[PaintLayer.MIDDLE.dynamicIndex].baseColor = baseColor;
            textureTabBar[PaintLayer.OUTER.dynamicIndex].baseColor = baseColor;
            textureTabBar[PaintLayer.LAMP.dynamicIndex].baseColor = baseColor;
        }
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int clickedMouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, clickedMouseButton);
        mainPanel.mouseClick(mc, mouseX, mouseY);
        //        colorPicker.mouseClick(this.mc, mouseX, mouseY);
        //        this.textureTabBar.mouseClick(this.mc, mouseX, mouseY);
        updateItemPreviewState();
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        mainPanel.mouseDrag(mc, mouseX, mouseY);
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
        if(hasUpdates && button.id == BUTTON_ID_ACCEPT)
        {
            ModMessages.INSTANCE.sendToServer(new PacketReplaceHeldItem(itemPreview.previewItem));
            hasUpdates = false;
        }
        mc.displayGuiScreen((GuiScreen)null);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void initGui()
    {
        super.initGui();


        ySize = MathHelper.clamp(height * 4 / 5, fontRenderer.FONT_HEIGHT * 28, height);
        yStart = (height - ySize) / 2;
        xSize = (int) (ySize * GuiUtil.GOLDEN_RATIO);
        xStart = (width - xSize) / 2;

        FontRenderer fr = mc.fontRenderer;
        buttonWidth = Math.max(fr.getStringWidth(STR_ACCEPT), fr.getStringWidth(STR_CANCEL)) + CONTROL_INTERNAL_MARGIN + CONTROL_INTERNAL_MARGIN;
        buttonHeight = fr.FONT_HEIGHT + CONTROL_INTERNAL_MARGIN + CONTROL_INTERNAL_MARGIN;

        int buttonTop = yStart + ySize - buttonHeight - CONTROL_EXTERNAL_MARGIN;
        int buttonLeft = xStart + xSize - CONTROL_EXTERNAL_MARGIN * 2 - buttonWidth * 2;

        // buttons are cleared by super each time
        this.addButton(new Button(BUTTON_ID_ACCEPT, buttonLeft, buttonTop, buttonWidth, buttonHeight, STR_ACCEPT));
        this.addButton(new Button(BUTTON_ID_CANCEL, buttonLeft + CONTROL_EXTERNAL_MARGIN + buttonWidth, buttonTop, buttonWidth, buttonHeight, STR_CANCEL));

        if(itemPreview == null)
        {
            itemPreview = new ItemPreview();
            itemPreview.previewItem = mc.player.getHeldItem(EnumHand.MAIN_HAND).copy();

            if (itemPreview.previewItem == null || !(itemPreview.previewItem.getItem() instanceof SuperItemBlock))
            {
                // Abort on strangeness
                return;
            }
            //            this.meta = this.itemPreview.previewItem.getMetadata();
            modelState = SuperItemBlock.getModelStateFromStack(itemPreview.previewItem);
        }

        // abort on strangeness
        if(modelState == null)
        {
            return;
        }

        if(textureTabBar == null)
        {
            materialPicker = new MaterialPicker();
            shapePicker = new ShapePicker();
            translucencyPicker = new TranslucencyPicker();
            textureTabBar = new TexturePicker[PaintLayer.DYNAMIC_SIZE];
            colorPicker = new ColorPicker[PaintLayer.DYNAMIC_SIZE];

            outerToggle = new Toggle().setLabel("Enabled");
            middleToggle = new Toggle().setLabel("Enabled");
            baseTranslucentToggle = new Toggle().setLabel("Translucent");
            lampTranslucentToggle = new Toggle().setLabel("Translucent");
            fullBrightToggle = new Toggle[PaintLayer.DYNAMIC_SIZE];
            brightnessSlider = new BrightnessSlider(mc);

            for(int i = 0; i < PaintLayer.DYNAMIC_SIZE; i++)
            {
                TexturePicker t = (TexturePicker) new TexturePicker(new ArrayList<TexturePallette>(), xStart + CONTROL_EXTERNAL_MARGIN, yStart + 100).setVerticalWeight(5);
                // only render textures with alpha for layers that will render that way in world
                t.renderAlpha = PaintLayer.DYNAMIC_VALUES[i] == PaintLayer.MIDDLE || PaintLayer.DYNAMIC_VALUES[i] == PaintLayer.OUTER;
                textureTabBar[i] = t;

                colorPicker[i] = (ColorPicker) new ColorPicker().setHorizontalWeight(5);

                fullBrightToggle[i] = new Toggle().setLabel("Glowing");
            }
        }

        if(mainPanel == null)
        {

            rightPanel = (VisibilityPanel) new VisibilityPanel(true)
                    .setOuterMarginWidth(CONTROL_EXTERNAL_MARGIN)
                    .setInnerMarginWidth(CONTROL_EXTERNAL_MARGIN)
                    .setHorizontalWeight(5)
                    .setBackgroundColor(GuiControl.CONTROL_BACKGROUND);

            group_base = rightPanel.createVisiblityGroup(PaintLayer.BASE.localizedName());
            GuiControl tempV = new Panel(true).addAll(fullBrightToggle[PaintLayer.BASE.ordinal()], baseTranslucentToggle)
                    .setHorizontalWeight(2);
            GuiControl tempH = new Panel(false).addAll(tempV, colorPicker[PaintLayer.BASE.ordinal()]).setVerticalWeight(2);
            rightPanel.addAll(group_base, tempH, textureTabBar[PaintLayer.BASE.ordinal()]);
            rightPanel.setVisiblityIndex(group_base);

            group_middle = rightPanel.createVisiblityGroup(PaintLayer.MIDDLE.localizedName());
            tempV = new Panel(true).addAll(middleToggle, fullBrightToggle[PaintLayer.MIDDLE.ordinal()])
                    .setHorizontalWeight(2);
            tempH = new Panel(false).addAll(tempV, colorPicker[PaintLayer.MIDDLE.ordinal()]).setVerticalWeight(2);
            rightPanel.addAll(group_middle, tempH, textureTabBar[PaintLayer.MIDDLE.ordinal()]);

            group_outer = rightPanel.createVisiblityGroup(PaintLayer.OUTER.localizedName());
            tempV = new Panel(true).addAll(outerToggle, fullBrightToggle[PaintLayer.OUTER.ordinal()])
                    .setHorizontalWeight(2);
            tempH = new Panel(false).addAll(tempV, colorPicker[PaintLayer.OUTER.ordinal()]).setVerticalWeight(2);
            rightPanel.addAll(group_outer, tempH,textureTabBar[PaintLayer.OUTER.ordinal()]);
            
            group_lamp = rightPanel.createVisiblityGroup(PaintLayer.LAMP.localizedName());
            tempV = new Panel(true).addAll(fullBrightToggle[PaintLayer.LAMP.ordinal()], lampTranslucentToggle)
                    .setHorizontalWeight(2);
            tempH = new Panel(false).addAll(tempV, colorPicker[PaintLayer.LAMP.ordinal()]).setVerticalWeight(2);
            rightPanel.addAll(group_lamp, tempH, textureTabBar[PaintLayer.LAMP.ordinal()]);

            group_shape = rightPanel.createVisiblityGroup(I18n.translateToLocal("label.shape"));
            rightPanel.add(group_shape, shapePicker.setVerticalWeight(5));
            shapeGui = GuiShapeFinder.findGuiForShape(modelState.getShape(), mc);
            rightPanel.add(group_shape, shapeGui.setVerticalWeight(2));

            group_material = rightPanel.createVisiblityGroup(I18n.translateToLocal("label.material"));
            rightPanel.add(group_material, materialPicker.setVerticalLayout(Layout.PROPORTIONAL));
            rightPanel.add(group_material, translucencyPicker.setVerticalLayout(Layout.PROPORTIONAL));
            rightPanel.add(group_material, brightnessSlider);
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
                    .resize(0, 0, (xSize - CONTROL_EXTERNAL_MARGIN) * 2.0 / 7.0, 1);

            mainPanel = (Panel) new Panel(false)
                    .setOuterMarginWidth(0)
                    .setInnerMarginWidth(CONTROL_EXTERNAL_MARGIN)
                    .resize(xStart + CONTROL_EXTERNAL_MARGIN, yStart + CONTROL_EXTERNAL_MARGIN, xSize - CONTROL_EXTERNAL_MARGIN * 2, ySize - CONTROL_EXTERNAL_MARGIN * 3 - buttonHeight);

            mainPanel.addAll(leftPanel, rightPanel);

            loadControlValuesFromModelState();

        }
        else
        {
            ((Panel)mainPanel.get(0)).resize( 0, 0, (xSize - CONTROL_EXTERNAL_MARGIN) * 2.0 / 7.0, 1);
            mainPanel.resize(xStart + CONTROL_EXTERNAL_MARGIN, yStart + CONTROL_EXTERNAL_MARGIN, xSize - CONTROL_EXTERNAL_MARGIN * 2, ySize - CONTROL_EXTERNAL_MARGIN * 3 - buttonHeight);
        }
    }

    private void loadControlValuesFromModelState()
    {
        materialPicker.setSubstance(SuperItemBlock.getStackSubstance(itemPreview.previewItem));
        shapePicker.setSelected(modelState.getShape());
        brightnessSlider.setBrightness(SuperItemBlock.getStackLightValue(itemPreview.previewItem));
        outerToggle.setOn(modelState.isOuterLayerEnabled());
        middleToggle.setOn(modelState.isMiddleLayerEnabled());
        baseTranslucentToggle.setOn(modelState.isTranslucent(PaintLayer.BASE));
        lampTranslucentToggle.setOn(modelState.isTranslucent(PaintLayer.LAMP));

        baseTranslucentToggle.setVisible(materialPicker.getSubstance().isTranslucent);
        lampTranslucentToggle.setVisible(materialPicker.getSubstance().isTranslucent);

        translucencyPicker.setVisible(materialPicker.getSubstance().isTranslucent);
        translucencyPicker.setTranslucency(modelState.getTranslucency());

        shapeGui.loadSettings(modelState);
        
        for(PaintLayer layer : PaintLayer.DYNAMIC_VALUES)
        {
            TexturePicker t = textureTabBar[layer.dynamicIndex];

            t.clear();
            t.addAll(Textures.getTexturesForSubstanceAndPaintLayer(Configurator.SUBSTANCES.flexstone, layer));
            TexturePallette tex = modelState.getTexture(layer);
            t.setSelected(tex == Textures.NONE ? null : modelState.getTexture(layer));
            t.showSelected();
            t.borderColor = modelState.isFullBrightness(layer)
                    ? modelState.getColorMap(layer).getColor(EnumColorMap.LAMP)
                    : modelState.getColorMap(layer).getColor(EnumColorMap.BASE);
            t.baseColor = modelState.isFullBrightness(PaintLayer.BASE)
                    ? modelState.getColorMap(PaintLayer.BASE).getColor(EnumColorMap.LAMP)
                    : modelState.getColorMap(PaintLayer.BASE).getColor(EnumColorMap.BASE);

            ColorPicker c = colorPicker[layer.dynamicIndex];
            c.setColorMapID(modelState.getColorMap(layer).ordinal);
            
            c.showLampColors = modelState.isFullBrightness(layer);
            fullBrightToggle[layer.dynamicIndex].setOn(modelState.isFullBrightness(layer));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {

        drawGradientRect(xStart, yStart, xStart + xSize, yStart + ySize, -1072689136, -804253680);

        mainPanel.drawControl(mc, itemRender, mouseX, mouseY, partialTicks);

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
        int i = Mouse.getEventX() * width / mc.displayWidth;
        int j = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        int scrollAmount = Mouse.getEventDWheel();
        if(scrollAmount != 0)
        {
            mainPanel.mouseScroll(i, j, scrollAmount);
            updateItemPreviewState();
        }

    }
}
