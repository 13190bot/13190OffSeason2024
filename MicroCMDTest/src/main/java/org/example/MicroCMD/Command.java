/*
Command.java
Part of MicroCMD

- When passed multiple commands, they will be run "synchronously"
- There MUST NOT be any sleeps / yields in the command; it must be instantaneous.
- When creating a command from multiple runnables, it will be synchronously run.
- WARNING: The same command should not be scheduled at the same time


Inspiration:
- https://docs.ftclib.org/ftclib/command-base/command-system/convenience-commands
 */
package org.example.MicroCMD;


import java.util.ArrayList;

public class Command {
    public Runnable function = null;
    public double lengthns = 0;
    public double startns = 0;
    public double endns = 0;
    public boolean firstRun = true;
    public int currenti = 0;
    public boolean scheduled = false;

    public Boolean done = null;

    public Runnable resetRunnable = null;

    public ArrayList<Command> doneRunnable = null; // when done
    public ArrayList<Command> scheduleRunnable = null; // when scheduled
    public ArrayList<Command> runRunnable = null; // when running

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
            currenti = 0;
            if (done != null) {
                done = false;
            }
        }
        if (function != null) {
            function.run();
        }
    }

    public void runOn(ArrayList<Command> runnable) {
        if (runnable != null) {
            for (int i = 0; i < runnable.size(); i++) {
                runnable.get(i).schedule();
            }
        }
    }

    public void runOnDone() {
        runOn(this.doneRunnable);
    }

    public void runOnSchedule() {
        runOn(this.scheduleRunnable);
    }

    public void runOnRun() {
        runOn(this.runRunnable);
    }

    public Command reset() {
        if (resetRunnable != null) {
            resetRunnable.run();
        }
        firstRun = true;
        return this;
    }

    public Command schedule() {
        // Reset
        this.reset();
        CommandScheduler.schedule(this);
        return this;
    }

    public Command on(ArrayList<Command> runnable, Command[] commands) {
        for (int i = 0; i < commands.length; i++) {
            runnable.add(commands[i]);
        }
        return this;
    }

    public Command onDone(Command... commands) {
        if (this.doneRunnable == null) {
            this.doneRunnable = new ArrayList<>();
        }
        return on(this.doneRunnable, commands);
    }

    public Command onSchedule(Command... commands) {
        if (this.scheduleRunnable == null) {
            this.scheduleRunnable = new ArrayList<>();
        }
        return on(this.scheduleRunnable, commands);
    }

    public Command onRun(Command... commands) {
        if (this.runRunnable == null) {
            this.runRunnable = new ArrayList<>();
        }
        return on(this.runRunnable, commands);
    }

    public Command() {
    }

    public Command(Command... commands) {
        done = false;
        resetRunnable = () -> {
            for (int i = 0; i < commands.length; i++) {
                commands[i].reset();
            }
        };
        for (int i = 1; i < commands.length; i++) {
            commands[i - 1].onDone(commands[i]);
        }
        this.onSchedule(commands[0]);
        commands[commands.length - 1].onDone(new Command(() -> {
            done = true;
        }));
//        this.function = () -> {
//            if (!commands[0].scheduled) {
//                commands[0].schedule();
//            }
//            done = commands[commands.length - 1].isDone();
//        };
    }

    public Command(Runnable... functions) {
        this.function = () -> {
            for (int i = 0; i < functions.length; i++) {
                functions[i].run();
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
        commands[0].schedule();
        this.function = () -> {
            if (currenti < commands.length) {
                Command command = commands[currenti];
                if (command.isDone()) {
                    currenti++;
                    if (currenti < commands.length) {
                        commands[currenti].schedule();
                    }
                }
            }
        };
    }

    public Command(double lengthms, Runnable... functions) {
        this.lengthns = lengthms * 1000000;
        this.function = () -> {
            for (int i = 0; i < functions.length; i++) {
                functions[i].run();
            }
        };
    }

    public Command(double lengthms) {
        this.lengthns = lengthms * 1000000;
        this.function = null;
    }
}
