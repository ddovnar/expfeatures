package org.dovnard.exp.test;

import org.dovnard.exp.db.cachedataset.CacheDataSet;
import org.dovnard.exp.db.cachedataset.CacheDbDataSetImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.*;

public class TestSuite {
    private static final Logger logger = LoggerFactory.getLogger(TestSuite.class);
    @BeforeTest
    protected void onBeforeTest() {
        System.out.println("TestSuite started");
    }
    @AfterTest
    protected void onAfterTest() {
        System.out.println("TestSuite finish");
    }

    @Test(priority = 1, description="simple loop with autonextpage")
    @Parameters({"dbUrl", "dbUser", "dbPass"})
    public void test(@Optional("dbUrl") String dbUrl, @Optional("dbUser") String dbUser, @Optional("dbPass") String dbPass) {
        logger.info("Test Cached db dataset started");
        if (dbUrl == null || dbUrl.equals("") || dbUrl.equals("dbUrl")) {
            return;
        }

        CacheDataSet ds = new CacheDbDataSetImpl();
        ds.setURL(dbUrl);
        ds.setUsername(dbUser);
        ds.setPassword(dbPass);

        ds.setPageSize(20);
        ds.setCommand("SELECT row_id as id, name as full_name FROM test");
        ds.execute();

        int cnt = 0;
        while (ds.next(true)) {
            logger.info("go next: " + ds.getString(0));
            cnt++;
        }
        while (ds.previous(true)) {
            logger.info("go prev: " + ds.getString(0));
        }

        int cnt2 = 0;
        boolean r = ds.first();
        while (r) {
            logger.info("2go next: " + ds.getString(0));
            r = ds.next(true);
            cnt2++;
        }
        logger.info("Loop1: " + cnt + ", Loop2: " + cnt2);
        Assert.assertEquals(cnt, cnt2, "Check processed records");
        ds.release();
    }
    @Test(priority = 2, description="simple loop with manual nextpage")
    @Parameters({"dbUrl", "dbUser", "dbPass"})
    public void testPagination(@Optional("dbUrl") String dbUrl, @Optional("dbUser") String dbUser, @Optional("dbPass") String dbPass) {
        if (dbUrl == null || dbUrl.equals("") || dbUrl.equals("dbUrl")) {
            return;
        }

        CacheDataSet ds = new CacheDbDataSetImpl();
        ds.setURL(dbUrl);
        ds.setUsername(dbUser);
        ds.setPassword(dbPass);

        ds.setPageSize(20);
        ds.setCommand("SELECT row_id as id, name as full_name FROM test");
        ds.execute();

        boolean hasRows = ds.first();
        int proc = 0;
        int pageReaded = 1;
        while (hasRows) {
            logger.info("=== Page " + pageReaded + " ===: " + ds.getLoadedRecords());

            proc = 0;
            while (proc < ds.getLoadedRecords()) {
                logger.info(proc + "). record: " + ds.getString(0));
                ds.next(false);
                proc++;
            }

            logger.info("Page readed: " + pageReaded);
            pageReaded++;
            hasRows = ds.nextPage();
        }
    }
}
