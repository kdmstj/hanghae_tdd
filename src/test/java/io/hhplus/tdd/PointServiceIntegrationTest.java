package io.hhplus.tdd;

import io.hhplus.tdd.point.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class PointServiceIntegrationTest {

    @Autowired
    PointService pointService;

    @Autowired
    UserPointRepository userPointRepository;

    @Autowired
    PointHistoryRepository pointHistoryRepository;

    @BeforeEach
    void setUp() {
        userPointRepository.clear();
        pointHistoryRepository.clear();
    }

    @Nested
    @DisplayName("포인트 조회")
    class GetPoint {
        /**
         * [서비스 동작]
         * 특정 유저의 현재 포인트를 조회할 수 있는지 검증한다.
         * repository.getBy로 정상적으로 DB 에서 데이터가 반환되는지 검증한다.
         */
        @Test
        void 특정_유저의_포인트를_조회한다() {
            // given
            long userId = 1L;
            long amount = 1000L;
            userPointRepository.save(userId, amount);

            // when
            UserPoint userPoint = pointService.get(userId);

            // then
            assertThat(userPoint.id()).isEqualTo(userId);
            assertThat(userPoint.point()).isEqualTo(amount);
        }
    }

    @Nested
    @DisplayName("포인트 내역 조회")
    class GetPointHistory {
        /**
         * [서비스 동작]
         * 특정 유저의 포인트 사용 내역을 조회할 수 있는지 검증한다.
         * repository.getAllBy로 정상적으로 DB 에서 데이터가 반환되는지 검증한다.
         */
        @Test
        void 특정_유저의_포인트_사용내역을_조회한다() {
            // given
            long userId = 1L;
            pointService.charge(userId, 500L);
            pointService.use(userId, 200L);

            // when
            List<PointHistory> histories = pointService.getHistory(userId);

            // then
            assertThat(histories).hasSize(2);
            assertThat(histories).anyMatch(h -> h.amount() == 500L && h.type() == TransactionType.CHARGE);
            assertThat(histories).anyMatch(h -> h.amount() == -200L && h.type() == TransactionType.USE);
        }
    }

    @Nested
    @DisplayName("포인트 충전")
    class ChargePoint {
        /**
         * [서비스 동작]
         * 포인트 충전 시:
         * 1. 포인트 업데이트가 DB 에 반영이 되는지
         * 2. 충전 이력이 DB 에 반영이 되는지
         */
        @Test
        void 특정_유저의_포인트_적립() {
            // given
            long userId = 1L;

            // when
            long chargeAmount = 1000L;
            pointService.charge(userId, chargeAmount);

            // then
            UserPoint result = pointService.get(userId);
            assertThat(result.point()).isEqualTo(chargeAmount);

            List<PointHistory> histories = pointService.getHistory(userId);
            assertThat(histories).anyMatch(h -> h.amount() == chargeAmount && h.type() == TransactionType.CHARGE);
        }
    }

    @Nested
    @DisplayName("포인트 사용")
    class UsePoint {

        /**
         * [서비스 동작]
         * 포인트 사용 시:
         * 1. 포인트 업데이트 하는 메서드가 호출되는지
         * 2. 사용 이력이 DB 에 반영이 되는지
         */
        @Test
        void 특정_유저의_포인트_차감() {
            // given
            long userId = 1L;
            pointService.charge(userId, 1500L);

            // when
            pointService.use(userId, 500L);

            // then
            UserPoint result = pointService.get(userId);
            assertThat(result.point()).isEqualTo(1000L);

            List<PointHistory> histories = pointService.getHistory(userId);
            assertThat(histories).anyMatch(h -> h.amount() == -500L && h.type() == TransactionType.USE);
        }

        @Test
        void 차감_실패시_포인트_이력을_업데이트하지_않는다() {
            // given
            long userId = 1L;
            pointService.charge(userId, 500L);

            // when & then
            assertThatThrownBy(() -> pointService.use(userId, 1000L))
                    .isInstanceOf(BusinessException.class);

            UserPoint result = pointService.get(userId);
            assertThat(result.point()).isEqualTo(500L);

            List<PointHistory> histories = pointService.getHistory(userId);
            assertThat(histories).noneMatch(h -> h.amount() < 0);
        }
    }
}
