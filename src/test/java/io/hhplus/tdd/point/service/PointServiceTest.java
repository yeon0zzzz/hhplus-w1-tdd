package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.MessageConstants;
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
}