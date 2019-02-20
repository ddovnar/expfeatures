package org.dovnard.exp;

import org.dovnard.exp.console.CommandExec;
import org.dovnard.exp.console.ConsoleRunner;
import org.dovnard.exp.db.cachedataset.CacheDataSet;
import org.dovnard.exp.db.cachedataset.CacheDbDataSetImpl;
import org.dovnard.exp.db.cachedataset.RowHeader;
import org.dovnard.exp.db.cachedataset.RowHeaderItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dovnard.exp.config.Config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        System.out.println("Main application running");
        Main app = new Main();
        //app.testCacheDB();
        //app.testSimpleCacheDB();
        //app.testDeleteAll();
        app.testAdd();
    }
    public void testAdd() {
        final Config config = Config.getInstance();

        CacheDataSet ds = new CacheDbDataSetImpl();
        ds.setURL(config.getProperty("dbUrl"));
        ds.setUsername(config.getProperty("dbUser"));
        ds.setPassword(config.getProperty("dbPass"));

        ds.setPageSize(5);
        ds.setCommand("SELECT row_id as id, name as full_name FROM test");
        ds.setRowIdColumnIndex(0);
        ds.setRowIdColumnName("row_id");
        List<String> cols = new ArrayList<String>();
        cols.add("row_id");
        cols.add("name");
        ds.setRealColumnNames(cols);
        ds.execute();

        RowHeader header = ds.getRowHeader();
        for (RowHeaderItem item : header.getHeaderItems()) {
            logger.info("HeaderItem: |" + item.getColumnName() + "|" + item.getRealTableColumnName() + "|" + item.getTableName());
        }
        logger.info("recs: " + ds.getLoadedRecords() + ", actRowIdx: " + ds.getActiveRecordIndex());
        for (int i = 0; i < 10; i++) {
            ds.add();
            ds.setValue("id", "row_" + (i + 10));
            ds.setValue("full_name", "item" + (i + 10));
            ds.save();
            logger.info("recs: " + ds.getLoadedRecords() + ", actRowIdx: " + ds.getActiveRecordIndex());
            showRecords(ds);
        }
//        ds.add();
//        logger.info("recs: " + ds.getLoadedRecords() + ", actRowIdx: " + ds.getActiveRecordIndex());
//        ds.setValue("id", "1");
//        ds.setValue("full_name", "test");
//        if (ds.save()) {
//            logger.info("New record added success");
//        } else {
//            logger.info("New record added failed");
//        }
        //logger.info("Record: " + ds.getString(0));
    }
    public void showRecords(CacheDataSet d) {
        StringBuilder s = new StringBuilder();

        for (int i = 0; i < d.getLoadedRecords(); i++) {
            s.setLength(0);
            int j = 0;
            for (RowHeaderItem h : d.getRowHeader().getHeaderItems()) {
                s.append(h.getColumnName());
                s.append("(");
                //s.append(1);
                s.append(d.getRowByIndex(i).getCell(j).getValue());
                s.append(")");
                s.append("|");
                j++;
            }
            logger.info("----->[" + s.toString() + "]");
        }
    }
    public void testDeleteAll() {
        final Config config = Config.getInstance();

        CacheDataSet ds = new CacheDbDataSetImpl();
        ds.setURL(config.getProperty("dbUrl"));
        ds.setUsername(config.getProperty("dbUser"));
        ds.setPassword(config.getProperty("dbPass"));

        ds.setPageSize(5);
//        ds.setCommand("SELECT row_id as id, name as full_name FROM test where marked = ?");
//        ds.addParameter("marked", 1);
        ds.setCommand("SELECT row_id as id, name as full_name FROM test");
        ds.setRowIdColumnIndex(0);
        ds.setRowIdColumnName("row_id");
        ds.execute();

        /*if (ds.first()) {
            logger.info("Record: " + ds.getString(0) + ", recs: " + ds.getLoadedRecords() + ", actRowIdx: " + ds.getActiveRecordIndex());
            ds.delete();
            logger.info("1After delete, recs: " + ds.getLoadedRecords() + ", actRowIdx: " + ds.getActiveRecordIndex());
            ds.delete();
            logger.info("2After delete, recs: " + ds.getLoadedRecords() + ", actRowIdx: " + ds.getActiveRecordIndex());
            ds.delete();
            logger.info("3After delete, recs: " + ds.getLoadedRecords() + ", actRowIdx: " + ds.getActiveRecordIndex());
            ds.delete();
            logger.info("4After delete, recs: " + ds.getLoadedRecords() + ", actRowIdx: " + ds.getActiveRecordIndex());
            ds.delete();
            logger.info("5After delete, recs: " + ds.getLoadedRecords() + ", actRowIdx: " + ds.getActiveRecordIndex());
        }*/
        int deletedCnt = 0;
        while (ds.first()) {
            ds.delete();
            logger.info("After delete, recs: " + ds.getLoadedRecords() + ", actRowIdx: " + ds.getActiveRecordIndex());
            deletedCnt++;
        }
        logger.info("Deleted recs: " + deletedCnt);
        ds.release();
    }
    @Deprecated
    public void testSimpleCacheDB() {
        final Config config = Config.getInstance();

        CacheDataSet ds = new CacheDbDataSetImpl();
        ds.setURL(config.getProperty("dbUrl"));
        ds.setUsername(config.getProperty("dbUser"));
        ds.setPassword(config.getProperty("dbPass"));

        logger.info("testSimpleCacheDB");

        ds.setPageSize(5);
        ds.setCommand("SELECT row_id as id, name as full_name FROM test where marked = ? and name=?");
        ds.addParameter("marked", 1);
        ds.addParameter("n", "name5");
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

                ds.setPageSize(5);
                ds.setCommand("SELECT row_id as id, name as full_name FROM test where marked = 1");
                ds.execute();

                boolean quit = false;
                boolean hasRows = ds.first();
                int proc = 0;
                int pageReaded = 1;
                while (!quit && hasRows) {
                    System.out.println("=== Page " + pageReaded + " ===: " + ds.getLoadedRecords());

                    proc = 0;
                    while (proc < ds.getLoadedRecords()) {
                        System.out.println(proc + "). record: " + ds.getString(0));
                        //r = ds.next(false);
                        ds.next(false);
                        proc++;
                    }

                    logger.info("Page readed: " + pageReaded);
                    pageReaded++;

//                    console.askToQuit();
//                    if (!quit) {
//                        quit = console.isExit();
//                    }
                    String key = console.askKey("Enter key-command ( v - prevPage, n - nextPage ):");
                    if (key.equalsIgnoreCase("n")) {
                        if (!ds.nextPage())
                            break;
                    } else if (key.equalsIgnoreCase("v")) {
                        if (!ds.prevPage())
                            break;
                        ds.first();
                    } else {
                        System.out.println("Unknown command");
                        console.askToQuit();
                        if (!quit) {
                            quit = console.isExit();
                        }
                    }
                }
            }
        });
        console.run();
    }
}
