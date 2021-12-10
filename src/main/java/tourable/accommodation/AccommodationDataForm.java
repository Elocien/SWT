package tourable.accommodation;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.web.multipart.MultipartFile;

public class AccommodationDataForm {

	@NotEmpty
	private String name;

	@NotEmpty
	private String city;

	@NotEmpty
	private String description;

	@NotNull
	private MultipartFile image;

	@NotNull
	private Double price;

	@NotNull
	private Integer bedNumber;

	@NotNull
	private Integer roomNumber;

	@NotNull
	private Double provision;

	@NotNull
	private AccommodationType type;

	@NotNull
	private AccommodationLocation location;

	public AccommodationDataForm(String name, String city, String description, MultipartFile image, Double price,
			Integer bedNumber, Integer roomNumber, Double provision, AccommodationType type,
			AccommodationLocation location) {
		this.name = name;
		this.city = city;
		this.type = type;
		this.description = description;
		this.image = image;
		this.roomNumber = roomNumber;
		this.price = price;
		this.bedNumber = bedNumber;
		this.provision = provision;
		this.location = location;
	}

	public String getName() {
		return this.name;
	}

	public String getCity() {
		return this.city;
	}

	public String getDescription() {
		return this.description;
	}

	public void setImage(MultipartFile image) {
		this.image = image;
	}

	public MultipartFile getImage() {
		return this.image;
	}

	public Double getPrice() {
		return this.price;
	}

	public Integer getBedNumber() {
		return this.bedNumber;
	}

	public Integer getRoomNumber() {
		return this.roomNumber;
	}

	public Double getProvision() {
		return this.provision;
	}

	public AccommodationType getType() {
		return this.type;
	}

	public AccommodationLocation getLocation() {
		return this.location;
	}

}