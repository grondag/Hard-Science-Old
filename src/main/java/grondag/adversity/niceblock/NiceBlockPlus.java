package grondag.adversity.niceblock;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import grondag.adversity.Adversity;
import grondag.adversity.niceblock.model.ModelRenderState;
import grondag.adversity.niceblock.support.NicePlacement;

public class NiceBlockPlus extends NiceBlock implements ITileEntityProvider {

	public NiceBlockPlus(NiceStyle style, NicePlacement placer, BaseMaterial material, int metaCount) {
		super(style, placer, material, metaCount);
	}
	
	private static long elapsedTime;
	private static int timerCount = 0;
	private static int hit = 0;
	private static int miss = 0;
	private static int dirtyCount = 0;
	
	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		
		long start = System.nanoTime();
		
    	TileEntity tileentity = world.getTileEntity(pos);
	    if (tileentity != null && tileentity instanceof NiceTileEntity && !((NiceTileEntity)tileentity).dirty ) {
//	    	Adversity.log.info("cache hit @" + pos.toString());
	    	state = ((NiceTileEntity)tileentity).state;
	    	hit++;
	    } else {
    		// should always be an IExtendedBlockState but avoid crash if somehow not
    		if (state instanceof IExtendedBlockState) {
    			ModelRenderState renderState = style.getModelController().getRenderState((IExtendedBlockState) state, world, pos);
    			state = ((IExtendedBlockState)state).withProperty( NiceBlock.MODEL_RENDER_STATE, renderState);
    			miss++;
//    	    	Adversity.log.info("cache miss @" + pos.toString());
    			
    			if(tileentity != null) {
    	   			if(((NiceTileEntity)tileentity).dirty){
        				dirtyCount++;
        			}
     
    				((NiceTileEntity)tileentity).state = (IExtendedBlockState)state;
		    		((NiceTileEntity)tileentity).dirty = false;
//		    		Adversity.log.info("cache refresh @" + pos.toString());
    			} 
    			

    			
    		}
	    }
		
		long end = System.nanoTime();
		timerCount++;

		elapsedTime += (end - start);
		if((timerCount & 0x800) == 0x800){
			Adversity.log.info("average getExtendedState =" +  elapsedTime / (timerCount)
					+ " cache hit rate =" + (hit * 100 / (hit + miss)) + "%  dirtyCount =" + dirtyCount );
			timerCount = 0;
			elapsedTime = 0;
		}
		return state;
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new NiceTileEntity();		
	}


}
