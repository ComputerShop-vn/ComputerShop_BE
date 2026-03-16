package sp26.group3.computer.sba301_computershop.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sp26.group3.computer.sba301_computershop.dto.request.CompatibleVariantsRequest;
import sp26.group3.computer.sba301_computershop.dto.response.CompatibilityFilterHint;
import sp26.group3.computer.sba301_computershop.dto.response.CompatibleVariantsResponse;
import sp26.group3.computer.sba301_computershop.entity.CompatibilityRule;
import sp26.group3.computer.sba301_computershop.entity.ProductVariantAttribute;
import sp26.group3.computer.sba301_computershop.enums.ComponentType;
import sp26.group3.computer.sba301_computershop.enums.RuleType;
import sp26.group3.computer.sba301_computershop.repository.CategoryRepository;
import sp26.group3.computer.sba301_computershop.repository.CompatibilityRuleRepository;
import sp26.group3.computer.sba301_computershop.repository.ProductVariantAttributeRepository;
import sp26.group3.computer.sba301_computershop.service.CompatibilityService;

import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CompatibilityServiceImpl implements CompatibilityService {

    CategoryRepository categoryRepository;
    CompatibilityRuleRepository compatibilityRuleRepository;
    ProductVariantAttributeRepository productVariantAttributeRepository;

    // ======================== GET FILTER HINTS ========================

    @Override
    public CompatibleVariantsResponse getFilterHints(
            List<CompatibleVariantsRequest.ItemHint> items, ComponentType targetType) {
        // Lookup categoryId từ DB theo tên category gắn trên enum
        Integer categoryId = categoryRepository
                .findByCategoryNameIgnoreCase(targetType.getCategoryName())
                .map(c -> c.getCategoryId())
                .orElse(null);
        log.info("[Compat] targetType={} categoryName='{}' categoryId={}",
                targetType, targetType.getCategoryName(), categoryId);

        if (items == null || items.isEmpty())
            return CompatibleVariantsResponse.builder().categoryId(categoryId).hints(List.of()).build();

        // Dedup: for multi-slot types (RAM), keep first variantId for attribute lookup
        Map<ComponentType, Integer> typeToVariantId = new HashMap<>();
        for (CompatibleVariantsRequest.ItemHint hint : items) {
            typeToVariantId.putIfAbsent(hint.getComponentType(), hint.getVariantId());
        }

        List<CompatibilityRule> allRules = compatibilityRuleRepository.findAll();
        List<CompatibilityFilterHint> hints = new ArrayList<>();

        for (CompatibilityRule rule : allRules) {
            // MIN_WATTAGE is handled separately below
            if (rule.getRuleType() == RuleType.MIN_WATTAGE) continue;
            if (rule.getComponentType2() == null) continue;

            ComponentType type1, type2;
            try {
                type1 = ComponentType.valueOf(rule.getComponentType1());
                type2 = ComponentType.valueOf(rule.getComponentType2());
            } catch (IllegalArgumentException e) {
                continue;
            }

            if (type2 == targetType && typeToVariantId.containsKey(type1)) {
                // Target is type2 (e.g. CASE). We know type1's attr1 value (e.g. GPU Length = 336mm).
                // Constraint: attr1_value ≤ attr2  →  attr2 ≥ attr1_value  →  "gte" for MUST_FIT/MUST_SUPPORT
                String comparison = switch (rule.getRuleType()) {
                    case MUST_MATCH -> "eq";
                    case MUST_SUPPORT, MUST_FIT -> "gte";
                    default -> "eq";
                };
                String val = getAttrValue(typeToVariantId.get(type1), rule.getAttribute1());
                if (val != null) hints.add(CompatibilityFilterHint.builder()
                        .attributeName(rule.getAttribute2())
                        .requiredValue(val)
                        .ruleType(rule.getRuleType().name())
                        .comparison(comparison)
                        .build());
            } else if (type1 == targetType && typeToVariantId.containsKey(type2)) {
                // Target is type1 (e.g. RAM). We know type2's attr2 value (e.g. MaxRAMSpeed = 7200).
                // Constraint: attr1 ≤ attr2_value  →  "lte" for MUST_FIT/MUST_SUPPORT
                String comparison = switch (rule.getRuleType()) {
                    case MUST_MATCH -> "eq";
                    case MUST_SUPPORT, MUST_FIT -> "lte";
                    default -> "eq";
                };
                String val = getAttrValue(typeToVariantId.get(type2), rule.getAttribute2());
                if (val != null) hints.add(CompatibilityFilterHint.builder()
                        .attributeName(rule.getAttribute1())
                        .requiredValue(val)
                        .ruleType(rule.getRuleType().name())
                        .comparison(comparison)
                        .build());
            }
        }

        // ── MIN_WATTAGE: nếu user đang chọn PSU, tính tổng TDP của các component đã chọn
        //    rồi thêm buffer 20%, làm tròn lên 50W gần nhất → hint cho PSU wattage
        if (targetType == ComponentType.PSU) {
            double totalTdp = typeToVariantId.values().stream()
                    .mapToDouble(variantId -> {
                        String tdp = getAttrValue(variantId, "TDP");
                        if (tdp == null) return 0;
                        try { return parseNumber(tdp); } catch (NumberFormatException e) { return 0; }
                    })
                    .sum();
            if (totalTdp > 0) {
                int minWattage = (int) (Math.ceil(totalTdp * 1.2 / 50) * 50); // round up to next 50W
                hints.add(CompatibilityFilterHint.builder()
                        .attributeName("Wattage")
                        .requiredValue(String.valueOf(minWattage))
                        .ruleType(RuleType.MIN_WATTAGE.name())
                        .comparison("gte") // PSU Wattage ≥ minWattage
                        .build());
                log.info("[Compat] PSU wattage hint: totalTDP={}W → minPSU={}W", (int) totalTdp, minWattage);
            }
        }

        return CompatibleVariantsResponse.builder()
                .categoryId(categoryId)
                .hints(hints)
                .build();
    }

    private String getAttrValue(int variantId, String attrName) {
        return productVariantAttributeRepository.findByVariantVariantId(variantId).stream()
                .filter(a -> a.getAttribute().getAttributeName().equalsIgnoreCase(attrName))
                .map(ProductVariantAttribute::getValue)
                .findFirst()
                .orElse(null);
    }

    private double parseNumber(String value) {
        String digits = value.replaceAll("[^0-9.]", "").trim();
        if (digits.isEmpty()) throw new NumberFormatException("No digits in: " + value);
        return Double.parseDouble(digits);
    }
}
