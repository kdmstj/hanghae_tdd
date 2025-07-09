package io.hhplus.tdd.point;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    public UserPoint get(long userId) {
        return userPointRepository.getBy(userId);
    }

    public List<PointHistory> getHistory(long userId) {
        return pointHistoryRepository.getAllBy(userId);
    }

    public UserPoint charge(long userId, long amount) {
        UserPoint userPoint = get(userId);
        UserPoint updatedPoint = userPoint.charge(amount);

        UserPoint savedPoint = userPointRepository.save(updatedPoint.id(), updatedPoint.point());
        pointHistoryRepository.save(userId, amount, TransactionType.CHARGE);

        return savedPoint;
    }

    public UserPoint use(long userId, long amount) {
        UserPoint userPoint = get(userId);
        UserPoint updatedPoint = userPoint.use(amount);

        UserPoint savePoint = userPointRepository.save(updatedPoint.id(), updatedPoint.point());
        pointHistoryRepository.save(userId, -amount, TransactionType.USE);

        return savePoint;
    }
}
