package sp26.group3.computer.sba301_computershop.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sp26.group3.computer.sba301_computershop.dto.request.AttributeCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.AttributeUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.AttributeResponse;
import sp26.group3.computer.sba301_computershop.entity.Attribute;
import sp26.group3.computer.sba301_computershop.exception.AppException;
import sp26.group3.computer.sba301_computershop.exception.ErrorCode;
import sp26.group3.computer.sba301_computershop.mapper.AttributeMapper;
import sp26.group3.computer.sba301_computershop.repository.AttributeRepository;
import sp26.group3.computer.sba301_computershop.service.AttributeService;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AttributeServiceImpl implements AttributeService {

    AttributeRepository attributeRepository;
    AttributeMapper attributeMapper;

    @Override
    public AttributeResponse createAttribute(AttributeCreationRequest request) {
        log.info("Creating attribute with name: {}", request.getAttributeName());

        // Check if attribute name already exists
        if (attributeRepository.existsByAttributeName(request.getAttributeName())) {
            throw new AppException(ErrorCode.ATTRIBUTE_NAME_EXISTED);
        }

        Attribute attribute = attributeMapper.toAttribute(request);
        Attribute savedAttribute = attributeRepository.save(attribute);

        log.info("Attribute created successfully with id: {}", savedAttribute.getAttributeId());

        return attributeMapper.toAttributeResponse(savedAttribute);
    }

    @Override
    public AttributeResponse updateAttribute(int attributeId, AttributeUpdateRequest request) {
        log.info("Updating attribute with id: {}", attributeId);

        Attribute attribute = attributeRepository.findById(attributeId)
                .orElseThrow(() -> new AppException(ErrorCode.ATTRIBUTE_NOT_FOUND));

        // Check if attribute name is being changed and if it already exists
        if (request.getAttributeName() != null &&
                !request.getAttributeName().equals(attribute.getAttributeName()) &&
                attributeRepository.existsByAttributeName(request.getAttributeName())) {
            throw new AppException(ErrorCode.ATTRIBUTE_NAME_EXISTED);
        }

        attributeMapper.updateAttribute(attribute, request);
        Attribute updatedAttribute = attributeRepository.save(attribute);

        log.info("Attribute updated successfully with id: {}", attributeId);

        return attributeMapper.toAttributeResponse(updatedAttribute);
    }

    @Override
    public AttributeResponse getAttributeById(int attributeId) {
        log.info("Getting attribute with id: {}", attributeId);

        Attribute attribute = attributeRepository.findById(attributeId)
                .orElseThrow(() -> new AppException(ErrorCode.ATTRIBUTE_NOT_FOUND));

        return attributeMapper.toAttributeResponse(attribute);
    }

    @Override
    public List<AttributeResponse> getAllAttributes() {
        log.info("Getting all attributes");

        return attributeRepository.findAll()
                .stream()
                .map(attributeMapper::toAttributeResponse)
                .toList();
    }

    @Override
    public void deleteAttribute(int attributeId) {
        log.warn("Deleting attribute with id: {}", attributeId);

        if (!attributeRepository.existsById(attributeId)) {
            throw new AppException(ErrorCode.ATTRIBUTE_NOT_FOUND);
        }

        attributeRepository.deleteById(attributeId);
        log.info("Attribute deleted successfully with id: {}", attributeId);
    }
}