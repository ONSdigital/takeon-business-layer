package uk.gov.ons.collection.test;

import org.junit.jupiter.api.Test;

import uk.gov.ons.collection.exception.InvalidDerivedResponseException;
import uk.gov.ons.collection.exception.InvalidJsonException;
import uk.gov.ons.collection.utilities.calculateDerivedValuesQuery;
import uk.gov.ons.collection.utilities.calculateDerviedValuesResponse;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;

class calculateDerivedValuesTest {

    String formInput = "{" + "\"data\": {" + "\"allContributors\": {" + "\"nodes\": [" + "{" + "\"formByFormid\": {"
            + "\"formdefinitionsByFormid\": {" + "\"nodes\": [" + "{" + "\"questioncode\": \"1000\","
            + "\"derivedformula\": \"\"" + "}," + "{" + "\"questioncode\": \"1001\"," + "\"derivedformula\": \"\""
            + "}," + "{" + "\"questioncode\": \"2000\"," + "\"derivedformula\": \"\"" + "}," + "{"
            + "\"questioncode\": \"3000\"," + "\"derivedformula\": \"\"" + "}," + "{" + "\"questioncode\": \"4000\","
            + "\"derivedformula\": \"1000 + 1001\"" + "}," + "{" + "\"questioncode\": \"4001\","
            + "\"derivedformula\": \"1000 - 1001\"" + "}" + "]" + "}" + "}" + "}" + "]" + "}" + "}" + "}";

    String responseInput = "{" + "\"data\": {" + "\"allResponses\": {" + "\"nodes\": [" + "{" + "\"response\": \"20\","
            + "\"questioncode\": \"1000\"" + "}," + "{" + "\"response\": \"1\"," + "\"questioncode\": \"1001\"" + "},"
            + "{" + "\"response\": \"1\"," + "\"questioncode\": \"2000\"" + "}," + "{" + "\"response\": \"Rhubarb\","
            + "\"questioncode\": \"3000\"" + "}," + "{" + "\"response\": \"21\"," + "\"questioncode\": \"4000\"" + "},"
            + "{" + "\"response\": \"19\"," + "\"questioncode\": \"4001\"" + "}" + "]" + "}" + "}" + "}";

    @Test
    void buildFormDefinitionQuery_validInput_queryBuiltSuccessfully() {
        Map<String, String> variables = new HashMap<>();
        variables.put("reference", "12345678001");
        variables.put("period", "201801");
        variables.put("survey", "999A");
        String formQuery = new String();
        try {
            formQuery = new calculateDerivedValuesQuery(variables).buildFormDefinitionQuery();
        } catch (InvalidJsonException e) {
            System.out.println("Can't build form query for calculating derived values" + e);
        }
        var expectedQuery = "{\"query\":\"query formDefinitionByReference "+
                "{allContributors(condition: {reference: \"12345678001\",period: \"201801\",survey: \"999A\"})"+
                "{nodes {formByFormid {formdefinitionsByFormid {nodes {questioncode,derivedformula}}}}}}\"}";
        assertEquals(expectedQuery, formQuery);
    }

    @Test
    void buildResponseQuery_validInput_querySuccessful() {
        Map<String,String> variables = new HashMap<>();
        variables.put("reference", "12345678001");
        variables.put("period", "201801");
        variables.put("survey", "999A");
        String responseQuery = new String();
        try {
            responseQuery = new calculateDerivedValuesQuery(variables).buildGetResponsesQuery();
        } catch (InvalidJsonException e) {
            System.out.println("Can't build response query for calculating derived values" + e);
        }
        var expectedQuery = "{\"query\":\"query getResponses($reference: String, $period: String, $survey: String){" +
            "allResponses(condition: {reference: \"12345678001\",period: \"201801\",survey: \"999A\"}){" +
            "nodes {response questioncode}}}\"}";
        assertEquals(expectedQuery, responseQuery);
    }

    @Test
    void parseFormData_returnsExpectedOutput_validInput(){
        var expectedOutput =  "{"
            +           "\"form_data\":["
            +             "{"
            +               "\"derivedformula\":\"\","
            +               "\"questioncode\":\"1000\""
            +             "},"
            +             "{"
            +               "\"derivedformula\":\"\","
            +               "\"questioncode\":\"1001\""
            +             "},"
            +             "{"
            +               "\"derivedformula\":\"\","
            +               "\"questioncode\":\"2000\""
            +             "},"
            +             "{"
            +               "\"derivedformula\":\"\","
            +               "\"questioncode\":\"3000\""
            +             "},"
            +             "{"
            +               "\"derivedformula\":\"1000 + 1001\","
            +               "\"questioncode\":\"4000\""
            +             "},"
            +             "{"
            +               "\"derivedformula\":\"1000 - 1001\","
            +               "\"questioncode\":\"4001\""
            +             "}"
            +           "]"
            +         "}";
        JSONObject response = new JSONObject();
        try {
            response = new calculateDerviedValuesResponse(formInput, responseInput).parseFormData();
        } catch (Exception e) {
            System.out.println("Can't parse form data calculating derived values" + e);
        }
        assertEquals(expectedOutput, response.toString());
        
    }

    @Test
    void parseResponseData_returnsExpectedOutput_validInput() {

        var expectedOutput = "{"
        +  "\"response_data\":["
        +  "{"
        +  "\"response\":\"20\","
        + "\"questioncode\":\"1000\""
        +  "},"
        +  "{"
        +  "\"response\":\"1\","
        +       "\"questioncode\":\"1001\""
        +     "},"
        +      "{"
        +        "\"response\":\"1\","
        +        "\"questioncode\":\"2000\""
        +      "},"
        +      "{"
        +        "\"response\":\"Rhubarb\","
        +        "\"questioncode\":\"3000\""
        +      "},"
        +      "{"
        +        "\"response\":\"21\","
        +        "\"questioncode\":\"4000\""
        +      "},"
        +     "{"
        +        "\"response\":\"19\","
        +        "\"questioncode\":\"4001\""
        +     "}"
        +    "]"
        +  "}";

        JSONObject response = new JSONObject();
        try {
            response = new calculateDerviedValuesResponse(formInput, responseInput).parseResponseData();
        } catch (Exception e) {
            System.out.println("Can't parse form data calculating derived values" + e);
        }
        assertEquals(expectedOutput, response.toString());

    }

    @Test
    void calculateDerviedValues_returnsExpectedValues(){

        var expectedOutput = "[{\"result\":\"21\",\"questioncode\":\"4000\"}," +
                            "{\"result\":\"19\",\"questioncode\":\"4001\"}]";
        JSONArray response = new JSONArray();
        try {
            response = new calculateDerviedValuesResponse(formInput, responseInput).calculateDerviedValues();
        } catch (Exception e) {
            System.out.println("Can't calculate derived values" + e);
        }
        assertEquals(expectedOutput, response.toString());
        
    }

    @Test
    void calculateDerviedValues_decimalInput_returnsDecimalValues() {
        var responseDecimalInput = "{"
        + "\"data\": {"
        + "\"allResponses\": {"
        +  "\"nodes\": ["
        +  "{"
        +  "\"response\": \"20.0000000\","
        + "\"questioncode\": \"1000\""
        +  "},"
        +  "{"
        +  "\"response\": \"1.00000000\","
        +        "\"questioncode\": \"1001\""
        +     "},"
        +      "{"
        +        "\"response\": \"1\","
        +        "\"questioncode\": \"2000\""
        +      "},"
        +      "{"
        +        "\"response\": \"Rhubarb\","
        +        "\"questioncode\": \"3000\""
        +      "},"
        +      "{"
        +        "\"response\": \"21\","
        +        "\"questioncode\": \"4000\""
        +      "},"
        +     "{"
        +        "\"response\": \"19\","
        +        "\"questioncode\": \"4001\""
        +     "}"
        +    "]"
        +  "}"
        + "}"
        + "}";

        var expectedOutput = "[{\"result\":\"21.0\",\"questioncode\":\"4000\"}," +
        "{\"result\":\"19.0\",\"questioncode\":\"4001\"}]";
        JSONArray response = new JSONArray();
        try {
            response = new calculateDerviedValuesResponse(formInput, responseDecimalInput).calculateDerviedValues();
        } catch (Exception e) {
            System.out.println("Can't calculate derived values" + e);
        }
        assertEquals(expectedOutput, response.toString());

    }

    @Test
    void calculateDerviedValues_stringResponseInput_returnsError() {
        var responseStringInput = "{"
        + "\"data\": {"
        + "\"allResponses\": {"
        +  "\"nodes\": ["
        +  "{"
        +  "\"response\": \"TEST\","
        + "\"questioncode\": \"1000\""
        +  "},"
        +  "{"
        +  "\"response\": \"1.00000000\","
        +        "\"questioncode\": \"1001\""
        +     "},"
        +      "{"
        +        "\"response\": \"1\","
        +        "\"questioncode\": \"2000\""
        +      "},"
        +      "{"
        +        "\"response\": \"Rhubarb\","
        +        "\"questioncode\": \"3000\""
        +      "},"
        +      "{"
        +        "\"response\": \"21\","
        +        "\"questioncode\": \"4000\""
        +      "},"
        +     "{"
        +        "\"response\": \"19\","
        +        "\"questioncode\": \"4001\""
        +     "}"
        +    "]"
        +  "}"
        + "}"
        + "}";

        Assertions.assertThrows(InvalidDerivedResponseException.class, () -> {
            new calculateDerviedValuesResponse(formInput, responseStringInput).calculateDerviedValues();
        });
    }

    @Test
    void updateDerivedResponses_validInput_returnsExpectedData() {
        var expectedOutput = "{\"responses\":[{\"question\":\"4000\",\"response\":\"21\"}," +
        "{\"question\":\"4001\",\"response\":\"19\"}]}";
        JSONObject response = new JSONObject();
        try {
            response = new calculateDerviedValuesResponse(formInput, responseInput)
                    .updateDerivedQuestionResponses();
        } catch (Exception e) {
            System.out.println("Can't update derived values" + e);
        }
        assertEquals(expectedOutput, response.toString());
    }


}