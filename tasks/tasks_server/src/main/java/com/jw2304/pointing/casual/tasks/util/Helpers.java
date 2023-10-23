package com.jw2304.pointing.casual.tasks.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Helpers {
    public static String getCurrentDateTimeString() {
        return DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now(ZoneId.of("UTC"))).replace(":", ".").substring(0, 25);
    }
}
