package grondag.hard_science.external.jei;

public class RecipeFormat implements IRecipeFormat
{
    private final static int LEFT = 24;
    private final static int RIGHT = 88;
    private final static int ROW_HEIGHT = 32;
    
    private final int inputCount;
    private final int outputCount;
    private final int rowCount;
    private final int[] inputY;
    private final int[] outputY;
    private final int width;
    private final int height;
    private final int centerX;
    private final int centerY;
    
    public RecipeFormat(int inputCount,int outputCount)
    {
        this.inputCount = inputCount;
        this.outputCount = outputCount;
        this.rowCount = Math.max(inputCount, outputCount);
        this.inputY = new int[inputCount];
        this.outputY = new int[outputCount];
        this.height = rowCount * ROW_HEIGHT;
        this.centerY = height() / 2;
        this.width = 128;
        this.centerX = this.width() / 2;
        
        if(inputCount > 0)
        {
            int startY = centerY() - (inputCount * ROW_HEIGHT / 2);
            for(int i = 0; i < inputCount; i++)
            {
                inputY[i] = startY;
                startY += ROW_HEIGHT;
            }
        }
        
        if(outputCount > 0)
        {
            int startY = centerY() - (outputCount * ROW_HEIGHT / 2);
            for(int i = 0; i < outputCount; i++)
            {
                outputY[i] = startY;
                startY += ROW_HEIGHT;
            }
        }
    }

    @Override
    public int centerX()
    {
        return centerX;
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
    public int width()
    {
        return width;
    }
    
    @Override
    public int inputX(int index)
    {
        return LEFT;
    }

    @Override
    public int inputY(int index)
    {
        return inputY[index];
    }
    
    @Override
    public int outputX(int index)
    {
        return RIGHT;
    }

    @Override
    public int outputY(int index)
    {
        return outputY[index];
    }

    @Override
    public int inputCount()
    {
        return inputCount;
    }

    @Override
    public int outputCount()
    {
        return outputCount;
    }
}
