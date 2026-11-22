package org.springframework.samples.petclinic.model;

import java.sql.Date;
import java.time.LocalDate;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/** Maps a calendar date to a SQL DATE without adding a time of day. */
@Converter
public class LocalDateConverter implements AttributeConverter<LocalDate, Date> {

    /** Converts an optional entity date to its SQL DATE representation. */
    @Override
    public Date convertToDatabaseColumn(LocalDate date) {
        return date == null ? null : Date.valueOf(date);
    }

    /** Restores the calendar date stored in the database. */
    @Override
    public LocalDate convertToEntityAttribute(Date date) {
        return date == null ? null : date.toLocalDate();
    }
}
