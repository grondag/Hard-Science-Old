package grondag.hard_science.network.client_to_server;


import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import grondag.hard_science.library.world.Rotation;
import grondag.hard_science.network.AbstractPlayerToServerPacket;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.placement.PlacementItem;
import grondag.hard_science.superblock.placement.PlacementMode;
import grondag.hard_science.superblock.varia.BlockSubstance;

/**
 * This is a packet that can be used to update the NBT on the held item of a player.
 */
public class ConfigurePlacementItem extends AbstractPlayerToServerPacket<ConfigurePlacementItem>
{
    
    private int meta;
    private ModelState modelState;
    private BlockSubstance blockSubstance;
    private int lightValue;
    //FIXME: these will be replaced when placement semantics change
    private EnumFacing face;
    private PlacementMode mode;
    private Rotation rotation;
    

    public ConfigurePlacementItem() 
    {
    }
    
    public ConfigurePlacementItem(ItemStack stack) 
    {
        this.meta = stack.getItemDamage();
        this.modelState = PlacementItem.getStackModelState(stack);
        if(this.modelState == null) this.modelState = new ModelState();
        this.blockSubstance = PlacementItem.getStackSubstance(stack);
        this.lightValue = PlacementItem.getStackLightValue(stack);
        this.face = PlacementItem.getFace(stack);
        this.mode = PlacementItem.getMode(stack);
        this.rotation = PlacementItem.getRotation(stack);
    }

    @Override
    public void fromBytes(PacketBuffer pBuff) 
    {
        this.meta = pBuff.readByte();
        this.modelState = new ModelState();
        this.modelState.fromBytes(pBuff);
        this.blockSubstance = pBuff.readEnumValue(BlockSubstance.class);
        this.lightValue = pBuff.readByte();
        this.face = pBuff.readEnumValue(EnumFacing.class);
        this.mode = pBuff.readEnumValue(PlacementMode.class);
        this.rotation = pBuff.readEnumValue(Rotation.class);
    }

    @Override
    public void toBytes(PacketBuffer pBuff) 
    {
        pBuff.writeByte(this.meta);
        this.modelState.toBytes(pBuff);
        pBuff.writeEnumValue(this.blockSubstance);
        pBuff.writeByte(this.lightValue);
        pBuff.writeEnumValue(this.face);
        pBuff.writeEnumValue(this.mode);
        pBuff.writeEnumValue(this.rotation);
    }
   
    @Override
    protected void handle(ConfigurePlacementItem message, EntityPlayerMP player)
    {
        ItemStack heldStack = player.getHeldItem(EnumHand.MAIN_HAND);
        if(PlacementItem.isPlacementItem(heldStack))
        {
            heldStack.setItemDamage(message.meta);
            PlacementItem.setStackModelState(heldStack, message.modelState);
            PlacementItem.setStackSubstance(heldStack, message.blockSubstance);
            PlacementItem.setStackLightValue(heldStack, message.lightValue);
            PlacementItem.setFace(heldStack, message.face);
            PlacementItem.setMode(heldStack, message.mode);
            PlacementItem.setRotation(heldStack, message.rotation);
        }
    }
}
