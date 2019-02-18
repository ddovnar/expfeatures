package org.dovnard.exp.test;

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
        logger.info("Test started");
    }
}
