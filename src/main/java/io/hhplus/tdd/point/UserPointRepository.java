package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserPointRepository {

    private final UserPointTable userPointTable;

    public UserPoint getBy(long userId) {
        return userPointTable.selectById(userId);
    }

    public UserPoint save(long userId, long amount){
        return userPointTable.insertOrUpdate(userId, amount);
    }
}
