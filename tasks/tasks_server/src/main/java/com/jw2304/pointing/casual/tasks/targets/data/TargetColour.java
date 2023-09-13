package com.jw2304.pointing.casual.tasks.targets.data;

/* control bitmasks:
    Colour  | Array  | LED

    Colour:
    00|XX|XXXX  => Single LED (White?)
    01|XX|XXXX  => Green target, rest red
    10|XX|XXXX  => Red target, rest blue
    11|XX|XXXX  => Blue target, rest green

    Ideally manager app will just need to send this byte to identify the target appearance
*/
public enum TargetColour {
    
}
