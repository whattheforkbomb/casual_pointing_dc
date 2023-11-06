package com.jw2304.pointing.casual.tasks.targets.data;

/* control bitmasks:
*  Colour  | Array  | LED
*   
*  Placement: Can compress location into 4 bits
*  00|00|0000  => RESET
*  XX|XX|0001  => Top Left
*  XX|XX|0010  => Top Centre
*  XX|XX|0011  => Top Right
*  XX|XX|0100  => Mid Left
*  XX|XX|0101  => Mid Centre
*  XX|XX|0110  => Mid Right
*  XX|XX|0111  => Bottom Left
*  XX|XX|1000  => Bottom Centre
*  XX|XX|1001  => Bottom Right
*  XX|XX|1010  => ALL
*/
public enum TargetLED {
    RESET(0b00000000),
    TOP_LEFT(0b00000001),
    TOP_CENTRE(0b00000010),
    TOP_RIGHT(0b00000011),
    MIDDLE_LEFT(0b00000100),
    MIDDLE_CENTRE(0b00000101),
    MIDDLE_RIGHT(0b00000110),
    BOTTOM_LEFT(0b00000111),
    BOTTOM_CENTRE(0b00001000),
    BOTTOM_RIGHT(0b00001001),
    ALL(0b00001010),
    TONE(0b00001111);

    public final byte mask;
    TargetLED(int mask) {
        this.mask = (byte)mask;
    }
}
