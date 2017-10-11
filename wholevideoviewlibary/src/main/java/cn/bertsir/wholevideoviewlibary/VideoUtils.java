package cn.bertsir.wholevideoviewlibary;

/**
 * Created by Bert on 2017/10/11.
 */

public class VideoUtils {

    private static VideoUtils instance;

    public static synchronized VideoUtils getInstance() {
        if(instance == null)
            instance = new VideoUtils();
        return instance;
    }

    /**
     * 毫秒到小时
     *
     * @param seconds
     * @return
     */
    public String milliseconds2hour(int seconds) {
        try {
            int time = seconds;
            int m = time % (60 * 60 * 1000) / (60 * 1000);
            int s = time % (60 * 1000) / 1000;
            return String.format("%02d:%02d", m, s);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return "00:00";
        }

    }
}
