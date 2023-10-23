package com.jw2304.pointing.casual.tasks.stroop.data;

import org.springframework.web.socket.TextMessage;

public class Stroop {
    public final String word;
    public final StroopColour colour;

    public int startDelayMilliseconds = 250;
    public int durationMilliseconds = 3750;

    public Stroop(String word, StroopColour colour) {
        this.word = word;
        this.colour = colour;
    }

    public Stroop(String word, StroopColour colour, int startDelayMilliseconds, int durationMilliseconds) {
        this.word = word;
        this.colour = colour;
        this.startDelayMilliseconds = startDelayMilliseconds;
        this.durationMilliseconds = durationMilliseconds;
    }

    public TextMessage getMessage() {
        return new TextMessage("{\"word\": \"%s\", \"colour\": \"%s\"}".formatted(word, colour.cssColour));
    }

    public static TextMessage getResetMessage() {
        return new TextMessage("{\"word\": \"\", \"colour\": \"black\"}");
    }
}
