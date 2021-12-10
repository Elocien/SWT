package tourable.booking.forms;

import javax.validation.constraints.NotEmpty;

public class CustomerSearchForm {

	@NotEmpty
	private String name;

	public CustomerSearchForm(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}
}
