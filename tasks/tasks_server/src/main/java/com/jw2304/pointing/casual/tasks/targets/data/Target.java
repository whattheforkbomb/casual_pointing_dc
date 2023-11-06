package com.jw2304.pointing.casual.tasks.targets.data;

public class Target {
    public final int id;
    public final int subTarget;
    public final int col;
    public final int row;

    public int startDelayMilliseconds = 3000;
    public int durationMilliseconds = 3000;

    public Target(int id, int subTarget) {
        this.id = id;
        this.subTarget = subTarget;
        col = id / 3;
        row = id % 3;
    }

    public Target(int id, int subTarget, int startDelayMilliseconds, int durationMilliseconds) {
        this.id = id;
        this.subTarget = subTarget;
        col = id / 3;
        row = id % 3;
        this.startDelayMilliseconds = startDelayMilliseconds;
        this.durationMilliseconds = durationMilliseconds;
    }
    
    public byte getCommandByte(TargetColour colour) {
        TargetArray array = TargetArray.values()[row];

        TargetLED led = subTarget == -1 ? TargetLED.ALL : TargetLED.values()[subTarget+1];

        return (byte) (colour.mask | array.mask | led.mask);
    }

    public byte getToneCommandByte() {
        TargetArray array = TargetArray.values()[row];
        return (byte) (array.mask | TONE);
    }

    public static byte OFF = 0b00000000;
    public static byte TONE = 0b00001111;

    public static final Target identifyColumn(int col) {
        return new Target((col*3)+1, col);
    }

    @Override
    public String toString() {
        return "Target[col: %d, row: %d, sub: %d, id: %d, delayMS: %d, durationMS: %d]"
            .formatted(col, row, subTarget, id, startDelayMilliseconds, durationMilliseconds);
    }
}