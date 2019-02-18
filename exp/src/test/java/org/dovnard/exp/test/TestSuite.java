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

    @Test(priority = 1, description="simple test")
    @Parameters({"dbUrl", "dbUser", "dbPass"})
    public void test(@Optional("dbUrl") String dbUrl, @Optional("dbUser") String dbUser, @Optional("dbPass") String dbPass) {
        logger.info("Test Cached db dataset started");
        if (dbUrl == null || dbUrl.equals("") || dbUrl.equals("dbUrl")) {
            return;
        }
        System.out.println("dbUrl:" + dbUrl);

        CacheDataSet ds = new CacheDbDataSetImpl();
        ds.setURL(dbUrl);
        ds.setUsername(dbUser);
        ds.setPassword(dbPass);

        ds.setPageSize(20);
        ds.setCommand("SELECT row_id as id, name as full_name FROM test");
        ds.execute();

        while (ds.next()) {
            logger.info("go next: " + ds.getString(0));
        }
        while (ds.previous()) {
            logger.info("go prev: " + ds.getString(0));
        }

        ds.release();
    }
}
