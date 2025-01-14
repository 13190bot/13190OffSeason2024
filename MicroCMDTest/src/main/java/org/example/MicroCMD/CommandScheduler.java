/*
Command.java
Part of MicroCMD

- When passed multiple commands, they will be run "asynchronously"
- Trying to schedule a command that is currently scheduled will result in nothing happening
 */
package org.example.MicroCMD;
import java.util.ArrayList;

public class CommandScheduler {

    // List of currently queued commands
    public static ArrayList<Command> currentCommands = new ArrayList<>();

    // Add commands to queue (schedule): They are run "asynchronously"\
    public static void schedule(Command... commands) {
        for (int i = 0; i < commands.length; i++) {
            if (commands[i].scheduled) {
                // ALREADY SCHEDULED!

                // DO NOTHING
            } else {
                currentCommands.add(commands[i]);
                commands[i].scheduled = true;
                commands[i].runOnSchedule();
            }
        }
    }

    // Call this every time in the loop so that it can run the currently queued commands
    public static void loop() {
        int offseti = 0;
        for (int i = 0; i < currentCommands.size(); i++) {
            int i2 = i + offseti;
            Command command = currentCommands.get(i2);
            command.run();
            command.runOnRun();
            if (command.isDone()) {
                command.runOnDone();
                currentCommands.remove(i2);
                command.scheduled = false;
                offseti--;
            }
        }
    }
}
