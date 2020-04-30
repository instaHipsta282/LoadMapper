package ru.boomq.loadMapper;

import java.util.HashMap;
import java.util.Map;

public class LoadMapper {
    public static void main(String[] args) {

        //Временная линия, где key - секунда(начиная c 0 и до конца теста) а value - количество активных thread`s в эту секунду.
        Map<Long, Long> timeLine = new HashMap<>();
        
        //функция mapper поэтапно смотрит, если ли такая точка в timeLine, если нет, то добавляет, если есть
        //(этот шаг накладывается на один или несколько предыдущих), то к уже существующему количеству thread`s 
        //в эту секунду добавляется значение за эту секунду из этого шага
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

        //тут у меня стояла задача равномерно разделить общее количество пользователей на количество секунд
        //rumpUp`a, поэтому я вычисляю среднее количество тредов, которые выводятся на этапе rump up за 1 секунду
        double secForThread = (double) startupTimeSec / startThreadsCount;
        double threadPerSec = 1 / secForThread;

        //startup
        int startupStepNumber = 0;
        for (long i = initDelaySec; i < initDelaySec + startupTimeSec; i++) {
            //Тут я умножаю секунду rump up`a(от 0 до конца rump up) на количество тредов в секунду и срезаю дробную
            // часть 
            // Допустим, у нас rumpUp 10 секунд, и надо вывести 25 пользователей: secForThread: 10 / 25 = 0.4
            // threadPerSec: 1 / 0.4 = 2.5 thread в секунду.
            // в первую секунду rump up выводится 2.5 == 2 thread,
            // во вторую, 2.5 * 2 = 5
            // в третью 2.5 * 3 = 7.5 == 7
            // в четвертую 2.5 * 4 = 10
            // в четвертую 2.5 * 5 = 12.5 == 12
            // в четвертую 2.5 * 6 = 15
            // в четвертую 2.5 * 7 = 17.5 == 17
            // в четвертую 2.5 * 8 = 20
            // в четвертую 2.5 * 9 = 22.5 == 22
            // в четвертую 2.5 * 10 = 25
            // в итоге мы сделали равномерное повышение нагрузки с 0 до 25
            // rump down работает аналогично, но с понижением
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

