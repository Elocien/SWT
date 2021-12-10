package tourable.accounting;

import java.util.List;
import java.util.Objects;

import org.salespointframework.core.DataInitializer;
import org.salespointframework.time.BusinessTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InvoiceDataInitializer implements DataInitializer {

	private final AccountingManagement accountingManagement;
	private final BusinessTime businessTime;

	private static final Logger LOG = LoggerFactory.getLogger(InvoiceDataInitializer.class);

	public InvoiceDataInitializer(AccountingManagement accountingManagement, BusinessTime bT) {
		this.accountingManagement = Objects.requireNonNull(accountingManagement);
		this.businessTime = Objects.requireNonNull(bT);
	}

	@Override
	public void initialize() {
		LOG.info("Creating default invoices");
		List.of(new Invoice(2000d, "Travelguide Test Sale 1", TransactionCategory.TRAVELGUIDE_SALE, businessTime),
				new Invoice(39.99d, "Travelguide Test Sale 2", TransactionCategory.TRAVELGUIDE_SALE, businessTime))
				.forEach(accountingManagement::add);
	}
}
