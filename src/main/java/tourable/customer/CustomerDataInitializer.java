package tourable.customer;

import java.util.List;
import java.util.Objects;

import org.salespointframework.core.DataInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class CustomerDataInitializer implements DataInitializer {

	private static final Logger logger = LoggerFactory.getLogger(CustomerDataInitializer.class);

	private final CustomerManagement customerManagement;

	public CustomerDataInitializer(CustomerManagement customerManagement) {
		this.customerManagement = Objects.requireNonNull(customerManagement);
	}

	@Override
	public void initialize() {

		logger.info("creating default customers");

		List.of(new CustomerDataForm("Kevin Keller", "Straße der Einheit 1", "kevinkeller@web.de", "123"),
				new CustomerDataForm("Jonas Lüppet", "Weg am Weier 5", "lueppet95@gmail.com", "456"),
				new CustomerDataForm("Frederike Lehmann", "Waldstraße 12", "fredi@web.de", "789"),
				new CustomerDataForm("Olaf Schach", "Prager Straße 1", "schacholaf@gmx.de", "010"),
				new CustomerDataForm("Jens Altmann", "Laternenpfad 90", "altmannjens@onlinehome.de", "563"),
				new CustomerDataForm("Sarah Stern", "An der Haupstraße 7", "sternsarah@web.de", "223"),
				new CustomerDataForm("Jana Lippo", "Augustusweg 44", "lippojana@gmail.com", "642"))
				.forEach(customerManagement::createCustomer);
	}
}
