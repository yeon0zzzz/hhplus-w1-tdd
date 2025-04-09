package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.MessageConstants;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @InjectMocks
    private PointService pointService;

    @Mock
    private UserPointRepository userPointRepository;

    @Mock
    private PointHistoryRepository pointHistoryRepository;


    @Test
    @DisplayName("사용자_포인트_충전_성공_테스트")
    void chargeUserPoint_Success_Test() {
        // given
        long userId = 1L;
        long amount = 100L;
        long updateMillis = System.currentTimeMillis();
        // 1. 사용자 조회
        given(userPointRepository.selectById(userId)).willReturn(new UserPoint(userId, 0L, updateMillis));
        // 2. 포인트 저장
        given(userPointRepository.insertOrUpdate(userId, amount)).willReturn(new UserPoint(userId, amount, updateMillis));

        // when
        UserPoint updateUserPoint = pointService.chargeUserPoint(userId, amount, updateMillis);

        // then
        assertThat(updateUserPoint.point()).isEqualTo(amount);
        verify(userPointRepository).selectById(eq(userId));
        verify(userPointRepository).insertOrUpdate(eq(userId), eq(amount));
        verify(pointHistoryRepository).insert(eq(userId), eq(amount), eq(TransactionType.CHARGE), eq(updateMillis));
    }

    @Test
    @DisplayName("사용자_포인트_충전_한도_초과_실패_테스트")
    void chargeUserPoint_maxLimit_Fail_Test() {
        // given
        long userId = 1L;
        long amount = 10_000L;
        long updateMillis = System.currentTimeMillis();
        // 1. 사용자 조회
        given(userPointRepository.selectById(userId)).willReturn(new UserPoint(userId, 0L, updateMillis));

        // when & then
        assertThatThrownBy(() -> pointService.chargeUserPoint(userId, amount, updateMillis))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(MessageConstants.EXCEEDS_POINT_LIMIT);

        verify(userPointRepository, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryRepository, never()).insert(anyLong(), anyLong(), any(TransactionType.class), anyLong());
    }

    @Test
    @DisplayName("사용자_포인트_사용_성공_테스트")
    void useUserPoint_Success_Test() {
        // given
        long userId = 1L;
        long currentPoint = 100L;
        long useAmount = 30L;
        long remainingPoint = (currentPoint - useAmount);
        long updateMillis = System.currentTimeMillis();
        // 1. 사용자 조회
        given(userPointRepository.selectById(userId)).willReturn(new UserPoint(userId, currentPoint, updateMillis));
        // 2. 포인트 저장
        given(userPointRepository.insertOrUpdate(userId, remainingPoint))
                .willReturn(new UserPoint(userId, remainingPoint, updateMillis));

        // when
        UserPoint updateUserPoint = pointService.useUserPoint(userId, useAmount, updateMillis);

        // then
        assertThat(updateUserPoint.point()).isEqualTo(remainingPoint);
        verify(userPointRepository).selectById(eq(userId));
        verify(userPointRepository).insertOrUpdate(eq(userId), eq(remainingPoint));
        verify(pointHistoryRepository).insert(eq(userId), eq(useAmount), eq(TransactionType.USE), eq(updateMillis));
    }

    @Test
    @DisplayName("사용자_포인트_사용_잔액_부족_실패_테스트")
    void useUserPoint_insufficient_Fail_Test() {
        // given
        long userId = 1L;
        long currentPoint = 100L;
        long useAmount = 150L;
        long updateMillis = System.currentTimeMillis();
        // 1. 사용자 조회
        given(userPointRepository.selectById(userId)).willReturn(new UserPoint(userId, currentPoint, updateMillis));

        // when & then
        assertThatThrownBy(() -> pointService.useUserPoint(userId, useAmount, updateMillis))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(MessageConstants.INSUFFICIENT_POINTS);

        verify(userPointRepository, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryRepository, never()).insert(anyLong(), anyLong(), any(TransactionType.class), anyLong());
    }

    @Test
    @DisplayName("사용자_포인트_요청_음수_예외처리_테스트")
    void throwException_validateAmount_Test() {
        // given
        long userId = 1L;
        long amount = -500L;
        long updateMillis = System.currentTimeMillis();
        // 1. 사용자 조회
        given(userPointRepository.selectById(userId)).willReturn(new UserPoint(userId, 100L, updateMillis));

        // when & then
        // 2. 충전 포인트 음수 예외처리
        assertThatThrownBy(() -> pointService.chargeUserPoint(userId, amount, updateMillis))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(MessageConstants.INVALID_AMOUNT);

        // 3. 사용 포인트 음수 예외처리
        assertThatThrownBy(() -> pointService.useUserPoint(userId, amount, updateMillis))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(MessageConstants.INVALID_AMOUNT);
    }

    @Test
    @DisplayName("사용자_포인트_조회_테스트")
    void get_UserPoint_Test() {
        // given
        long userId = 1L;
        long amount = 100L;
        long updateMillis = System.currentTimeMillis();

        given(userPointRepository.selectById(userId)).willReturn(new UserPoint(userId, amount, updateMillis));

        // when
        UserPoint userPoint = pointService.getUserPoint(userId);

        // then
        assertThat(userPoint.id()).isEqualTo(userId);
        assertThat(userPoint.point()).isEqualTo(amount);
        assertThat(userPoint.updateMillis()).isEqualTo(updateMillis);

        verify(userPointRepository).selectById(eq(userId));
    }

    @Test
    @DisplayName("사용자_포인트_내역_조회_성공_테스트")
    void get_UserPointHistory_Success_Test() {
        // given
        long userId = 1L;
        long amount = 100L;
        long useAmount = 30L;
        long updateMillis = System.currentTimeMillis();

        given(pointHistoryRepository.selectAllByUserId(userId))
                .willReturn(List.of(
                        new PointHistory(1, userId, amount, TransactionType.CHARGE, updateMillis),
                        new PointHistory(2, userId, amount, TransactionType.CHARGE, updateMillis),
                        new PointHistory(3, userId, useAmount, TransactionType.USE, updateMillis)
                ));

        // when
        List<PointHistory> pointHistories = pointService.getPointHistory(userId);

        // then
        assertThat(pointHistories).hasSize(3);
        assertThat(pointHistories.get(0).amount()).isEqualTo(100L);
        assertThat(pointHistories.get(2).amount()).isEqualTo(30L);
        assertThat(pointHistories.get(2).type()).isEqualTo(TransactionType.USE);
    }

    @Test
    @DisplayName("사용자_포인트_내역이_없을때_조회_테스트")
    void get_UserPointHistory_Empty_Test() {
        // given
        long userId = 1L;

        given(pointHistoryRepository.selectAllByUserId(userId)).willReturn(List.of());

        // when
        List<PointHistory> pointHistories = pointService.getPointHistory(userId);

        // then
        assertThat(pointHistories).isEmpty();
    }
}