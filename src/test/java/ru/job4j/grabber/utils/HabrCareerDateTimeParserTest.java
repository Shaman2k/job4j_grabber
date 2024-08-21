package ru.job4j.grabber.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HabrCareerDateTimeParserTest {
    @Test
    void whenParseValidString() {
        String date = "2024-08-14T13:09:04+03:00";
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        LocalDateTime result = parser.parse(date);

        assertThat(result).isEqualTo("2024-08-14T13:09:04");
    }

    @Test
    void whenParseInvalidStringThenThrowException() {
        String date = "2024-08-2110:20:04+03:00";
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();

        assertThrows(DateTimeParseException.class, () -> parser.parse(date));
    }
}