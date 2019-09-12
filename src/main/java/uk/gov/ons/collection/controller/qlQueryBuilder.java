package uk.gov.ons.collection.controller;

import java.util.Map;

public class qlQueryBuilder {

    public String buildQuery(Map<String, String> searchParameters){

        String queryPrefix = "{\"query\": \"query contributorSearchBy {" +
                "allContributors ";
        String querySuffix = "{" +
                "nodes {" +
                "reference, period, survey, formid, status, receiptdate, lockedby, lockeddate}}}\" }";

        StringBuilder builtQuery = new StringBuilder();
        builtQuery.append(queryPrefix);
        if (searchParameters != null && !searchParameters.isEmpty()){
            builtQuery.append("(condition: { ");
            searchParameters.forEach((key,value) -> builtQuery.append(key + ": \\\"" + value + "\\\" "));
            builtQuery.append("})");
        }
        builtQuery.append(querySuffix);
        return builtQuery.toString();
    }
}