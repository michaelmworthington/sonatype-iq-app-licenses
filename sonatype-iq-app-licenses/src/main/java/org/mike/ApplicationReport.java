package org.mike;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.TreeSet;

class ApplicationReport
{
  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ApplicationReport.class);

  String appId;
  String appName;
  String stage;
  String reportId;
  private Set<String> declaredLicenseSet;
  private Set<String> observedLicenseSet;
  private Set<String> overriddenLicenseSet;

  ApplicationReport(String appId, String reportId)
  {
    this.appName = appId;
    this.reportId = reportId;
  }

  ApplicationReport(Report pReport)
  {
    this.appId = pReport.appId;
    this.appName = pReport.appName;
    this.stage = pReport.stage;
    this.reportId = pReport.reportId;
  }

  ApplicationReport invoke() throws Exception
  {
    String url = String.format(TryThreeJson.APP_REPORT_REST_V2, TryThreeJson.BASE_URL, appName, reportId);

    TryThreeJson myapp = new TryThreeJson();
    JSONObject rep = myapp.get(url, JSONObject.class);

    JSONObject matchSummary = rep.getJSONObject("matchSummary");
    int totalComponentCount = matchSummary.getInt("totalComponentCount");
    int knownComponentCount = matchSummary.getInt("knownComponentCount");
    LOGGER.debug("Known Components: {}/{}", knownComponentCount, totalComponentCount);

    JSONArray components = rep.getJSONArray("components");
    declaredLicenseSet = new TreeSet<String>();
    observedLicenseSet = new TreeSet<String>();
    overriddenLicenseSet = new TreeSet<String>();

    for (int i = 0; i < components.length(); i++)
    {
      JSONObject component = components.getJSONObject(i);
      if (component.isNull("licenseData") == false)
      {
        JSONObject licenseData = component.getJSONObject("licenseData");

        //if overridden licenses (a.k.a. Effective licenses)
        //add those and skip declared and observed


        //TODO: Add a counter so that we know how many components are using each license
        if (licenseData.has("overriddenLicenses")
                && licenseData.getJSONArray("overriddenLicenses").length() > 0)
        {
          addLicenseNameToSet(overriddenLicenseSet, licenseData, "overriddenLicenses");
        } else
        {
          addLicenseNameToSet(declaredLicenseSet, licenseData, "declaredLicenses");
          addLicenseNameToSet(observedLicenseSet, licenseData, "observedLicenses");
        }
      }
    }
    return this;
  }

  boolean doesContainLicense(String pLicenseString)
  {
    return overriddenLicenseSet.contains(pLicenseString)
            || declaredLicenseSet.contains(pLicenseString)
            || observedLicenseSet.contains(pLicenseString);
  }

  void printLicenseInfo()
  {
    Set<String> declaredLicenseSet = getDeclaredLicenseSet();
    Set<String> observedLicenseSet = getObservedLicenseSet();
    Set<String> overriddenLicenseSet = getOverriddenLicenseSet();

    LOGGER.info("Declared Licenses ({}):", declaredLicenseSet.size());
    App.printLicenseSet(declaredLicenseSet);

    LOGGER.info("Observed Licenses ({}):", observedLicenseSet.size());
    App.printLicenseSet(observedLicenseSet);

    LOGGER.info("Overridden Licenses ({}):", overriddenLicenseSet.size());
    App.printLicenseSet(overriddenLicenseSet);
  }


  private void addLicenseNameToSet(Set<String> pLicenseSet,
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

  private Set<String> getDeclaredLicenseSet()
  {
    return declaredLicenseSet;
  }

  private Set<String> getObservedLicenseSet()
  {
    return observedLicenseSet;
  }

  private Set<String> getOverriddenLicenseSet()
  {
    return overriddenLicenseSet;
  }

}
