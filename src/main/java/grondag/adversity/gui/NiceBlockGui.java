package grondag.adversity.gui;

import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;

public class NiceBlockGui extends GuiContainer
{

    public EntityPlayer player;
    public NiceBlockContainer container;

    public NiceBlockGui(InventoryPlayer iinventory, EnumHand hand) 
    {
        super(new NiceBlockContainer(iinventory, hand));
        player = iinventory.player;
        xSize = 252;
        ySize = 202;

        container = (NiceBlockContainer) inventorySlots;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int j, int i) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        String line = container.usedItemStack.getDisplayName();
        List<String> lines = fontRendererObj.listFormattedStringToWidth(line, 40);
        int y = 60;
        for (String s : lines) {
            fontRendererObj.drawString(s, 32 - fontRendererObj.getStringWidth(s) / 2, y, 0x404040);
            y += 10;
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int mx, int my) 
    {
        drawDefaultBackground();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        int i = width - xSize >> 1;
        int j = height - ySize >> 1;

        String texture = "adversity:textures/adversity_gui.png";

        Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(texture));
        drawTexturedModalRect(i, j, 0, 0, xSize, ySize);

    }

//    @Override
//    protected void actionPerformed(GuiButton button) {
//        if (button.id == 0) {
//            if (container.chisel != null && container.chisel.getItem() instanceof IAdvancedChisel) {
//                IAdvancedChisel items = (IAdvancedChisel) container.chisel.getItem();
//                currentMode = items.getNextMode(container.chisel, currentMode);
//                PacketHandler.INSTANCE.sendToServer(new MessageChiselMode(currentMode));
//            } else {
//                currentMode = ChiselMode.next(currentMode);
//                PacketHandler.INSTANCE.sendToServer(new MessageChiselMode(currentMode));
//                setButtonText();
//            }
//        }
//        super.actionPerformed(button);
//    }
    

}
