package tourable.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.salespointframework.time.BusinessTime;
import org.springframework.beans.factory.annotation.Autowired;

import tourable.AbstractIntegrationTest;
import tourable.accounting.InvoiceRepository;

public class UserManagementTest extends AbstractIntegrationTest {

	@Autowired
	BusinessTime businessTime;

	@Autowired
	InvoiceRepository invoiceRepository;

	@Autowired
	UserRepository users;

	@Autowired
	UserManagement userManagement;

	@Test
	public void paySalaryTest() {
		businessTime.reset();
		businessTime.forward(Duration.between(LocalDateTime.now(), LocalDateTime.of(2020, 2, 1, 9, 0, 0)));
		double salary = -users.stream().filter(u -> u.getUserAccount().isEnabled()).mapToDouble(User::getSalary).sum();

		assertEquals(salary, userManagement.paySalary());
		// try second salary payment
		assertEquals(0d, userManagement.paySalary());

		businessTime.reset();
	}

	@Test
	public void testCreateUserFormNull() {

		UserDataForm form = null;
		boolean thrown = false;

		try {
			userManagement.createUser(form);
		} catch (NullPointerException e) {
			thrown = true;
		}
		assertTrue(thrown);
	}

	@Test
	public void testUpdateUser() {
		Optional<User> user = users.stream().findAny();
		UserDataForm newForm = new UserDataForm("Hans", "Holger", "123", "Bsp Weg 1", "1234567", "hans@web.de", 123d);

		if (user.isEmpty()) {
			fail();
		}

		userManagement.updateUser(newForm, user.get().getId());

		assertEquals(user.get().getFirstname(), newForm.getFirstname());
		assertEquals(user.get().getLastname(), newForm.getLastname());
		assertEquals(user.get().getAddress(), newForm.getAddress());
		assertEquals(user.get().getPhone(), newForm.getPhone());
		assertEquals(user.get().getSalary(), newForm.getSalary());
		assertEquals(user.get().getUserAccount().getEmail(), newForm.getEmail());
	}

	@Test
	public void testDeleteUser() {

		Optional<User> optUser = users.stream().findAny();

		if (!optUser.isPresent()) {
			fail();
		}

		User user = optUser.get();
		userManagement.deleteUser(user.getId());

	}

}
