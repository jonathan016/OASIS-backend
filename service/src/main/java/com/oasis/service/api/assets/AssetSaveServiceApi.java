package com.oasis.service.api.assets;

import com.oasis.model.exception.BadRequestException;
import com.oasis.model.exception.DataNotFoundException;
import com.oasis.model.exception.DuplicateDataException;
import com.oasis.model.exception.UnauthorizedOperationException;
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
