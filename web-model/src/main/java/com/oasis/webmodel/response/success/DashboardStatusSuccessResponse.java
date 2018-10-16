package com.oasis.webmodel.response.success;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatusSuccessResponse {
    private String employeeId;
    private String role;
    private Integer requestedRequests;
    private Integer pendingHandoverRequests;
    private Integer availableAsset;
}
