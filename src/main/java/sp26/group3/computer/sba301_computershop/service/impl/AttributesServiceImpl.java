package sp26.group3.computer.sba301_computershop.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sp26.group3.computer.sba301_computershop.dto.request.AttributeNameRequest;
import sp26.group3.computer.sba301_computershop.dto.request.CategoryAttributeLinkRequest;
import sp26.group3.computer.sba301_computershop.dto.response.AttributeDetailResponse;
import sp26.group3.computer.sba301_computershop.dto.response.AttributeResponse;
import sp26.group3.computer.sba301_computershop.entity.Attribute;
import sp26.group3.computer.sba301_computershop.entity.Categories;
import sp26.group3.computer.sba301_computershop.entity.CategoriesAttributes;
import sp26.group3.computer.sba301_computershop.exception.NotFoundException;
import sp26.group3.computer.sba301_computershop.mapper.AttributeMappter;
import sp26.group3.computer.sba301_computershop.repository.AttributeRepository;
import sp26.group3.computer.sba301_computershop.repository.CategoriesAttributesRepository;
import sp26.group3.computer.sba301_computershop.repository.CategoriesRepository;
import sp26.group3.computer.sba301_computershop.service.AttributesService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttributesServiceImpl implements AttributesService {

    private final AttributeRepository attributeRepo;
    private final CategoriesRepository categoriesRepo;
    private final CategoriesAttributesRepository categoriesAttributesRepo;
    private final AttributeMappter attributeMappter;

    @Override
    @Transactional
    public AttributeResponse addAttributesToCategory(CategoryAttributeLinkRequest req) {

        Categories category = categoriesRepo.findById(req.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found: " + req.getCategoryId()));

        List<Attribute> attributes = new ArrayList<>();
        attributes = attributeRepo.findAllById(req.getAttributeIds());

        List<CategoriesAttributes> cateAttr = attributes.stream()
                .map(attr -> CategoriesAttributes.builder()
                        .categories(category)
                        .attribute(attr)
                        .build())
                .collect(Collectors.toList());

        categoriesAttributesRepo.saveAll(cateAttr);

        return AttributeResponse.builder()
                .categoryId(req.getCategoryId())
                .attributes(attributeMappter.toAttributeDetailResponseList(attributes))
                .build();
    }

    @Override
    @Transactional
    public List<AttributeDetailResponse> createAttributes(List<AttributeNameRequest> req) {

        List<Attribute> responses = req.stream()
                .map(item -> {
                    return Attribute.builder()
                            .attributeName(item.getAttributeName().trim())
                            .build();
                })
                .collect(Collectors.toList());
        attributeRepo.saveAll(responses);

        return attributeMappter.toAttributeDetailResponseList(responses);
    }

    @Override
    public List<AttributeDetailResponse> getAllAttributes() {
        return attributeRepo.findAll().stream()
                .map(a -> attributeMappter.toAttributeDetailResponse(a))
                .collect(Collectors.toList());
    }

    @Override
    public AttributeDetailResponse getAttributeById(int id) {
        Attribute attr = attributeRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Attribute not found: " + id));
        return attributeMappter.toAttributeDetailResponse(attr);
    }

    @Override
    @Transactional
    public AttributeDetailResponse updateAttribute(int id, AttributeNameRequest req) {
        Attribute attr = attributeRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Attribute not found: " + id));

        attr.setAttributeName(req.getAttributeName().trim());
        Attribute saved = attributeRepo.save(attr);
        return attributeMappter.toAttributeDetailResponse(saved);
    }

    @Override
    @Transactional
    public void deleteAttribute(int id) {
        if (!attributeRepo.existsById(id)) {
            throw new NotFoundException("Attribute not found: " + id);
        }
        attributeRepo.deleteById(id);
    }

}
