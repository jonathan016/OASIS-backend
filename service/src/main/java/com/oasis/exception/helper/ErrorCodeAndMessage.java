package com.oasis.exception.helper;

import com.oasis.model.BaseError;

public class ErrorCodeAndMessage {

    public static final BaseError USER_NOT_FOUND = new BaseError(
            "USER_NOT_FOUND",
            "User with specified username could not be found in database"
    );

    public static final BaseError PASSWORD_DOES_NOT_MATCH = new BaseError(
            "PASSWORD_DOES_NOT_MATCH",
            "User found in database but typed password does not match user's password in database"
    );

    public static final BaseError REQUESTS_NOT_FOUND = new BaseError(
            "REQUESTS_NOT_FOUND",
            "No other request in database could be found"
    );

    public static final BaseError INCORRECT_EMPLOYEE_NIK = new BaseError(
            "INCORRECT_EMPLOYEE_NIK",
            "The given employee NIK could not be found in database and thus inferred" +
                    " as incorrect employee NIK"
    );

    public static final BaseError SUPERVISION_DATA_NOT_FOUND = new BaseError(
            "SUPERVISION_DATA_NOT_FOUND",
            "No supervision with specified data could be found"
    );

    public static final BaseError EMPTY_SEARCH_QUERY = new BaseError(
            "EMPTY_SEARCH_QUERY",
            "No search query given, thus the server cannot search for asset"
    );

    public static final BaseError EMPLOYEE_INSERTION_ATTEMPT_BY_NON_ADMINISTRATOR = new BaseError(
            "EMPLOYEE_INSERTION_ATTEMPT_BY_NON_ADMINISTRATOR",
            "Non-administrator attempted to insert new employee"
    );

    public static final BaseError DUPLICATE_EMPLOYEE_DATA_FOUND = new BaseError(
            "DUPLICATE_EMPLOYEE_DATA_FOUND",
            "Employee(s) with matching data with the to-be-inserted employee found!"
    );

    public static final BaseError EMPLOYEE_UPDATE_ATTEMPT_BY_NON_ADMINISTRATOR = new BaseError(
            "EMPLOYEE_UPDATE_ATTEMPT_BY_NON_ADMINISTRATOR",
            "Non-administrator attempted to update employee data"
    );

    public static final BaseError CYCLIC_SUPERVISING_OCCURED = new BaseError(
            "CYCLIC_SUPERVISING_OCCURED",
            "Cyclic supervising between specified employees exists"
    );

    public static final BaseError EMPLOYEE_DELETE_ATTEMPT_BY_NON_ADMINISTRATOR = new BaseError(
            "EMPLOYEE_DELETE_ATTEMPT_BY_NON_ADMINISTRATOR",
            "Non-administrator attempted to delete employee data"
    );

    public static final BaseError EMPTY_EMPLOYEE_NIK = new BaseError(
            "EMPTY_EMPLOYEE_NIK",
            "No employee NIK given, thus the server cannot delete employee"
    );

    public static final BaseError SELF_DELETION_ATTEMPT = new BaseError(
            "SELF_DELETION_ATTEMPT",
            "Admin attempted to delete his/her own data"
    );

    public static final BaseError EXISTING_SUPERVISED_EMPLOYEES_ON_DELETION_ATTEMPT = new BaseError(
            "EXISTING_SUPERVISED_EMPLOYEES_ON_DELETION_ATTEMPT",
            "Admin attempted to delete an employee who still have supervised employees"
    );

    public static final BaseError EXISTING_USED_ASSETS_ON_DELETION_ATTEMPT = new BaseError(
            "EXISTING_USED_ASSETS_ON_DELETION_ATTEMPT",
            "Admin attempted to delete an employee who has not returned all assets used by him/her"
    );

    public static final BaseError SELECTED_EMPLOYEE_DOES_NOT_SUPERVISE = new BaseError(
            "SELECTED_EMPLOYEE_DOES_NOT_SUPERVISE",
            "Selected employee for change of supervisor does not supervise any employee"
    );

    public static final BaseError ASSET_NOT_FOUND = new BaseError(
            "ASSET_NOT_FOUND",
            "Asset with given search query cannot be found"
    );

    public static final BaseError SAME_ASSET_EXISTS = new BaseError(
            "SAME_ASSET_EXISTS",
            "Same asset exists and so insertion process cannot continue"
    );

    public static final BaseError ASSET_INSERTION_ATTEMPT_BY_NON_ADMINISTRATOR = new BaseError(
            "ASSET_INSERTION_ATTEMPT_BY_NON_ADMINISTRATOR",
            "Non-administrator attempted to create new asset"
    );

    public static final BaseError ASSET_UPDATE_ATTEMPT_BY_NON_ADMINISTRATOR = new BaseError(
            "ASSET_UPDATE_ATTEMPT_BY_NON_ADMINISTRATOR",
            "Non-administrator attempted to update existing asset"
    );

    public static final BaseError NO_ASSET_SELECTED = new BaseError(
            "NO_ASSET_SELECTED",
            "No asset has been selected for deletion"
    );

    public static final BaseError SELECTED_ASSET_STILL_REQUESTED = new BaseError(
            "SELECTED_ASSET_STILL_REQUESTED",
            "Selected asset for deletion is still being requested, thus cannot be deleted"
    );

    public static final BaseError MISSING_ASSET_IMAGE = new BaseError(
            "MISSING_ASSET_IMAGE",
            "No image(s) can be found for specified asset"
    );

}
