package tourable.accommodation;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.salespointframework.core.DataInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import tourable.city.City;
import tourable.city.CityManagement;

@Component
@Order(2)
public class AccommodationDataInitializer implements DataInitializer {

	private final AccommodationManagement accommodationManagement;
	private final CityManagement cityManagement;

	private static final Logger LOG = LoggerFactory.getLogger(AccommodationDataInitializer.class);

	public AccommodationDataInitializer(AccommodationManagement accommodationManagement,
			CityManagement cityManagement) {
		this.accommodationManagement = Objects.requireNonNull(accommodationManagement);
		this.cityManagement = Objects.requireNonNull(cityManagement);
	}

	@Override
	public void initialize() {
		LOG.info("Creating default accommodations");

		City dresden = cityManagement.findByName("Dresden").get();
		City newyork = cityManagement.findByName("New York").get();
		City berlin = cityManagement.findByName("Berlin").get();

		try {
			byte[] dresdenImage = this.getClass().getClassLoader()
					.getResourceAsStream("static/resources/img/accommodations/Dresden.jpg").readAllBytes();
			byte[] dirtyApartment = this.getClass().getClassLoader()
					.getResourceAsStream("static/resources/img/accommodations/dirtyapartment.jpg").readAllBytes();
			byte[] beachhouse = this.getClass().getClassLoader()
					.getResourceAsStream("static/resources/img/accommodations/beachhouse.jpeg").readAllBytes();
			byte[] farmhouse = this.getClass().getClassLoader()
					.getResourceAsStream("static/resources/img/accommodations/farmhouse.jpg").readAllBytes();
			byte[] hsz = this.getClass().getClassLoader()
					.getResourceAsStream("static/resources/img/accommodations/hsz.jpg").readAllBytes();
			byte[] minimal = this.getClass().getClassLoader()
					.getResourceAsStream("static/resources/img/accommodations/log-cabin-1886620_1280.jpg")
					.readAllBytes();
			byte[] Casa_luna = this.getClass().getClassLoader()
					.getResourceAsStream("static/resources/img/accommodations/architecture-1477041_1280.jpg")
					.readAllBytes();
			byte[] Casa_Till = this.getClass().getClassLoader()
					.getResourceAsStream("static/resources/img/accommodations/architecture-1836070_1280.jpg")
					.readAllBytes();
			byte[] Altmarkt = this.getClass().getClassLoader()
					.getResourceAsStream("static/resources/img/accommodations/room-2269594_1280.jpg").readAllBytes();
			byte[] Student_WG = this.getClass().getClassLoader()
					.getResourceAsStream("static/resources/img/accommodations/laptop-1890547_1280.jpg").readAllBytes();
			byte[] Prohlis = this.getClass().getClassLoader()
					.getResourceAsStream("static/resources/img/accommodations/apartment-2094699_1280.jpg")
					.readAllBytes();
			byte[] Garden_House = this.getClass().getClassLoader()
					.getResourceAsStream("static/resources/img/accommodations/spring-2955582_1280.jpg").readAllBytes();
			byte[] University = this.getClass().getClassLoader()
					.getResourceAsStream("static/resources/img/accommodations/apartment-406901_1280.jpg")
					.readAllBytes();
			byte[] Airport = this.getClass().getClassLoader()
					.getResourceAsStream("static/resources/img/accommodations/kitchen-416027_1280.jpg").readAllBytes();
			byte[] Elbe = this.getClass().getClassLoader()
					.getResourceAsStream("static/resources/img/accommodations/landscape-4628845_1280.jpg")
					.readAllBytes();
			byte[] Hudson = this.getClass().getClassLoader()
					.getResourceAsStream("static/resources/img/accommodations/home-4137379_1280.jpg").readAllBytes();
			byte[] Granny = this.getClass().getClassLoader()
					.getResourceAsStream("static/resources/img/accommodations/condominium-2859414_1280.jpg")
					.readAllBytes();
			byte[] Downtown = this.getClass().getClassLoader()
					.getResourceAsStream("static/resources/img/accommodations/lounge-2048716_1280.jpg").readAllBytes();
			byte[] South = this.getClass().getClassLoader()
					.getResourceAsStream("static/resources/img/accommodations/berlin-83339_1280.jpg").readAllBytes();
			byte[] Cozy = this.getClass().getClassLoader()
					.getResourceAsStream("static/resources/img/accommodations/beds-182965_1280.jpg").readAllBytes();
			byte[] Fancy2 = this.getClass().getClassLoader()
					.getResourceAsStream("static/resources/img/accommodations/travel-1677347_1280.jpg").readAllBytes();

			List.of(new Accommodation("Haus Gerda", dresden, "Is in Ordnung", dresdenImage, 13d, 10, 1, 0.1d,
					AccommodationType.STANDARD, AccommodationLocation.COUNTRYSIDE),
					new Accommodation("Sehr runtergekommenes Apartment", dresden, "Diese Unterkunft ist besser",
							dirtyApartment, 10.5d, 2, 1, 0.1d, AccommodationType.SIMPLE,
							AccommodationLocation.COUNTRYSIDE),
					new Accommodation("Case de la Luna", dresden, "Shines like the Moon", Casa_luna, 34d, 4, 2, 0.15d,
							AccommodationType.LUXURY, AccommodationLocation.CITY_CENTRE),
					new Accommodation("Apartment by the Hudson River", newyork,
							"Nice views onto the river and parklands", Hudson, 120d, 4, 2, 0.15d,
							AccommodationType.STANDARD_PLUS, AccommodationLocation.CITY_CENTRE),
					new Accommodation("Casa de Till", dresden, "Till's Haus, echt super!", Casa_Till, 32.5d, 5, 2, 0.1d,
							AccommodationType.STANDARD_PLUS, AccommodationLocation.CITY_CENTRE),
					new Accommodation("Berlin Südkreuz Apartment", berlin,
							"Kleine Wohnung an einer belebten Straße. Sehr gute Anbindung an Nah- und Fernverkehr",
							South, 40d, 1, 1, 0.1d, AccommodationType.STANDARD, AccommodationLocation.CITY_CENTRE),
					new Accommodation("Fancy Apartments am Dresdner Altmarkt", dresden,
							"Mitten in der Altstadt. Aussicht auf den historischen Stadtkern von Dresden", Altmarkt,
							80d, 2, 1, 0.1d, AccommodationType.LUXURY, AccommodationLocation.CITY_CENTRE),
					new Accommodation("Student WG Couch", dresden,
							"Verbringe eine Nacht oder zwei in dem lebhaftesten Viertel von Dresden", Student_WG, 10d,
							3, 1, 0.1d, AccommodationType.SIMPLE, AccommodationLocation.CITY_CENTRE),
					new Accommodation("Granny Flat", newyork,
							"Small and simple flat in the back courtyard. Right in the city centre", Granny, 180d, 2, 1,
							0.15d, AccommodationType.SIMPLE, AccommodationLocation.CITY_CENTRE),
					new Accommodation("Dresden Prohlis Einzelapartment", dresden,
							"Sehr empfehlenswerte Ecke von Dresden, mit vielem Sehenswerten und Unterhaltsamen",
							Prohlis, 13.5d, 3, 1, 0.1d, AccommodationType.SIMPLE, AccommodationLocation.SUBURBAN),
					new Accommodation("Haus mit großem Garten, Dresden Plattleite", dresden,
							"Diese Unterkunft ist einfach noch besser", Garden_House, 33.5d, 8, 3, 0.1d,
							AccommodationType.LUXURY, AccommodationLocation.SUBURBAN),
					new Accommodation("1m² Haus", dresden, "Klein aber gemütlich", minimal, 22.5d, 2, 1, 0.1d,
							AccommodationType.STANDARD_PLUS, AccommodationLocation.COUNTRYSIDE),
					new Accommodation("Luxus WG", berlin, "Platz für eine ganze Gruppe", Cozy, 45.5d, 6, 1, 0.1d,
							AccommodationType.STANDARD_PLUS, AccommodationLocation.SUBURBAN),
					new Accommodation("Studentenwerk", dresden, "Direkt bei der TU Dresden", University, 12d, 2, 2,
							0.1d, AccommodationType.STANDARD, AccommodationLocation.CITY_CENTRE),
					new Accommodation("Downtown Apartment", newyork,
							"Tidy and cute, fit for a romantic weekend in the big smoke", Downtown, 110d, 2, 1, 0.15d,
							AccommodationType.STANDARD, AccommodationLocation.SUBURBAN),
					new Accommodation("Flughafenapartment", dresden, "Nah am Flughafen, gut für Reisende", Airport,
							19.99d, 2, 1, 0.1d, AccommodationType.STANDARD, AccommodationLocation.SUBURBAN),
					new Accommodation("Villa Gloria", berlin, "Weiträumig und wunderschön", Fancy2, 110d, 6, 3, 0.1d,
							AccommodationType.LUXURY, AccommodationLocation.COUNTRYSIDE),
					new Accommodation("Fährhaus", dresden,
							"In Dresden Friedrichstadt, mit wundervollem Blick auf die Elbe", Elbe, 23.99d, 5, 2, 0.1d,
							AccommodationType.STANDARD_PLUS, AccommodationLocation.SUBURBAN),
					new Accommodation("Subtropical Island Apartment", dresden, "Direkter Zugang zum Strand", beachhouse,
							600d, 8, 6, 0.1d, AccommodationType.LUXURY, AccommodationLocation.COUNTRYSIDE),
					new Accommodation("Fancy Barn", newyork,
							"Feel free to pay too much money for this overly lavish apartment", farmhouse, 1999.99d, 2,
							10, 0.35d, AccommodationType.LUXURY, AccommodationLocation.COUNTRYSIDE),
					new Accommodation("HSZ - Audimax", dresden, "Schlafen und Lernen in Einem!", hsz, 9.99d, 500, 1,
							0.35d, AccommodationType.LUXURY, AccommodationLocation.CITY_CENTRE))
					.forEach(accommodationManagement::add);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
