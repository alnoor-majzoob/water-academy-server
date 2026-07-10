package com.wateracademy.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

class PaginationUtilsTest {

    private static final Set<String> SORT_FIELDS = Set.of("name", "createdAt");

    @Test
    void pageable_shouldParseCommaSeparatedSort() {
        var pageable = PaginationUtils.pageable(0, 20, List.of("name,asc"), SORT_FIELDS, Sort.by("createdAt"));

        var order = pageable.getSort().getOrderFor("name");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void pageable_shouldParseSplitSortDirection() {
        var pageable = PaginationUtils.pageable(0, 20, List.of("name", "desc"), SORT_FIELDS, Sort.by("createdAt"));

        var order = pageable.getSort().getOrderFor("name");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void pageable_shouldParseMultipleSplitSorts() {
        var pageable = PaginationUtils.pageable(0, 20,
                List.of("name", "asc", "createdAt", "desc"), SORT_FIELDS, Sort.by("createdAt"));

        assertThat(pageable.getSort().getOrderFor("name").getDirection()).isEqualTo(Sort.Direction.ASC);
        assertThat(pageable.getSort().getOrderFor("createdAt").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void pageable_shouldRejectUnsupportedSortField() {
        assertThatThrownBy(() -> PaginationUtils.pageable(0, 20, List.of("badField", "asc"), SORT_FIELDS, Sort.by("createdAt")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unsupported sort field: badField");
    }
}
