package tourable.customer;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

public class CustomerDataForm {

	@NotEmpty
	private final String name;

	@NotEmpty
	private final String address;

	@NotEmpty
	@Email
	private final String email;

	@NotEmpty
	private final String phone;

	public CustomerDataForm(String name, String address, String email, String phone) {
		this.name = name;
		this.address = address;
		this.email = email;
		this.phone = phone;
	}

	public String getName() {
		return this.name;
	}

	public String getAddress() {
		return this.address;
	}

	public String getEmail() {
		return this.email;
	}

	public String getPhone() {
		return this.phone;
	}
}
