/*
Command.java
Part of MicroCMD

- When passed multiple commands, they will be run "synchronously"
- There MUST NOT be any sleeps / yields in the command; it must be instantaneous.
- When creating a command from multiple runnables, it will be synchronously run.


Inspiration:
- https://docs.ftclib.org/ftclib/command-base/command-system/convenience-commands
 */
package org.firstinspires.ftc.teamcode.MicroCMD;


public class Command {
    public Runnable function;
    public double lengthns = 0;
    public double startns = 0;
    public double endns = 0;
    public boolean firstRun = true;
    public int currenti = 0;

    public Boolean done = null;

    public Runnable resetRunnable = null;

    public boolean isDone() {
        if (done != null) {
            return done;
        } else {
            if (lengthns == 0) {
                return true;
            } else {
                return System.nanoTime() > endns;
            }
        }
    }
    public void run() {
        if (firstRun) {
            startns = System.nanoTime();
            endns = startns + lengthns;
            firstRun = false;
        }
        if (function != null) {
            function.run();
        }
    }

    public void reset() {
        if (resetRunnable != null) {
            resetRunnable.run();
        }
        firstRun = false;
    }

    public void schedule() {
        CommandScheduler.schedule(this);
    }

    public Command() {
        this.function = null;
    }

    public Command(Command... commands) {
        done = false;
        resetRunnable = () -> {
            for (int i = 0; i < commands.length; i++) {
                commands[i].reset();
            }
        };
        this.function = () -> {
            if (currenti < commands.length) {
                commands[currenti].run();
                if (commands[currenti].isDone()) {
                    currenti++;
                }
            } else {
                commands[commands.length - 1].run();
                done = commands[currenti].isDone();
            }
        };
    }

    public Command(double lengthms, Command... commands) {
        this.lengthns = lengthms * 1000000;
        resetRunnable = () -> {
            for (int i = 0; i < commands.length; i++) {
                commands[i].reset();
            }
        };
        this.function = () -> {
            if (currenti < commands.length) {
                commands[currenti].run();
                if (commands[currenti].isDone()) {
                    currenti++;
                }
            } else {
                commands[commands.length - 1].run();
            }
        };
    }

    public Command(double lengthms) {
        this.lengthns = lengthms * 1000000;
        this.function = null;
    }





}
