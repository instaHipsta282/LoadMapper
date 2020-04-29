package ru.boomq.loadMapper;

import java.util.HashMap;
import java.util.Map;

public class LoadMapper {
    public static void main(String[] args) {

        Map<Long, Long> timeLine = new HashMap<>();

        mapper(timeLine, 15, 23, 1, 45, 8);
        mapper(timeLine, 19, 7, 9, 12, 0);
        mapper(timeLine, 3, 200, 23, 10, 10);

        timeLine.forEach((k, v) -> System.out.println(k + ";" + v));
    }


    static void mapper(Map<Long, Long> timeLine,
                       long startThreadsCount,
                       long initDelaySec,
                       long startupTimeSec,
                       long holdLoadTimeSec,
                       long shutdownTimeSec) {
        /*
        map.merge(a, b, Long::sum)
        ==

        if (map.contains(a)) {
            map.get(a) += b;
        }
        else {
            map.put(a, b);
        }
         */

        //initial
        for (long i = 0; i < initDelaySec; i++) {
            timeLine.merge(i, 0L, Long::sum);
        }

        double secForThread = (double) startupTimeSec / startThreadsCount;
        double threadPerSec = 1 / secForThread;

        //startup
        int startupStepNumber = 0;
        for (long i = initDelaySec; i < initDelaySec + startupTimeSec; i++) {
            timeLine.merge(i, (long) (threadPerSec * startupStepNumber), Long::sum);
            startupStepNumber++;
        }

        //hold
        for (long i = initDelaySec + startupTimeSec; i < initDelaySec + startupTimeSec + holdLoadTimeSec; i++) {
            timeLine.merge(i, startThreadsCount, Long::sum);
        }

        secForThread = (double) shutdownTimeSec / startThreadsCount;
        threadPerSec = 1 / secForThread;

        //shutdown
        long shutdownStepNumber = shutdownTimeSec;
        for (long i = initDelaySec + startupTimeSec + holdLoadTimeSec; i <= initDelaySec + startupTimeSec + holdLoadTimeSec + shutdownTimeSec; i++) {
            timeLine.merge(i, (long) (threadPerSec * shutdownStepNumber), Long::sum);
            shutdownStepNumber--;
        }
    }
}

