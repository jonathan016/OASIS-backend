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
    public static final FailedResponse SUPERVISION_DATA_NOT_FOUND = new FailedResponse(
            "SUPERVISION_DATA_NOT_FOUND",
            "No supervision with specified data could be found"
    );
    public static final FailedResponse EMPTY_SEARCH_QUERY = new FailedResponse(
            "EMPTY_SEARCH_QUERY",
            "No search query given, thus the server cannot search for asset"
    );
    public static final FailedResponse EMPLOYEE_INSERTION_ATTEMPT_BY_NON_ADMINISTRATOR = new FailedResponse(
            "EMPLOYEE_INSERTION_ATTEMPT_BY_NON_ADMINISTRATOR",
            "Non-administrator attempted to insert new employee"
    );
    public static final FailedResponse DUPLICATE_EMPLOYEE_DATA_FOUND = new FailedResponse(
            "DUPLICATE_EMPLOYEE_DATA_FOUND",
            "Employee(s) with matching data with the to-be-inserted employee found!"
    );
}
