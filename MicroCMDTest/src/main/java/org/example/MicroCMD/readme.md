# MicroCMD: An easy, fast command scheduler for FTC

## How to use:
- Just copy all the files in [MicroCMD](./) to inside your TeamCode or anywhere you can access

## Features:
- CommandScheduler
- Command
- FTCLib-like API, except much easier
  - You only have to use [Command.java](Command.java)
  - If you are moving from an FTCLib project, use the [FTCLibCompat](FTCLibCompat) package
- Lightweight, simple
  - Only 2 files: [Command.java](Command.java) and [CommandScheduler.java](CommandScheduler.java)
  - Each loop: `CommandScheduler.loop();`
  - Synthesizing commands:
    - Create and schedule a command: `new Command(...).schedule();`
    - Create command from Runnable: `new Command(() -> {...})`
    - Create command from list of commands:
      - Will be run synchronously and chronologically
      - Using commands:
      - ```java
        new Command(
          new Command(...),
          new Command(...),
          new Command(...),
          ...
        )
      - Alternatively, using Runnables:
      - ```java
        new Command(
          () -> {...},
          () -> {...},
          () -> {...},
          ...
        )
    - Create command that will take `ms` milliseconds to finish:
      - Empty command:
      - `new Command(ms)`
      - Execute command and wait til `ms` is up
      - `new Command(ms, new Command(...))`
      - Execute runnable and wait til `ms` is up
      - `new Command(ms, () -> {...})`
      - Execute list of commands and wait til `ms` is up
      - ```java
        new Command(
          ms,
          new Command(...),
          new Command(...),
          new Command(...),
          ...
        )
      - Alternatively, using Runnables:
      - ```java
        new Command(
          ms,
          () -> {...},
          () -> {...},
          () -> {...},
          ...
        )
  - Functions for commands:
    - Check if command is done: `command.isDone()`
    - Reset command (must be done before every recycled usage): `command.reset();`
    - Schedule command (be sure to reset command before): `command.schedule();`
    - Make command finish: `command.done = true;`
    - Make command not finish: `command.done = false;`
    - Make command based on time (default): `command.done = null;`
    - Get command length (ns): `command.lengthns`
    - Set command length (ns): `command.lengthns = ns;`
    - Get command start time (ns): `command.startns`
    - Set command start time (ns): `command.startns = ns;`
    - Get command end time (ns): `command.endns`
    - Set command end time (ns): `command.endns = ns;`
    - Get command internal runnable: `command.function`
    - Set command internal runnable: `command.function = function;`


License: MIT
FTC Team 8872/13190, Kosei Tsukamoto