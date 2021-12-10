package tourable.city;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class CityTest {

	@Test
	public void cityConstructorTest() {

		var name = "city_name";
		City city = new City(name);

		assertEquals(name, city.getName());
	}

	@Test
	public void citySetterTest() {

		City city = new City("random_name");

		var new_name = "another_city_name";
		city.setName(new_name);
		city.setDisabled(true);

		assertEquals(new_name, city.getName());
		assertEquals(true, city.isDisabled());
	}

}
