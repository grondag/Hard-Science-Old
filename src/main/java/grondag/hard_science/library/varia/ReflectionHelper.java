package grondag.hard_science.library.varia;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ReflectionHelper
{
    @SuppressWarnings("rawtypes")
    public static Field getField(Class clazz, String fieldName) throws NoSuchFieldException 
    {
        try 
        {
            return clazz.getDeclaredField(fieldName);
        } 
        catch (NoSuchFieldException e)
        {
            Class superClass = clazz.getSuperclass();
            if (superClass == null) 
            {
                throw e;
            } 
            else 
            {
                return getField(superClass, fieldName);
            }
        }
    }
    
    public static void makeAccessible(Field field)
    {
        if (!Modifier.isPublic(field.getModifiers()) ||
            !Modifier.isPublic(field.getDeclaringClass().getModifiers()))
        {
            field.setAccessible(true);
        }
    }
    
    @SuppressWarnings("rawtypes")
    public static Field getAccessibleField(Class clazz, String fieldName) throws NoSuchFieldException 
    {
        Field result = getField(clazz, fieldName);
        makeAccessible(result);
        return result;
    }
}
