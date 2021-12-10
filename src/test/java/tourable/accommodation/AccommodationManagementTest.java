package tourable.accommodation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import tourable.AbstractIntegrationTest;
import tourable.accounting.InvoiceRepository;

public class AccommodationManagementTest extends AbstractIntegrationTest {

	@Autowired
	AccommodationManagement management;

	@Autowired
	AccommodationRepository accommodations;

	@Autowired
	InvoiceRepository invoices;

	@Test
	public void testUpdateAccommodationNull() {
		long numAccommodations = accommodations.count();
		management.updateAccommodation(-1l,
				new AccommodationDataForm(null, null, null, null, null, null, null, null, null, null));
		assertEquals(numAccommodations, accommodations.count());
	}

	@Test
	public void testDeleteAccommodationNull() {
		long numAccommodations = accommodations.stream().filter(Accommodation::isDisabled).count();
		management.deleteAccommodation(-1l);
		assertEquals(numAccommodations, accommodations.stream().filter(Accommodation::isDisabled).count());
	}

}
