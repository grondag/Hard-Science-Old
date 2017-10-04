package grondag.hard_science;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log
{

    public static final boolean DEBUG_MODE = true;
    private static Logger log;
    
    private static Logger getLog()
    {
        // allow access to log during unit testing or other debug scenarios
        if(log == null) log = LogManager.getLogger();
        return log;
    }
    
    public static void setLog(Logger lOG)
    {
        log = lOG;
    }

    public static void warn(String message)
    {
        getLog().warn(message);
    }
    
    public static void warn(String message, Object...args)
    {
        getLog().warn(String.format(message, args));
    }

    public static void info(String message)
    {
        getLog().info(message);
    }

    public static void info(String message, Object...args)
    {
        getLog().info(String.format(message, args));
    }
    
    public static void debug(String message)
    {
        getLog().debug(message);
    }
    
    public static void debug(String message, Object...args)
    {
        getLog().debug(String.format(message, args));
    }

    public static void error(String message)
    {
        getLog().error(message);
    }

    public static void error(String message, Throwable t)
    {
        getLog().error(message, t);
    }
}
