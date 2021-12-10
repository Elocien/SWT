package tourable.travelguide;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import tourable.city.City;

public class TravelguideTest {

	@Test
	public void travelguideConstructorTest() {

		var name = "travelguide_title";
		var city = new City("testcity");
		var price = 9.99f;
		Travelguide travelguide = new Travelguide(name, city, price);

		assertEquals(name, travelguide.getName());
		assertEquals(city, travelguide.getCity());
		assertEquals(price, travelguide.getPrice());
	}

	@Test
	public void travelguideSetterTest() {

		Travelguide travelguide = new Travelguide("name", new City("city"), 1f);

		var new_name = "another_travelguide_title";
		var new_city = new City("justanothertestcity");
		var new_price = 10f;

		travelguide.setName(new_name);
		travelguide.setCity(new_city);
		travelguide.setPrice(new_price);

		assertEquals(new_name, travelguide.getName());
		assertEquals(new_city, travelguide.getCity());
		assertEquals(new_price, travelguide.getPrice());
	}

}
