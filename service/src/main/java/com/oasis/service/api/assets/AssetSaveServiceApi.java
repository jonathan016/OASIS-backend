package com.oasis.service.api.assets;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.DuplicateDataException;
import com.oasis.exception.UnauthorizedOperationException;
import com.oasis.model.entity.AssetModel;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AssetSaveServiceApi {

    void saveAsset(
            final List< MultipartFile > imagesGiven, final String username, final AssetModel asset,
            final boolean addAssetOperation
    )
            throws
            DuplicateDataException,
            UnauthorizedOperationException,
            DataNotFoundException,
            BadRequestException;

}
