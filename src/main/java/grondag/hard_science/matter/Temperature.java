package grondag.hard_science.matter;

public class Temperature
{
    public static final double KELVIN_OFFSET = 273.15;
    
    public static double celsiusToKelvin(double celsius)
    {
        return celsius + KELVIN_OFFSET; 
    }
    
    public static double kelvinToCelsius(double kelvin)
    {
        return kelvin - KELVIN_OFFSET; 
    }
}
