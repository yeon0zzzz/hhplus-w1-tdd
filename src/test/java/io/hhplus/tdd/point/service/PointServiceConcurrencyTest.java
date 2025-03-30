package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.repository.InMemoryPointHistoryRepositoryImpl;
import io.hhplus.tdd.point.repository.InMemoryUserPointRepositoryImpl;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;



@DisplayName("동시성_이슈_테스트_케이스")
public class PointServiceConcurrencyTest {

    PointService pointService;
    UserPointRepository userPointRepository;
    PointHistoryRepository pointHistoryRepository;
    PointHistoryTable pointHistoryTable;
    UserPointTable userPointTable;
    UserPoint userPoint;

    @BeforeEach
    void setUp() {
        // 각 테스트마다 새로운 인스턴스를 초기화하여 상태를 초기화
        // 동시성 테스트를 위해 실제 인스터스로 테스트
        userPointTable = new UserPointTable();
        userPointRepository = new InMemoryUserPointRepositoryImpl(userPointTable);
        pointHistoryTable = new PointHistoryTable();
        pointHistoryRepository = new InMemoryPointHistoryRepositoryImpl(pointHistoryTable);
        pointService = new PointService(userPointRepository, pointHistoryRepository);
    }


    @Test
    @DisplayName("사용자_포인트_충전_동시에_여러_스레드로_접근_테스트")
    void User_Points_Charge_Access_from_Multiple_Threads_Test() throws InterruptedException {
        //given
        long userId = 1L;
        long amount = 10L;
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        //when
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    pointService.chargeUserPoint(userId, amount, System.currentTimeMillis());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        UserPoint userPoint = pointService.getUserPoint(userId);

        //then
        assertThat(userPoint.point()).isEqualTo(100L); // (10 * 10)
    }

    @Test
    @DisplayName("사용자_포인트_사용_동시에_여러_스레드로_접근_테스트")
    void User_Points_Use_Access_from_Multiple_Threads_Test() throws InterruptedException {
        //given
        long userId = 1L;
        long amount = 1000L;
        long useAmount = 10L;
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        pointService.chargeUserPoint(userId, amount, System.currentTimeMillis());

        //when
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    pointService.useUserPoint(userId, useAmount, System.currentTimeMillis());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        UserPoint userPoint = pointService.getUserPoint(userId);

        //then
        assertThat(userPoint.point()).isEqualTo(900L); // 1000 - (10 * 10)
    }
}
