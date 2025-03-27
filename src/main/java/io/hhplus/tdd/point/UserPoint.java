package io.hhplus.tdd.point;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    // 포인트 한도 설정
    private static final long MAX_POINT = 1000L;

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    public UserPoint charge(long amount) {
        long updatePoint = this.point + amount;

        // 포인트 한도 검증
        if (updatePoint > MAX_POINT){
            throw new IllegalArgumentException(MessageConstants.EXCEEDS_POINT_LIMIT);
        }

        return new UserPoint(this.id, updatePoint, System.currentTimeMillis());
    }

    public UserPoint use(long amount) {

        invalidAmount(amount);

        long updatePoint = this.point - amount;

        // 포인트 잔액 부족 검증
        if (updatePoint < 0){
            throw new IllegalArgumentException(MessageConstants.INSUFFICIENT_POINTS);
        }

        return new UserPoint(this.id, updatePoint, System.currentTimeMillis());
    }

    public void invalidAmount(long amount) {
        // 포인트 유효성 검사(음수 불가)
        if (amount < 0) {
            throw new IllegalArgumentException(MessageConstants.INVALID_AMOUNT);
        }
    }
}
