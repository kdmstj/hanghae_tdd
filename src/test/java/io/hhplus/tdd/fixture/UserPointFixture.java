package io.hhplus.tdd.fixture;

import io.hhplus.tdd.point.UserPoint;

public class UserPointFixture {

    public static UserPoint withPoint(long point) {
        return new UserPoint(1L, point, System.currentTimeMillis());
    }

    public static UserPoint withUserId(long userId){
        return new UserPoint(userId, 1000, System.currentTimeMillis());
    }

    public static UserPoint withUserIdAndPoint(long userId, long point) {
        return new UserPoint(userId, point, System.currentTimeMillis());
    }
}
