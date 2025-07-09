package io.hhplus.tdd;

import io.hhplus.tdd.fixture.PointHistoryFixture;
import io.hhplus.tdd.fixture.UserPointFixture;
import io.hhplus.tdd.point.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PointServiceTest {

    @InjectMocks
    PointService pointService;

    @Mock
    UserPointRepository userPointRepository;

    @Mock
    PointHistoryRepository pointHistoryRepository;

    @Test
    void 특정_유저의_포인트를_조회한다() {
        // given
        long userId = 1L;
        UserPoint userPoint = UserPointFixture.withUserId(userId);
        given(userPointRepository.getBy(userId)).willReturn(userPoint);

        // when
        UserPoint result = pointService.get(userId);

        // then
        assertThat(result.id()).isEqualTo(userId);
    }

    @Test
    void 특정_유저의_포인트_사용내역을_조회한다() {
        // given
        long userId = 1L;
        List<PointHistory> histories = List.of(
                PointHistoryFixture.withIdAndUserIdAndAmountAndTransactionType(1, userId, 1000, TransactionType.CHARGE),
                PointHistoryFixture.withIdAndUserIdAndAmountAndTransactionType(2, userId, -500, TransactionType.USE)
        );
        given(pointHistoryRepository.getAllBy(userId)).willReturn(histories);

        // when
        List<PointHistory> result = pointService.getHistory(userId);

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    void 포인트_적립시_이력을_남긴다() {
        // given
        long userId = 1L;
        long chargeAmount = 1000L;
        UserPoint current = UserPointFixture.withUserId(userId);
        UserPoint updated = current.charge(chargeAmount);

        given(userPointRepository.getBy(userId)).willReturn(current);

        // when
        pointService.charge(userId, chargeAmount);

        // then
        verify(userPointRepository).save(updated.id(), updated.point());
        verify(pointHistoryRepository).save(userId, chargeAmount, TransactionType.CHARGE);
    }

    @Test
    void 포인트_차감시_이력을_남긴다() {
        // given
        long userId = 1L;
        long useAmount = 1000L;
        UserPoint current = UserPointFixture.withUserId(userId);
        UserPoint updated = current.use(useAmount);

        given(userPointRepository.getBy(userId)).willReturn(current);

        // when
        pointService.use(userId, useAmount);

        // then
        verify(userPointRepository).save(updated.id(), updated.point());
        verify(pointHistoryRepository).save(userId, -useAmount, TransactionType.USE);
    }
}
