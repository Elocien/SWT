package tourable.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.salespointframework.time.BusinessTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.ExtendedModelMap;

import tourable.AbstractIntegrationTest;
import tourable.booking.BookingManagement;

@AutoConfigureMockMvc
public class CustomerControllerTest extends AbstractIntegrationTest {

	@Autowired
	CustomerController controller;

	@Autowired
	CustomerManagement management;

	@Autowired
	MockMvc mockMvc;

	@Autowired
	BusinessTime businessTime;

	@Autowired
	BookingManagement bookingManagement;

	@Autowired
	CustomerRepository customerRepo;

	@Test
	@WithMockUser(roles = "BOSS")
	public void testCustomerOverview() {
		ExtendedModelMap model = new ExtendedModelMap();

		controller.customers(model);
		assertThat(model.get("customerList")).isNotNull();
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testCustomerDetailsNull() {
		ExtendedModelMap model = new ExtendedModelMap();

		String page = controller.customerDetails(model, null);
		assertThat(page).isEqualTo("redirect:../customers");
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testCustomerDetailsNonExistent() {
		ExtendedModelMap model = new ExtendedModelMap();

		String page = controller.customerDetails(model, 0l);
		assertThat(page).isEqualTo("redirect:../customers");
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testCustomerDetails() throws Exception {

		Optional<Customer> optCustomer = customerRepo.stream()
				.filter(c -> !bookingManagement.hasOpenBookingsForCustomer(c)).findAny();
		Optional<Customer> optSecond = customerRepo.stream()
				.filter(c -> bookingManagement.hasOpenBookingsForCustomer(c)).findAny();
		if (optCustomer.isEmpty() || optSecond.isEmpty()) {
			fail();
		}

		long id = optCustomer.get().getId();

		mockMvc.perform(get("/customers/details").param("id", String.valueOf(id))).andExpect(status().isOk())
				.andExpect(model().attribute("customer", is(notNullValue())))
				.andExpect(model().attribute("allBookings", is(notNullValue())));

		id = optSecond.get().getId();

		mockMvc.perform(get("/customers/details").param("id", String.valueOf(id))).andExpect(status().isOk())
				.andExpect(model().attribute("customer", is(notNullValue())))
				.andExpect(model().attribute("allBookings", is(notNullValue())));
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testCustomerCreationPage() {
		ExtendedModelMap model = new ExtendedModelMap();

		String page = controller.createCustomer(model, new CustomerDataForm(null, null, null, null));
		assertThat(page).isEqualTo("customercreate");
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testCustomerCreateValidation() throws Exception {

		mockMvc.perform(post("/customers/create").param("name", "new_name").param("address", "new_address")
				.param("email", "newEmail@email.de").param("phone", "123")).andExpect(status().is3xxRedirection());

		Optional<Customer> newCustomer = customerRepo.stream().filter(customer -> customer.getName().equals("new_name"))
				.findAny();
		assertThat(newCustomer.isPresent());

		mockMvc.perform(post("/customers/create").param("name", "").param("address", "new_address")
				.param("email", "newEmail@email.de").param("phone", "123")).andExpect(status().isOk());

	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testEditCustomerNonExistent() {
		ExtendedModelMap model = new ExtendedModelMap();

		String page = controller.editCustomer(model, 0l);
		assertThat(page).isEqualTo("redirect:../customers");
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testEditCustomerNull() {
		ExtendedModelMap model = new ExtendedModelMap();

		String page = controller.editCustomer(model, null);
		assertThat(page).isEqualTo("redirect:../customers");
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testEditCustomerPage() throws Exception {

		Optional<Customer> optCustomer = customerRepo.stream().findAny();

		if (optCustomer.isEmpty()) {
			fail();
		}

		Customer customer = optCustomer.get();
		long id = customer.getId();

		mockMvc.perform(get("/customers/edit").param("id", String.valueOf(id))).andExpect(status().isOk())
				.andExpect(model().attribute("form", is(notNullValue())))
				.andExpect(model().attribute("form", is(notNullValue())));
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testEditCustomer() throws Exception {

		Optional<Customer> optCustomer = customerRepo.stream().findAny();

		if (optCustomer.isEmpty()) {
			fail();
		}

		Customer customer = optCustomer.get();
		long id = customer.getId();

		mockMvc.perform(post("/customers/edit").param("name", "new_name").param("address", "new_address")
				.param("email", "email@email.com").param("phone", "123").param("id", String.valueOf(id)))
				.andExpect(status().is3xxRedirection());

		Customer updatedCustomer = customerRepo.findById(id).get();
		assertEquals("new_name", updatedCustomer.getName());
		assertEquals("new_address", updatedCustomer.getAddress());
		assertEquals("email@email.com", updatedCustomer.getEmail());
		assertEquals("123", updatedCustomer.getPhone());

		mockMvc.perform(post("/customers/edit").param("name", "").param("address", "new_address")
				.param("email", "email@email.com").param("phone", "123").param("id", String.valueOf(id)))
				.andExpect(status().isOk());

		Long otherId = customerRepo.save(new Customer("name", "adress", "em@i.l", "123", businessTime)).getId();

		mockMvc.perform(post("/customers/edit").param("name", "").param("address", "new_address")
				.param("email", "email@email.com").param("phone", "123").param("id", String.valueOf(otherId)))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testCustomerDeletion() throws Exception {

		Optional<Customer> optCustomer = customerRepo.stream()
				.filter(c -> !bookingManagement.hasOpenBookingsForCustomer(c)).findAny();
		if (!optCustomer.isPresent()) {
			fail();
		}

		long id = optCustomer.get().getId();

		mockMvc.perform(post("/customers/delete").param("id", "-1")).andExpect(status().is3xxRedirection());

		mockMvc.perform(post("/customers/delete").param("id", String.valueOf(id)))
				.andExpect(status().is3xxRedirection());

		Optional<Customer> deleted = customerRepo.findByIdAndDisabledFalse(id);
		assertThat(deleted.isEmpty());
	}
}
