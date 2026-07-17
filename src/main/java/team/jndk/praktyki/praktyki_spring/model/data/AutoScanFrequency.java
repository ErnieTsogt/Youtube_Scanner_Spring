package team.jndk.praktyki.praktyki_spring.model.data;

public enum AutoScanFrequency {
    MANUAL(null),
    EVERY_MINUTE("0 * * * * *"),
    EVERY_15_MIN("0 */15 * * * *"),
    EVERY_30_MIN("0 */30 * * * *"),
    HOURLY("0 0 * * * *"),
    DAILY_CUSTOM(null),
    EVERY_6_HOURS("0 0 */6 * * *"),
    DAILY_2AM("0 0 2 * * *");

    private final String cronExpression;

    AutoScanFrequency(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public boolean isManual() {
        return this == MANUAL;
    }
}
