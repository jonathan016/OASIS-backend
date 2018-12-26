package com.oasis.tool.util;

public class Regex {

    public static final String REGEX_USERNAME = "^([a-z0-9]+\\.?)*[a-z0-9]+$";

    public static final String REGEX_ASSET_SORT = "^[AD]-(SKU|name)$";

    public static final String REGEX_EMPLOYEE_SORT = "^[AD]$";

    public static final String REGEX_EMPLOYEE_NAME = "^([A-Za-z]+ ?)*[A-Za-z]+$";

    public static final String REGEX_USERNAME_LOGIN_SUFFIX = "^([A-Za-z0-9]+.?[A-Za-z0-9]+)+@gdn-commerce.com$";

    public static final String REGEX_USERNAME_LOGIN_NO_SUFFIX = "^([A-Za-z0-9]+.?[A-Za-z0-9]+)+$";

    public static final String REGEX_REQUEST_SORT = "^[AD]-(status|updatedDate)$";

    public static final String REGEX_JPEG_FILE_NAME = "^.+\\.[jJ][pP][eE][gG]$";

    public static final String REGEX_PNG_FILE_NAME = "^.+\\.[pP][nN][gG]$";

    public static final String REGEX_ASSET_STRINGS = "^[A-Za-z0-9]+(( )?(.+)?[A-Za-z0-9]+)*$";

}
