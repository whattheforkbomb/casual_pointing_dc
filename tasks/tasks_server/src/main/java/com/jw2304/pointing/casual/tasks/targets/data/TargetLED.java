package com.jw2304.pointing.casual.tasks.targets.data;

/* control bitmasks:
    Colour  | Array  | LED
    
    Placement: Can compress location into 4 bits
    00|00|0000  => RESET
    XX|XX|0001  => Top Left
    XX|XX|0010  => Top Centre
    XX|XX|0011  => Top Right
    XX|XX|0100  => Mid Left
    XX|XX|0101  => Mid Centre
    XX|XX|0110  => Mid Right
    XX|XX|0111  => Bottom Left
    XX|XX|1000  => Bottom Centre
    XX|XX|1001  => Bottom Right
    XX|XX|1010  => ALL

    Ideally manager app will just need to send this byte to identify the target appearance
*/
public enum TargetLED {
    
}
