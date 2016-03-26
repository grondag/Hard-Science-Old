package grondag.adversity.taskmaster;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Taskmaster {

	private static ExecutorService executor;
	
	public static void start()
	{
		executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	}
	
	public static void stop()
	{
		executor.shutdown();
	}
	
	private static ExampleWorldSavedData get(World world) {
		  // The IS_GLOBAL constant is there for clarity, and should be simplified into the right branch.
		  MapStorage storage = IS_GLOBAL ? world.getMapStorage() : world.getPerWorldStorage();
		  ExampleWorldSavedData instance = (ExampleWorldSavedData) storage.loadData(ExampleWorldSavedData.class, DATA_NAME);

		  if (instance == null) {
		    instance = new ExampleWorldSavedData();
		    storage.setData(DATA_NAME, instance);
		  }
		  return instance;
		}
}
