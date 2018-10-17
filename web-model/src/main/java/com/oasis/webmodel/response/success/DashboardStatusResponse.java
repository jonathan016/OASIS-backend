package com.oasis.webmodel.response.success;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatusResponse {
    private int requestedRequests;
    private int pendingHandoverRequests;
    private int availableAsset;
}
