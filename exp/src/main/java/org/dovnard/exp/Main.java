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
    @Deprecated
    public void testSimpleCacheDB() {
        final Config config = Config.getInstance();

        CacheDataSet ds = new CacheDbDataSetImpl();
        ds.setURL(config.getProperty("dbUrl"));
        ds.setUsername(config.getProperty("dbUser"));
        ds.setPassword(config.getProperty("dbPass"));

        ds.setPageSize(5);
        ds.setCommand("SELECT row_id as id, name as full_name FROM test");
        ds.execute();

        boolean r = ds.first();
        int proc = 0;
        int pageReaded = 1;
        while (r) {
            System.out.println("=== Page " + pageReaded + " ===");

            proc = 0;
            while (proc < ds.getLoadedRecords()) {
                System.out.println(proc + "). record: " + ds.getString(0));
                r = ds.next(false);
                proc++;
            }

            logger.info("Page readed: " + pageReaded);
            pageReaded++;
            r = ds.nextPage();
        }
    }
    public void testCacheDB() {
        logger.info("Invoke testCacheDB");
        final Config config = Config.getInstance();

        final ConsoleRunner console = new ConsoleRunner();
        console.setCommand(new CommandExec() {
            public void run() {
                CacheDataSet ds = new CacheDbDataSetImpl();
                ds.setURL(config.getProperty("dbUrl"));
                ds.setUsername(config.getProperty("dbUser"));
                ds.setPassword(config.getProperty("dbPass"));

                ds.setPageSize(20);
                ds.setCommand("SELECT row_id as id, name as full_name FROM test");
                ds.execute();

                boolean quit = false;
                boolean hasRows = ds.first();
                int proc = 0;
                int pageReaded = 1;
                while (!quit && hasRows) {
                    System.out.println("=== Page " + pageReaded + " ===");

                    proc = 0;
                    while (proc < ds.getLoadedRecords() && ds.next(false)) {
                        System.out.println(proc + "). record: " + ds.getString(0));
                        //r = ds.next(false);
                        proc++;
                    }

                    logger.info("Page readed: " + pageReaded);
                    pageReaded++;
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
