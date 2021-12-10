package tourable.city;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Simple value object that represents a city
 * @author Pascal Juppe
 */
@Entity
public class City {

	private @Id @GeneratedValue long id;
	private String name;
	
	private boolean disabled = false;

	public City() {

	}

	public long getId() {
		return this.id;
	}

	public City(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	
	public boolean isDisabled() {
		return this.disabled;
	}

}
