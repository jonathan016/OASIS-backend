package com.oasis.service;

import java.io.File;

public class ServiceConstant {

    public static final int DASHBOARD_REQUEST_UPDATE_PAGE_SIZE = 5;
    public static final int EMPLOYEES_LIST_PAGE_SIZE = 10;
    public static final int ASSETS_LIST_PAGE_SIZE = 10;
    public static final int REQUESTS_LIST_PAGE_SIZE = 10;
    public static final int ASSET_REQUEST_DETAILS_LIST_PAGE_SIZE = 5;
    public static final int ZERO = 0;

    public static final String STATUS_REQUESTED = "REQUESTED";
    public static final String STATUS_ACCEPTED = "ACCEPTED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_DELIVERED = "DELIVERED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_RETURNED = "RETURNED";

    public static final String ROLE_EMPLOYEE = "EMPLOYEE";
    public static final String ROLE_SUPERIOR = "SUPERIOR";
    public static final String ROLE_ADMINISTRATOR = "ADMINISTRATOR";

    public static final String TAB_OTHERS = "others";
    public static final String TAB_MY = "my";

    public static final String PREFIX_DEFAULT_PASSWORD = "gdn";
    public static final String PREFIX_SKU = "SKU";
    public static final String EXTENSION_PDF = ".pdf";

    public static final String ASCENDING = "A";

    private static final String ROOT_IMAGE_DIRECTORY = "C:".concat(File.separator)
                                                           .concat("oasis")
                                                           .concat(File.separator)
                                                           .concat("images");
    public static final String STATIC_IMAGE_DIRECTORY = ROOT_IMAGE_DIRECTORY.concat(File.separator)
                                                                            .concat("static");
    public static final String ASSET_IMAGE_DIRECTORY = ROOT_IMAGE_DIRECTORY.concat(File.separator)
                                                                           .concat("assets");
    public static final String EMPLOYEE_PHOTO_DIRECTORY = ROOT_IMAGE_DIRECTORY.concat(File.separator)
                                                                              .concat("employees");

}
