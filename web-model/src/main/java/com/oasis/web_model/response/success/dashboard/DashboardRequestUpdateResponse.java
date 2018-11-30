package com.oasis.web_model.response.success.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardRequestUpdateResponse {

    private List<RequestUpdateModel> requests;

    @Data
    @AllArgsConstructor
    public static class RequestUpdateModel {

        private Request request;
        private Employee employee;
        private Supervisor supervisor;
        private Asset asset;

        @Data
        @AllArgsConstructor
        public static class Request {

            private String id;
            private String status;
            private String requestNote;
        }

        @Data
        @AllArgsConstructor
        public static class Employee {

            private String username;
            private String name;
        }

        @Data
        @AllArgsConstructor
        public static class Supervisor {

            private String username;
            private String name;
        }

        @Data
        @AllArgsConstructor
        public static class Asset {

            private String sku;
            private String name;
            private int quantity;
        }

    }

}
