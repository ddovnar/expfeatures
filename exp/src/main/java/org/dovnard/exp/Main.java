package org.dovnard.exp;

import org.dovnard.exp.console.CommandExec;
import org.dovnard.exp.console.ConsoleRunner;
import org.dovnard.exp.db.cachedataset.CacheDataSet;
import org.dovnard.exp.db.cachedataset.CacheDbDataSetImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dovnard.exp.config.Config;

import java.util.Scanner;

public class Main {
    private static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        System.out.println("Main application running");
        Main app = new Main();
        app.testCacheDB();
    }
    public void testCacheDB() {
        logger.info("Invoke testCacheDB");
        final Config config = Config.getInstance();
        System.out.println("Test Param1:" + config.getProperty("param1"));

        final ConsoleRunner console = new ConsoleRunner();
        console.setCommand(new CommandExec() {
            public void run() {
                //System.out.println("Scanner: " + console.getScanner());
                CacheDataSet ds = new CacheDbDataSetImpl();
                ds.setURL(config.getProperty("dbUrl"));
                ds.setUsername(config.getProperty("dbUser"));
                ds.setPassword(config.getProperty("dbPass"));

                ds.setPageSize(20);
                ds.setCommand("SELECT row_id as id, name as full_name FROM test");
                ds.execute();

                boolean quit = false;
                boolean first = false;
                while (!quit) {
                    int processed = 0;
                    while ((( !first && processed < ds.getLoadedRecords()) || ( first && processed < ds.getLoadedRecords()-1)) && ds.next()) {
                        System.out.println("record: " + ds.getString(0));
                        processed++;
                    }
                    first = true;
                    quit = !ds.nextPage();

                    console.askToQuit();
                    if (!quit) {
                        quit = console.isExit();
                    }
                }
            }
        });
        console.run();
    }
}
