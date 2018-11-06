package com.oasis.constant;

public class APIMappingValue {

    public static final String API_LOGIN = "/api/login";
    public static final String API_DASHBOARD_STATUS = "/api/dashboard/status/{employeeNik}";
    public static final String API_DASHBOARD_REQUEST_UPDATE =
            "/api/dashboard/requestUpdate/{employeeNik}";

    public static final String API_EMPLOYEE_LIST = "/api/employees";
    public static final String API_EMPLOYEE_DETAIL = "/api/employees/{employeeNik}";
    public static final String API_EMPLOYEE_FIND = "/api/employees/find";
    public static final String API_SAVE_EMPLOYEE = "/api/employees/save";
    public static final String API_DELETE_EMPLOYEE = "/api/employees/delete";
    public static final String API_CHANGE_SUPERVISOR_ON_DELETE = "/api/employees/delete/change-supervisor";
}