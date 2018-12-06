package com.oasis.service;

import java.io.File;

public class ServiceConstant {

    public static final int DASHBOARD_REQUEST_UPDATE_PAGE_SIZE = 5;
    public static final int EMPLOYEES_FIND_EMPLOYEE_PAGE_SIZE = 10;
    public static final int ASSETS_FIND_ASSET_PAGE_SIZE = 10;
    public static final int REQUESTS_FIND_REQUEST_PAGE_SIZE = 10;
    public static final int ZERO = 0;

    public static final String REQUESTED = "Requested";
    public static final String ACCEPTED = "Accepted";
    public static final String CANCELLED = "Cancelled";
    public static final String DELIVERED = "Delivered";
    public static final String REJECTED = "Rejected";
    public static final String RETURNED = "Returned";

    public static final String ROLE_EMPLOYEE = "EMPLOYEE";
    public static final String ROLE_SUPERIOR = "SUPERIOR";
    public static final String ROLE_ADMINISTRATOR = "ADMINISTRATOR";

    public static final String TAB_OTHERS = "Others";
    public static final String TAB_SELF = "Self";

    public static final String NIK_PREFIX = "GDN";
    public static final String SKU_PREFIX = "SKU";

    private static final String ROOT_IMAGE_DIRECTORY =
            "C:".concat(File.separator).concat("oasis").concat(File.separator).concat("images");
    public static final String RESOURCE_IMAGE_DIRECTORY = ROOT_IMAGE_DIRECTORY.concat(File.separator).concat("static");
    public static final String ASSET_IMAGE_DIRECTORY = ROOT_IMAGE_DIRECTORY.concat(File.separator).concat("assets");
    public static final String EMPLOYEE_IMAGE_DIRECTORY = ROOT_IMAGE_DIRECTORY.concat(File.separator).concat(
            "employees");

    public static final String PDF_EXTENSION = ".pdf";
    public static final String SPACE = " ";
    public static final String ASCENDING = "A";
    public static final String DESCENDING = "D";
}
