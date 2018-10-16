package com.oasis.webmodel.response.success;

import com.oasis.webmodel.response.responsemodel.RequestUpdateSectionResponseModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardRequestUpdateSuccessResponse {
    private String employeeId;
    private String role;
    private List<RequestUpdateSectionResponseModel> requests;
}
