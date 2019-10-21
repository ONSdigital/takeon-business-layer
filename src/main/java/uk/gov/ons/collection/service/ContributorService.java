package uk.gov.ons.collection.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.collection.entity.ContributorEntity;

@Log4j2
@Service
public class ContributorService {

    @Autowired
    ContributorProxy contributorProxy;

    public Iterable<ContributorEntity> generalSearch(String parameters) {
        log.info("Reached contributor service");
        return contributorProxy.searchContributor(parameters);
    }
}
