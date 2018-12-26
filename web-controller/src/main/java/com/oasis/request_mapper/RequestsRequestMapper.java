package com.oasis.request_mapper;

import com.oasis.model.entity.RequestModel;
import com.oasis.web_model.request.requests.SaveRequestRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RequestsRequestMapper {

    public List< RequestModel > getRequestsListFromRequest(
            final String username, final SaveRequestRequest request
    ) {

        List< RequestModel > requests = new ArrayList<>();

        for (SaveRequestRequest.Request requestObject : request.getRequests()) {
            RequestModel requestModel = new RequestModel();

            requestModel.set_id(requestObject.get_id());
            requestModel.setUsername(username);
            requestModel.setSku(requestObject.getSku());
            requestModel.setQuantity((int) requestObject.getQuantity());
            requestModel.setStatus(requestObject.getStatus());
            requestModel.setRequestNote(requestObject.getRequestNote());
            requestModel.setTransactionNote(requestObject.getTransactionNote());

            requests.add(requestModel);

        }

        return requests;

    }

}
