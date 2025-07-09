package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class PointHistoryRepository {

    private final PointHistoryTable pointHistoryTable;

    public List<PointHistory> getAllBy(long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }

    public void save(long userId, long amount, TransactionType transactionType){
        pointHistoryTable.insert(userId, amount, transactionType, System.currentTimeMillis());
    }
}
