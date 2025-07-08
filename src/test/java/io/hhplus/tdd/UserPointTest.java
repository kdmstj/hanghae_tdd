package io.hhplus.tdd;

import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UserPointTest {

    @Test
    void 포인트가_정상적으로_충전된다() {
        //given
        long userId = 1;
        long originAmount = 0;
        UserPoint userPoint = new UserPoint(userId, originAmount, System.currentTimeMillis());

        //when
        long chargeAmount = 2000;
        UserPoint updatedPoint = userPoint.charge(chargeAmount);

        //then
        assertThat(originAmount + chargeAmount).isEqualTo(updatedPoint.point());
    }

    @Test
    void 포인트가_정상적으로_차감된다() {
        //given
        long userId = 1;
        long originAmount = 2000;
        UserPoint userPoint = new UserPoint(userId, originAmount, System.currentTimeMillis());

        //when
        long useAmount = 1000;
        UserPoint updatedPoint = userPoint.use(useAmount);

        //then
        assertThat(originAmount - useAmount).isEqualTo(updatedPoint.point());
    }

    @Test
    void 포인트가_0인_경우_차감이_불가능하다() {
        // given
        long userId = 1;
        long originAmount = 0;
        UserPoint userPoint = new UserPoint(userId, originAmount, System.currentTimeMillis());

        // when & then
        long useAmount = 2000;
        assertThatThrownBy(() -> userPoint.use(useAmount))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INSUFFICIENT_POINT.getMessage());
    }

    @Test
    void 기존포인트보다_차감액이_큰경우_차감이_불가능하다() {
        // given
        long userId = 1;
        long originAmount = 1000;
        UserPoint userPoint = new UserPoint(userId, originAmount, System.currentTimeMillis());

        // when & then
        long useAmount = 2000;
        assertThatThrownBy(() -> userPoint.use(useAmount))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INSUFFICIENT_POINT.getMessage());
    }
}
