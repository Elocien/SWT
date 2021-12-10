package tourable.accommodation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import tourable.city.City;

public class AccommodationTest {

	@Test
	public void accommodationConstructorTest() {

		var name = "test_acmname";
		var city = new City("test_acmcity");
		var description = "test_acmdescription";
		var image = "testimage".getBytes();
		var price = 100.00f;
		var bedNumber = 10;
		var roomNumber = 5;
		var provision = 0.2f;
		var type = AccommodationType.SIMPLE;
		var location = AccommodationLocation.SUBURBAN;
		Accommodation accommodation = new Accommodation(name, city, description, image, price, bedNumber, roomNumber,
				provision, type, location);

		assertEquals(name, accommodation.getName());
		assertEquals(city, accommodation.getCity());
		assertEquals(description, accommodation.getDescription());
		assertEquals(image, accommodation.getImage());
		assertEquals(price, accommodation.getPrice());
		assertEquals(bedNumber, accommodation.getBedNumber());
		assertEquals(roomNumber, accommodation.getRoomNumber());
		assertEquals(provision, accommodation.getProvision());
		assertEquals(type, accommodation.getType());
		assertEquals(location, accommodation.getLocation());
	}

	@Test
	public void AccommodationSetterTest() {
		var name = "test_acmname";
		var city = new City("test_acmcity");
		var description = "test_acmdescription";
		var image = "testimage".getBytes();
		var price = 100.00f;
		var bedNumber = 10;
		var roomNumber = 5;
		var provision = 0.2f;
		var type = AccommodationType.SIMPLE;
		var location = AccommodationLocation.SUBURBAN;
		Accommodation accommodation = new Accommodation(name, city, description, image, price, bedNumber, roomNumber,
				provision, type, location);

		var newName = "new";
		var newDesc = "newDesc";
		var newImage = "newImage".getBytes();
		var newPrice = 100f;
		var newBedNumber = 5;
		var newRoomNumber = 3;
		var newProvision = 0.25f;
		accommodation.setBedNumber(newBedNumber);
		accommodation.setDescription(newDesc);
		accommodation.setImage(newImage);
		accommodation.setName(newName);
		accommodation.setPrice(newPrice);
		accommodation.setProvision(newProvision);
		accommodation.setRoomNumber(newRoomNumber);
		accommodation.setDisabled(true);

		assertEquals(newName, accommodation.getName());
		assertEquals(newDesc, accommodation.getDescription());
		assertEquals(newImage, accommodation.getImage());
		assertEquals(newPrice, accommodation.getPrice());
		assertEquals(newBedNumber, accommodation.getBedNumber());
		assertEquals(newRoomNumber, accommodation.getRoomNumber());
		assertEquals(newProvision, accommodation.getProvision());
		assertEquals(true, accommodation.isDisabled());
	}
}