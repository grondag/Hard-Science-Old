package grondag.hard_science.superblock.placement.spec;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.world.CubicBlockRegion;
import grondag.hard_science.superblock.placement.PlacementSpecType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

/**
     * Multi-block placement of the same block in a cuboid region
     */
    public class CuboidPlacementSpec extends VolumetricPlacementSpec
    {
        CubicBlockRegion region;
        
        public CuboidPlacementSpec() {};
        
        protected CuboidPlacementSpec(CuboidBuilder builder)
        {
            super(builder);
        }
        
        @Override
        public void deserializeNBT(NBTTagCompound tag)
        {
            super.deserializeNBT(tag);
            this.isHollow= tag.getBoolean(ModNBTTag.PLACMENT_IS_HOLLOW);
            BlockPos minPos = BlockPos.fromLong(tag.getLong(ModNBTTag.PLACEMENT_FIXED_REGION_START_POS));
            BlockPos maxPos = BlockPos.fromLong(tag.getLong(ModNBTTag.PLACEMENT_FIXED_REGION_END_POS));
            this.region = new CubicBlockRegion(minPos, maxPos, this.isHollow);
        }

        @Override
        public void serializeNBT(NBTTagCompound tag)
        {
            super.serializeNBT(tag);
            tag.setBoolean(ModNBTTag.PLACMENT_IS_HOLLOW, this.isHollow);
            tag.setLong(ModNBTTag.PLACEMENT_FIXED_REGION_START_POS, this.region.minPos().toLong());
            tag.setLong(ModNBTTag.PLACEMENT_FIXED_REGION_END_POS, this.region.maxPos().toLong());
        }

        @Override
        public PlacementSpecType specType()
        {
            return PlacementSpecType.CUBOID;
        }

 
    }