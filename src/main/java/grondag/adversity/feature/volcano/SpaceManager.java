package grondag.adversity.feature.volcano;

import java.util.TreeSet;

import com.google.common.collect.ComparisonChain;

import grondag.adversity.Adversity;
import grondag.adversity.library.RelativeBlockPos;
import net.minecraft.util.math.BlockPos;

public class SpaceManager 
{
    private final BlockPos pos;
    private TreeSet<OpenSpace> spaces;
    
    private static final int FLAG_SUPPORTED = 1;
    private static final int FLAG_TOP = 2;
    private static final int FLAG_DESC = 4;

    public SpaceManager(BlockPos pos)
    {
        this.pos = pos;
        spaces = new TreeSet<OpenSpace>();
    }

    public int getCount() 
    {
        return spaces.size();
    }

    public SpaceManager(BlockPos pos, int[] values)
    {
        this(pos);

        //to be valid, must have a multiple of three
        if(values.length % 3 != 0)
        {
            Adversity.log.warn("Invalid open space data loading volcano at " + pos.toString()
            + ". Volcano may not place lava properly.");
            return;
        }

        int i = 0;
        while(i < values.length)
        {
            spaces.add(new OpenSpace(values[i++], values[i++], 
                    (values[i] & FLAG_SUPPORTED) == FLAG_SUPPORTED,
                    (values[i] & FLAG_TOP) == FLAG_TOP,
                    (values[i++] & FLAG_DESC) == FLAG_DESC));
        }
    }

    public int[] getArray()
    {
        int[] result = new int[this.spaces.size() * 3];
        int i = 0;

        for(OpenSpace space: this.spaces)
        {
            result[i++] = space.pos;
            result[i++] = space.origin;
            result[i++] = (space.isSupported ? FLAG_SUPPORTED : 0) 
                    | (space.isTop ? FLAG_TOP : 0)
                    | (space.isDescending ? FLAG_DESC : 0);
        }	    
        return result;
    }

    public void add(BlockPos posIn, BlockPos origin, boolean isSupported, boolean isTop, boolean isDescending)
    {
        //	    Adversity.log.info("adding space @ " + posIn.toString() + " with origin " + origin.toString());
        spaces.add(new OpenSpace(posIn, origin, isSupported, isTop, isDescending));
    }

    public OpenSpace pollFirst()
    {
        return spaces.pollFirst();
    }

    public OpenSpace peekFirst()
    {
        return spaces.first();
    }
    
//    public OpenSpace pollLast()
//    {
//        return spaces.pollLast();
//    }

    public class OpenSpace implements Comparable<OpenSpace>
    {
        private final int pos;
        private final int origin;
        private final int distance;
        private final boolean isSupported;
        private final boolean isTop;
        /** true if this origin is result of a descending flow */
        private final boolean isDescending;

        private OpenSpace(BlockPos pos, BlockPos origin, boolean isSupported, boolean isTop, boolean isDescending)
        {
            this(RelativeBlockPos.getKey(pos, SpaceManager.this.pos), RelativeBlockPos.getKey(origin, SpaceManager.this.pos), isSupported, isTop, isDescending);
        }

        private OpenSpace(int pos, int origin, boolean isSupported, boolean isTop, boolean isDescending)
        {
            this.pos = pos;
            this.origin = origin;
            this.isSupported = isSupported;
            this.isTop = isTop;
            this.isDescending = isDescending;
            int dx = getPos().getX() - SpaceManager.this.pos.getX();
            int dz = getPos().getZ() - SpaceManager.this.pos.getZ();
            this.distance = dx * dx + dz * dz;
        }

        public BlockPos getPos()
        {
            return RelativeBlockPos.getPos(pos, SpaceManager.this.pos);
        }

        public BlockPos getOrigin()
        {
            return RelativeBlockPos.getPos(origin, SpaceManager.this.pos);
        }
        
        public boolean isDescending()
        {
            return this.isDescending;
        }

        @Override
        public int compareTo(OpenSpace other)
        {
            return ComparisonChain.start()
                    .compareFalseFirst(this.isTop, other.isTop)
                    .compareTrueFirst(this.isSupported, other.isSupported)
                    .compare(other.getPos().getY(), this.getPos().getY())
                    .compare(this.distance, other.distance)
                    .compare(this.pos, other.pos)
                    .result();
        }
    }
}
