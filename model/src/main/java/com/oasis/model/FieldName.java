package com.oasis.model;

public class FieldName {
    //Field names for EmployeeModel
    public static final String EMPLOYEE_FULLNAME = "fullname";
    public static final String EMPLOYEE_DOB = "dob";
    public static final String EMPLOYEE_USERNAME = "username";
    public static final String EMPLOYEE_PASSWORD = "password";
    public static final String EMPLOYEE_PHONE = "phone";
    public static final String EMPLOYEE_JOB_TITLE = "jobTitle";
    public static final String EMPLOYEE_DIVISION = "division";
    public static final String EMPLOYEE_SUPERVISING_COUNT = "supervisingCount";
    public static final String EMPLOYEE_SUPERVISION_ID = "supervisionId";

    //Field names for AssetModel
    public static final String ASSET_NAME = "name";
    public static final String ASSET_LOCATION = "location";
    public static final String ASSET_PRICE = "price";
    public static final String ASSET_STOCK = "stock";
    public static final String ASSET_BRAND = "brand";
    public static final String ASSET_TYPE = "type";

    //Field names for RequestModel
    public static final String REQUEST_EMPLOYEE_ID = "employeeId";
    public static final String REQUEST_ASSET_ID = "assetId";
    public static final String REQUEST_ASSET_QUANTITY = "assetQuantity";
    public static final String REQUEST_STATUS = "status";
    public static final String REQUEST_REQUEST_NOTE = "requestNote";
    public static final String REQUEST_TRANSACTION_NOTE = "transactionNote";

    //Field names for SupervisionModel
    public static final String SUPERVISION_SUPERVISOR_ID = "supervisorId";
    public static final String SUPERVISION_EMPLOYEE_ID = "employeeId";
}
