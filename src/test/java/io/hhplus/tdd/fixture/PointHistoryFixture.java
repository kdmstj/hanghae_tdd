package io.hhplus.tdd.fixture;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;

public class PointHistoryFixture {
    public static PointHistory withIdAndUserIdAndAmountAndTransactionType(long id, long userId, long amount, TransactionType transactionType){
        return new PointHistory(id, userId, amount, transactionType, System.currentTimeMillis());
    }
}

