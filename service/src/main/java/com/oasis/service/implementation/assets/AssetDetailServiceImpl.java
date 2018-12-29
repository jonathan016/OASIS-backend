package com.oasis.service.implementation.assets;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.oasis.exception.DataNotFoundException;
import com.oasis.model.entity.AssetModel;
import com.oasis.repository.AssetRepository;
import com.oasis.service.api.assets.AssetDetailServiceApi;
import com.oasis.tool.constant.ServiceConstant;
import com.oasis.tool.helper.DocumentHeader;
import com.oasis.tool.helper.ImageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.oasis.exception.helper.ErrorCodeAndMessage.DATA_NOT_FOUND;

@Service
@Transactional
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class AssetDetailServiceImpl
        implements AssetDetailServiceApi {

    private Logger logger = LoggerFactory.getLogger(AssetDetailServiceImpl.class);

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private ImageHelper imageHelper;
    @Autowired
    private DocumentHeader documentHeader;



    @Override
    @Cacheable(value = "assetDetailData",
               key = "#sku")
    public AssetModel getAssetDetailData(
            final String sku
    )
            throws
            DataNotFoundException {

        final AssetModel assetDetailData = assetRepository.findByDeletedIsFalseAndSkuEquals(sku);

        if (assetDetailData == null) {
            throw new DataNotFoundException(DATA_NOT_FOUND);
        } else {
            return assetDetailData;
        }
    }

    @Override
    public List< String > getAssetDetailImages(
            final String sku, final String imageDirectory
    ) {

        List< String > imageURLs = new ArrayList<>();

        final File directory = new File(imageDirectory);
        final File[] images = directory.listFiles();

        if (Files.exists(directory.toPath()) && images != null && images.length != 0) {
            for (int i = 0; i < images.length; i++) {
                final String extension = imageHelper.getExtensionFromFileName(images[ i ].getName());

                imageURLs.add("http://localhost:8085/oasis/api/assets/".concat(sku).concat("/").concat(sku)
                                                                       .concat("-").concat(String.valueOf(i + 1))
                                                                       .concat("?extension=").concat(extension));
            }
        } else {
            imageURLs.add("http://localhost:8085/oasis/api/assets/".concat(sku).concat("/image_not_found")
                                                                   .concat("?extension=jpeg"));
        }

        return imageURLs;
    }

    @Override
    @SuppressWarnings("LoopStatementThatDoesntLoop")
    public byte[] getAssetDetailInPdf(
            final String sku
    ) {

        final AssetModel asset = assetRepository.findByDeletedIsFalseAndSkuEquals(sku);

        if (asset == null) {
            return new byte[ 0 ];
        } else {
            Document document = new Document(PageSize.A4, 36, 36, 90, 36);

            new File(sku.concat(ServiceConstant.EXTENSION_PDF));

            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream);

                writer.setPageEvent(documentHeader);

                document.open();

                document.add(Chunk.NEWLINE);

                final Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 30,
                                                      new BaseColor(32, 162, 223)
                );
                final Chunk chunk = new Chunk(asset.getName(), font);
                Paragraph paragraph = new Paragraph(chunk);
                paragraph.setAlignment(Paragraph.ALIGN_CENTER);
                document.add(paragraph);

                document.add(Chunk.NEWLINE);

                final File possibleDirectory = new File(asset.getImageDirectory());
                final File[] images = possibleDirectory.listFiles();

                if (possibleDirectory.exists() && possibleDirectory.isDirectory() && images != null &&
                    images.length != 0) {
                    for (final File image : images) {
                        if (Files.exists(image.toPath())) {
                            Image detailImage = Image.getInstance(Files.readAllBytes(Paths.get(image.toURI())));
                            detailImage.scaleToFit(detailImage.getWidth(), document.getPageSize().getWidth() / 5);
                            detailImage.setAlignment(Element.ALIGN_CENTER);
                            document.add(detailImage);
                        }

                        break;  //Takes only first result, but code is prepared for multiple images
                    }
                } else {
                    Paragraph altDetailImageParagraph = new Paragraph("No Image Available!");
                    altDetailImageParagraph.setAlignment(Element.ALIGN_CENTER);
                    document.add(altDetailImageParagraph);
                }
                document.add(Chunk.NEWLINE);

                PdfPTable detailTable = new PdfPTable(2);
                detailTable.setWidths(new float[]{ 2, 7 });

                final Font infoFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 12, BaseColor.BLACK);

                addHeaderCellToTable(detailTable, "Description", infoFont);
                addHeaderCellToTable(detailTable, "Attribute", infoFont);

                addContentCellToTable(detailTable, "SKU", asset.getSku());
                addContentCellToTable(detailTable, "Name", asset.getName());
                addContentCellToTable(detailTable, "Brand", asset.getBrand());
                addContentCellToTable(detailTable, "Type", asset.getType());
                addContentCellToTable(detailTable, "Stock", String.valueOf(asset.getStock()));
                addContentCellToTable(detailTable, "Location", asset.getLocation());
                addContentCellToTable(detailTable, "Price", getFormattedPrice(asset.getPrice()));

                document.add(detailTable);

                document.close();

                return byteArrayOutputStream.toByteArray();
            } catch (DocumentException | IOException exception) {
                logger.error(
                        "Failed to generate PDF document as DocumentException or IOException occurred with message: " +
                        exception.getMessage());
                return new byte[ 0 ];
            }
        }
    }

    private void addHeaderCellToTable(
            PdfPTable table, final String value, final Font font
    ) {

        Paragraph description = new Paragraph(value, font);
        description.setAlignment(Element.ALIGN_CENTER);

        PdfPCell headerCell = new PdfPCell(description);
        headerCell.setPadding(5);
        headerCell.setPaddingBottom(10);

        table.addCell(headerCell);
    }

    private void addContentCellToTable(
            PdfPTable table, final String name, final String data
    ) {

        table.addCell(name);

        PdfPCell contentCell = new PdfPCell(new Phrase(data));
        contentCell.setPadding(2);
        contentCell.setPaddingLeft(10);
        contentCell.setPaddingBottom(5);

        table.addCell(contentCell);
    }

    private String getFormattedPrice(
            final double price
    ) {

        StringBuilder priceStr = new StringBuilder(String.format("%.2f", price));
        priceStr.reverse();

        String fraction = String.valueOf(priceStr).substring(0, 3);
        priceStr.replace(0, 3, "");

        StringBuilder formattedPriceStr = new StringBuilder();

        final int initialLength = priceStr.length();
        while (priceStr.length() > 0) {
            StringBuilder temp;

            if (priceStr.length() >= 3) {
                temp = new StringBuilder(String.valueOf(priceStr).substring(0, 3));
                priceStr.replace(0, 3, "");
            } else {
                temp = new StringBuilder(String.valueOf(priceStr));
                priceStr.replace(0, String.valueOf(priceStr).length(), "");
            }

            temp.reverse();
            if (initialLength - 3 != priceStr.length()) {
                temp.append(",");
            }
            formattedPriceStr.insert(0, temp);
        }

        formattedPriceStr.append(new StringBuilder(fraction).reverse());

        formattedPriceStr.insert(0, "Rp. ");

        return String.valueOf(formattedPriceStr);
    }

}
