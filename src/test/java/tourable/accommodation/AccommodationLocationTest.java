package tourable.accommodation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class AccommodationLocationTest {

	@Test
	public void testNumberOfLocations() {
		assertEquals(3, AccommodationLocation.values().length);
	}

	@Test
	public void testPreviousLocations() {
		assertEquals(Optional.<AccommodationLocation>empty(), AccommodationLocation.CITY_CENTRE.prev());
		assertEquals(Optional.of(AccommodationLocation.CITY_CENTRE), AccommodationLocation.SUBURBAN.prev());
		assertEquals(Optional.of(AccommodationLocation.SUBURBAN), AccommodationLocation.COUNTRYSIDE.prev());
	}

	@Test
	public void testNextLocations() {
		assertEquals(Optional.of(AccommodationLocation.SUBURBAN), AccommodationLocation.CITY_CENTRE.next());
		assertEquals(Optional.of(AccommodationLocation.COUNTRYSIDE), AccommodationLocation.SUBURBAN.next());
		assertEquals(Optional.<AccommodationLocation>empty(), AccommodationLocation.COUNTRYSIDE.next());
	}

}
