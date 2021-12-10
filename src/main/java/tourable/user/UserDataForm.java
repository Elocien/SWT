package tourable.user;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

class UserDataForm {

	@NotEmpty
	private final String firstname;

	@NotEmpty
	private final String lastname;

	private final String password;

	@NotEmpty
	private final String address;

	@NotEmpty
	private final String phone;

	@NotEmpty
	@Email
	private final String email;

	@NotNull
	private final Double salary;

	public UserDataForm(String firstname, String lastname, String password, String address, String phone, String email,
			Double salary) {
		this.firstname = firstname;
		this.lastname = lastname;
		this.password = password;
		this.address = address;
		this.phone = phone;
		this.email = email;
		this.salary = salary;
	}

	public String getFirstname() {
		return this.firstname;
	}

	public String getLastname() {
		return this.lastname;
	}

	public String getPassword() {
		return this.password;
	}

	public String getAddress() {
		return this.address;
	}

	public String getPhone() {
		return this.phone;
	}

	public String getEmail() {
		return this.email;
	}

	public Double getSalary() {
		return this.salary;
	}
}
