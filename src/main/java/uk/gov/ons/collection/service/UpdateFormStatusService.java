package uk.gov.ons.collection.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UpdateFormStatusService {

    @Autowired
    UpdateFormStatusProxy updateFormStatusProxy;
    
    public String updateStatus(String params, String body) {
        return updateFormStatusProxy.updateStatus(params, body);
    }
}
