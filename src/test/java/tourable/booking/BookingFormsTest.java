package tourable.booking;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import tourable.accommodation.AccommodationLocation;
import tourable.accommodation.AccommodationType;
import tourable.booking.forms.AccommodationSearchForm;
import tourable.booking.forms.CustomerSearchForm;

public class BookingFormsTest {
	
	@Test
	public void accommodationSearchFormTest() {
		
		var city = "test_City";
		var type = AccommodationType.STANDARD;
		var location = AccommodationLocation.SUBURBAN;
		var dateRange = "2019-12-10 - 2020-01-19";
		
		AccommodationSearchForm asf = new AccommodationSearchForm(city, type, location, dateRange);
		
		assertEquals(city, asf.getCity());
		assertEquals(type, asf.getType());
		assertEquals(location, asf.getLocation());
		assertEquals(dateRange, asf.getDateRange());
	}
	
	@Test
	public void customerSearchFormTest() {
		
		var name = "Jana";
		
		CustomerSearchForm csf = new CustomerSearchForm(name);
		
		assertEquals(name, csf.getName());
		
	}

}
