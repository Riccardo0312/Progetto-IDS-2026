package it.unicam.cs.ids.hackhub.config;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CurrentDateProvider {

	private static final DateTimeFormatter CONFIGURED_DATE_FORMATTER =
			DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);

	private final Clock systemClock;
	private final LocalDate fixedCurrentDate;

	public CurrentDateProvider(@Value("${hackhub.current-date:}") String configuredCurrentDate) {
		this.systemClock = Clock.systemDefaultZone();
		this.fixedCurrentDate = parseConfiguredDate(configuredCurrentDate);
	}

	public LocalDate today() {
		if (fixedCurrentDate != null) {
			return fixedCurrentDate;
		}
		return LocalDate.now(systemClock);
	}

	private LocalDate parseConfiguredDate(String configuredCurrentDate) {
		if (configuredCurrentDate == null || configuredCurrentDate.isBlank()) {
			return null;
		}
		try {
			return LocalDate.parse(configuredCurrentDate.strip(), CONFIGURED_DATE_FORMATTER);
		} catch (DateTimeParseException exception) {
			throw new IllegalStateException(
					"hackhub.current-date must use format dd-MM-yyyy", exception);
		}
	}
}
