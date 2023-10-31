package com.jw2304.pointing.casual.tasks.stroop.data;

public enum StroopColour {
    RED("red"),
    GREEN("darkgreen"),
    BLUE("darkblue"),
    YELLOW("yellow"),
    PINK("pink"),
    ORANGE("darkorange");

    public final String cssColour;
    StroopColour(String css) {
        cssColour = css;
    }
}