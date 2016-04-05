import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class MyLogger {
	public Logger LOGGER = Logger.getLogger(MyLogger.class.getName()); 
	int id;
	
	public MyLogger(Node n) {
		id = n.id;
		setupLogger();
	}

	public   void setupLogger() {
		FileHandler fileHandler;
		try {
			LOGGER.setUseParentHandlers(false);
			int limit = 100000000;
			fileHandler = new FileHandler("./myLogFile"+id, limit, 1, true);
			 SimpleFormatter formatter = new SimpleFormatter();  
			 fileHandler.setFormatter(formatter);  
			 
			LOGGER.addHandler(fileHandler);
			LOGGER.setLevel(Level.FINEST);
			
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
