package com.oasis.constant;

public class APIMappingValue {

    public static final String API_LOGIN = "/api/login";
    public static final String API_DASHBOARD_STATUS = "/api/dashboard/status/{employeeNik}";
    public static final String API_DASHBOARD_REQUEST_UPDATE =
            "/api/dashboard/requestUpdate/{employeeNik}";
    public static final String API_FIND_ASSET = "/api/assets/find";
    public static final String API_ASSET_LIST = "/api/assets";
    public static final String API_SAVE_ASSET = "/api/assets/save";
    public static final String API_DELETE_ASSET = "/api/assets/delete";
    public static final String API_ASSET_DETAIL = "/api/assets/{assetSku}";
    public static final String API_ASSET_DETAIL_IMAGE = "/api/assets/{assetSku}/{assetPhotoName}.{extension}";
}