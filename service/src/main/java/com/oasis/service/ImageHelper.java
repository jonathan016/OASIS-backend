package com.oasis.service;

import org.springframework.stereotype.Component;

@Component
public class ImageHelper {

    public String getExtensionFromFileName(
            final String fileName
    ) {

        StringBuilder extensionBuilder = new StringBuilder();

        extensionBuilder.append(fileName);
        extensionBuilder.reverse();
        extensionBuilder.replace(0, extensionBuilder.length(),
                                 extensionBuilder.substring(0, String.valueOf(extensionBuilder).indexOf("."))
        );
        extensionBuilder.reverse();

        return String.valueOf(extensionBuilder);
    }
}
