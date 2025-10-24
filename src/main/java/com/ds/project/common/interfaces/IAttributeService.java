package com.ds.project.common.interfaces;

import com.ds.project.common.entities.dto.request.AttributeRequest;
import com.ds.project.common.entities.dto.response.AttributeResponse;

import java.util.List;

public interface IAttributeService {
    AttributeResponse createAttribute(AttributeRequest request);
    AttributeResponse updateAttribute(String id, AttributeRequest request);
    AttributeResponse getAttributeById(String id);
    List<AttributeResponse> getAllAttributes();
    void deleteAttribute(String id);
}
