package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;


@Service
public class PointService {

    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final ConcurrentHashMap<Long, ReentrantLock> userLocks = new ConcurrentHashMap<>();


    public PointService(
            @Qualifier("InMemoryUserPointRepository") UserPointRepository userPointRepository,
            @Qualifier("InMemoryPointHistoryRepository") PointHistoryRepository pointHistoryRepository
    ) {
        this.userPointRepository = userPointRepository;
        this.pointHistoryRepository = pointHistoryRepository;
    }

    private static final Logger log = LoggerFactory.getLogger(PointService.class);

    public UserPoint getUserPoint(long userId) {
        return userPointRepository.selectById(userId);
    }

    public UserPoint chargeUserPoint(long userId, long amount, long updateMillis) {

        ReentrantLock user = userLocks.computeIfAbsent(userId, k -> new ReentrantLock());
        user.lock();

        try {
            // 1. 사용자 조회
            UserPoint selectUserPoint = userPointRepository.selectById(userId);

            // 2. 사용자 포인트 충전
            UserPoint updateUserPoint = selectUserPoint.charge(amount);

            // 3. 포인트 이력 생성
            pointHistoryRepository.insert(userId, amount, TransactionType.CHARGE, updateMillis);

            return userPointRepository.insertOrUpdate(userId, updateUserPoint.point());
        } finally {
            user.unlock();
        }
    }

    public UserPoint useUserPoint(long userId, long amount, long updateMillis) {

        ReentrantLock user = userLocks.computeIfAbsent(userId, k -> new ReentrantLock());
        user.lock();

        try {
            // 1. 사용자 조회
            UserPoint selectUserPoint = userPointRepository.selectById(userId);

            // 2. 사용자 포인트 사용
            UserPoint updateUserPoint = selectUserPoint.use(amount);

            // 3. 포인트 이력 생성
            pointHistoryRepository.insert(userId, amount, TransactionType.USE, updateMillis);

            return userPointRepository.insertOrUpdate(userId, updateUserPoint.point());
        } finally {
            user.unlock();
        }

    }

    public List<PointHistory> getPointHistory(long userId) {
        return pointHistoryRepository.selectAllByUserId(userId);
    }
}
