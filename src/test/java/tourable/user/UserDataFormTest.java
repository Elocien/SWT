package tourable.user;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import tourable.AbstractIntegrationTest;

public class UserDataFormTest extends AbstractIntegrationTest {

	@Test
	public void testUserDataForm() {
		var firstname = "Heinrich";
		var lastname = "Der 1.";
		var password = "123";
		var address = "Bsp Weg 187";
		var email = "heinrich@web.de";
		var phone = "1234567";
		var salary = 123d;

		UserDataForm user = new UserDataForm(firstname, lastname, password, address, phone, email,
				salary);
		assertEquals(firstname, user.getFirstname());
		assertEquals(lastname, user.getLastname());
		assertEquals(password, user.getPassword());
		assertEquals(address, user.getAddress());
		assertEquals(phone, user.getPhone());
		assertEquals(email, user.getEmail());
		assertEquals(salary, user.getSalary());
	}
}
