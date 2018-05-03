package org.mike;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

class Reports
{
  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Reports.class);

  private List<Report> reportSet;

  List<Report> getReportSet(){ return reportSet; }


  Reports()
  {
  }

  Reports invoke() throws Exception
  {
    String url = String.format(App.ALL_REPORTS_REST_V2, App.BASE_URL);
    reportSet = new ArrayList<Report>();

    TryThreeJson myapp = new TryThreeJson();
    JSONArray reports = myapp.get(url, JSONArray.class);
    LOGGER.info("Reports: {}", reports.length());


    for (int i = 0; i < reports.length(); i++)
    {
      JSONObject component = reports.getJSONObject(i);
      Report report = new Report();
      report.stage = component.getString("stage");
      report.appId = component.getString("applicationId");
      String htmlUrl = component.getString("reportHtmlUrl");
      report.reportId = htmlUrl.substring(htmlUrl.lastIndexOf("/") + 1);
      report.appName = htmlUrl.split("/")[3];

      reportSet.add(report);
    }

    return this;
  }

  public List<ApplicationReport> getApplicationReports() throws Exception
  {
    List<ApplicationReport> returnValue = new ArrayList<ApplicationReport>();
    int i = 0;
    for (Report report : getReportSet())
    {
      i++;
      LOGGER.debug("Progress: {}", i);

      returnValue.add(new ApplicationReport(report).invoke());
    }

    return returnValue;
  }
}

class Report
{
  String stage;
  String appId;
  String appName;
  String reportId;
}