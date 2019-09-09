package org.mike;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.LoggerFactory;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AppTest.class);

  /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    public void testAppReport() throws Exception
    {
      String appId = "webgoat";
      String reportId = "f525fd12979646c9a02d96da542e2666";

      ApplicationReport appHelper2 = new ApplicationReport(appId, reportId).invoke();
      appHelper2.printLicenseInfo();
      LOGGER.info("Contains GPL-2.0: {}", appHelper2.doesContainLicense("GPL-2.0"));
      LOGGER.info("Contains JSON: {}", appHelper2.doesContainLicense("JSON"));

      assertTrue(appHelper2.doesContainLicense("GPL-2.0") );
      assertFalse(appHelper2.doesContainLicense("JSON"));
    }

    public void testAllReports() throws Exception
    {
      Reports reports = new Reports().invoke();

      assertTrue(reports.getReportSet().size() > 0);

    }
}
