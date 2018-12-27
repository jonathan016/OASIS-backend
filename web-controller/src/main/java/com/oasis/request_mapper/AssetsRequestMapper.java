package com.oasis.request_mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oasis.exception.BadRequestException;
import com.oasis.model.entity.AssetModel;
import com.oasis.web_model.request.assets.SaveAssetRequest;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.oasis.exception.helper.ErrorCodeAndMessage.INCORRECT_PARAMETER;

@Component
public class AssetsRequestMapper {

    private Logger logger = LoggerFactory.getLogger(EmployeesRequestMapper.class);

    public boolean isAddAssetOperationFromRawData(final String rawAssetData)
            throws
            BadRequestException {

        final JsonNode asset;

        try {
            asset = new ObjectMapper().readTree(rawAssetData).path("asset");
        } catch (IOException exception) {
            logger.error("Failed to read attribute 'asset' from passed JSON data as IOException occurred with message: "
                         + exception.getMessage());
            throw new BadRequestException(INCORRECT_PARAMETER);
        }

        return asset.path("sku").isNull();
    }

    public AssetModel getAssetModelFromRawData(
            final String rawAssetData, final boolean addAssetOperation
    ) {

        final SaveAssetRequest.Asset request;

        try {

            JsonNode asset = new ObjectMapper().readTree(rawAssetData).path("asset");

            if (addAssetOperation) {
                request = new SaveAssetRequest.Asset(null, asset.path("name").textValue(),
                                                     asset.path("location").textValue(),
                                                     asset.path("brand").textValue(), asset.path("type").textValue(),
                                                     Long.valueOf(asset.path("quantity").textValue()),
                                                     Double.valueOf(asset.path("price").textValue()),
                                                     asset.path("expendable").asBoolean()
                );
            } else {
                request = new SaveAssetRequest.Asset(asset.path("sku").textValue(), asset.path("name").textValue(),
                                                     asset.path("location").textValue(),
                                                     asset.path("brand").textValue(),
                                                     asset.path("type").textValue(),
                                                     Long.valueOf(asset.path("quantity").textValue()),
                                                     Double.valueOf(asset.path("price").textValue()),
                                                     asset.path("expendable").asBoolean()
                );
            }
        } catch (IOException | NumberFormatException exception) {
            logger.error("Failed to process data as IOException or NumberFormatException occurred with message: " +
                         exception.getMessage());
            return null;
        }

        MapperFactory assetDataFactory = new DefaultMapperFactory.Builder().build();
        assetDataFactory.classMap(SaveAssetRequest.Asset.class, AssetModel.class).field("quantity", "stock").byDefault()
                        .register();

        return assetDataFactory.getMapperFacade(SaveAssetRequest.Asset.class, AssetModel.class).map(request);
    }

}
