package batch.config.classes;

import java.util.Objects;

public enum Level {
    VIP(5000000, null),
    GOLD(4000000, VIP),
    SILVER(300000, GOLD), 
    NORMAL(200000, SILVER);

    private final int nextAmount;
    private final Level nextLevel;

    private Level(int nextAmount, Level nextLevel ) {
        this.nextAmount = nextAmount;
        this.nextLevel = nextLevel;
    }

    /** 고객의 등급이 업그레이드 가능 여부를 반환한다.*/
    public static boolean availableLevelUp(Level level, int totalAmount) {
        if (Objects.isNull(level)) {
            return false;
        }

        if (Objects.isNull(level.nextLevel)) {
            return false;
        }

        if (totalAmount >= level.nextAmount) {
            return true;
        }
        return false;
    }

    /** 고객의 총 구입 금액에 따른 고객 등급을 반환한다. */
    public static Level getNextLevel(int totalAmount) {
        if (totalAmount >= Level.VIP.nextAmount) {
            return VIP;
        }

        if (totalAmount >= Level.GOLD.nextAmount) {
            return GOLD.nextLevel;
        }

        if (totalAmount >= Level.SILVER.nextAmount) {
            return SILVER.nextLevel;
        }

        if (totalAmount >= Level.NORMAL.nextAmount) {
            return NORMAL.nextLevel;
        }
        return null;
    }
}
