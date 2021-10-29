import com.epicbot.api.shared.util.time.Time;

public class utils {

    public static void sleep(int time) {
        long startTime = Time.getRuntime(0);
        while(!(Time.getRuntime(0) - startTime >= time)) { }
    }

    public static void sleepUntil(boolean completable) {
        while(!completable) {
            if(completable) {
                break;
            }
        }
    }

    public static void sleepUntilMin(int minSleepTime, boolean completable) {
        Time.sleep(minSleepTime);
        while(!completable) {
            if(completable) {
                break;
            }
        }
    }

    public static void sleepUntilMax(int maxSleepTime, boolean completable) {
        long startTime = Time.getRuntime(0);
        while(!completable) {
            if(completable || Time.getRuntime(0) - startTime >= maxSleepTime) {
                break;
            }
        }
    }

    public static void sleepUntil(int minSleepTime, int maxSleepTime, boolean completable) {
        long startTime = Time.getRuntime(0);
        Time.sleep(minSleepTime);
        while(!completable) {
            if(completable || Time.getRuntime(0) - startTime >= maxSleepTime) {
                break;
            }
        }
    }

}
