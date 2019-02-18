package org.dovnard.exp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dovnard.exp.config.Config;

public class Main {
    private static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        System.out.println("Main application running");
        Main app = new Main();
        app.testCacheDB();
    }
    public void testCacheDB() {
        logger.info("Invoke testCacheDB");
        Config config = Config.getInstance();
        System.out.println("Test Param1:" + config.getProperty("param1"));
    }
}
