package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.UserPoint;
import org.springframework.stereotype.Repository;


@Repository("InMemoryUserPointRepository")
public class InMemoryUserPointRepositoryImpl implements UserPointRepository {

    private final UserPointTable userPointTable;

    public InMemoryUserPointRepositoryImpl(UserPointTable userPointTable) {
        this.userPointTable = userPointTable;
    }

    @Override
    public UserPoint selectById(long userId) {
        return userPointTable.selectById(userId);
    }

    @Override
    public UserPoint insertOrUpdate(long userId, long amount) {
        return userPointTable.insertOrUpdate(userId, amount);
    }
}
