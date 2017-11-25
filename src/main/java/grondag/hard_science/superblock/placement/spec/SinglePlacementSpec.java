package grondag.hard_science.superblock.placement.spec;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.superblock.placement.PlacementSpecType;

/**
     * Places a single block.
     */
    public class SinglePlacementSpec extends SingleStackPlacementSpec
    {
        public SinglePlacementSpec() {};
        
        protected SinglePlacementSpec(SingleStackBuilder builder)
        {
            super(builder);
            SingleStackEntry entry = new SingleStackEntry(0, builder.placementPosition().inPos);
            this.entries = ImmutableList.of(entry);
        }

        @Override
        public PlacementSpecType specType()
        {
            return PlacementSpecType.SINGLE;
        }

 
    }