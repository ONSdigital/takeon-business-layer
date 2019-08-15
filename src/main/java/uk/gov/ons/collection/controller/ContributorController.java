package uk.gov.ons.collection.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.gov.ons.collection.entity.ContributorEntity;
import uk.gov.ons.collection.exception.DataNotFondException;
import uk.gov.ons.collection.service.ContributorService;

import java.util.*;

@Log4j2
@Api(value = "Contributor Controller", description = "Main (and so far only) end point for the connection between the UI and persistance layer")
@RestController
@RequestMapping(value = "/contributor")
public class ContributorController {

    private List<String> defaultValidSearchColumns = new ArrayList<>(Arrays.asList("reference", "period", "survey",
            "status", "formid"));

    @Autowired
    ContributorService service;
    @ApiOperation(value = "Search contributor table by arbitrary parameters", response = String.class)
    @GetMapping(value = "/search/{vars}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of Contributor details", response = ContributorEntity.class),
            @ApiResponse(code = 404, message = "Contributor does not exist"),
            @ApiResponse(code = 500, message = "Internal server error")})
    @ResponseBody
    public Iterable<ContributorEntity> getSearch(@MatrixVariable Map<String, String> matrixVars) throws Exception {
        Iterable<ContributorEntity> contributorEntities = null;
        String filteredSearchParameters = filterAndPrepareSearchParameters(matrixVars, this.defaultValidSearchColumns);
        log.info("Filtered search parameters { }", filteredSearchParameters);
        contributorEntities = service.generalSearch(filteredSearchParameters);
        log.info("Contributor Entities after calling Persistance Layer { }", filteredSearchParameters);

        if (contributorEntities == null) {
            throw new Exception();
        } else {
            if (contributorEntities instanceof Collection) {
                int size =  ((Collection<?>) contributorEntities).size();
                if(size == 0) {
                    throw new DataNotFondException("Persistance Layer is returning Zero records");
                }
            }
        }

        return contributorEntities;

    }

    public String filterAndPrepareSearchParameters(Map<String, String> inputParameters, List<String> allowedParameters) {
        Map<String,String> filteredParameters = UrlParameterBuilder.filter(inputParameters, allowedParameters);
        return UrlParameterBuilder.buildParameterString(filteredParameters);
    }


}