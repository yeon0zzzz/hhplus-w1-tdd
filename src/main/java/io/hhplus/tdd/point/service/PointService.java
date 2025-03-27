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


@Service
public class PointService {

    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;

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

    public UserPoint chargeUserPoint(long userId, long amount) {

        // 1. 사용자 조회
        UserPoint selectUserPoint = userPointRepository.selectById(userId);

        // 2. 사용자 포인트 충전
        UserPoint updateUserPoint = selectUserPoint.charge(amount);

        // 3. 포인트 이력 생성
        pointHistoryRepository.insert(userId, updateUserPoint.point(), TransactionType.CHARGE, System.currentTimeMillis());

        return userPointRepository.insertOrUpdate(userId, updateUserPoint.point());
    }
}
