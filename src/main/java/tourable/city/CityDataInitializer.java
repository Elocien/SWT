package tourable.city;

import java.util.List;
import java.util.Objects;

import org.salespointframework.core.DataInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class CityDataInitializer implements DataInitializer {

	private static final Logger logger = LoggerFactory.getLogger(CityDataInitializer.class);

	private final CityManagement cityManagement;

	public CityDataInitializer(CityManagement cityManagement) {
		this.cityManagement = Objects.requireNonNull(cityManagement);
	}

	@Override
	public void initialize() {

		logger.info("creating default cities");

		List.of(new CityDataForm("Berlin"), new CityDataForm("Dresden"), new CityDataForm("New York"),
				new CityDataForm("London"), new CityDataForm("Paris"), new CityDataForm("Wien"),
				new CityDataForm("Prag"), new CityDataForm("Amsterdam"), new CityDataForm("Bern"),
				new CityDataForm("Rostock"), new CityDataForm("Leipzig")).forEach(cityManagement::createCity);

	}

}
