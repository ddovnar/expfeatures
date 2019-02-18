package org.dovnard.exp.test;

import org.dovnard.exp.db.cachedataset.CacheDataSet;
import org.dovnard.exp.db.cachedataset.CacheDbDataSetImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

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
    public void test() {
        logger.info("Test Cached db dataset started");

        CacheDataSet ds = new CacheDbDataSetImpl();
        ds.setURL("DATABASE_URL");
        ds.setUsername("DATABASE_USERNAME");
        ds.setPassword("DATABASE_PASSWORD");

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
