package sp26.group3.computer.sba301_computershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sp26.group3.computer.sba301_computershop.entity.CompatibilityRule;
import sp26.group3.computer.sba301_computershop.enums.RuleType;

import java.util.List;

public interface CompatibilityRuleRepository extends JpaRepository<CompatibilityRule, Integer> {

    List<CompatibilityRule> findByRuleType(RuleType ruleType);
}
