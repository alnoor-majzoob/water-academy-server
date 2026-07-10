package com.wateracademy.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PaginationUtils {

    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    private PaginationUtils() {
    }

    public static Pageable pageable(Integer page, Integer size, List<String> sort, Set<String> allowedSorts, Sort defaultSort) {
        int safePage = Math.max(page == null ? 0 : page, 0);
        int requestedSize = size == null ? DEFAULT_SIZE : size;
        int safeSize = Math.min(Math.max(requestedSize, 1), MAX_SIZE);
        return PageRequest.of(safePage, safeSize, parseSort(sort, allowedSorts, defaultSort));
    }

    public static String like(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return "%" + value.trim().toLowerCase() + "%";
    }

    private static Sort parseSort(List<String> values, Set<String> allowedSorts, Sort defaultSort) {
        if (values == null || values.isEmpty()) {
            return defaultSort;
        }

        var orders = new ArrayList<Sort.Order>();
        for (int i = 0; i < values.size(); i++) {
            var raw = values.get(i);
            if (raw == null || raw.isBlank()) continue;
            var parts = raw.split(",");
            var field = parts[0].trim();
            if (!allowedSorts.contains(field)) {
                throw new IllegalArgumentException("Unsupported sort field: " + field);
            }
            var directionValue = parts.length > 1 ? parts[1].trim() : null;
            if (directionValue == null && i + 1 < values.size() && isDirection(values.get(i + 1))) {
                directionValue = values.get(++i).trim();
            }
            var direction = "desc".equalsIgnoreCase(directionValue) ? Sort.Direction.DESC : Sort.Direction.ASC;
            orders.add(new Sort.Order(direction, field));
        }
        return orders.isEmpty() ? defaultSort : Sort.by(orders);
    }

    private static boolean isDirection(String value) {
        return value != null && ("asc".equalsIgnoreCase(value.trim()) || "desc".equalsIgnoreCase(value.trim()));
    }
}
