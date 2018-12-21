package com.oasis.tool.helper;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import com.oasis.tool.constant.ImageDirectoryConstant;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Component
public class DocumentHeader
        extends PdfPageEventHelper {

    private PdfPTable table;
    private float tableHeight;

    public DocumentHeader() {

        table = new PdfPTable(5);
        table.setTotalWidth(523);
        table.setLockedWidth(true);

        Image blibliImage = null;
        try {
            File blibliLogo = new File(
                    ImageDirectoryConstant.STATIC_IMAGE_DIRECTORY.concat(File.separator).concat("pdf_header_images")
                                                                 .concat(File.separator).concat("blibli.png"));
            blibliImage = Image.getInstance(Files.readAllBytes(blibliLogo.toPath()));
        } catch (IOException | BadElementException e) {
            e.printStackTrace();
        }
        PdfPCell blibliCell = new PdfPCell(blibliImage, true);
        blibliCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        blibliCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(blibliCell);

        PdfPCell emptyCell = new PdfPCell();
        emptyCell.setBorder(Rectangle.NO_BORDER);
        for (int i = 0; i < 3; i++) {
            table.addCell(emptyCell);
        }

        Image oasisImage = null;
        try {
            File oasisLogo = new File(
                    ImageDirectoryConstant.STATIC_IMAGE_DIRECTORY.concat(File.separator).concat("pdf_header_images")
                                                                 .concat(File.separator).concat("oasis.png"));
            oasisImage = Image.getInstance(Files.readAllBytes(oasisLogo.toPath()));
        } catch (IOException | BadElementException e) {
            e.printStackTrace();
        }
        PdfPCell oasisCell = new PdfPCell(oasisImage, true);
        oasisCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        oasisCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(oasisCell);

        tableHeight = table.getTotalHeight();
    }

    @SuppressWarnings("unused")
    public float getTableHeight() {

        return tableHeight;
    }

    public void onEndPage(
            PdfWriter writer, Document document
    ) {

        table.writeSelectedRows(0, -1, document.left(), document.top() + ((document.topMargin() + tableHeight) / 2),
                                writer.getDirectContent()
        );
    }

}
