package tourable.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.salespointframework.useraccount.UserAccount;

public class UserTest {

	@Test
	public void userConstructorTest() {

		var firstname = "Hans";
		var lastname = "Wurst";
		var address = "Bsp Weg 5";
		var phone = "123456";
		var salary = 123d;
		var userAccount = mock(UserAccount.class);

		User user = new User(userAccount, firstname, lastname, address, phone, salary);

		assertEquals(firstname, user.getFirstname());
		assertEquals(lastname, user.getLastname());
		assertEquals(address, user.getAddress());
		assertEquals(phone, user.getPhone());
		assertEquals(salary, user.getSalary());
	}

	@Test
	public void userSetterTest() {

		var firstname = "Hans";
		var lastname = "Wurst";
		var address = "Bsp Weg 5";
		var phone = "123456";
		var salary = 123d;
		var userAccount = mock(UserAccount.class);

		User user = new User(userAccount, firstname, lastname, address, phone, salary);

		var newFirstname = "Holger";
		var newLastname = "Wurst";
		var newAddress = "Bsp Weg 4";
		var newPhone = "123478";
		var newSalary = 123d;

		user.setFirstname(newFirstname);
		user.setLastname(newLastname);
		user.setAddress(newAddress);
		user.setPhone(newPhone);
		user.setSalary(newSalary);

		assertEquals(newFirstname, user.getFirstname());
		assertEquals(newLastname, user.getLastname());
		assertEquals(newAddress, user.getAddress());
		assertEquals(newPhone, user.getPhone());
		assertEquals(newSalary, user.getSalary());
	}
}
