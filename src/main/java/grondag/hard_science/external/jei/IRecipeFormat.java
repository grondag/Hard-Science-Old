package grondag.hard_science.external.jei;

public interface IRecipeFormat
{

    int centerX();

    int centerY();

    int height();

    int width();

    int inputX(int index);

    int inputY(int index);

    int outputX(int index);

    int outputY(int index);

    int inputCount();

    int outputCount();

}