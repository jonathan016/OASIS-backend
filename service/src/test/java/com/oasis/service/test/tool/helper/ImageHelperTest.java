package com.oasis.service.test.tool.helper;

import com.oasis.service.tool.helper.ImageHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("Duplicates")
@RunWith(MockitoJUnitRunner.class)
public class ImageHelperTest {

    final String[] fileNames = new String[]{ "This is a simple jpeg file.jpeg", "This is a simple jpg file.jpg",
                                             "This is a simple png file.png", "This is a simple docx file.docx" };
    final String[] extensions = new String[]{ "jpeg", "jpg", "png", "docx" };

    @InjectMocks
    private ImageHelper imageHelper;



    @Test
    public void getExtensionFromFileName_ValidExtension_ReturnsValidExtension() {

        for (int i = 0; i < fileNames.length; i++) {
            final String extension = imageHelper.getExtensionFromFileName(fileNames[ i ]);

            assertEquals(extensions[ i ], extension);
        }
    }

}