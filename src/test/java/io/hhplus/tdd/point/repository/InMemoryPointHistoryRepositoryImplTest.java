package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.given;


class InMemoryPointHistoryRepositoryImplTest {

    private UserPointRepository userPointRepository;

    private PointHistoryRepository pointHistoryRepository;

    @BeforeEach
    void setUp() {
        pointHistoryRepository = new InMemoryPointHistoryRepositoryImpl(new PointHistoryTable());
    }

    @Test
    @DisplayName("포인트_내역_생성_테스트")
    void insert_point_history_Test() {
        // given
        long userId = 1L;
        long amount = 100L;
        TransactionType type = TransactionType.CHARGE;
        long timestamp = System.currentTimeMillis();

        // when
        pointHistoryRepository.insert(userId, amount, type, timestamp);

        // then
        List<PointHistory> histories = pointHistoryRepository.selectAllByUserId(userId);
        assertThat(histories).hasSize(1);
        PointHistory history = histories.get(0);
        assertThat(history.userId()).isEqualTo(userId);
        assertThat(history.amount()).isEqualTo(amount);
        assertThat(history.type()).isEqualTo(type);
        assertThat(history.updateMillis()).isEqualTo(timestamp);
    }

    @Test
    @DisplayName("포인트_내역_생성_실패_테스트")
    void insert_point_history_fail_Test() {
        // given
        long userId = 1L;
        long amount = 100L;
        long updateMillis = System.currentTimeMillis();

        pointHistoryRepository.insert(userId, amount, TransactionType.CHARGE, updateMillis);
        pointHistoryRepository.insert(userId, amount, TransactionType.USE, updateMillis);

        // when
        List<PointHistory> pointHistories = pointHistoryRepository.selectAllByUserId(userId);

        // then
        Assertions.assertThat(pointHistories).hasSize(2);
        Assertions.assertThat(pointHistories.get(0).amount()).isEqualTo(100L);
        Assertions.assertThat(pointHistories.get(1).amount()).isEqualTo(100L);
        Assertions.assertThat(pointHistories.get(1).type()).isEqualTo(TransactionType.USE);
    }
}