package grondag.hard_science.network.client_to_server;


import grondag.hard_science.network.AbstractPlayerToServerPacket;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.placement.PlacementItem;
import grondag.hard_science.superblock.placement.PlacementItem.FixedRegionBounds;
import grondag.hard_science.superblock.placement.TargetMode;
import grondag.hard_science.superblock.placement.RegionOrientation;
import grondag.hard_science.superblock.placement.SpeciesMode;
import grondag.hard_science.superblock.placement.BlockOrientationAxis;
import grondag.hard_science.superblock.placement.BlockOrientationCorner;
import grondag.hard_science.superblock.placement.BlockOrientationEdge;
import grondag.hard_science.superblock.placement.BlockOrientationFace;
import grondag.hard_science.superblock.placement.FilterMode;
import grondag.hard_science.superblock.varia.BlockSubstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

/**
 * This is a packet that can be used to update the NBT on the held item of a player.
 */
public class ConfigurePlacementItem extends AbstractPlayerToServerPacket<ConfigurePlacementItem>
{
    
    private int meta;
    private ModelState modelState;
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
        this.mode = PlacementItem.getTargetMode(stack);
        this.axis = PlacementItem.getBlockOrientationAxis(stack);
        this.face = PlacementItem.getBlockOrientationFace(stack);
        this.edge = PlacementItem.getBlockOrientationEdge(stack);
        this.corner = PlacementItem.getBlockOrientationCorner(stack);
        this.floatingSelectionRange = PlacementItem.getFloatingSelectionRange(stack);
        this.regionOrientation = PlacementItem.getRegionOrientation(stack);
        this.filterMode = PlacementItem.getFilterMode(stack);
        this.speciesMode = PlacementItem.getSpeciesMode(stack);
        this.isDeleteModeEnabled = PlacementItem.isDeleteModeEnabled(stack);
        this.isFixedRegionEnabled = PlacementItem.isFixedRegionEnabled(stack);
        this.regionSize = PlacementItem.getRegionSize(stack, false);
    }

    @Override
    public void fromBytes(PacketBuffer pBuff) 
    {
        this.meta = pBuff.readByte();
        this.modelState = new ModelState();
        this.modelState.fromBytes(pBuff);
        this.blockSubstance = pBuff.readEnumValue(BlockSubstance.class);
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
        pBuff.writeEnumValue(this.blockSubstance);
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
    protected void handle(ConfigurePlacementItem message, EntityPlayerMP player)
    {
        ItemStack heldStack = player.getHeldItem(EnumHand.MAIN_HAND);
        if(PlacementItem.isPlacementItem(heldStack))
        {
            heldStack.setItemDamage(message.meta);
            PlacementItem.setStackModelState(heldStack, message.modelState);
            PlacementItem.setStackSubstance(heldStack, message.blockSubstance);
            PlacementItem.setStackLightValue(heldStack, message.lightValue);
            PlacementItem.setTargetMode(heldStack, message.mode);
            PlacementItem.setBlockOrientationAxis(heldStack, message.axis);
            PlacementItem.setBlockOrientationFace(heldStack, message.face);
            PlacementItem.setBlockOrientationEdge(heldStack, message.edge);
            PlacementItem.setBlockOrientationCorner(heldStack, message.corner);
            PlacementItem.setSelectionTargetRange(heldStack, message.floatingSelectionRange);
            PlacementItem.setRegionOrientation(heldStack, message.regionOrientation);
            PlacementItem.setFilterMode(heldStack, message.filterMode);
            PlacementItem.setSpeciesMode(heldStack, message.speciesMode);
            PlacementItem.setDeleteModeEnabled(heldStack, message.isDeleteModeEnabled);
            PlacementItem.setFixedRegionEnabled(heldStack, message.isFixedRegionEnabled);
            PlacementItem.setRegionSize(heldStack, message.regionSize);
        }
    }
}
