package uk.gov.ons.collection.entity;

import lombok.extern.log4j.Log4j2;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import uk.gov.ons.collection.exception.InvalidIdbrPeriodException;
import uk.gov.ons.collection.exception.InvalidJsonException;
import uk.gov.ons.collection.utilities.RelativePeriod;

@Log4j2
public class HistoryDetailsResponse {
    private JSONObject jsonPeriodicityResponse;
    private static final int NUMBER_HISTORY_PERIODS = 12;
    private static final String REFERENCE = "reference";
    private static final String PERIOD = "period";
    private static final String SURVEY = "survey";

    public HistoryDetailsResponse(String jsonString) {
        try {
            jsonPeriodicityResponse = new JSONObject(jsonString);
        } catch (Exception e) {
            jsonString = "{}";
            jsonPeriodicityResponse = new JSONObject(jsonString);
        }
    }

    public HistoryDetailsResponse() {

    }

    public String parsePeriodicityFromSurvey() throws InvalidJsonException {
        var outputArray = new JSONArray();
        String periodStr = "";
        outputArray = jsonPeriodicityResponse.getJSONObject("data").getJSONObject("allSurveys").getJSONArray("nodes");
        if (outputArray.length() > 0) {
            for (int i = 0; i < outputArray.length(); i++) {
                periodStr = outputArray.getJSONObject(i).getString("periodicity");
            }
            //This is when no no periodicity is associated to a given survey
            if (periodStr.isEmpty()) {
                throw new InvalidJsonException(" There is no periodicity for the given survey ");
            }

        } else {
            //When there is no survey table
            throw new InvalidJsonException(" There is no configuration Survey table which provides periodicity");
        }

        return periodStr;
    }

    public List<String> getHistoryPeriods(String currentPeriod, String periodicity) throws InvalidIdbrPeriodException {
        List<String> historyPeriodList = new ArrayList<String>();
        List<Integer> offsetList = new ArrayList<Integer>();

        historyPeriodList.add(currentPeriod);
        for (int i = 1; i <= NUMBER_HISTORY_PERIODS; i++) {
            offsetList.add(i);
        }
        try {
            RelativePeriod rp = new RelativePeriod(periodicity);
            List<String> periods = rp.getIdbrPeriods(offsetList, currentPeriod);
            log.info("IDBR previous periods: " + periods.toString());
            historyPeriodList.addAll(periods);
        } catch (Exception e) {
            throw new InvalidIdbrPeriodException("Problem in getting IDBR periods" + e.getMessage(), e);
        }
        return historyPeriodList;
    }

    public List<String> getCurrentAndPreviousHistoryPeriod(String currentPeriod, String periodicity) throws InvalidIdbrPeriodException {
        List<String> historyPeriodList = new ArrayList<String>();
        historyPeriodList.add(currentPeriod);
        try {
            RelativePeriod rp = new RelativePeriod(periodicity);
            String previousPeriod = rp.getPreviousPeriod(currentPeriod);
            historyPeriodList.add(previousPeriod);
            log.info("IDBR current and Previous periods: " + historyPeriodList.toString());
        } catch (Exception e) {
            throw new InvalidIdbrPeriodException("Problem in getting IDBR periods" + e.getMessage(), e);
        }
        return historyPeriodList;
    }

    public String parseHistoryDataResponses(String responseJson) throws InvalidJsonException {
        JSONObject queryOutput = new JSONObject(responseJson);
        JSONArray contribArray = new JSONArray();
        var historyDataObj = new JSONObject();
        var historyDataArr = new JSONArray();
        try {
            if (queryOutput.getJSONObject("data").getJSONObject("allContributors").getJSONArray("nodes").length() > 0) {
                contribArray = queryOutput.getJSONObject("data").getJSONObject("allContributors").getJSONArray("nodes");
                for (int i = 0; i < contribArray.length(); i++) {
                    JSONObject eachContributorObj = queryOutput.getJSONObject("data").getJSONObject("allContributors").getJSONArray("nodes").getJSONObject(i);
                    var historyDetailsObject = new JSONObject();
                    historyDetailsObject.put(REFERENCE, eachContributorObj.get(REFERENCE));
                    historyDetailsObject.put(PERIOD, eachContributorObj.get(PERIOD));
                    historyDetailsObject.put(SURVEY,eachContributorObj.get(SURVEY));
                    JSONArray formDefinitionArray = eachContributorObj.getJSONObject("formByFormid")
                            .getJSONObject("formdefinitionsByFormid").getJSONArray("nodes");
                    if (formDefinitionArray.length() > 0) {
                        var viewFormResponsesArray = buildFormDefinitionJsonWithEmptyResponses(formDefinitionArray);
                        buildJsonResponsesByResponsePeriod(queryOutput, viewFormResponsesArray, i);
                        historyDetailsObject.put("view_form_responses", viewFormResponsesArray);
                    } else {
                        throw new InvalidJsonException("There is no form definition associated to a given Contributor. Please verify");
                    }
                    historyDataArr.put(historyDetailsObject);
                }
                historyDataObj.put("history_data", historyDataArr);
            } else {
                throw new InvalidJsonException("There are no contributors for a given survey, reference and periods. Please verify");
            }

        } catch (Exception e) {
            throw new InvalidJsonException("Problem in parsing History Detail GraphQL responses " + e.getMessage(), e);
        }
        return historyDataObj.toString();
    }


    private JSONArray buildFormDefinitionJsonWithEmptyResponses(JSONArray formDefinitionArray) throws JSONException {
        var viewFormResponsesArray = new JSONArray();
        for (int j = 0; j < formDefinitionArray.length(); j++) {
            var eachFormObject = new JSONObject();
            eachFormObject.put("questioncode", formDefinitionArray.getJSONObject(j).getString("questioncode"));
            eachFormObject.put("displaytext", formDefinitionArray.getJSONObject(j).getString("displaytext"));
            eachFormObject.put("displayquestionnumber", formDefinitionArray.getJSONObject(j).getString("displayquestionnumber"));
            eachFormObject.put("displayorder", formDefinitionArray.getJSONObject(j).getInt("displayorder"));
            eachFormObject.put("type", formDefinitionArray.getJSONObject(j).getString("type"));
            eachFormObject.put("response", "");
            eachFormObject.put("instance", "");
            viewFormResponsesArray.put(eachFormObject);
        }
        return viewFormResponsesArray;
    }

    private void buildJsonResponsesByResponsePeriod(JSONObject queryOutput, JSONArray viewFormResponsesArray, int index) throws JSONException {
        var responseArray = new JSONArray();
        responseArray = queryOutput.getJSONObject("data").getJSONObject("allContributors").getJSONArray("nodes").getJSONObject(index)
                .getJSONObject("responsesByReferenceAndPeriodAndSurvey").getJSONArray("nodes");
        for (int k = 0; k < viewFormResponsesArray.length(); k++) {
            for (int l = 0; l < responseArray.length(); l++) {
                if (viewFormResponsesArray.getJSONObject(k).getString("questioncode").equals(responseArray.getJSONObject(l)
                        .getString("questioncode"))) {
                    if(responseArray.getJSONObject(l).isNull("response")){
                        viewFormResponsesArray.getJSONObject(k).put("response", "");
                    } else {
                        viewFormResponsesArray.getJSONObject(k).put("response", responseArray.getJSONObject(l).get("response"));
                    }
                    viewFormResponsesArray.getJSONObject(k).put("instance", responseArray.getJSONObject(l).getInt("instance"));
                }
            }
        }
    }

}
