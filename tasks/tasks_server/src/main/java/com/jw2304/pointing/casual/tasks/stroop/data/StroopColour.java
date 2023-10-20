package com.jw2304.pointing.casual.tasks.stroop.data;

public enum StroopColour {
    RED("red"),
    GREEN("dark-green"),
    BLUE("dark-blue"),
    YELLOW("yellow"),
    PINK("pink"),
    ORANGE("dark-orange");

    public final String cssColour;
    StroopColour(String css) {
        cssColour = css;
    }
}