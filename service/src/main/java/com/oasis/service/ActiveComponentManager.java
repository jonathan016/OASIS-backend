package com.oasis.service;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ActiveComponentManager {

    public Map<String, Boolean> getAssetsListActiveComponents(String role) {
        Map<String, Boolean> activeComponents = new HashMap<>();

        activeComponents.put("btnAssetListAdd", true);
        activeComponents.put("btnAssetListDelete", true);
        activeComponents.put("btnAssetTableEdit", true);

        return activeComponents;
    }

    public Map<String, Boolean> getAssetDetailActiveComponents(String role) {
        Map<String, Boolean> activeComponents = new HashMap<>();

        activeComponents.put("btn-asset-detail-edit", true);
        activeComponents.put("btn-asset-detail-delete", true);

        return activeComponents;
    }

}
