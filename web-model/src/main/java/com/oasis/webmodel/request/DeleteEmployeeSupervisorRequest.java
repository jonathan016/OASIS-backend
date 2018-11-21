package com.oasis.webmodel.request;

import lombok.Data;

@Data
public class DeleteEmployeeSupervisorRequest {
    private String adminNik;
    private String oldSupervisorNik;
    private String newSupervisorNik;
}
