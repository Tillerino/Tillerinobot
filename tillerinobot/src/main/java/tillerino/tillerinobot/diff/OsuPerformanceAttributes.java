package tillerino.tillerinobot.diff;

public record OsuPerformanceAttributes(
    double aim,
    double speed,
    double accuracy,
    double flashlight,
    double effectiveMissCount,
    double speedDeviation,
    double total) {
}
