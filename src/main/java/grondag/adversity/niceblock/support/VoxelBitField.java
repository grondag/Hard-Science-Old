package grondag.adversity.niceblock.support;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3i;


public class VoxelBitField
{
    private final int power;
    
    private final BitSet bits;
    
    private Vec3i origin;
    
    private double inflatedVolume = 0;
    /** if this was the result of a simplification, the empty space added */
    public double getInflatedVolume() { return inflatedVolume; }
    
    private double discardedVolume = 0;
    /** if this was the result of a simplification, the filled space discarded */
    public double getDiscardedVolume() { return discardedVolume; }   

    private ArrayList<DistanceRankedVoxel> distances;
    
    private static class DistanceRankedVoxel extends Vec3i
    {
        private final double distanceSq;
        
        private DistanceRankedVoxel(int x, int y, int z, Vec3i origin)
        {
            super(x, y, z);
            distanceSq = this.distanceSq(origin);
        }
    }
    
    private static class DistanceComparator implements Comparator<DistanceRankedVoxel>
    {
        private static final DistanceComparator INSTANCE = new DistanceComparator();
        
        @Override
        public int compare(DistanceRankedVoxel o1, DistanceRankedVoxel o2)
        {
            if(o1.distanceSq < o2.distanceSq)
                return -1;
            else if(o1.distanceSq == o2.distanceSq)
                return 0;
            else
                return 1;
        }
        
    }
    
        
    private int firstDistanceIndex;
    
    /**
     * Power is how many bits per dimension.
     * Equates to powers of 2.
     */
    public VoxelBitField(int power)
    {
        this.power = power;
        this.bits = new BitSet(getBitsInCube());
    }
    
    /** total number of voxel bits in the cube */
    private int getBitsInCube()
    {
        return 1 << (power * 3);
    }
    
    private VoxelBitField(VoxelBitField template)
    {
        this.power = template.power;
        this.bits = (BitSet) template.bits.clone();
    }
    
    public VoxelBitField clone()
    {
        return new VoxelBitField(this);
    }
    
    public VoxelBitField simplify()
    {
        if(power < 2) return clone();
        
        VoxelBitField retVal = new VoxelBitField(power - 1);
        
        int inflationCount = 0;
        int discardCount = 0;
        
        for(int i = 0; i < retVal.getBitsPerAxis(); i++)
        {
            for(int j = 0; j < retVal.getBitsPerAxis(); j++)
            {
                for(int k = 0; k < retVal.getBitsPerAxis(); k++)
                {
                    int fillCount = 0;
                    if(isFilled(i * 2, j * 2, k * 2)) fillCount++;
                    if(isFilled(i * 2, j * 2, k * 2 + 1)) fillCount++;
                    if(isFilled(i * 2, j * 2 + 1, k * 2)) fillCount++;
                    if(isFilled(i * 2, j * 2 + 1, k * 2 + 1)) fillCount++;
                    if(isFilled(i * 2 + 1, j * 2, k * 2)) fillCount++;
                    if(isFilled(i * 2 + 1, j * 2, k * 2 + 1)) fillCount++;
                    if(isFilled(i * 2 + 1, j * 2 + 1, k * 2)) fillCount++;
                    if(isFilled(i * 2 + 1, j * 2 + 1, k * 2 + 1)) fillCount++;
                    
                    if(fillCount > 5)
                    {
                        retVal.setFilled(i, j, k, fillCount > 4);
                        inflationCount += (8-fillCount);
                    }
                    else
                    {
                        discardCount += fillCount;
                    }
                }
            }
        }
        
        retVal.discardedVolume = (double)discardCount / this.getBitsInCube();
        retVal.inflatedVolume = (double)inflationCount / this.getBitsInCube();
        return retVal;
    }
    
    public int getBitsPerAxis()
    {
        return 1 << power;
    }
    
    public double getFilledRatio()
    {
        return (double) bits.cardinality() / (getBitsInCube());
    }
    
    private int clamp(int i)
    {
        return Math.max(0, Math.min(i, getBitsPerAxis() - 1));
    }
    
    public void setOrigin(int x, int y, int z)
    {
        this.origin = new Vec3i(clamp(x), clamp(y), clamp(z));
        this.distances = new ArrayList<DistanceRankedVoxel>(getBitsPerAxis() * getBitsPerAxis() * getBitsPerAxis());
        this.firstDistanceIndex = 0;
        
        for(int i = 0; i < getBitsPerAxis(); i++)
        {
            for(int j = 0; j < getBitsPerAxis(); j++)
            {
                for(int k = 0; k < getBitsPerAxis(); k++)
                {
                    DistanceRankedVoxel point = new DistanceRankedVoxel(i, j, k, origin);
                    this.distances.add(point);
                }
            }
        }
        this.distances.sort(DistanceComparator.INSTANCE);
    }
    
    /**
     * True if all bits that were true at origin set appear
     * to have been consumed via bounding box creation.
     */
    public boolean areAllBitsConsumed()
    {
        return firstDistanceIndex >= distances.size() || distances.size() == 0;
    }
    
    /** 
     * Creates a 1-voxel box at a filled voxel nearest the origin.
     * The bit in this voxel is set unfilled when it is created.
     * 
     * Assumes that voxels that no more voxels are filled after origin is set.
     * (Voxels are only unfilled after that, as boxes are created that consume them.)
     * Returns null if no more filled voxels.
     */
    public VoxelBox getNearestVoxelBox()
    {
        while(firstDistanceIndex < distances.size())
        {
            DistanceRankedVoxel candidate = distances.get(firstDistanceIndex);
            
            if(isFilled(candidate.getX(), candidate.getY(), candidate.getZ()))
            {
                setFilled(candidate.getX(), candidate.getY(), candidate.getZ(), false);
                return new VoxelBox(candidate.getX(), candidate.getY(), candidate.getZ());
            }
            
            firstDistanceIndex++;
        }
        return null;
    }
    
    private int getAddress(int x, int y, int z)
    {
        return clamp(x) | (clamp(y) << power) | (clamp(z) << (power * 2));
    }
    
    public void setFilled(int x, int y, int z, boolean isFilled)
    {
        bits.set(getAddress(x, y, z), isFilled);
    }
    
    public boolean isFilled(int x, int y, int z)
    {
        return bits.get(getAddress(x, y, z));
    }
    
    /** Sets bits in bounding box.Inclusive of bounds. */
    public void setFilled(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean isFilled)
    {
        for(int z = clamp(minZ); z <= clamp(maxZ); z++)
        {
            for(int y = clamp(minY); y <= clamp(maxY); y++)
            {
                // + 1 because BitSet addressing not inclusive
                bits.set(getAddress(clamp(minX), y, z), getAddress(clamp(maxX), y, z) + 1, isFilled);
            }
        }
    }
    
    /**
     * Returns true if all bits in bounding box are filled.
     */
    public boolean isFilled(int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
    {
        boolean retVal=true;
        for(int z = clamp(minZ); z <= clamp(maxZ); z++)
        {
            for(int y = clamp(minY); y <= clamp(maxY); y++)
            {
                // + 1 because BitSet addressing not inclusive
                for(int x = clamp(minX); x <= clamp(maxX); x++)
                {
                //retVal = retVal && bits.get(getAddress(clamp(minX), y, z), getAddress(clamp(maxX), y, z) + 1).cardinality() == (clamp(maxX) - clamp(minX) + 1);
                    retVal = retVal && isFilled(x, y, z);
                    if(!retVal) break;
                }
                if(!retVal) break;
            }
            if(!retVal) break;
        }
        return retVal;
    }
    
    /** 
     * Creates a 1-voxel box at the given coordinates
     */
    public VoxelBox getVoxelBox(int x, int y, int z)
    {
        return new VoxelBox(clamp(x), clamp(y), clamp(z));
    }
    
    public class VoxelBox
    {
        private int minX;
        private int minY;
        private int minZ;
        private int maxX;
        private int maxY;
        private int maxZ;

        /** 
         * Creates a 1-voxel box at the given coordinates
         */
        private VoxelBox(int x, int y, int z)
        {
            this.minX = x;
            this.minY = y;
            this.minZ = z;
            this.maxX = x;
            this.maxY = y;
            this.maxZ = z;
        }
        
        public boolean canExpand(EnumFacing face)
        {
            return expandify(face, false);
        }
        
        /** 
         * The actual guts of canExpand and expandIfPossible.
         * Handles the testing and bit flipping.
         * Doesn't actually change the bounding values of the box.
         * That is done by expandIfPossible.
         * 
         * Could probably make this much cleaner if I used vectors
         * of some kind but sometimes I enjoy being old fashioned.
         */
        private boolean expandify(EnumFacing face, boolean doit)
        {
            int minTestX = minX;
            int minTestY = minY;
            int minTestZ = minZ;
            int maxTestX = maxX;
            int maxTestY = maxY;
            int maxTestZ = maxZ;
            
            switch(face)
            {
            case UP:
                if(maxY == getBitsPerAxis() - 1) return false;
                minTestY = maxY + 1;
                maxTestY = maxY + 1;
                break;
            
            case DOWN:
                if(minY == 0) return false;
                minTestY = minY - 1;
                maxTestY = minY - 1;
                break;
                
            case EAST:
                // X+
                if(maxX == getBitsPerAxis() - 1) return false;
                minTestX = maxX + 1;
                maxTestX = maxX + 1;
                break;
                
            case WEST:
                // X-
                if(minX == 0) return false;
                minTestX = minX - 1;
                maxTestX = minX - 1;
                break;

            case SOUTH:
                // Z+
                if(maxZ == getBitsPerAxis() - 1) return false;
                minTestZ = maxZ + 1;
                maxTestZ = maxZ + 1;
                break;
                
            case NORTH:
                // Z-
                if(minZ == 0) return false;
                minTestZ = minZ - 1;
                maxTestZ = minZ - 1;
                break;

            default:
                return false;
            }
            
            boolean retVal = isFilled(minTestX, minTestY, minTestZ, maxTestX, maxTestY, maxTestZ);
            
            if(retVal && doit)
            {
                setFilled(minTestX, minTestY, minTestZ, maxTestX, maxTestY, maxTestZ, false);
            }
            
            return retVal;
        }
        
        public boolean expandIfPossible(EnumFacing face)
        {
            boolean retVal = expandify(face, true);
            
            if(retVal)
            {
                switch(face)
                {
                case UP:
                    maxY++;
                    break;
                    
                case DOWN:
                    minY--; 
                    break;
                    
                case EAST:
                    // X+
                    maxX++;
                    break;
                    
                case WEST:
                    // X-
                    minX--;
                    break;
    
                case SOUTH:
                    // Z+
                    maxZ++;
                    break;
                    
                case NORTH:
                    // Z-
                    minZ--;
                    break;
    
                default:
                   //never happens
                }
            }
            return retVal;
        }
        
        public int getMinX()
        {
            return minX;
        }

        public int getMinY()
        {
            return minY;
        }

        public int getMinZ()
        {
            return minZ;
        }

        public int getMaxX()
        {
            return maxX;
        }

        public int getMaxY()
        {
            return maxY;
        }

        public int getMaxZ()
        {
            return maxZ;
        }

    }   
}
