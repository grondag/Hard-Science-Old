package grondag.hard_science.external.jei;

public interface IRecipeFormat
{
    public final static int DEFAULT_LEFT = 24;
    public final static int DEFAULT_RIGHT = 88;
    public final static int DEFAULT_ROW_HEIGHT = 32;
    public final static int DEFAULT_WIDTH = 128;
    
    default int centerX() { return DEFAULT_WIDTH / 2; }

    int centerY();

    int height();

    default int width() { return DEFAULT_WIDTH; }

    default int inputX(int index) { return DEFAULT_LEFT; }

    int inputY(int index);

    default int outputX(int index) { return DEFAULT_RIGHT; }

    int outputY(int index);

}