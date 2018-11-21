package com.oasis.constant;

public class APIMappingValue {

    public static final String API_LOGIN = "/api/login";

    public static final String API_DASHBOARD_STATUS = "/api/dashboard/status/{employeeNik}";
    public static final String API_DASHBOARD_REQUEST_UPDATE = "/api/dashboard/request-update/{employeeNik}";

    public static final String API_LIST_ASSET = "/api/assets";
    public static final String API_FIND_ASSET = "/api/assets/find";
    public static final String API_SAVE_ASSET = "/api/assets/save";
    public static final String API_DELETE_ASSET = "/api/assets/delete";
    public static final String API_DETAIL_ASSET = "/api/assets/{sku}";
    public static final String API_IMAGE_ASSET = "/api/assets/{sku}/{imageName}.{extension}";
    public static final String API_PDF_ASSET = "/api/assets/{sku}/pdf";

    public static final String API_LIST_EMPLOYEE = "/api/employees";
    public static final String API_FIND_EMPLOYEE = "/api/employees/find";
    public static final String API_SAVE_EMPLOYEE = "/api/employees/save";
    public static final String API_DELETE_EMPLOYEE = "/api/employees/delete";
    public static final String API_CHANGE_SUPERVISOR_ON_DELETE = "/api/employees/delete/change-supervisor";
    public static final String API_DETAIL_EMPLOYEE = "/api/employees/{employeeNik}";

}