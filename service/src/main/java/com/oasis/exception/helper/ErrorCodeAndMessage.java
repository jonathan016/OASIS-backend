package com.oasis.exception.helper;

public class ErrorCodeAndMessage {

    public static final BaseError USER_NOT_FOUND = new BaseError(
            "USER_NOT_FOUND",
            "User with specified username/NIK does not exist!"
    );

    public static final BaseError PASSWORD_DOES_NOT_MATCH = new BaseError(
            "PASSWORD_DOES_NOT_MATCH",
            "Typed password does not match user's password!"
    );

    public static final BaseError REQUESTS_NOT_FOUND = new BaseError(
            "REQUESTS_NOT_FOUND",
            "No other request in database could be found!"
    );

    public static final BaseError SUPERVISION_DATA_NOT_FOUND = new BaseError(
            "SUPERVISION_DATA_NOT_FOUND",
            "No supervision with specified data could be found!"
    );

    public static final BaseError EMPTY_SEARCH_QUERY = new BaseError(
            "EMPTY_SEARCH_QUERY",
            "System cannot perform searching process as no search query is given!"
    );

    public static final BaseError EMPLOYEE_SAVE_ATTEMPT_BY_NON_ADMINISTRATOR = new BaseError(
            "EMPLOYEE_SAVE_ATTEMPT_BY_NON_ADMINISTRATOR",
            "Non-administrator attempted to save employee data!"
    );

    public static final BaseError DUPLICATE_EMPLOYEE_DATA_FOUND = new BaseError(
            "DUPLICATE_EMPLOYEE_DATA_FOUND",
            "Duplicate employee data found in database!"
    );

    public static final BaseError CYCLIC_SUPERVISING_OCCURRED = new BaseError(
            "CYCLIC_SUPERVISING_OCCURRED",
            "Cyclic supervising between specified employees exists!"
    );

    public static final BaseError EMPLOYEE_DELETE_ATTEMPT_BY_NON_ADMINISTRATOR = new BaseError(
            "EMPLOYEE_DELETE_ATTEMPT_BY_NON_ADMINISTRATOR",
            "Non-administrator attempted to delete employee data!"
    );

    public static final BaseError EMPTY_EMPLOYEE_NIK = new BaseError(
            "EMPTY_EMPLOYEE_NIK",
            "System cannot delete employee as no NIK is given!"
    );

    public static final BaseError SELF_DELETION_ATTEMPT = new BaseError(
            "SELF_DELETION_ATTEMPT",
            "Administrator attempted to delete his/her own data!"
    );

    public static final BaseError EXISTING_SUPERVISED_EMPLOYEES_ON_DELETION_ATTEMPT = new BaseError(
            "EXISTING_SUPERVISED_EMPLOYEES_ON_DELETION_ATTEMPT",
            "Administrator attempted to delete an employee who still have supervised employees!"
    );

    public static final BaseError UNRETURNED_ASSETS_ON_DELETION_ATTEMPT = new BaseError(
            "UNRETURNED_ASSETS_ON_DELETION_ATTEMPT",
            "Administrator attempted to delete an employee who has not returned all assigned assets!"
    );

    public static final BaseError SELECTED_EMPLOYEE_DOES_NOT_SUPERVISE = new BaseError(
            "SELECTED_EMPLOYEE_DOES_NOT_SUPERVISE",
            "Selected old supervisor does not supervise any employee!"
    );

    public static final BaseError ASSET_NOT_FOUND = new BaseError(
            "ASSET_NOT_FOUND",
            "Asset with given search query cannot be found!"
    );

    public static final BaseError DUPLICATE_ASSET_DATA_FOUND = new BaseError(
            "DUPLICATE_ASSET_DATA_FOUND",
            "Duplicate asset data found in database!"
    );

    public static final BaseError ASSET_SAVE_ATTEMPT_BY_NON_ADMINISTRATOR = new BaseError(
            "ASSET_SAVE_ATTEMPT_BY_NON_ADMINISTRATOR",
            "Non-administrator attempted to save asset data!"
    );

    public static final BaseError NO_ASSET_SELECTED = new BaseError(
            "NO_ASSET_SELECTED",
            "No asset has been selected for deletion!"
    );

    public static final BaseError SELECTED_ASSET_STILL_REQUESTED = new BaseError(
            "SELECTED_ASSET_STILL_REQUESTED",
            "Cannot delete selected asset as it is still being requested!"
    );

    public static final BaseError MISSING_ASSET_IMAGE = new BaseError(
            "MISSING_ASSET_IMAGE",
            "No image(s) can be found for specified asset!"
    );

}
