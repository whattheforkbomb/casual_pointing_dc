package com.jw2304.pointing.casual.tasks.stroop.data;

public enum StroopColour {
    RED("red"),
    GREEN("green"),
    BLUE("royalblue"),
    PURPLE("purple"),
    BROWN("saddlebrown");

    public final String cssColour;
    StroopColour(String css) {
        cssColour = css;
    }
}