package com.oasis.exception.helper;

public class ErrorCodeAndMessage {

//    Fixed
    public static final BaseError DATA_NOT_FOUND = new BaseError(
            "DATA_NOT_FOUND",
            "No data in database matches given information"
    );

    public static final BaseError INVALID_PASSWORD = new BaseError(
            "INVALID_PASSWORD",
            "Invalid password given"
    );

    public static final BaseError INCORRECT_PARAMETER = new BaseError(
            "INCORRECT_PARAMETER",
            "Parameter contains empty/incorrect value"
    );

    public static final BaseError UNAUTHORIZED_OPERATION = new BaseError(
            "UNAUTHORIZED_OPERATION",
            "Unauthorized operation attempted"
    );

    public static final BaseError DUPLICATE_DATA_FOUND = new BaseError(
            "DUPLICATE_DATA_FOUND",
            "Duplicate data in database found"
    );

//    Old Ones
    public static final BaseError USER_NOT_FOUND = new BaseError("USER_NOT_FOUND",
                                                                 "User with specified username/NIK does not exist!"
    );

    public static final BaseError REQUESTS_NOT_FOUND = new BaseError("REQUESTS_NOT_FOUND",
                                                                     "No other request in database could be found!"
    );

    public static final BaseError EMPTY_SEARCH_QUERY = new BaseError("EMPTY_SEARCH_QUERY",
                                                                     "System cannot perform searching process as no " +
                                                                     "search query is given!"
    );

    public static final BaseError EMPTY_EMPLOYEE_NIK = new BaseError("EMPTY_EMPLOYEE_NIK",
                                                                     "System cannot delete employee as no NIK is given!"
    );

    public static final BaseError ASSET_NOT_FOUND = new BaseError("ASSET_NOT_FOUND",
                                                                  "Asset with given search query cannot be found!"
    );

    public static final BaseError LOCKED_DATA_MODIFICATION_ATTEMPT = new BaseError(
            "LOCKED_DATA_MODIFICATION_ATTEMPT",
            "LOCKED_DATA_MODIFICATION_ATTEMPT"
    );

    public static final BaseError SAME_DATA_ON_UPDATE = new BaseError(
            "SAME_DATA_ON_UPDATE",
            "SAME_DATA_ON_UPDATE"
    );

    public static final BaseError DUPLICATE_ASSET_DATA_FOUND = new BaseError("DUPLICATE_ASSET_DATA_FOUND",
                                                                             "Duplicate asset data found in database!"
    );

    public static final BaseError ASSET_SAVE_ATTEMPT_BY_NON_ADMINISTRATOR = new BaseError(
            "ASSET_SAVE_ATTEMPT_BY_NON_ADMINISTRATOR", "Non-administrator attempted to save asset data!");

    public static final BaseError NO_ASSET_SELECTED = new BaseError("NO_ASSET_SELECTED",
                                                                    "No asset has been selected for deletion!"
    );

    public static final BaseError SELECTED_ASSET_STILL_REQUESTED = new BaseError("SELECTED_ASSET_STILL_REQUESTED",
                                                                                 "Cannot delete selected asset as it is still being requested!"
    );

    public static final BaseError MISSING_ASSET_IMAGE = new BaseError("MISSING_ASSET_IMAGE",
                                                                      "No image(s) can be found for specified asset!"
    );

}
