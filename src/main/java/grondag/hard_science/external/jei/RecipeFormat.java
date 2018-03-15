package grondag.hard_science.external.jei;

public class RecipeFormat implements IRecipeFormat
{
    private final int rowCount;
    private final int[] inputY;
    private final int[] outputY;
    private final int height;
    private final int centerY;
    
    public RecipeFormat(int inputCount,int outputCount)
    {
        this.rowCount = Math.max(inputCount, outputCount);
        this.inputY = new int[inputCount];
        this.outputY = new int[outputCount];
        this.height = rowCount * DEFAULT_ROW_HEIGHT;
        this.centerY = height() / 2;
        
        if(inputCount > 0)
        {
            int startY = centerY() - (inputCount * DEFAULT_ROW_HEIGHT / 2);
            for(int i = 0; i < inputCount; i++)
            {
                inputY[i] = startY;
                startY += DEFAULT_ROW_HEIGHT;
            }
        }
        
        if(outputCount > 0)
        {
            int startY = centerY() - (outputCount * DEFAULT_ROW_HEIGHT / 2);
            for(int i = 0; i < outputCount; i++)
            {
                outputY[i] = startY;
                startY += DEFAULT_ROW_HEIGHT;
            }
        }
    }

    @Override
    public int centerY()
    {
        return centerY;
    }

    @Override
    public int height()
    {
        return height;
    }

    @Override
    public int inputY(int index)
    {
        return inputY[index];
    }

    @Override
    public int outputY(int index)
    {
        return outputY[index];
    }
}
