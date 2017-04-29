package grondag.adversity.library;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

public class Danger
{
    public static final Unsafe UNSAFE;

    private static final int longBase;
    private static final int longScale;
    private static final int longShift;
    
    private static final int objectBase;
    private static final int objectScale;
    private static final int objectShift;

    static 
    {
        UNSAFE = createUnsafe();
        
        if(UNSAFE == null)
        {
            longBase = 0;
            longScale = 0;
            longShift = 0;
            objectBase = 0;
            objectScale = 0;
            objectShift = 0;
        }
        else
        {
            longBase = UNSAFE.arrayBaseOffset(long[].class);
            longScale = UNSAFE.arrayIndexScale(long[].class);
            if ((longScale & (longScale - 1)) != 0)
                throw new Error("data type scale not a power of two");
            longShift = 31 - Integer.numberOfLeadingZeros(longScale);
            
            objectBase = UNSAFE.arrayBaseOffset(Object[].class);
            objectScale = UNSAFE.arrayIndexScale(Object[].class);
            if ((objectScale & (objectScale - 1)) != 0)
                throw new Error("data type scale not a power of two");
            objectShift = 31 - Integer.numberOfLeadingZeros(objectScale);
        }
    }
  
    private static Unsafe createUnsafe() 
    {
      try 
      {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
      } 
      catch (Exception e) 
      {
        return null;
      }
    }
    
    public static final long longByteOffset(int i) 
    {
        return ((long) i << longShift) + longBase;
    }
    
    public static final long objectByteOffset(int i) 
    {
        return ((long) i << objectShift) + objectBase;
    }
    
}
