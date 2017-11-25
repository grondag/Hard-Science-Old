package grondag.hard_science.superblock.placement.spec;

import grondag.hard_science.superblock.placement.PlacementSpecType;

/**
 * Placement that defines target region based on blocks that match a given predicate.
 */
public class PredicatePlacementSpec extends SingleStackPlacementSpec
{
    public PredicatePlacementSpec() {};
    
    protected PredicatePlacementSpec(PredicateBuilder builder)
    {
        super(builder);
    }

    @Override
    public PlacementSpecType specType()
    {
        return PlacementSpecType.PREDICATE;
    }
}