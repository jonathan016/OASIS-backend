package com.oasis.request_mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oasis.exception.BadRequestException;
import com.oasis.model.entity.AssetModel;
import com.oasis.web_model.request.assets.SaveAssetRequest;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.oasis.exception.helper.ErrorCodeAndMessage.INCORRECT_PARAMETER;

@Component
public class AssetsRequestMapper {

    public String getAdminUsernameFromRawData(final String rawAssetData) {

        String adminUsername;

        try {
            adminUsername = new ObjectMapper().readTree(rawAssetData).path("username").asText();
        } catch (IOException e) {
            return "";
        }

        return adminUsername;
    }

    public boolean isAddAssetOperationFromRawData(final String rawAssetData)
            throws
            BadRequestException {

        JsonNode asset;

        try {
            asset = new ObjectMapper().readTree(rawAssetData).path("asset");
        } catch (IOException e) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        }

        return asset.path("sku").isNull();
    }

    public AssetModel getAssetModelFromRawData(
            final String rawAssetData, final boolean addAssetOperation
    ) {

        SaveAssetRequest.Asset request;

        try {

            JsonNode asset = new ObjectMapper().readTree(rawAssetData).path("asset");

            if (addAssetOperation) {
                request = new SaveAssetRequest.Asset(null, asset.path("name").asText(), asset.path("location").asText(),
                                                     asset.path("brand").asText(), asset.path("type").asText(),
                                                     asset.path("quantity").asLong(), asset.path("price").asDouble(),
                                                     asset.path("expendable").asBoolean()
                );
            } else {
                request = new SaveAssetRequest.Asset(asset.path("sku").asText(), asset.path("name").asText(),
                                                     asset.path("location").asText(), asset.path("brand").asText(),
                                                     asset.path("type").asText(), asset.path("quantity").asLong(),
                                                     asset.path("price").asDouble(),
                                                     asset.path("expendable").asBoolean()
                );
            }

        } catch (IOException e) {
            return null;
        }

        MapperFactory assetDataFactory = new DefaultMapperFactory.Builder().build();
        assetDataFactory.classMap(SaveAssetRequest.Asset.class, AssetModel.class).field("quantity", "stock").byDefault()
                        .register();

        return assetDataFactory.getMapperFacade(SaveAssetRequest.Asset.class, AssetModel.class).map(request);
    }

}
