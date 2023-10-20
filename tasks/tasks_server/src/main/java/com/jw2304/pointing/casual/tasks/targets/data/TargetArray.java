package com.jw2304.pointing.casual.tasks.targets.data;

/* control bitmasks:
*  Colour  | Array  | LED
*
*  Array:
*  XX|00|XXXX => Top
*  XX|01|XXXX => Mid
*  XX|10|XXXX => Bottom
*  XX|11|XXXX => All
*/
public enum TargetArray {
    TOP(0b00000000),
    MIDDLE(0b00010000),
    BOTTOM(0b00100000),
    ALL(0b00110000);

    public final byte mask;
    TargetArray(int mask) {
        this.mask = (byte)mask;
    }
}
