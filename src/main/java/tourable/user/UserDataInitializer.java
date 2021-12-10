package tourable.user;

import java.util.List;
import java.util.Objects;

import org.salespointframework.core.DataInitializer;
import org.salespointframework.useraccount.Password.UnencryptedPassword;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccountManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class UserDataInitializer implements DataInitializer {

	private static final Logger LOG = LoggerFactory.getLogger(UserDataInitializer.class);

	private final UserAccountManager userAccountManager;
	private final UserManagement userManagement;

	UserDataInitializer(UserAccountManager userAccountManager, UserManagement userManagement) {
		this.userManagement = Objects.requireNonNull(userManagement);
		this.userAccountManager = Objects.requireNonNull(userAccountManager);
	}

	@Override
	public void initialize() {

		LOG.info("Creating default users");

		userAccountManager.create("boss@citytours.de", UnencryptedPassword.of("123"), Role.of("BOSS"));

		List.of(new UserDataForm("Hans", "Holger", "Passwort123", "Bsp Weg 1", "1234567", "hans@web.de", 123d),
				new UserDataForm("Rainer", "Holger", "Passwort143", "Bsp Weg 2", "1234567", "rainer@web.de", 50d))
				.forEach(userManagement::createUser);
	}
}
