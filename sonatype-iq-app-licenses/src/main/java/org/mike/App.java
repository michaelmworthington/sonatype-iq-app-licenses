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

  /**
   * Demo class to pull license data from IQ Server and aggregate based on the licenses
   *
   * The reports and UI in IQ Server are structured as:
   *     Application - to - Component - to - License
   *
   * The question I try to answer with this sample code "is which licenses are the most popular?"
   *
   * This requires inverting the data model to something like:
   *     License - to - Application
   *
   * TODO: enhancements:
   *       The code as written will simply report on all unique licenses. This could be enhanced to
   *       maintain a counter for each occurrence of a license so that you know if a license
   *       is used across a lot of components across a lot of applications or just used hidden in the corner
   *
   *
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception
  {
    SLF4JBridgeHandler.install();

    Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    root.setLevel(Level.DEBUG);

    Reports reports = new Reports().invoke();

    Set<String> jsonLicenseSet = new HashSet<String>();
    for (ApplicationReport appReport : reports.getApplicationReports())
    {
      //TODO: Change the logic to loop through all the reports and collect the desired information

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
