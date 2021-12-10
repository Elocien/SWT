package tourable.travelguide;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import tourable.AbstractIntegrationTest;
import tourable.accounting.InvoiceRepository;

public class TravelguideManagementTest extends AbstractIntegrationTest {

	@Autowired
	TravelguideManagement management;

	@Autowired
	TravelguideRepository travelguides;

	@Autowired
	InvoiceRepository invoices;

	@Test
	public void testBuyTravelguideNull() {
		long numInvoices = invoices.count();
		management.buyTravelguide(-1);
		assertEquals(numInvoices, invoices.count());
	}

	@Test
	public void testBuyTravelguide() {
		long numInvoices = invoices.count();
		Optional<Travelguide> optTravelguide = travelguides.stream().findAny();
		if (optTravelguide.isEmpty()) {
			fail();
		}

		long id = optTravelguide.get().getId();
		management.buyTravelguide(id);
		assertEquals(numInvoices + 1, invoices.count());
	}

	@Test
	public void testUpdateTravelguideNull() {
		long numTravelguides = travelguides.count();
		management.updateTravelguide(-1, null);
		assertEquals(numTravelguides, travelguides.count());
	}

}
