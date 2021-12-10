package tourable.user;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import org.salespointframework.useraccount.UserAccount;

@Entity
public class User {

	private @Id @GeneratedValue long id;
	private String address;
	private String phone;
	private String firstname;
	private String lastname;
	private double salary;

	@OneToOne
	UserAccount userAccount;

	@SuppressWarnings("unused")
	private User() {
	}

	public User(UserAccount userAccount, String firstname, String lastname, String address, String phone,
			double salary) {
		this.userAccount = userAccount;
		this.address = address;
		this.phone = phone;
		this.salary = salary;
		this.firstname = firstname;
		this.lastname = lastname;
	}

	public long getId() {
		return this.id;
	}

	public double getSalary() {
		return this.salary;
	}

	public void setSalary(double salary) {
		this.salary = salary;
	}

	public String getFirstname() {
		return this.firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return this.lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getAddress() {
		return this.address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhone() {
		return this.phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public UserAccount getUserAccount() {
		return this.userAccount;
	}
}
