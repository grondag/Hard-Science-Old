package grondag.adversity.niceblock.model;

public abstract class ModelRenderData {

	public static class TwoVars extends ModelRenderData{
		public final int var1;
		public final int var2;
		
		public TwoVars(int var1, int var2){
			this.var1 = var1;
			this.var2 = var2;
		}
		
	}

}


