package com.oasis.webcontroller;

import com.oasis.constant.APIMappingValue;
import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.responsemapper.AssetsResponseMapper;
import com.oasis.service.implementation.AssetsServiceImpl;
import com.oasis.webmodel.response.PagingResponse;
import com.oasis.webmodel.response.success.assets.AssetListResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

@RestController
public class AssetsController {

    @Autowired
    private AssetsResponseMapper assetsResponseMapper;
    @Autowired
    private AssetsServiceImpl assetsServiceImpl;

    @GetMapping(value = APIMappingValue.API_FIND_ASSET)
    public PagingResponse<?> callFindAssetsService(@RequestParam String searchQuery,
                                                   @RequestParam int pageNumber,
                                                   @RequestParam String sortInfo) {
        List<AssetListResponse.Asset> assetsFound;

        try {
            assetsFound = new ArrayList<>(assetsServiceImpl.getAssetsBySearchQuery(searchQuery, pageNumber, sortInfo));
        } catch (BadRequestException | DataNotFoundException e) {
            return assetsResponseMapper.produceFindAssetFailedResult(e.getErrorCode(), e.getErrorMessage());
        }

        return assetsResponseMapper.produceFindAssetSuccessResult(assetsFound, pageNumber);
    }

    @GetMapping(value = APIMappingValue.API_ASSET_LIST,
            produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_OCTET_STREAM_VALUE)
    public PagingResponse<?> callGetAssetsListService(@RequestParam int pageNumber,
                                                      @RequestParam String sortInfo) {
        List<AssetListResponse.Asset> assetsFound;

        try {
            assetsFound = new ArrayList<>(assetsServiceImpl.getAvailableAsset(pageNumber, sortInfo));
        } catch (DataNotFoundException e) {
            return assetsResponseMapper.produceFindAssetFailedResult(e.getErrorCode(), e.getErrorMessage());
        }

        return assetsResponseMapper.produceFindAssetSuccessResult(assetsFound, pageNumber);
    }
}
