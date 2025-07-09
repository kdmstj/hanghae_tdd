package io.hhplus.tdd;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hhplus.tdd.fixture.PointHistoryFixture;
import io.hhplus.tdd.fixture.UserPointFixture;
import io.hhplus.tdd.point.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PointController.class)
class PointControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    PointService pointService;

    @Test
    void 특정_유저의_포인트_조회시_성공한다() throws Exception {
        // given
        long userId = 1L;
        long point = 1000L;
        UserPoint userPoint = UserPointFixture.withUserIdAndPoint(userId, point);
        given(pointService.get(userId)).willReturn(userPoint);

        // when & then
        mockMvc.perform(get("/points/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.point").value(point));
    }

    @Test
    void 특정_유저의_포인트_내역을_조회시_성공한다() throws Exception {
        // given
        long userId = 1L;
        List<PointHistory> histories = List.of(
                PointHistoryFixture.withIdAndUserIdAndAmountAndTransactionType(1, userId, 1000, TransactionType.CHARGE),
                PointHistoryFixture.withIdAndUserIdAndAmountAndTransactionType(2, userId, -500, TransactionType.USE)
        );

        given(pointService.getHistory(userId)).willReturn(histories);

        // when & then
        mockMvc.perform(get("/points/{userId}/histories", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(userId))
                .andExpect(jsonPath("$[0].amount").value(1000))
                .andExpect(jsonPath("$[0].type").value("CHARGE"))
                .andExpect(jsonPath("$[1].userId").value(userId))
                .andExpect(jsonPath("$[1].amount").value(-500))
                .andExpect(jsonPath("$[1].type").value("USE"));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1000})
    void 특정_유저의_포인트_충전에_성공한다(long chargeAmount) throws Exception {
        //given
        long userId = 1L;
        long originAmount = 0;
        UserPoint userPoint = UserPointFixture.withUserIdAndPoint(userId, originAmount + chargeAmount);

        given(pointService.charge(userId, chargeAmount)).willReturn(userPoint);

        //when & then
        PointRequest request = new PointRequest(chargeAmount);
        mockMvc.perform(patch("/points/{userId}/charge", userId)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.point").value(originAmount + chargeAmount));
    }

    @Test
    void 특정_유저의_포인트_충전시_음수로하면_실패한다() throws Exception {
        //given
        long userId = 1L;
        long chargeAmount = -1;

        //when & then
        PointRequest request = new PointRequest(chargeAmount);
        mockMvc.perform(patch("/points/{userId}/charge", userId)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("포인트는 0을 포함한 양수이어야 합니다"));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1000})
    void 특정_유저의_포인트_차감에_성공한다(long useAmount) throws Exception {
        //given
        long userId = 1L;
        long originAmount = 2000L;
        UserPoint userPoint = UserPointFixture.withUserIdAndPoint(userId, originAmount - useAmount);

        given(pointService.use(userId, useAmount)).willReturn(userPoint);

        PointRequest request = new PointRequest(useAmount);

        //when & then
        mockMvc.perform(patch("/points/{userId}/use", userId)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.point").value(originAmount - useAmount));
    }

    @Test
    void 특정_유저의_포인트_차감시_음수로하면_실패한다() throws Exception {
        //given
        long userId = 1L;
        long useAmount = -1;

        PointRequest request = new PointRequest(useAmount);

        //when & then
        mockMvc.perform(patch("/points/{userId}/use", userId)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("포인트는 0을 포함한 양수이어야 합니다"));
    }

    @Test
    void 특정_유저의_포인트_차감시_포인트가_부족하다면_실패한다() throws Exception {
        long userId = 1L;
        long useAmount = 5000L;

        given(pointService.use(userId, useAmount))
                .willThrow(new BusinessException(ErrorCode.INSUFFICIENT_POINT));

        PointRequest request = new PointRequest(useAmount);

        mockMvc.perform(patch("/points/{userId}/use", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.INSUFFICIENT_POINT.getMessage()));
    }
}
