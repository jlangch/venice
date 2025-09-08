package com.github.jlangch.venice.impl.util.time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;


/**
 * Unix timestamp. Seconds since Jan 01 1970 (UTC).
 *
 * @see https://www.unixtimestamp.com/
 */
public class UnixTimestamp {

    public UnixTimestamp() {
        this.timestamp = 0;
    }

    public UnixTimestamp(final long secondsSinceEpoch) {
        this.timestamp = secondsSinceEpoch;
    }

    public static UnixTimestamp of(final LocalDateTime ts) {
        final ZoneId zoneId = ZoneId.systemDefault();
        return new UnixTimestamp(ts.atZone(zoneId).toEpochSecond());
    }

    public static UnixTimestamp of(final ZonedDateTime ts) {
        return new UnixTimestamp(ts.toEpochSecond());
    }

    public static UnixTimestamp of(final LocalDate ts) {
        final ZoneId zoneId = ZoneId.systemDefault();
        return new UnixTimestamp(ts.atStartOfDay(zoneId).toInstant().getEpochSecond());
    }

    public static UnixTimestamp of(final Date ts) {
        return new UnixTimestamp(ts.toInstant().getEpochSecond());
    }

    public long getTimestamp() {
        return timestamp;
    }


    @Override
    public String toString() {
        return String.valueOf(timestamp);
    }


    private final long timestamp;
}
