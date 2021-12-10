package tourable.city;

import javax.validation.constraints.NotEmpty;

public class CityDataForm {

	@NotEmpty
	private final String name;

	public CityDataForm(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

}
