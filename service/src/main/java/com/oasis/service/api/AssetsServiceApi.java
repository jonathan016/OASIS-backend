package com.oasis.service.api;

import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfPTable;
import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.DuplicateDataException;
import com.oasis.exception.UnauthorizedOperationException;
import com.oasis.model.entity.AssetModel;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

public interface AssetsServiceApi {

    /*-------------Assets List Methods-------------*/
    List< AssetModel > getAvailableAssetsList(
            final String query, final int page, final String sort
    )
            throws
            BadRequestException,
            DataNotFoundException;

    Set< AssetModel > getSortedAvailableAssets(
            final int page, final String sort
    );

    Set< AssetModel > getSortedAvailableAssetsFromQuery(
            final int page, final String query, final String sort
    );

    long getAvailableAssetsCount(
            final String query, final String sort
    );

    AssetModel getAssetDetailData(
            final String sku
    )
            throws
            DataNotFoundException;

    String getExtensionFromFileName(
            final String fileName
    );

    List< String > getAssetDetailImages(
            final String sku, final String imageDirectory
    );

    String getFormattedPrice(
            final double price
    );

    byte[] getAssetDetailInPdf(
            final String sku
    );

    void addHeaderCellToTable(
            PdfPTable table, final String value, final Font font
    );

    void addContentCellToTable(
            PdfPTable table, final String name, final String data
    );

    byte[] getAssetImage(
            final String sku, final String imageName, final String extension
    );

    /*-------------Save Asset Methods-------------*/
    void saveAsset(
            final List< MultipartFile > photos, final String username, final AssetModel asset,
            final boolean isAddOperation
    )
            throws
            DuplicateDataException,
            UnauthorizedOperationException,
            DataNotFoundException,
            BadRequestException;

    String generateSkuCode(
            final String username, final String brand, final String type
    );

    void savePhotos(
            final List< MultipartFile > photos, final String sku
    );

    /*-------------Delete Asset(s) Method-------------*/
    void deleteAssets(
            final List< String > skus, final String username
    )
            throws
            UnauthorizedOperationException,
            BadRequestException,
            DataNotFoundException;

}