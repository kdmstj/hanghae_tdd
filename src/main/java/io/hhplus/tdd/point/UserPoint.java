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

    public UserPoint charge(long amount) {

        return new UserPoint(id, point + amount, System.currentTimeMillis());
    }

    public UserPoint use(long amount) {
        if (point < amount) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_POINT);
        }

        return new UserPoint(id, point - amount, System.currentTimeMillis());
    }
}