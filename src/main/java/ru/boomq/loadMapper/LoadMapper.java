package ru.boomq.loadMapper;

import java.util.HashMap;
import java.util.Map;

public class LoadMapper {
    public static void main(String[] args) {

        Map<Long, Long> timeLine = new HashMap<>();

        mapper(timeLine,7, 4, 4, 4, 4);

//        alg(map, 4, 20, 4, 4, 4);

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
        int idx = 0;
        for (long i = initDelaySec; i < initDelaySec + startupTimeSec; i++) {
            timeLine.merge(i, (long) (threadPerSec * idx), Long::sum);
            idx++;
        }

        //hold
        for (long i = initDelaySec + startupTimeSec; i < initDelaySec + startupTimeSec + holdLoadTimeSec; i++) {
            timeLine.merge(i, startThreadsCount, Long::sum);
        }

        //shutdown
        long count2 = startupTimeSec;
        for (long i = initDelaySec + startupTimeSec + holdLoadTimeSec; i <= initDelaySec + startupTimeSec + holdLoadTimeSec + shutdownTimeSec; i++) {
            timeLine.merge(i, (long) (threadPerSec * count2), Long::sum);
            count2--;
        }
    }
}

