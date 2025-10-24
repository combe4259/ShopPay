package org.zerock.shoppay.repository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import org.zerock.shoppay.Entity.Product;

public class ProductSpecification {

    /**
     * 항상 is_active = true 조건을 포함하기 위한 기본 Specification
     */
    public static Specification<Product> isActive() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isTrue(root.get("isActive"));
    }

    /**
     * 카테고리 이름으로 필터링하는 Specification
     * @param categoryName 필터링할 카테고리 이름
     * @return Specification<Product>
     */
    public static Specification<Product> hasCategory(String categoryName) {
        return (root, query, criteriaBuilder) -> {
            // categoryName 파라미터가 없거나, 비어있거나, "All"이면 이 조건은 무시됩니다.
            if (!StringUtils.hasText(categoryName) || "All".equalsIgnoreCase(categoryName)) {
                return null;
            }
            // p.category.name = :categoryName 에 해당하는 조건입니다.
            return criteriaBuilder.equal(root.get("category").get("name"), categoryName);
        };
    }
}
