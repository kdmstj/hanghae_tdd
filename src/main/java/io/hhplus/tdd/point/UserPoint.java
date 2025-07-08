package io.hhplus.tdd.point;

import io.hhplus.tdd.BusinessException;
import io.hhplus.tdd.ErrorCode;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }
}
