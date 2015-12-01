package grondag.adversity.library;

import java.util.HashMap;
import java.util.Random;

import net.minecraft.util.BlockPos;

/**
 * Fast randomizer, intended for randomizing block variants for a position.
 * Uses bytes to save space, so can only have up to 255 variants.
 * @author grondag
 *
 */
public class Alternator implements IAlternator{
	
	private final byte[][][] mix = new byte[32][32][32];
	
	private static final UnAlternator noAlternative = new UnAlternator();
	private static HashMap<Integer, Alternator> cache = new HashMap<Integer, Alternator>();
	
	/** 
	 * Convenience factory method. Caches for reuse.
	 * @return
	 */
	public static IAlternator getAlternator(byte alternateCount){
		if(alternateCount == (byte)1){
			return noAlternative;
		} else {
			Integer key = (Integer)(int) alternateCount;
			if(cache.containsKey(key)){
				return cache.get(key);
			} else {
				Alternator newAlt = new Alternator(alternateCount);
				cache.put(key, newAlt);
				return newAlt;
			}
		}
	}
	
	Alternator(byte alternateCount){
		final Random r = new Random(471958271);
		for (int i = 0; i < 32; i++) {
			for (int j = 0; j < 32; j++) {
				for (int k = 0; k < 32; k++) {
					mix[i][j][k] = (byte) r.nextInt(alternateCount);
				}
			}
		}		

	}
	
	public int getAlternate(BlockPos pos){
		return (int) mix[pos.getX() & 31][pos.getY() & 31][pos.getZ() & 31];
	}

	/**
	 * handles special case of no alternates
	 * @author grondag
	 *
	 */
	private static class UnAlternator implements IAlternator{
		@Override
		public int getAlternate(BlockPos pos) {
			return 0;
		}
	}

}
