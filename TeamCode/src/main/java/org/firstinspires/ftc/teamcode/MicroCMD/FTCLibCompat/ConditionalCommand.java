package org.firstinspires.ftc.teamcode.MicroCMD.FTCLibCompat;

import org.firstinspires.ftc.teamcode.MicroCMD.Command;

import java.util.function.BooleanSupplier;

public class ConditionalCommand extends Command {
    public ConditionalCommand(Command onTrue, Command onFalse, BooleanSupplier f) {
        super(() -> {
            boolean b = f.getAsBoolean();
            if (b) {
                onTrue.schedule();
            } else {
                onFalse.schedule();
            }
        });
    }
}
