package tourable.travelguide;

import java.util.List;
import java.util.Objects;

import org.salespointframework.core.DataInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class TravelguideDataInitializer implements DataInitializer {

	private static final Logger logger = LoggerFactory.getLogger(TravelguideDataInitializer.class);

	private final TravelguideManagement travelguideManagement;

	public TravelguideDataInitializer(TravelguideManagement travelguideManagement) {
		this.travelguideManagement = Objects.requireNonNull(travelguideManagement);
	}

	@Override
	public void initialize() {

		logger.info("creating default travelguides");

		List.of(new TravelguideDataForm("New York: ein kleiner Reiseführer", "New York", 3.99d),
				new TravelguideDataForm("New York: ein größerer Reiseführer", "New York", 5.99d),
				new TravelguideDataForm("Die schönsten Orte in Berlin", "Berlin", 5d),
				new TravelguideDataForm("Dresden ist auch toll", "Dresden", 2.5d),
				new TravelguideDataForm("Goedemorgen aus Amsterdam", "Amsterdam", 4.99d),
				new TravelguideDataForm("Wien hautnah", "Wien", 9.99d),
				new TravelguideDataForm("Die beliebtesten Sehenswürdigkeiten in London", "London", 8.99d),
				new TravelguideDataForm("Prag mal anders", "Prag", 2.99d),
				new TravelguideDataForm("Bern: ein schöner Reiseführer", "Bern", 4.99d),
				new TravelguideDataForm("Verträumt durch Paris reisen", "Paris", 9.99d),
				new TravelguideDataForm("Ostseereiseführer", "Rostock", 7.5d))
				.forEach(travelguideManagement::createTravelguide);

	}

}
