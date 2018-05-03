package org.mike;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Hello world!
 *
 */
public class App
{
  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(App.class);

  static final String APP_REPORT_REST_V2 = "%s/api/v2/applications/%s/reports/%s";
  static final String ALL_REPORTS_REST_V2 = "%s/api/v2/reports/applications";
  static final String BASE_URL = "http://localhost:8060/iq";


  public static void main(String[] args) throws Exception
  {
    SLF4JBridgeHandler.install();

    Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    root.setLevel(Level.DEBUG);

    Reports reports = new Reports().invoke();

    Set<String> jsonLicenseSet = new HashSet<String>();
    for (ApplicationReport appReport : reports.getApplicationReports())
    {
      //LOGGER.info("Does application {} at {} contain JSON: {}", appReport.appName, appReport.stage, appReport.doesContainLicense("JSON"));
      if(appReport.doesContainLicense("JSON"))
      {
        jsonLicenseSet.add(appReport.appName);
      }
    }

    LOGGER.info("JSON Licenses ({}):", jsonLicenseSet.size());
    App.printLicenseSet(jsonLicenseSet);


  }

  static void printLicenseSet(Set<String> pLicenseSet)
  {
    for (String licenseName : pLicenseSet)
    {
      LOGGER.info("\t * {}", licenseName);
    }
  }

}
