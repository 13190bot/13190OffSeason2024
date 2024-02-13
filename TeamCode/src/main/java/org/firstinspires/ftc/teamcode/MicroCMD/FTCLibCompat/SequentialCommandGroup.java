package org.firstinspires.ftc.teamcode.MicroCMD.FTCLibCompat;

import org.firstinspires.ftc.teamcode.MicroCMD.Command;

public class SequentialCommandGroup extends Command {
    public SequentialCommandGroup(Command... commands) {
        super(commands);
    }
}
