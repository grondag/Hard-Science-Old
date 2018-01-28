package grondag.hard_science.external.jei;

public class RecipeLayout
{
    public final static int LEFT = 24;
    public final static int RIGHT = 88;
    public final static int ROW_HEIGHT = 32;
    
    public final int inputCount;
    public final int outputCount;
    public final int rowCount;
    public final int[] inputY;
    public final int[] outputY;
    public final int width;
    public final int height;
    public final int centerX;
    public final int centerY;
    
    public RecipeLayout(int inputCount,int outputCount)
    {
        this.inputCount = inputCount;
        this.outputCount = outputCount;
        this.rowCount = Math.max(inputCount, outputCount);
        this.inputY = new int[inputCount];
        this.outputY = new int[outputCount];
        this.height = rowCount * ROW_HEIGHT;
        this.centerY = height / 2;
        this.width = 128;
        this.centerX = this.width / 2;
        
        if(inputCount > 0)
        {
            int startY = centerY - (inputCount * ROW_HEIGHT / 2);
            for(int i = 0; i < inputCount; i++)
            {
                inputY[i] = startY;
                startY += ROW_HEIGHT;
            }
        }
        
        if(outputCount > 0)
        {
            int startY = centerY - (outputCount * ROW_HEIGHT / 2);
            for(int i = 0; i < outputCount; i++)
            {
                outputY[i] = startY;
                startY += ROW_HEIGHT;
            }
        }
    }
}
