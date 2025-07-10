package io.hhplus.tdd;

import io.hhplus.tdd.point.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


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

    /**
     * 동시에 여러번 충전 시 모두 정상적으로 충전되는 것을 검증
     */
    @Test
    void 동시에_여러번_적립시_정상적으로_적립된다() throws InterruptedException {
        // given
        long userId = 1L;
        int threadCount = 100;
        long amountPerThread = 100;

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    pointService.charge(userId, amountPerThread);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        UserPoint result = pointService.get(userId);
        assertThat(result.point()).isEqualTo(threadCount * amountPerThread);

        List<PointHistory> histories = pointService.getHistory(userId);
        assertThat(histories.size()).isEqualTo(threadCount);
    }

    /**
     * 동시에 여러번 차감 시 모두 정상적으로 차감되는 것을 검증
     */
    @Test
    void 동시에_여러번_차감시_정상적으로_차감된다() throws InterruptedException {
        // given
        long userId = 1L;
        int threadCount = 50;
        long amountPerThread = 20;

        pointService.charge(userId, threadCount * amountPerThread);

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    pointService.use(userId, amountPerThread);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        UserPoint result = pointService.get(userId);
        assertThat(result.point()).isEqualTo(0);

        List<PointHistory> histories = pointService.getHistory(userId);
        assertThat(histories).filteredOn(h -> h.amount() < 0).hasSize(threadCount);
    }
}
