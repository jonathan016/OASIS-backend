package com.oasis.web_model.request.employees;

import lombok.Data;

@Data
public class DeleteEmployeeSupervisorRequest {

    private String adminUsername;
    private String oldSupervisorUsername;
    private String newSupervisorUsername;

}
