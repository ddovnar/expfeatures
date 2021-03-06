package org.dovnard.exp;

import com.carrotsearch.sizeof.RamUsageEstimator;
import org.dovnard.exp.console.CommandExec;
import org.dovnard.exp.console.ConsoleRunner;
import org.dovnard.exp.db.cachedataset.CacheDataSet;
import org.dovnard.exp.db.cachedataset.CacheDbDataSetImpl;
import org.dovnard.exp.db.cachedataset.RowHeader;
import org.dovnard.exp.db.cachedataset.RowHeaderItem;
import org.dovnard.exp.util.MemoryInfo;
import org.dovnard.exp.web.RequestProcessor;
import org.dovnard.exp.web.WebServer;
import org.dovnard.exp.web.WebServerAlone;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dovnard.exp.config.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public class Main {
    private static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        System.out.println("Main application running");
        Main app = new Main();
        //app.testCacheDB();
        //app.testSimpleCacheDB();
        //app.testDeleteAll();
        //app.testAdd();
        //app.testWebServerAlone();
        app.testInWeb();
        //app.testWebServer();
    }
    public void testInWeb() {
        try {
            try (WebServerAlone server = new WebServerAlone()) {
                /////////////// init data objects
                final Config config = Config.getInstance();
                final CacheDataSet ds = new CacheDbDataSetImpl();
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
                /////////////
                // shutdown hook
                Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                    public void run() {
                        logger.info("In shutdown hook");
                        try {
                            ds.release();
                            server.close();
                        } catch(Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }, "Shutdown-thread"));
                //~ shutdown hook
                server.setRequestProcessor(new RequestProcessor() {
                    @Override
                    public void process(BufferedReader in, PrintWriter out) throws IOException {
                        StringBuilder payload = new StringBuilder();
                        while(in.ready()){
                            payload.append((char) in.read());
                        }
                        logger.info("Payload data is: "+payload.toString());
                        out.print(payload);
                    }

                    @Override
                    public void process(JSONObject in, PrintWriter out) throws IOException {
                        String action = in.getString("action");
                        JSONObject data = in.getJSONObject("data");

                        logger.info("Request data:" + data.toString());

                        boolean actionResult = false;
                        if (action.equals("first")) {
                            actionResult = ds.first();
                        } else if (action.equals("last")) {
                            actionResult = ds.last();
                        } else if (action.equals("next")) {
                            actionResult = ds.next(false);
                        } else if (action.equals("previous")) {
                            actionResult = ds.previous(false);
                        } else if (action.equals("next_auto")) {
                            actionResult = ds.next(true);
                        } else if (action.equals("previous_auto")) {
                            actionResult = ds.previous(true);
                        } else if (action.equals("next_page")) {
                            actionResult = ds.nextPage();
                        } else if (action.equals("prev_page")) {
                            actionResult = ds.prevPage();
                        } else if (action.equals("gc")) {
                            Runtime.getRuntime().gc();
                            actionResult = true;
                        } else if (action.equals("delete")) {
                            actionResult = ds.delete();
                        } else if (action.equals("save")) {
                            Iterator<String> keys = data.keys();
                            while(keys.hasNext()) {
                                String key = keys.next();
                                logger.info("data[" + key + "]=" + data.get(key));
                                ds.setValue(key, data.get(key).toString());
                            }
                            actionResult = ds.save();
                        } else if (action.equals("add")) {
                            actionResult = ds.add();

                        }

                        JSONObject res = new JSONObject();
                        res.put("action_result", actionResult);
                        /// print results
                        ArrayList<JSONObject> jsonArrayColumnData = new ArrayList<JSONObject>();
                        RowHeader header = ds.getRowHeader();
                        for (RowHeaderItem item : header.getHeaderItems()) {
                            //logger.info("HeaderItem: |" + item.getColumnName() + "|" + item.getRealTableColumnName() + "|" + item.getTableName());
                            JSONObject jsonItem = new JSONObject();
                            jsonItem.put("column_name", item.getColumnName());
                            jsonItem.put("table_column_name", item.getRealTableColumnName());
                            jsonItem.put("table_name", item.getTableName());
                            jsonArrayColumnData.add(jsonItem);
                        }
                        res.put("column_info", jsonArrayColumnData);

                        ArrayList<JSONObject> jsonArrayRowData = new ArrayList<JSONObject>();
                        for (int i = 0; i < ds.getLoadedRecords(); i++) {
                            JSONObject rowItem = new JSONObject();

                            int j = 0;
                            StringBuilder s = new StringBuilder();
                            for (RowHeaderItem h : ds.getRowHeader().getHeaderItems()) {
                                s.append(h.getColumnName());
                                s.append("(");
                                //s.append(1);
                                s.append(ds.getRowByIndex(i).getCell(j).getValue());
                                s.append(")");
                                s.append("|");
//                                rowItem.put("column_name", h.getColumnName());
//                                rowItem.put("value", ds.getRowByIndex(i).getCell(j).getValue());
                                if (ds.getRowByIndex(i).getCell(j).getValue() == null)
                                    rowItem.put(h.getColumnName(), "");
                                else
                                    rowItem.put(h.getColumnName(), ds.getRowByIndex(i).getCell(j).getValue());
                                j++;
                            }
                            logger.info("----->[" + s.toString() + "]");
                            jsonArrayRowData.add(rowItem);
//                            if (i == ds.getActiveRecordIndex()) {
//                                logger.info("----->[" + s.toString() + "] *");
//                            } else {
//                                logger.info("----->[" + s.toString() + "]");
//                            }
                        }
                        res.put("active_row", ds.getActiveRecordIndex());
                        res.put("data_row", jsonArrayRowData);
                        //StringBuilder payload = new StringBuilder();
                        //payload.append("{}");
                        //logger.info("Payload data is json: "+payload.toString());
                        //out.print(payload);

                        JSONObject memory_info = new JSONObject();
                        getMemoryInfo(memory_info);
                        res.put("memory_info", memory_info);

                        res.put("object_size", RamUsageEstimator.sizeOf(ds));

                        logger.info("Response:" + res.toString());
                        out.print(res.toString());
                    }

                    @Override
                    public void process(String in, PrintWriter out) throws IOException {
                        StringBuilder payload = new StringBuilder();
                        payload.append("string");
                        logger.info("Payload data is: "+payload.toString());
                        out.print(payload);
                    }
                });
                server.run();
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    private void getMemoryInfo(JSONObject memory_info) {
        memory_info.put("free_memory", MemoryInfo.getFreeMemory());
        memory_info.put("allocated", MemoryInfo.getAllocatedMemory());
        memory_info.put("total_free", MemoryInfo.getTotalFreeMemory());
        memory_info.put("max", MemoryInfo.getMaxMemory());
        memory_info.put("used", MemoryInfo.getUsedMemory());
        logger.info("Memory info: " + memory_info.toString());
    }
    @Deprecated
    public void testWebServerAlone() {
        try {
            try (WebServerAlone server = new WebServerAlone()) {
                server.setRequestProcessor(new RequestProcessor() {
                    @Override
                    public void process(BufferedReader in, PrintWriter out) throws IOException {
                        StringBuilder payload = new StringBuilder();
                        while(in.ready()){
                            payload.append((char) in.read());
                        }
                        logger.info("Payload data is: "+payload.toString());
                        out.print(payload);
//                        String line;
//                        while ((line = in.readLine()) != null) {
//                            if (line.length() == 0)
//                                break;
//                            logger.info(line + "\\r\\n");
//                            out.print("BODY:" + line + "\r\n");
//                        }
                        /*
                        String headerLine = null;
                        while((headerLine = in.readLine()).length() != 0){
                            logger.info("Header:" + headerLine);
                        }

                        StringBuilder payload = new StringBuilder();
                        while(in.ready()){
                            payload.append((char) in.read());
                        }
                        logger.info("Payload data is: "+payload.toString());
                        */
                        //out.print("BODY:" + "FROM CLIENT" + "\r\n");
                        //out.print("\r\n");
                        //out.print(out.)
                    }

                    @Override
                    public void process(JSONObject in, PrintWriter out) throws IOException {
//                        StringBuilder payload = new StringBuilder();
//                        while(in.ready()){
//                            payload.append((char) in.read());
//                        }
                        StringBuilder payload = new StringBuilder();
                        payload.append("{}");
                        logger.info("Payload data is json: "+payload.toString());
                        out.print(payload);
                    }

                    @Override
                    public void process(String in, PrintWriter out) throws IOException {
                        StringBuilder payload = new StringBuilder();
                        payload.append("string");
                        logger.info("Payload data is: "+payload.toString());
                        out.print(payload);
                    }
                });
                server.run();
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    public void testWebServer() {
        try {
            final WebServer srv = new WebServer(8080);

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    logger.info("In shutdown hook");
                    try {
                        srv.close();
                    } catch(IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }, "Shutdown-thread"));

            srv.run(new CommandExec() {
                public void run() {
                    logger.info("Command runned");
                    try {
                        while (true) {
                            logger.info("Start request");
                            try (Socket socket = srv.accept()) {
                                logger.info("in accept");
                                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                                Date today = new Date();

                                out.print("HTTP/1.1 200 \r\n");
                                out.print("Content-Type: text/plain\r\n"); // The type of data
                                out.print("Connection: close\r\n"); // Will close stream
                                out.print("\r\n"); // End of headers
                                logger.info("before process");
                                String line;
                                while ((line = in.readLine()) != null) {
                                    if (line.length() == 0)
                                        break;
                                    out.print("BODY:" + line + "\r\n");
                                }
                                logger.info("after process");
                                out.close();
                                in.close();
                            }
                        }
                    } catch(IOException ex) {
                        ex.printStackTrace();
                        try {
                            srv.close();
                        } catch(IOException e2) {e2.printStackTrace();}
                    }
                }
            });
        } catch(IOException ex) {
            ex.printStackTrace();
        }
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

        ds.first();
        logger.info("first>recs: " + ds.getLoadedRecords() + ", actRowIdx: " + ds.getActiveRecordIndex());
        showRecords(ds);

        ds.next(false);
        logger.info("first>recs: " + ds.getLoadedRecords() + ", actRowIdx: " + ds.getActiveRecordIndex());
        showRecords(ds);

        ds.next(false);
        logger.info("first>recs: " + ds.getLoadedRecords() + ", actRowIdx: " + ds.getActiveRecordIndex());
        showRecords(ds);

        ds.next(false);
        logger.info("first>recs: " + ds.getLoadedRecords() + ", actRowIdx: " + ds.getActiveRecordIndex());
        showRecords(ds);

        ds.next(false);
        logger.info("first>recs: " + ds.getLoadedRecords() + ", actRowIdx: " + ds.getActiveRecordIndex());
        showRecords(ds);

        ds.add();
        ds.setValue("id", "row_" + "end1");
        ds.setValue("full_name", "item" + "end1");
        ds.save();
        logger.info("recs: " + ds.getLoadedRecords() + ", actRowIdx: " + ds.getActiveRecordIndex());
        showRecords(ds);

        //loop adding rows from first or without position
//        for (int i = 0; i < 10; i++) {
//            ds.add();
//            ds.setValue("id", "row_" + (i + 10));
//            ds.setValue("full_name", "item" + (i + 10));
//            ds.save();
//            logger.info("recs: " + ds.getLoadedRecords() + ", actRowIdx: " + ds.getActiveRecordIndex());
//            showRecords(ds);
//        }
        //~loop adding rows from first or without position
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
            if (i == d.getActiveRecordIndex()) {
                logger.info("----->[" + s.toString() + "] *");
            } else {
                logger.info("----->[" + s.toString() + "]");
            }
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
