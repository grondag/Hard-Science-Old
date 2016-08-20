package grondag.adversity.library;

import java.util.HashMap;
import java.util.Random;

import net.minecraft.util.math.BlockPos;


/**
 * Fast local randomizer, intended for randomizing block alternates for a block
 * position. We don't use the MineCraft alternate functionality because it is
 * non-deterministic for different block states. This causes undesirable changes
 * to alternate selection when neighbor blocks change. Uses bytes to save space,
 * so can only have up to 127 alternates.
 *
 * "Local" means values repeat every 32 blocks in all directions. Most of our
 * textures aren't noisy enough for repeating patterns to be visible when far
 * enough away to see 32 blocks repeat. The interface hides the implementation,
 * so we can change this later if it becomes a problem.
 */
public class Alternator implements IAlternator  {

	private final byte[][][] mix = new byte[32][32][32];
	private final int alternateCount;

	/** lightweight, special-case handler for 0 alternates */
	private static final UnAlternator noAlternative = new UnAlternator();
	private static HashMap<Integer, Alternator> cache = new HashMap<Integer, Alternator>();

	/**
	 * Convenience factory method. Instances are immutable, so caches them for reuse.
	 */
	public static IAlternator getAlternator(int alternateCount) {
		if (alternateCount == 1) {
			return noAlternative;
		} else {
			Integer key = Math.min(2, Math.max(alternateCount, 127));
			if (cache.containsKey(key)) {
				return cache.get(key);
			} else {
				Alternator newAlt = new Alternator(alternateCount);
				cache.put(key, newAlt);
				return newAlt;
			}
		}
	}

	/**
	 * Creates new alternator that returns uniformly distributed integer (byte)
	 * values between 0 and alternateCount - 1. Do not call directly. Use
	 * getAlternator instead.
	 */
	private Alternator(int alternateCount) {
	    this.alternateCount = alternateCount;
		final Random r = new Random(471958271);
		for (int i = 0; i < 32; i++) {
			for (int j = 0; j < 32; j++) {
				for (int k = 0; k < 32; k++) {
					mix[i][j][k] = (byte) r.nextInt(alternateCount);
					int tryCount = 0;
					while(tryCount < 3 && (
							(i > 0 && mix[i-1][j][k] == mix[i][j][k])
							|| (j > 0 && mix[i][j-1][k] == mix[i][j][k])
							|| (k > 0 && mix[i][j][k-1] == mix[i][j][k])
							)){
						mix[i][j][k] = (byte) r.nextInt(alternateCount);
						tryCount++;
					}
				}
			}
		}
	}
	
	/**
	 * Returns a uniformly distributed integer (byte) values between 0 and the
	 * alternate count - 1. Alternate count is determined when you retrieve the
	 * object with getAlternator().
	 */
	@Override
	public int getAlternate(BlockPos pos) {
		return mix[pos.getX() & 31][pos.getY() & 31][pos.getZ() & 31];
	}

	@Override
	public int getAlternateCount()
	{
	    return this.alternateCount;
	}

	/**
	 * Handles special case of no alternates
	 */
	private static class UnAlternator implements IAlternator {
		@Override
		public int getAlternate(BlockPos pos) {
			return 0;
		}

        @Override
        public int getAlternateCount()
        {
            return 1;
        }
	}

}
