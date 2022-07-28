package com.negen;

import java.util.Timer;
import java.util.TimerTask;

public class CacheHandleSchedule {
    public void start(){
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                CacheUtil.getInstance().handleExpireKeys();
            }
        };
        // 在指定延迟0毫秒后开始，随后地执行以1000毫秒间隔执行timerTask
        new Timer().schedule(timerTask, 0L, 1000L);
    }
}
