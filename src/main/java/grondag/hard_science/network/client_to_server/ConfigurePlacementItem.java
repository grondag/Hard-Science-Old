package grondag.hard_science.network.client_to_server;


import grondag.hard_science.network.AbstractPlayerToServerPacket;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.placement.PlacementItem;
import grondag.hard_science.superblock.placement.PlacementMode;
import grondag.hard_science.superblock.placement.PlacementOrientationAxis;
import grondag.hard_science.superblock.placement.PlacementOrientationCorner;
import grondag.hard_science.superblock.placement.PlacementOrientationEdge;
import grondag.hard_science.superblock.placement.PlacementOrientationFace;
import grondag.hard_science.superblock.varia.BlockSubstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;

/**
 * This is a packet that can be used to update the NBT on the held item of a player.
 */
public class ConfigurePlacementItem extends AbstractPlayerToServerPacket<ConfigurePlacementItem>
{
    
    private int meta;
    private ModelState modelState;
    private BlockSubstance blockSubstance;
    private int lightValue;
    private PlacementMode mode;
    private PlacementOrientationAxis axis;
    private PlacementOrientationFace face;
    private PlacementOrientationEdge edge;
    private PlacementOrientationCorner corner;
//    private BlockPos selectedRegionStart;
//    private BlockPos selectedRegion;
    
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
        this.mode = PlacementItem.getMode(stack);
        this.axis = PlacementItem.getOrientationAxis(stack);
        this.face = PlacementItem.getOrientationFace(stack);
        this.edge = PlacementItem.getOrientationEdge(stack);
        this.corner = PlacementItem.getOrientationCorner(stack);
//        this.selectedRegionStart = PlacementItem.isSelectRegionInProgress(stack) ? PlacementItem.selectedRegionStart(stack) : null;
//        this.selectedRegion = PlacementItem.selectedRegion(stack);
    }

    @Override
    public void fromBytes(PacketBuffer pBuff) 
    {
        this.meta = pBuff.readByte();
        this.modelState = new ModelState();
        this.modelState.fromBytes(pBuff);
        this.blockSubstance = pBuff.readEnumValue(BlockSubstance.class);
        this.lightValue = pBuff.readByte();
        this.mode = PlacementMode.FILL_REGION.fromBytes(pBuff);
        this.axis = PlacementOrientationAxis.DYNAMIC.fromBytes(pBuff);
        this.face = PlacementOrientationFace.DYNAMIC.fromBytes(pBuff);
        this.edge = PlacementOrientationEdge.DYNAMIC.fromBytes(pBuff);
        this.corner = PlacementOrientationCorner.DYNAMIC.fromBytes(pBuff);
//        this.selectedRegionStart = pBuff.readBoolean() ? pBuff.readBlockPos() : null;
//        this.selectedRegion = pBuff.readBoolean() ? pBuff.readBlockPos() : null;
    }

    @Override
    public void toBytes(PacketBuffer pBuff) 
    {
        pBuff.writeByte(this.meta);
        this.modelState.toBytes(pBuff);
        pBuff.writeEnumValue(this.blockSubstance);
        pBuff.writeByte(this.lightValue);
        this.mode.toBytes(pBuff);
        this.axis.toBytes(pBuff);
        this.face.toBytes(pBuff);
        this.edge.toBytes(pBuff);
        this.corner.toBytes(pBuff);
        
//        if(this.selectedRegionStart == null)
//        {
//            pBuff.writeBoolean(false);
//        }
//        else
//        {
//            pBuff.writeBoolean(false);
//            pBuff.writeBlockPos(this.selectedRegionStart);
//        }
//        
//        if(this.selectedRegion == null)
//        {
//            pBuff.writeBoolean(false);
//        }
//        else
//        {
//            pBuff.writeBoolean(false);
//            pBuff.writeBlockPos(this.selectedRegion);
//        }
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
            PlacementItem.setMode(heldStack, message.mode);
            PlacementItem.setOrientationAxis(heldStack, message.axis);
            PlacementItem.setOrientationFace(heldStack, message.face);
            PlacementItem.setOrientationEdge(heldStack, message.edge);
            PlacementItem.setOrientationCorner(heldStack, message.corner);
            
//            if(this.selectedRegionStart == null)
//            {
//                if(PlacementItem.isSelectRegionInProgress(heldStack)) PlacementItem.selectRegionCancel(heldStack);
//            }
//            else
//            {
//                if(!PlacementItem.isSelectRegionInProgress(heldStack)) PlacementItem.selectRegionStart(heldStack, this.selectedRegionStart, false);
//            }
//            
//            if(this.selectedRegion != null)
//            {
//                PlacementItem.selectedRegionUpdate(heldStack, this.selectedRegion);
//            }
        }
    }
}
