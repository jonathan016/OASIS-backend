package com.oasis.exception.helper;

import com.oasis.webmodel.response.failed.FailedResponse;

public class ErrorCodeAndMessage {

    public static final FailedResponse USER_NOT_FOUND = new FailedResponse(
            "USER_NOT_FOUND",
            "User with specified username could not be found in database"
    );
    public static final FailedResponse PASSWORD_DOES_NOT_MATCH = new FailedResponse(
            "PASSWORD_DOES_NOT_MATCH",
            "User found in database but typed password does not match user's " +
                    "password in database"
    );
    public static final FailedResponse REQUESTS_NOT_FOUND = new FailedResponse(
            "REQUESTS_NOT_FOUND",
            "No other request in database could be found"
    );
    public static final FailedResponse INCORRECT_EMPLOYEE_NIK = new FailedResponse(
            "INCORRECT_EMPLOYEE_NIK",
            "The given employee NIK could not be found in database and thus inferred" +
                    " as incorrect employee NIK"
    );
    public static final FailedResponse EMPTY_SEARCH_QUERY = new FailedResponse(
            "EMPTY_SEARCH_QUERY",
            "No search query given, thus the server cannot search for asset"
    );
    public static final FailedResponse ASSET_NOT_FOUND = new FailedResponse(
            "ASSET_NOT_FOUND",
            "Asset with given search query cannot be found"
    );
    public static final FailedResponse SAME_ASSET_EXISTS = new FailedResponse(
            "SAME_ASSET_EXISTS",
            "Same asset exists and so insertion process cannot continue"
    );
    public static final FailedResponse ASSET_INSERTION_ATTEMPT_BY_NON_ADMINISTRATOR = new FailedResponse(
            "ASSET_INSERTION_ATTEMPT_BY_NON_ADMINISTRATOR",
            "Non-administrator attempted to create new asset"
    );
    public static final FailedResponse ASSET_UPDATE_ATTEMPT_BY_NON_ADMINISTRATOR = new FailedResponse(
            "ASSET_UPDATE_ATTEMPT_BY_NON_ADMINISTRATOR",
            "Non-administrator attempted to update existing asset"
    );
    public static final FailedResponse NO_ASSET_SELECTED = new FailedResponse(
            "NO_ASSET_SELECTED",
            "No asset has been selected for deletion"
    );
    public static final FailedResponse SELECTED_ASSET_STILL_REQUESTED = new FailedResponse(
            "SELECTED_ASSET_STILL_REQUESTED",
            "Selected asset for deletion is still being requested, thus cannot be deleted"
    );
}
