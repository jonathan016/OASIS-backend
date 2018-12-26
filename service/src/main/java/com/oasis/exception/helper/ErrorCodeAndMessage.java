package com.oasis.exception.helper;

public class ErrorCodeAndMessage {

    public static final BaseError DATA_NOT_FOUND = new BaseError("DATA_NOT_FOUND",
                                                                 "No data in database matches given information"
    );

    public static final BaseError INVALID_PASSWORD = new BaseError("INVALID_PASSWORD", "Invalid password given");

    public static final BaseError INCORRECT_PARAMETER = new BaseError("INCORRECT_PARAMETER",
                                                                      "Parameter contains empty/incorrect value"
    );

    public static final BaseError UNAUTHORIZED_OPERATION = new BaseError("UNAUTHORIZED_OPERATION",
                                                                         "Unauthorized operation attempted"
    );

    public static final BaseError DUPLICATE_DATA_FOUND = new BaseError("DUPLICATE_DATA_FOUND",
                                                                       "Duplicate data in database found"
    );

}
