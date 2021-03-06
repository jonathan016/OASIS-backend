package com.oasis.web_model.response.success.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestOthersListResponse {

    private List< RequestListObject > requests;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestListObject {

        private RequestListObject.Request request;
        private RequestListObject.Employee employee;
        private RequestListObject.Asset asset;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Request {

            private String id;
            private String status;
            private String note;
            private String updatedDate;

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
