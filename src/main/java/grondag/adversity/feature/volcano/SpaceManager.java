package grondag.adversity.feature.volcano;

import java.util.HashSet;
import java.util.TreeMap;

import grondag.adversity.Adversity;
import grondag.adversity.library.RelativeBlockPos;
import grondag.adversity.library.Useful;
import net.minecraft.util.math.BlockPos;

public class SpaceManager 
{
	private final BlockPos pos;
	private TreeMap<Integer, HashSet<OpenSpace>> spaces;
	private int spaceCount = 0;

	public SpaceManager(BlockPos pos)
	{
		this.pos = pos;
		spaces = new TreeMap<Integer, HashSet<OpenSpace>>();
	}

	public int getCount() 
	{
		return this.spaceCount;
	}
	
	public SpaceManager(BlockPos pos, int[] values)
	{
		this(pos);
		
        //to be valid, must have a multiple of two
        if(values.length % 2 != 0)
        {
            Adversity.log.warn("Invalid open space data loading volcano at " + pos.toString()
                    + ". Volcano may not place lava properly.");
            return;
        }
        
		int i = 0;
		while(i < values.length)
		{
		    OpenSpace space = new OpenSpace(values[i++], values[i++]);
	        int distanceHash = space.getDistanceHash();
	        
	        if(!spaces.containsKey(distanceHash))
	        {
	            spaces.put(distanceHash, new HashSet<OpenSpace>());
	        }
	        spaces.get(distanceHash).add(space);
	        this.spaceCount++;
		}
	}
	
	public int[] getArray()
	{
	    int[] result = new int[this.spaceCount * 2];
	    int i = 0;
	    
	    for(HashSet<OpenSpace> set: this.spaces.values())
	    {
	        for(OpenSpace space : set)
	        {
	            result[i++] = space.pos;
	            result[i++] = space.origin;
	        }
	    }	    
		return result;
	}

	public void add(BlockPos posIn, BlockPos origin)
	{
        OpenSpace space = new OpenSpace(posIn, origin);
        int distanceHash = space.getDistanceHash();
        
        if(!spaces.containsKey(distanceHash))
        {
            spaces.put(distanceHash, new HashSet<OpenSpace>());
        }
        spaces.get(distanceHash).add(space);
        this.spaceCount++;
	}
	
	public OpenSpace pollFirstEntry()
	{
		OpenSpace result = null;
	    HashSet<OpenSpace> things = spaces.firstEntry().getValue();
        if(!things.isEmpty())
        {
        	result= things.iterator().next();
            things.remove(result);
            spaceCount--;
            if(things.isEmpty())
            {
                spaces.remove(result.getDistanceHash());
            }
        }
        return result;
	}
	
	public class OpenSpace
	{
		private final int pos;
		private final int origin;

		private OpenSpace(BlockPos pos, BlockPos origin)
		{
			this(RelativeBlockPos.getKey(pos, SpaceManager.this.pos), RelativeBlockPos.getKey(origin, SpaceManager.this.pos));
		}

		private OpenSpace(int pos, int origin)
		{
			this.pos = pos;
			this.origin = origin;
		}

		public BlockPos getPos()
		{
			return RelativeBlockPos.getPos(pos, SpaceManager.this.pos);
		}

		public BlockPos getOrigin()
		{
			return RelativeBlockPos.getPos(origin, SpaceManager.this.pos);
		}

		/**
		 * Generates hash keys that facilitate sorting of spaces for new placement.
		 * Lower blocks come first.  Blocks within the same level are sorted by distance
		 * from center of volcano.
		 */
		public int getDistanceHash()
		{
			BlockPos myPos = this.getPos();
			int dx = myPos.getX() - SpaceManager.this.pos.getX();
			int dz = myPos.getZ() - SpaceManager.this.pos.getZ();
			return myPos.getY() << 20 | (dx * dx + dz * dz);
		}

		@Override
		public int hashCode()
		{
			return (int) (Useful.longHash((((long)origin) << 32) | pos) & 0xFFFFFFFF);
		}
	}
}
