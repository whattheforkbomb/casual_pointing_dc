package com.jw2304.pointing.casual.tasks.targets.data;

public class ResetTarget extends Target {

    public ResetTarget(int col) {
        super((col*3)+1, -1);
    }

    @Override
    public byte getCommandByte(TargetColour colour) {
        return 0b00000000;
    }

}