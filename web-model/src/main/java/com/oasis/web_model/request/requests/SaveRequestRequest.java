package com.oasis.web_model.request.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveRequestRequest {

    List< SaveRequestRequest.Request > requests;
    private String username;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {

        private String _id;
        private String sku;
        private long quantity;
        private String status;
        private String requestNote;
        private String transactionNote;

    }

}
