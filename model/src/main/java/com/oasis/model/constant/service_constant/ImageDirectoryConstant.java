package com.oasis.model.constant.service_constant;

import java.io.File;

public class ImageDirectoryConstant {

    private static final String ROOT_IMAGE_DIRECTORY = "C:".concat(File.separator).concat("oasis")
                                                           .concat(File.separator).concat("images");
    public static final String EMPLOYEE_PHOTO_DIRECTORY = ROOT_IMAGE_DIRECTORY.concat(File.separator)
                                                                              .concat("employees");
    public static final String ASSET_IMAGE_DIRECTORY = ROOT_IMAGE_DIRECTORY.concat(File.separator).concat("assets");
    public static final String STATIC_IMAGE_DIRECTORY = ROOT_IMAGE_DIRECTORY.concat(File.separator).concat("static");

}
