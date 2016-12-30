package grondag.adversity.feature.volcano.lava;

import static org.junit.Assert.*;

import org.junit.Test;

public class LavaCellTest
{

    @Test
    public void test()
    {
        assert(LavaCell.makeSortKey(-1.23F, 0.04F, 1) < LavaCell.makeSortKey(-1.1F, 0.04F, 1));
        assert(LavaCell.makeSortKey(-1.23F, 0.04F, 1) < LavaCell.makeSortKey(-1.23F, 0.3F, 1));
        assert(LavaCell.makeSortKey(-1.23F, 0.04F, 1) < LavaCell.makeSortKey(-1.1F, 0.9F, 1));
        
        assert((LavaCell.makeSortKey(-1.23F, 0.04F, 1) & 1) == 1);
        assert((LavaCell.makeSortKey(-1.23F, 0.04F, 3) & 3) == 3);
    }

}
