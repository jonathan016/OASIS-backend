package com.oasis.web_model.response.success.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatusResponse {

    private long requestedRequests;
    private long pendingHandoverRequests;
    private long availableAsset;

}
