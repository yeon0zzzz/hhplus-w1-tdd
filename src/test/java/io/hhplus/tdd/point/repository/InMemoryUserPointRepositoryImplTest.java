package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class InMemoryUserPointRepositoryImplTest {

    private UserPointRepository userPointRepository;

    @BeforeEach
    void setUp() {
        userPointRepository = new InMemoryUserPointRepositoryImpl(new UserPointTable());
    }

    @Test
    @DisplayName("사용자_포인트_조회_테스트")
    void get_user_point_Test() {
        // given
        long userId = 1L;
        long amount = 100L;
        userPointRepository.insertOrUpdate(userId, amount);

        // when
        UserPoint userPoint = userPointRepository.selectById(userId);

        // then
        assertThat(userPoint.id()).isEqualTo(userId);
        assertThat(userPoint.point()).isEqualTo(amount);
    }

    @Test
    @DisplayName("없는_사용자_조회_테스트")
    void get_empty_user_point_Test() {
        // given
        long userId = 1L;

        // when
        UserPoint userPoint = userPointRepository.selectById(userId);

        // then
        assertThat(userPoint.id()).isEqualTo(userId);
        assertThat(userPoint.point()).isEqualTo(0L);
    }

    @Test
    @DisplayName("기존_사용자_포인트_업데이트_테스트")
    void insert_or_update_exists_user_Test() {
        // given
        long userId = 1L;
        long amount = 100L;
        userPointRepository.insertOrUpdate(userId, amount);

        // when
        UserPoint userPoint = userPointRepository.insertOrUpdate(userId, 500L);

        // then
        assertThat(userPoint.id()).isEqualTo(userId);
        assertThat(userPoint.point()).isEqualTo(500L);

    }
}