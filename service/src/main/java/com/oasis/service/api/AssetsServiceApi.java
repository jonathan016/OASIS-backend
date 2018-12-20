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
            final String query, final int page, String sort
    )
            throws
            BadRequestException,
            DataNotFoundException;

    long getAvailableAssetsCount(
            final String query, final String sort
    );

    AssetModel getAssetDetailData(
            final String sku
    )
            throws
            DataNotFoundException;

    List< String > getAssetDetailImages(
            final String sku, final String imageDirectory
    );

    byte[] getAssetDetailInPdf(
            final String sku
    );

    byte[] getAssetImage(
            final String sku, final String imageName, final String extension
    );

    /*-------------Save Asset Methods-------------*/
    void saveAsset(
            final List< MultipartFile > imagesGiven, final String username, final AssetModel asset,
            final boolean addAssetOperation
    )
            throws
            DuplicateDataException,
            UnauthorizedOperationException,
            DataNotFoundException;

    /*-------------Delete Asset(s) Method-------------*/
    void deleteAssets(
            final List< String > skus, final String username
    )
            throws
            UnauthorizedOperationException,
            BadRequestException,
            DataNotFoundException;

}