package tourable.user;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

import org.salespointframework.time.BusinessTime;
import org.salespointframework.useraccount.Password.UnencryptedPassword;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccountManager;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tourable.accounting.AccountingManagement;
import tourable.accounting.Invoice;
import tourable.accounting.TransactionCategory;

/**
 * 
 * @author Robert Thoß
 * @author Florian Richter
 * @author Pascal Juppe
 */
@Service
@Transactional
public class UserManagement {

	private static final Role USER_ROLE = Role.of("USER");

	private final UserRepository users;
	private final UserAccountManager userAccounts;
	private final AccountingManagement accountingManagement;
	private final BusinessTime businessTime;

	public UserManagement(UserRepository users, UserAccountManager userAccounts,
			AccountingManagement accountingManagement, BusinessTime businessTime) {
		this.users = Objects.requireNonNull(users);
		this.userAccounts = Objects.requireNonNull(userAccounts);
		this.accountingManagement = Objects.requireNonNull(accountingManagement);
		this.businessTime = Objects.requireNonNull(businessTime);
	}

	/**
	 * creates a new {@linkplain User} with values from a given
	 * {@linkplain UserDataForm} and saves it to the database
	 * 
	 * @param form the given form
	 * @return the new user
	 */
	public User createUser(UserDataForm form) {

		Objects.requireNonNull(form);

		var password = UnencryptedPassword.of(form.getPassword());
		var userAccount = userAccounts.create(form.getEmail(), password, form.getEmail(), USER_ROLE);
		var salary = form.getSalary();
		var address = form.getAddress();
		var Firstname = form.getFirstname();
		var Lastname = form.getLastname();
		var phone = form.getPhone();

		return users.save(new User(userAccount, Firstname, Lastname, address, phone, salary));
	}

	/**
	 * updates a {@linkplain User} with values from a given
	 * {@linkplain UserDataForm}
	 * 
	 * @param form the form that contains the new information
	 * @param id   the id that contains the new information
	 * 
	 * @return the updated User
	 */
	public User updateUser(UserDataForm form, long id) {
		Optional<User> newUser = users.findById(id);
		User user = newUser.get();
		user.setFirstname(form.getFirstname());
		user.setLastname(form.getLastname());
		user.setAddress(form.getAddress());
		user.setSalary(form.getSalary());
		user.setPhone(form.getPhone());

		var newPassword = form.getPassword();
		if (newPassword != null && !newPassword.isEmpty()) {
			userAccounts.changePassword(user.getUserAccount(), UnencryptedPassword.of(newPassword));
		}

		return users.save(user);
	}

	/**
	 * deletes given user
	 * 
	 * @param id Identifier to identify the user
	 */
	public void deleteUser(long id) {
		Optional<User> newUser = users.findById(id);
		User user = newUser.get();
		userAccounts.delete(user.userAccount);
		users.delete(user);
	}

	/**
	 * checks, if the updated email of a {@linkplain User} already exists
	 * 
	 * @param id    the id that contains the new information
	 * @param email the updated email
	 * @return true or false
	 */
	public boolean isMailUnique(long id, String email) {
		return users.stream().filter(user -> user.getId() != id)
				.noneMatch(user -> user.getUserAccount().getEmail().equals(email));
	}

	/**
	 * @return all {@linkplain User}s in the database
	 */
	public Iterable<User> findAll() {
		return users.findAll();
	}

	/**
	 * @param id the user id
	 * @return an {@linkplain Optional} of a {@linkplain User} with the given id
	 */
	public Optional<User> findById(long id) {
		return users.findById(id);
	}

	/**
	 * Checks if Salary was already payed this month, if not, pays it.
	 * 
	 * @return salary, if nothing paid: 0
	 */
	@Scheduled(cron = "0 0 9 * * ?")
	public double paySalary() {
		ResourceBundle resourceBundle = ResourceBundle.getBundle("messages", LocaleContextHolder.getLocale());
		String salaryFrom = resourceBundle.getString("accounting.description.salary");

		double salary = 0;

		LocalDateTime firstSecondOfThisMonth = LocalDateTime.of(businessTime.getTime().getYear(),
				businessTime.getTime().getMonthValue(), 1, 0, 0, 0);

		List<Invoice> invlist = accountingManagement.findByTransactionCategoryAndDateAfter(TransactionCategory.SALARY,
				firstSecondOfThisMonth);

		if (invlist.isEmpty()) {
			salary = -1 * users.stream().filter(u -> u.getUserAccount().isEnabled()).mapToDouble(User::getSalary).sum();

			accountingManagement.createInvoice(salary,
					salaryFrom + " " + businessTime.getTime().format(DateTimeFormatter.ISO_DATE),
					TransactionCategory.SALARY);
		}
		return salary;
	}
}