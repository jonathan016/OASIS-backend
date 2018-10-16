package com.oasis.webmodel.response.responsemodel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
public class RequestUpdateSectionResponseModel {
    private String assetId;
    private String assetName;
    private Integer assetQuantity;
    private String requestNote;
    private String employeeId;
    private String employeeName;
    private String requestStatus;
    private String supervisorId;
    private String supervisorName;
}
