package grondag.adversity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Output
{

    public static final boolean DEBUG_MODE = true;
    private static Logger log;
    
    public static Logger getLog()
    {
        // allow access to log during unit testing or other debug scenarios
        if(log == null) log = LogManager.getLogger();
        return log;
    }
    public static void setLog(Logger lOG)
    {
        log = lOG;
    }

}
