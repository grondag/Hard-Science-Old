package grondag.hard_science.network.client_to_server;


import grondag.exotic_matter.model.BlockSubstance;
import grondag.exotic_matter.model.ISuperModelState;
import grondag.hard_science.network.AbstractPlayerToServerPacket;
import grondag.hard_science.superblock.placement.PlacementItem;
import grondag.hard_science.superblock.placement.TargetMode;
import grondag.hard_science.superblock.placement.RegionOrientation;
import grondag.hard_science.superblock.placement.SpeciesMode;
import grondag.hard_science.superblock.model.state.ModelState;
import grondag.hard_science.superblock.placement.BlockOrientationAxis;
import grondag.hard_science.superblock.placement.BlockOrientationCorner;
import grondag.hard_science.superblock.placement.BlockOrientationEdge;
import grondag.hard_science.superblock.placement.BlockOrientationFace;
import grondag.hard_science.superblock.placement.FilterMode;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

/**
 * This is a packet that can be used to update the NBT on the held item of a player.
 */
public class PacketConfigurePlacementItem extends AbstractPlayerToServerPacket<PacketConfigurePlacementItem>
{
    
    private int meta;
    private ISuperModelState modelState;
    private BlockSubstance blockSubstance;
    private int lightValue;
    private TargetMode mode;
    private BlockOrientationAxis axis;
    private BlockOrientationFace face;
    private BlockOrientationEdge edge;
    private BlockOrientationCorner corner;
    private RegionOrientation regionOrientation;
    private int floatingSelectionRange;
    private FilterMode filterMode;
    private SpeciesMode speciesMode;
    private boolean isDeleteModeEnabled;
    private boolean isFixedRegionEnabled;
    private BlockPos regionSize;
    
    
    public PacketConfigurePlacementItem() 
    {
    }
    
    public PacketConfigurePlacementItem(ItemStack stack) 
    {
        this.meta = stack.getItemDamage();
        
        this.modelState = PlacementItem.getStackModelState(stack);
        if(this.modelState == null) this.modelState = new ModelState();
        
        this.blockSubstance = PlacementItem.getStackSubstance(stack);
        this.lightValue = PlacementItem.getStackLightValue(stack);
        
        if(PlacementItem.isPlacementItem(stack))
        {
            PlacementItem item = (PlacementItem)stack.getItem();
            this.mode = item.getTargetMode(stack);
            this.axis = item.getBlockOrientationAxis(stack);
            this.face = item.getBlockOrientationFace(stack);
            this.edge = item.getBlockOrientationEdge(stack);
            this.corner = item.getBlockOrientationCorner(stack);
            this.floatingSelectionRange = item.getFloatingSelectionRange(stack);
            this.regionOrientation = item.getRegionOrientation(stack);
            this.filterMode = item.getFilterMode(stack);
            this.speciesMode = item.getSpeciesMode(stack);
            this.isDeleteModeEnabled = false;//item.isDeleteModeEnabled(stack);
            this.isFixedRegionEnabled = item.isFixedRegionEnabled(stack);
            this.regionSize = item.getRegionSize(stack, false);
        }
    }

    @Override
    public void fromBytes(PacketBuffer pBuff) 
    {
        this.meta = pBuff.readByte();
        this.modelState = new ModelState();
        this.modelState.fromBytes(pBuff);
        this.blockSubstance = BlockSubstance.fromBytes(pBuff);
        this.lightValue = pBuff.readByte();
        this.mode = TargetMode.FILL_REGION.fromBytes(pBuff);
        this.axis = BlockOrientationAxis.DYNAMIC.fromBytes(pBuff);
        this.face = BlockOrientationFace.DYNAMIC.fromBytes(pBuff);
        this.edge = BlockOrientationEdge.DYNAMIC.fromBytes(pBuff);
        this.corner = BlockOrientationCorner.DYNAMIC.fromBytes(pBuff);
        this.floatingSelectionRange = pBuff.readByte();
        this.regionOrientation = RegionOrientation.XYZ.fromBytes(pBuff);
        this.filterMode = FilterMode.FILL_REPLACEABLE.fromBytes(pBuff);
        this.speciesMode = SpeciesMode.MATCH_CLICKED.fromBytes(pBuff);
        this.isDeleteModeEnabled = pBuff.readBoolean();
        this.isFixedRegionEnabled = pBuff.readBoolean();
        this.regionSize = pBuff.readBlockPos();

    }

    @Override
    public void toBytes(PacketBuffer pBuff) 
    {
        pBuff.writeByte(this.meta);
        this.modelState.toBytes(pBuff);
        this.blockSubstance.toBytes(pBuff);
        pBuff.writeByte(this.lightValue);
        this.mode.toBytes(pBuff);
        this.axis.toBytes(pBuff);
        this.face.toBytes(pBuff);
        this.edge.toBytes(pBuff);
        this.corner.toBytes(pBuff);
        pBuff.writeByte(floatingSelectionRange);
        this.regionOrientation.toBytes(pBuff);
        this.filterMode.toBytes(pBuff);
        this.speciesMode.toBytes(pBuff);
        pBuff.writeBoolean(this.isDeleteModeEnabled);
        pBuff.writeBoolean(this.isFixedRegionEnabled);
        pBuff.writeBlockPos(this.regionSize);
    }
   
    @Override
    protected void handle(PacketConfigurePlacementItem message, EntityPlayerMP player)
    {
        ItemStack heldStack = player.getHeldItem(EnumHand.MAIN_HAND);
        if(PlacementItem.isPlacementItem(heldStack))
        {
            heldStack.setItemDamage(message.meta);
            PlacementItem.setStackModelState(heldStack, message.modelState);
            PlacementItem.setStackSubstance(heldStack, message.blockSubstance);
            PlacementItem.setStackLightValue(heldStack, message.lightValue);
            PlacementItem item = (PlacementItem)heldStack.getItem();
            item.setTargetMode(heldStack, message.mode);
            item.setBlockOrientationAxis(heldStack, message.axis);
            item.setBlockOrientationFace(heldStack, message.face);
            item.setBlockOrientationEdge(heldStack, message.edge);
            item.setBlockOrientationCorner(heldStack, message.corner);
            item.setSelectionTargetRange(heldStack, message.floatingSelectionRange);
            item.setRegionOrientation(heldStack, message.regionOrientation);
            item.setFilterMode(heldStack, message.filterMode);
            item.setSpeciesMode(heldStack, message.speciesMode);
//            item.setDeleteModeEnabled(heldStack, message.isDeleteModeEnabled);
            item.setFixedRegionEnabled(heldStack, message.isFixedRegionEnabled);
            item.setRegionSize(heldStack, message.regionSize);
        }
    }
}
