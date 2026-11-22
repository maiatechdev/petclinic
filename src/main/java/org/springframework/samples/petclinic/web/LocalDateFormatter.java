package org.springframework.samples.petclinic.web;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.util.Locale;

import org.springframework.format.Formatter;

/** Retains the pet and visit forms' slash-separated calendar dates. */
public class LocalDateFormatter implements Formatter<LocalDate> {

    // Allow single-digit months and days while rejecting impossible calendar dates.
    private static final DateTimeFormatter FORMAT = new DateTimeFormatterBuilder()
            .parseLenient().appendPattern("uuuu/MM/dd").toFormatter(Locale.ROOT)
            .withResolverStyle(ResolverStyle.STRICT);

    /** Reads a form date without rolling invalid days into another date. */
    @Override
    public LocalDate parse(String text, Locale locale) {
        return LocalDate.parse(text, FORMAT);
    }

    /** Prints a zero-padded date for the existing date picker. */
    @Override
    public String print(LocalDate date, Locale locale) {
        return FORMAT.format(date);
    }
}
