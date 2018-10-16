package com.oasis.constant;

public class ErrorCodeAndMessage {
    public static final String[] USER_NOT_FOUND = {
            "USER_NOT_FOUND",
            "User with specified username could not be found in database"};
    public static final String[] PASSWORD_DOES_NOT_MATCH = {
            "PASSWORD_DOES_NOT_MATCH",
            "User found in database but typed password does not match user's password in database"};
    public static final String[] REQUESTS_NOT_FOUND = {
            "REQUESTS_NOT_FOUND",
            "No other request in database could be found"};
    public static final String[] INCORRECT_EMPLOYEE_ID = {
            "INCORRECT_EMPLOYEE_ID",
            "The employee ID given could not be found in database and thus inferred as incorrect employee ID"};
}
