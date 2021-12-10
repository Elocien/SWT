package tourable.customer;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import org.salespointframework.time.BusinessTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tourable.booking.BookingManagement;

/**
 * {@linkplain Service} class that handles all business logic for customer 
 * components 
 * 
 * @author Leon Augustat
 *
 */

@Service
@Transactional
public class CustomerManagement {

	private final CustomerRepository customers;
	private final BookingManagement bookingManagement;
	private final BusinessTime businessTime;

	public CustomerManagement(CustomerRepository customers, BookingManagement bookingManagement,
			BusinessTime businessTime) {
		this.customers = Objects.requireNonNull(customers);
		this.bookingManagement = Objects.requireNonNull(bookingManagement);
		this.businessTime = Objects.requireNonNull(businessTime);
	}

	/**
	 * creates a new {@linkplain Customer} with values from a given 
	 * {@linkplain CustomerDataForm} and saves it to the database
	 * 
	 * @param form the given form
	 * @return the new customer
	 */
	
	public Customer createCustomer(CustomerDataForm form) {

		Objects.requireNonNull(form);

		var name = form.getName();
		var address = form.getAddress();
		var email = form.getEmail();
		var phone = form.getPhone();

		return customers.save(new Customer(name, address, email, phone, businessTime));
	}
	
	/**
	 * updates a {@linkplain Customer} with values from a given 
	 * {@linkplain CustomerDataForm}
	 * 
	 * @param form the form that contains the new information
	 * @param id   the id that contains the new information
	 */

	public void updateCustomer(CustomerDataForm form, long id) {
		Optional<Customer> optCustomer = customers.findById(id);
		if (optCustomer.isPresent()) {
			Customer customer = optCustomer.get();
			customer.setName(form.getName());
			customer.setAddress(form.getAddress());
			customer.setEmail(form.getEmail());
			customer.setPhone(form.getPhone());

			customers.save(customer);
		}
	}
	
	/**
	 * searches in the database for {@linkplain Customer}, whose latest booking 
	 * date is older then 3 years and deletes them
	 */
	
	public void deleteInactiveCustomers() {
		LocalDateTime threshold = businessTime.getTime().minusYears(3);
		customers.stream().filter(customer -> customer.getLastBookingDate().isBefore(threshold))
				.forEach(customer -> deleteCustomer(customer.getId()));
	}
	
	/**
	 * checks, if the updated email of a {@linkplain Customer} already exists 
	 * @param id    the id that contains the new information
	 * @param email the updated email
	 * @return true, if the mail is unique
	 */

	public boolean isMailUnique(long id, String email) {
		return customers.stream().filter(customer -> customer.getId() != id)
				.noneMatch(customer -> customer.getEmail().equals(email));
	}
	
	/**
	 * deletes an inactive {@linkplain Customer} from the database
	 * @param id the id of the customer to delete
	 */
	
	public void deleteCustomer(Long id) {
		Optional<Customer> optCustomer = findById(id);
		if (optCustomer.isPresent()) {
			Customer customer = optCustomer.get();
			if (!bookingManagement.hasOpenBookingsForCustomer(customer)) {
				customer.setDisabled(true);
				customers.save(customer);
			}
		}
	}

	/**
	 * @return all {@linkplain Customer}s in the database
	 */
	
	public Iterable<Customer> findAllCustomers() {
		return customers.findByDisabledFalse();
	}
	
	/**
	 * @param id the customer id
	 * @return an {@linkplain Optional} of a {@linkplain Customer} with the given
	 * id
	 */
	
	public Optional<Customer> findById(long id) {
		return customers.findByIdAndDisabledFalse(id);
	}
	
	/**
	 * @param name the customer name
	 * @return all {@linkplain Customer}s with the given name
	 */

	public Iterable<Customer> findByName(String name) {
		return customers.findByNameContainingIgnoreCaseAndDisabledFalse(name);
	}
	
	/**
	 * @param email the customer email
	 * @return an {@linkplain Optional} of a {@linkplain Customer} with the given
	 * email
	 */

	public Optional<Customer> findByEmail(String email) {
		return customers.findByEmailAndDisabledFalse(email);
	}
	
	/**
	 * @param customer a {@linkplain Customer}
	 * @return a {@linkplain CustomerDataForm} with the values of the 
	 * given Customer
	 */

	public CustomerDataForm getFormForCustomer(Customer customer) {
		return new CustomerDataForm(customer.getName(), customer.getAddress(), customer.getEmail(),
				customer.getPhone());
	}
}
