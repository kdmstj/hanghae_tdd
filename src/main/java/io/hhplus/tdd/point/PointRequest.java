package io.hhplus.tdd.point;

import jakarta.validation.constraints.PositiveOrZero;

public record PointRequest(
        @PositiveOrZero(message = "포인트는 0을 포함한 양수이어야 합니다")
        long amount
) {
}
