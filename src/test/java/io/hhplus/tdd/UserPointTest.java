package io.hhplus.tdd;

import io.hhplus.tdd.fixture.UserPointFixture;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UserPointTest {

    @Nested
    @DisplayName("포인트 충전")
    class Charge {
        /**
         * [도메인 규칙]
         * 포인트 충전 시 현재 포인트에 충전 금액이 더해진 값이 반환되는지 검증한다.
         * 비즈니스 규칙상 충전은 음수가 아닌 금액을 더하는 동작이다.
         */
        @Test
        void 충전시_포인트가_정상적으로_충전된다() {
            //given
            long originAmount = 0;
            UserPoint userPoint = UserPointFixture.withPoint(originAmount);

            //when
            long chargeAmount = 2000;
            UserPoint updatedPoint = userPoint.charge(chargeAmount);

            //then
            assertThat(originAmount + chargeAmount).isEqualTo(updatedPoint.point());
        }
    }

    @Nested
    @DisplayName("포인트 사용")
    class Use {
        /**
         * [도메인 규칙]
         * 포인트 사용 시 현재 포인트에서 사용 금액만큼 차감된 값이 반환되는지 검증한다.
         * 비즈니스 규칙상 사용 금액은 현재 보유 포인트 이하이어야 한다.
         */
        @ParameterizedTest
        @CsvSource({
                "2000, 1000",
                "2000, 2000"
        })
        void 포인트가_정상적으로_차감된다(long originAmount, long useAmount) {
            //given
            UserPoint userPoint = UserPointFixture.withPoint(originAmount);

            //when
            UserPoint updatedPoint = userPoint.use(useAmount);

            //then
            assertThat(originAmount - useAmount).isEqualTo(updatedPoint.point());
        }

        /**
         * [도메인 규칙]
         * 현재 포인트보다 더 큰 금액을 사용하려고 하면 예외가 발생해야 한다.
         * 포인트는 음수가 될 수 없으며, 비즈니스 규칙상 INSUFFICIENT_POINT 에러를 던진다.
         */
        @ParameterizedTest
        @CsvSource({
                "1000, 2000",
                "0, 2000"
        })
        void 포인트가_부족하면_차감이_불가능하다(long originAmount, long useAmount) {
            // given
            UserPoint userPoint = UserPointFixture.withPoint(originAmount);

            // when & then
            assertThatThrownBy(() -> userPoint.use(useAmount))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorCode.INSUFFICIENT_POINT.getMessage());
        }
    }
}
