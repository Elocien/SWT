package tourable.booking;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.ResourceBundle;

import org.springframework.context.i18n.LocaleContextHolder;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.DoubleBorder;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.BorderCollapsePropertyValue;
import com.itextpdf.layout.property.TextAlignment;

import tourable.accounting.Invoice;

public class PDFGenerator {

	private final NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("de", "DE"));
	private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");

	private PdfFont font;
	private PdfFont bold;

	public PDFGenerator() {
		try {
			font = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);
			bold = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public byte[] getPdfInvoiceAsBytes(Booking booking) {
		ResourceBundle resourceBundle = ResourceBundle.getBundle("messages", LocaleContextHolder.getLocale());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PdfDocument pdf = new PdfDocument(new PdfWriter(baos));
		PdfPage page = pdf.addNewPage();
		PdfCanvas canvas = new PdfCanvas(page);

		Canvas recipient = new Canvas(canvas, pdf, new Rectangle(50, 650, 250, 70));
		recipient.add(new Paragraph(
				new Text("Abs: CityTours GmbH · Musterstr. 1 · 01069 Dresden").setFont(font).setFontSize(10)));
		Text address = new Text(booking.getCustomer().getName() + "\n" + booking.getCustomer().getAddress())
				.setFont(font);
		recipient.add(new Paragraph(address).setMarginLeft(5));
		recipient.close();

		Canvas citytours = new Canvas(canvas, pdf, new Rectangle(400, 570, 150, 150));
		citytours.add(new Paragraph(new Text("CityTours GmbH\n").setFont(bold)).add(new Text(
				"Musterstr. 1\n01069 Dresden\n\nTel.: 0180/1234567\nE-Mail: info@citytours.de\nInternet: citytours.de")
						.setFont(font))
				.setTextAlignment(TextAlignment.RIGHT));
		citytours.close();

		Canvas invoiceText = new Canvas(canvas, pdf, new Rectangle(50, 480, 250, 100));
		var invoiceNr = new SimpleDateFormat("yyyy").format(new Date()) + "-" + new Random().nextInt(99999);
		invoiceText.add(new Paragraph(new Text("Rechnung\n").setFont(bold).setFontSize(20))
				.add(new Text("\nRechnung Nr. " + invoiceNr + "\n").setFont(bold))
				.add(new Text("Bitte bei Zahlungen und Schriftverkehr angeben!").setFont(font).setFontSize(8)));
		invoiceText.close();

		Canvas invoiceParams = new Canvas(canvas, pdf, new Rectangle(400, 530, 150, 50));
		var currentDate = LocalDateTime.now().format(dateFormat);
		invoiceParams.add(new Paragraph(
				new Text("Rechnungsdatum: " + currentDate + "\nKundennummer: " + booking.getCustomer().getId())
						.setFont(font)).setTextAlignment(TextAlignment.RIGHT));
		invoiceParams.close();

		Canvas thanksText = new Canvas(canvas, pdf, new Rectangle(50, 440, 500, 50));
		thanksText.add(new Paragraph(new Text("Sehr geehrter " + booking.getCustomer().getName()
				+ ", \nvielen Dank für Ihr Vertrauen in CityTours! Im Folgenden finden Sie die Details zu Ihrer Buchung.")
						.setFont(font)));
		thanksText.close();

		Canvas invoiceTable = new Canvas(canvas, pdf, new Rectangle(50, 190, 500, 250));

		Table table = new Table(6);
		table.setAutoLayout();
		table.setWidth(500);
		table.setBorderCollapse(BorderCollapsePropertyValue.SEPARATE);
		table.setVerticalBorderSpacing(10);

		addHeaderCell(table, "BNr.", TextAlignment.LEFT);
		addHeaderCell(table, "Beschreibung", TextAlignment.LEFT);
		addHeaderCell(table, "UNr.", TextAlignment.CENTER);
		addHeaderCell(table, "Dauer", TextAlignment.CENTER);
		addHeaderCell(table, "Preis / Nacht", TextAlignment.RIGHT);
		addHeaderCell(table, "Gesamt", TextAlignment.RIGHT);

		addCell(table, String.valueOf(booking.getId()), TextAlignment.LEFT);
		table.addCell(new Cell(1, 1).setTextAlignment(TextAlignment.LEFT)
				.add(new Paragraph(booking.getAccommodation().getName() + "\n").setFont(font)
						.add(new Text(booking.getAccommodation().getDescription()).setFontSize(10)))
				.setBorder(Border.NO_BORDER));
		addCell(table, String.valueOf(booking.getAccommodation().getId()), TextAlignment.CENTER);
		var interval = booking.getStart().format(dateFormat) + " - " + booking.getEnd().format(dateFormat);
		addCell(table, interval, TextAlignment.CENTER);
		var pricePerNight = currency.format(booking.getAccommodation().getPrice());
		addCell(table, pricePerNight, TextAlignment.RIGHT);
		var totalPrice = currency.format(booking.getPrice());
		addCell(table, totalPrice, TextAlignment.RIGHT);

		var bruttoDouble = booking.getPrice();

		if (booking.getStatus() == BookingStatus.CANCELLED) {
			String cancellationRefund = "Stornierungsrückzahlung " + getCancellationRefund(booking);
			addCell(table, "—", TextAlignment.LEFT);
			addCell(table, cancellationRefund, TextAlignment.LEFT);
			addCell(table, "—", TextAlignment.CENTER);
			addCell(table, "—", TextAlignment.CENTER);
			addCell(table, "—", TextAlignment.CENTER);

			double refundDouble = 0d;
			if (getCancellationRefund(booking).equals("70%")) {
				refundDouble = booking.getPrice() * -0.7d;
			}

			var refund = currency.format(refundDouble);
			addCell(table, refund, TextAlignment.RIGHT);

			bruttoDouble = booking.getPrice() + refundDouble;
		}

		table.addCell(
				new Cell(1, 4).setBorder(Border.NO_BORDER).setBorderTop(new SolidBorder(ColorConstants.GRAY, 1f)));

		table.addCell(
				new Cell(1, 1).setTextAlignment(TextAlignment.RIGHT).add(new Paragraph("Netto:\nUSt.:").setFont(font))
						.setBorder(Border.NO_BORDER).setBorderTop(new SolidBorder(ColorConstants.GRAY, 1f)));
		table.addCell(new Cell(1, 1).setTextAlignment(TextAlignment.RIGHT)
				.add(new Paragraph(currency.format(bruttoDouble * 0.81) + "\n" + currency.format(bruttoDouble * 0.19))
						.setFont(font))
				.setBorder(Border.NO_BORDER).setBorderTop(new SolidBorder(ColorConstants.GRAY, 1f)));

		table.addCell(new Cell(1, 4).setBorder(Border.NO_BORDER));
		table.addCell(new Cell(1, 1).setTextAlignment(TextAlignment.RIGHT).add(new Paragraph("Brutto:").setFont(bold))
				.setBorder(Border.NO_BORDER).setBorderBottom(new DoubleBorder(ColorConstants.GRAY, 5f)));
		table.addCell(new Cell(1, 1).setTextAlignment(TextAlignment.RIGHT)
				.add(new Paragraph(currency.format(bruttoDouble)).setFont(bold)).setBorder(Border.NO_BORDER)
				.setBorderBottom(new DoubleBorder(ColorConstants.GRAY, 5f)));

		invoiceTable.add(table);
		invoiceTable.close();

		Canvas paymentMethod = new Canvas(canvas, pdf, new Rectangle(50, 180, 200, 50));
		var statusString = resourceBundle.getString("bookings.status." + booking.getStatus());
		var paymentMethodString = resourceBundle.getString("bookings.paymentMethod." + booking.getPaymentMethod());
		paymentMethod.add(new Paragraph("Buchungsstatus: " + statusString + "\n")
				.add("Zahlungsmethode: " + paymentMethodString).setFont(font));
		paymentMethod.close();

		Canvas payments = new Canvas(canvas, pdf, new Rectangle(300, 130, 250, 100));
		if ((booking.getStatus() == BookingStatus.PAID) || (booking.getStatus() == BookingStatus.CANCELLED)) {
			table = new Table(2);
			table.setAutoLayout();
			table.setWidth(250);

			table.addHeaderCell(new Cell(1, 2).add(new Paragraph("Bisher erhaltene Zahlungen").setFont(bold))
					.setBorder(Border.NO_BORDER));
			for (Invoice invoice : booking.getInvoicesOfBooking()) {
				table.addCell(new Cell(1, 1)
						.add(new Paragraph(invoice.getDate().format(dateFormat)).setFont(font).setFontSize(10))
						.setBorder(Border.NO_BORDER));
				table.addCell(new Cell(1, 1).add(new Paragraph(currency.format(invoice.getValue())).setFont(font)
						.setFontSize(10).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER));
			}
			table.addCell(new Cell(1, 1).add(new Paragraph("Offener Betrag:").setFont(bold)).setBorder(Border.NO_BORDER)
					.setBorderTop(new SolidBorder(ColorConstants.GRAY, 1f)));
			table.addCell(new Cell(1, 1)
					.add(new Paragraph(currency.format(0d)).setFont(bold).setTextAlignment(TextAlignment.RIGHT))
					.setBorder(Border.NO_BORDER).setBorderTop(new SolidBorder(ColorConstants.GRAY, 1f)));
			payments.add(table);
		} else {
			var lastPaymentDate = booking.getStart().minusDays(4).format(dateFormat);
			payments.add(new Paragraph("Bitte begleichen Sie den fälligen Betrag bis spätestens zum " + lastPaymentDate)
					.setFont(font).setTextAlignment(TextAlignment.RIGHT));
		}
		payments.close();

		canvas.setStrokeColor(ColorConstants.GRAY).moveTo(51, 100).lineTo(549, 100).closePathStroke();

		Canvas info = new Canvas(canvas, pdf, new Rectangle(50, 10, 500, 80));
		table = new Table(3);
		table.setAutoLayout();
		table.setWidth(500);
		table.addCell(new Cell(1, 1)
				.add(new Paragraph("CityTours GmbH\nMusterstr. 1\n01069 Dresden").setFont(font).setFontSize(10))
				.setBorder(Border.NO_BORDER));
		table.addCell(new Cell(1, 1).add(
				new Paragraph("Ostsächsische Sparkasse Dresden\nBIC: OSDDDE81XXX\nIBAN: DE89 3704 0044 0532 0130 00")
						.setFont(font).setFontSize(10).setTextAlignment(TextAlignment.CENTER))
				.setBorder(Border.NO_BORDER));
		table.addCell(new Cell(1, 1).add(new Paragraph("Steuernr.: 12388465\nFinanzamt Dresden Süd").setFont(font)
				.setFontSize(10).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER));
		info.add(table);
		info.close();

		pdf.close();
		return baos.toByteArray();
	}

	private void addHeaderCell(Table table, String content, TextAlignment alignment) {
		table.addHeaderCell(new Cell(1, 1).setTextAlignment(alignment).add(new Paragraph(content).setFont(bold))
				.setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(ColorConstants.GRAY, 1f)));
	}

	private void addCell(Table table, String content, TextAlignment alignment) {
		table.addCell(new Cell(1, 1).setTextAlignment(alignment).add(new Paragraph(content).setFont(font))
				.setBorder(Border.NO_BORDER));
	}

	private String getCancellationRefund(Booking booking) {
		for (Invoice invoice : booking.getInvoicesOfBooking()) {
			if (invoice.getDescription().contains("30%")) {
				return "70%";
			}
		}
		return "0%";
	}

}
