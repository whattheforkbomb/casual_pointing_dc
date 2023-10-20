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
    
    public byte getCommandByte(TargetColour colour, TargetType targetType) {
        TargetArray array = TargetArray.values()[row];

        TargetLED led = targetType == TargetType.CLUSTER ? TargetLED.ALL : TargetLED.values()[subTarget+1];

        return (byte) (colour.mask | array.mask | led.mask);
    }

}