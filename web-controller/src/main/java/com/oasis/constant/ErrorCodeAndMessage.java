package com.oasis.constant;

import com.oasis.webmodel.response.failed.FailedResponse;

public class ErrorCodeAndMessage {
    public static final FailedResponse USER_NOT_FOUND =
            new FailedResponse(
                    "USER_NOT_FOUND",
                    "User with specified username could not be found in database"
            );
    public static final FailedResponse PASSWORD_DOES_NOT_MATCH =
            new FailedResponse(
                    "PASSWORD_DOES_NOT_MATCH",
                    "User found in database but typed password does not match user's password in database"
            );
    public static final FailedResponse REQUESTS_NOT_FOUND =
            new FailedResponse(
                    "REQUESTS_NOT_FOUND",
                    "No other request in database could be found"
            );
    public static final FailedResponse INCORRECT_EMPLOYEE_ID =
            new FailedResponse(
                    "INCORRECT_EMPLOYEE_ID",
                    "The employee ID given could not be found in database and thus inferred as incorrect employee ID"
            );
}
