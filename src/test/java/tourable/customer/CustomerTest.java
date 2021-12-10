package tourable.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.salespointframework.time.BusinessTime;
import org.springframework.beans.factory.annotation.Autowired;

import tourable.AbstractIntegrationTest;

public class CustomerTest extends AbstractIntegrationTest {

	@Autowired
	BusinessTime businessTime;

	@Test
	public void customerConstructorTest() {
		businessTime.reset();

		var name = "Dieter Bohlen";
		var address = "Am Anger 5";
		var email = "dieterbohlen@gmail.com";
		var phone = "123";

		Customer customer = new Customer(name, address, email, phone, businessTime);

		assertEquals(name, customer.getName());
		assertEquals(address, customer.getAddress());
		assertEquals(email, customer.getEmail());
		assertEquals(phone, customer.getPhone());
	}

	public void customerSetterTest() {
		businessTime.reset();

		var name = "Dieter Bohlen";
		var address = "Am Anger 5";
		var email = "dieterbohlen@gmail.com";
		var phone = "123";

		Customer customer = new Customer(name, address, email, phone, businessTime);

		var newName = "Chris Tall";
		var newAddress = "Tallweg 99";
		var newEmail = "Talliger@web.de";
		var newPhone = "999";

		customer.setName(newName);
		customer.setAddress(newAddress);
		customer.setEmail(newEmail);
		customer.setPhone(newPhone);
		customer.setDisabled(true);

		assertEquals(newName, customer.getName());
		assertEquals(newAddress, customer.getAddress());
		assertEquals(newEmail, customer.getEmail());
		assertEquals(newPhone, customer.getPhone());
		assertEquals(true, customer.isDisabled());
	}
}
