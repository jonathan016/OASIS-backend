package com.oasis.web_model.constant;

public class APIMappingValue {

    public static final String CROSS_ORIGIN_LINK = "http://localhost";

    public static final String API_MISDIRECT = "**/**";

    public static final String API_LOGIN = "/api/login";
    public static final String API_LOGOUT = "/api/logout";
    public static final String API_ASSET = "/api/assets";
    public static final String API_DASHBOARD = "/api/dashboard";
    public static final String API_EMPLOYEE = "/api/employees";
    public static final String API_REQUEST = "/api/requests";

    public static final String API_LIST = "/list";
    public static final String API_SAVE = "/save";
    public static final String API_DELETE = "/delete";

    public static final String API_DATA_ASSET = "/{identifier:SKU-[0-9][0-9][0-9]-[0-9][0-9][0-9]-[0-9][0-9][0-9]}";
    public static final String API_IMAGE_ASSET = "/{identifier:SKU-[0-9][0-9][0-9]-[0-9][0-9][0-9]-[0-9][0-9][0-9]}/{image:.+}";
    public static final String API_PDF_ASSET = "/{identifier:SKU-[0-9][0-9][0-9]-[0-9][0-9][0-9]-[0-9][0-9][0-9]}/pdf";

    public static final String API_STATUS = "/status";
    public static final String API_REQUEST_UPDATE = "/request-update";

    public static final String API_CHANGE_SUPERVISOR_ON_DELETE = "/delete/change-supervisor";
    public static final String API_DATA_EMPLOYEE = "/{username:.+}";
    public static final String API_PHOTO_EMPLOYEE = "/{username:.+}/{photo:.+}";
    public static final String API_USERNAMES = "/usernames";
    public static final String API_PASSWORD_CHANGE = "/change-password";

    public static final String API_MY_REQUESTS = "/list/my";
    public static final String API_OTHERS_REQUESTS = "/list/others";
    public static final String API_REQUESTED_ASSETS = "/list/requested-assets";

}