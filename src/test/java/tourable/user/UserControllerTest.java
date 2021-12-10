package tourable.user;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.ExtendedModelMap;

import tourable.AbstractIntegrationTest;

@AutoConfigureMockMvc
public class UserControllerTest extends AbstractIntegrationTest {

	@Autowired
	UserController controller;

	@Autowired
	UserRepository users;

	@Autowired
	MockMvc mockMvc;

	@Autowired
	UserManagement management;

	@Test
	@WithMockUser(roles = "BOSS")
	public void testUserOverview() {
		ExtendedModelMap model = new ExtendedModelMap();

		controller.users(model);
		assertThat(model.get("userList")).isNotNull();
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testUserDetailsNull() {
		ExtendedModelMap model = new ExtendedModelMap();

		String page = controller.userDetails(model, null);
		assertThat(page).isEqualTo("redirect:../users");
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testUserDetailsNonExistent() {
		ExtendedModelMap model = new ExtendedModelMap();

		String page = controller.userDetails(model, 0l);
		assertThat(page).isEqualTo("redirect:../users");
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testUserDetails() throws Exception {

		Optional<User> optUser = users.stream().findAny();
		if (optUser.isEmpty()) {
			fail();
		}

		long id = optUser.get().getId();

		mockMvc.perform(get("/users/details").param("id", String.valueOf(id))).andExpect(status().isOk())
				.andExpect(model().attribute("user", is(notNullValue())));
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testUserCreationPage() {
		ExtendedModelMap model = new ExtendedModelMap();

		String page = controller.createUser(model, new UserDataForm(null, null, null, null, null, null, null));
		assertThat(page).isEqualTo("usercreate");
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testUserCreateValidation() throws Exception {

		mockMvc.perform(post("/users/create").param("firstname", "new_firstname").param("lastname", "new_lastname")
				.param("address", "new_address").param("phone", "123").param("salary", "1337.00")
				.param("email", "testmail@usertest.de").param("password", "123445rtEwq"))
				.andExpect(status().is3xxRedirection());

		Optional<User> newUser = users.stream().filter(user -> user.getFirstname().equals("new_firstname")).findAny();
		assertThat(newUser.isPresent());

		mockMvc.perform(post("/users/create").param("firstname", "new_firstname").param("lastname", "new_lastname")
				.param("address", "new_address").param("phone", "123").param("salary", "1337.00")
				.param("email", "testmail@usertest.de").param("password", "")).andExpect(status().isOk());

	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testEditUserNonExistent() {
		ExtendedModelMap model = new ExtendedModelMap();

		String page = controller.editUser((long) 01, new UserDataForm(null, null, null, null, null, null, null), model);
		assertThat(page).isEqualTo("redirect:../users");
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testEditUserNull() {
		ExtendedModelMap model = new ExtendedModelMap();

		String page = controller.editUser(null, new UserDataForm(null, null, null, null, null, null, null), model);
		assertThat(page).isEqualTo("redirect:../users");
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testEditUserPage() throws Exception {

		Optional<User> optUser = users.stream().findAny();

		if (optUser.isEmpty()) {
			fail();
		}

		User user = optUser.get();
		long id = user.getId();

		mockMvc.perform(get("/users/edit").param("id", String.valueOf(id))).andExpect(status().isOk())
				.andExpect(model().attribute("form", is(notNullValue())));
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testEditUser() throws Exception {

		Optional<User> optUser = users.stream().findAny();

		if (optUser.isEmpty()) {
			fail();
		}

		User user = optUser.get();
		long id = user.getId();

		mockMvc.perform(post("/users/edit").param("id", String.valueOf(id)).param("firstname", "new_firstname")
				.param("lastname", "new_lastname").param("address", "new_address").param("phone", "123")
				.param("salary", "1337.00").param("email", user.getUserAccount().getEmail())
				.param("password", "NewPassword123")).andExpect(status().is3xxRedirection());

		User updatedUser = users.findById(id).get();
		assertEquals("new_firstname", updatedUser.getFirstname());
		assertEquals("new_lastname", updatedUser.getLastname());
		assertEquals("new_address", updatedUser.getAddress());
		assertEquals("123", updatedUser.getPhone());
		assertEquals(1337d, updatedUser.getSalary());

		Optional<User> optSecond = users.stream().filter(u -> u.getId() != id).findAny();
		if (optSecond.isEmpty()) {
			fail();
		}

		mockMvc.perform(post("/users/edit").param("id", String.valueOf(id)).param("firstname", "new_firstname")
				.param("lastname", "new_lastname").param("address", "new_address").param("phone", "123")
				.param("salary", "1337.00").param("email", optSecond.get().getUserAccount().getEmail())
				.param("password", "")).andExpect(status().isOk());

		mockMvc.perform(post("/users/edit").param("id", String.valueOf(id)).param("firstname", "new_firstname")
				.param("lastname", "new_lastname").param("address", "new_address").param("phone", "123")
				.param("salary", "1337.00").param("email", user.getUserAccount().getEmail()).param("password", "test"))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testUserDeletion() throws Exception {

		Optional<User> optUser = users.stream().findAny();
		if (!optUser.isPresent()) {
			fail();
		}

		long id = optUser.get().getId();

		mockMvc.perform(get("/users/delete").param("id", String.valueOf(id))).andExpect(status().is3xxRedirection());

		Optional<User> deleted = users.findById(id);
		assertThat(deleted.isEmpty());
	}

}
