package com.oasis.webmodel.request;

import lombok.Data;

@Data
public class DashboardRequestUpdateRequest {
    private String employeeId;
    private String role;
    private String currentTab;
    private Integer startIndex;
}
