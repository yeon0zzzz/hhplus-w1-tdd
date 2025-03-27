package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.point.UserPoint;


public interface UserPointRepository {

    UserPoint selectById(long userId);

    UserPoint insertOrUpdate(long userId, long amount);

}
