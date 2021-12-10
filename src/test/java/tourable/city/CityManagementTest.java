package tourable.city;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import tourable.AbstractIntegrationTest;

public class CityManagementTest extends AbstractIntegrationTest {

	@Autowired
	CityManagement management;

	@Autowired
	CityRepository cities;

	@Test
	public void testUpdateCityNull() {
		long numCities = cities.count();
		management.updateCity(-1, null);
		assertEquals(numCities, cities.count());
	}

	@Test
	public void testDeleteCityNull() {
		long numCities = cities.stream().filter(City::isDisabled).count();
		management.deleteCity(-1);
		assertEquals(numCities, cities.stream().filter(City::isDisabled).count());
	}

	@Test
	public void deleteCityWithAndWithoutReferences() {
		long numCities = cities.stream().filter(City::isDisabled).count();

		long id = cities.findByNameAndDisabledFalse("Dresden").get().getId();
		management.deleteCity(id);
		assertEquals(numCities, cities.stream().filter(City::isDisabled).count());

		id = cities.findByNameAndDisabledFalse("Prag").get().getId();
		management.deleteCity(id);
		assertEquals(numCities, cities.stream().filter(City::isDisabled).count());

		id = cities.findByNameAndDisabledFalse("Leipzig").get().getId();
		management.deleteCity(id);
	}

}
