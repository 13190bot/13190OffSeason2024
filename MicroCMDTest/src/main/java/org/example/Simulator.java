package org.example;

import org.example.MicroCMD.Command;
import org.example.MicroCMD.CommandScheduler;

import static java.lang.Thread.sleep;

public class Simulator {
    public static long lengthns = 10 * (long) 1000000000;

    public static void main(String[] args) throws InterruptedException {
        sleep(1000);
        long startns = System.nanoTime();

        boolean a = true;
        while (System.nanoTime() - startns < lengthns) {
            CommandScheduler.loop();

//            System.out.println(CommandScheduler.currentCommands.size());

            if (a) {
//                new Command(
//                    new Command(() -> {System.out.println(1);}),
//                    new Command(2000).onSchedule(new Command(() -> {System.out.println(3);})
//                    ).onRun(new Command (() -> {System.out.println(4);})),
//                    new Command(() -> {System.out.println(2);})
//                ).schedule();
                new Command(() -> {System.out.println(1);}).onDone(
                    new Command(3000).onDone(
                        new Command(() -> {System.out.println(2);})
                    )
                ).schedule();
                a = false;
            }
        }
        System.out.println("end");
    }
}