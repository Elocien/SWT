package tourable.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.salespointframework.time.BusinessTime;
import org.springframework.beans.factory.annotation.Autowired;

import tourable.AbstractIntegrationTest;
import tourable.booking.BookingManagement;

public class CustomerManagementTest extends AbstractIntegrationTest {

	@Autowired
	BusinessTime businessTime;

	@Autowired
	CustomerManagement customerManagement;

	@Autowired
	BookingManagement bookingManagement;

	@Autowired
	CustomerRepository customerRepo;

	@Test
	public void testCreateCustomer() {

		CustomerDataForm form = new CustomerDataForm("Nicole Nicolinski", "Am Weier 5", "nicolinski@gmail.com", "123");

		assertNotNull(customerManagement.createCustomer(form));
	}

	@Test
	public void testCreateCustomerFormNull() {

		CustomerDataForm form = null;
		boolean thrown = false;

		try {
			customerManagement.createCustomer(form);
		} catch (NullPointerException e) {
			thrown = true;
		}

		assertTrue(thrown);
	}

	@Test
	public void testUpdateCustomerNull() {
		long numCustomers = customerRepo.count();
		customerManagement.updateCustomer(null, -1);
		assertEquals(numCustomers, customerRepo.count());
	}

	@Test
	public void testUpdateCustomer() {
		Optional<Customer> optCustomer = customerRepo.stream().findAny();
		CustomerDataForm newForm = new CustomerDataForm("Nicole Nicolinski", "Am Weier 5", "nicolinski@gmail.com",
				"123");

		if (optCustomer.isEmpty() || !optCustomer.isPresent()) {
			fail();
		}

		customerManagement.updateCustomer(newForm, optCustomer.get().getId());

		assertEquals(optCustomer.get().getName(), newForm.getName());
		assertEquals(optCustomer.get().getAddress(), newForm.getAddress());
		assertEquals(optCustomer.get().getEmail(), newForm.getEmail());
		assertEquals(optCustomer.get().getPhone(), newForm.getPhone());

	}

	@Test
	public void testDeleteCustomer() {

		Optional<Customer> optCustomer = customerRepo.stream().findAny();

		if (!optCustomer.isPresent()) {
			fail();
		}

		Customer customer = optCustomer.get();
		customerManagement.deleteCustomer(customer.getId());

		// TODO: crude fix, needs proper handling
		if (bookingManagement.hasOpenBookingsForCustomer(customer)) {
			assertFalse(customer.isDisabled());
		} else {
			assertTrue(customer.isDisabled());
		}
	}
}
