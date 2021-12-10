package tourable.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.salespointframework.time.BusinessTime;
import org.springframework.beans.factory.annotation.Autowired;

import tourable.AbstractIntegrationTest;

public class CustomerDataFormTest extends AbstractIntegrationTest {

	@Autowired
	BusinessTime businessTime;

	@Test
	public void testCustomerDataForm() {
		businessTime.reset();
		var name = "Heiko Hacke";
		var address = "Dietrichgasse 1";
		var email = "heikohacke@gmail.com";
		var phone = "999";

		CustomerDataForm customer = new CustomerDataForm(name, address, email, phone);

		assertEquals(name, customer.getName());
		assertEquals(address, customer.getAddress());
		assertEquals(email, customer.getEmail());
		assertEquals(phone, customer.getPhone());
	}
}
