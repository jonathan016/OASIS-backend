package com.oasis.web_model.response.success.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
public class RequestListResponse {

    private List<RequestListObject> requests;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestListObject {

        private Request request;
        private Employee employee;
        private Asset asset;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Request {

            private String id;
            private String status;
            private String requestNote;

        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Employee {

            private String username;
            private String name;
            private String photo;

        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Asset {

            private String sku;
            private String name;
            private long quantity;

        }
    }
}
