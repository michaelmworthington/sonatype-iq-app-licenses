package org.mike;

import java.util.Set;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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

  public static void main(String[] args) throws Exception
  {
    SLF4JBridgeHandler.install();

    Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    root.setLevel(Level.INFO);

    String appId = "webgoat";
    String reportId = "24bf38c8763949c2bb3ec36be7cfd733";

    String baseUrl = "http://localhost:8070";

    String url = String.format("%s/api/v2/applications/%s/reports/%s", baseUrl, appId, reportId);

    // String url =
    // "http://search.maven.org/solrsearch/select?q=guice&rows=20&start=20&wt=json";
    // String url =
    // "http://search.maven.org/solrsearch/select?q=guice&rows=2&wt=xml";
    // String url = "http://localhost:8182/";
    // String url = "http://www.google.com/404";

    TryThreeJson myapp = new TryThreeJson();
    JSONObject rep = myapp.get(url);

    JSONObject matchSummary = rep.getJSONObject("matchSummary");
    int totalComponentCount = matchSummary.getInt("totalComponentCount");
    int knownComponentCount = matchSummary.getInt("knownComponentCount");
    LOGGER.info("Known Components: {}/{}", knownComponentCount, totalComponentCount);

    JSONArray components = rep.getJSONArray("components");
    Set<String> declaredLicenseSet = new TreeSet<String>();
    Set<String> observedLicenseSet = new TreeSet<String>();
    Set<String> overriddenLicenseSet = new TreeSet<String>();

    for (int i = 0; i < components.length(); i++)
    {
      JSONObject component = components.getJSONObject(i);
      if (component.isNull("licenseData") == false)
      {
        JSONObject licenseData = component.getJSONObject("licenseData");

        addLicenseNameToSet(declaredLicenseSet, licenseData, "declaredLicenses");
        addLicenseNameToSet(observedLicenseSet, licenseData, "observedLicenses");
        addLicenseNameToSet(overriddenLicenseSet, licenseData, "overriddenLicenses");
      }
    }

    LOGGER.info("Declared Licenses ({}):", declaredLicenseSet.size());
    printLicenseSet(declaredLicenseSet);

    LOGGER.info("Observed Licenses ({}):", observedLicenseSet.size());
    printLicenseSet(observedLicenseSet);

    LOGGER.info("Overridden Licenses ({}):", overriddenLicenseSet.size());
    printLicenseSet(overriddenLicenseSet);

  }

  private static void printLicenseSet(Set<String> pLicenseSet)
  {
    for (String licenseName : pLicenseSet)
    {
      LOGGER.info("\t * {}", licenseName);
    }
  }

  private static void addLicenseNameToSet(Set<String> pLicenseSet,
                                          JSONObject pLicenseData,
                                          String pLicenseIdentification) throws JSONException
  {
    JSONArray licenseArray = pLicenseData.getJSONArray(pLicenseIdentification);

    for (int j = 0; j < licenseArray.length(); j++)
    {
      JSONObject license = licenseArray.getJSONObject(j);
      String licenseName = license.getString("licenseName");
      pLicenseSet.add(licenseName);
    }
  }
}
