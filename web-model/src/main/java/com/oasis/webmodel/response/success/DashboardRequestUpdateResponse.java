package com.oasis.webmodel.response.success;

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

            private String requestId;
            private String requestStatus;
            private String requestNote;
        }

        @Data
        @AllArgsConstructor
        public static class Employee {

            private String employeeNik;
            private String employeeName;
        }

        @Data
        @AllArgsConstructor
        public static class Supervisor {

            private String supervisorNik;
            private String supervisorName;
        }

        @Data
        @AllArgsConstructor
        public static class Asset {

            private String assetSku;
            private String assetName;
            private int assetQuantity;
        }
    }

}
