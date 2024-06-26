package de.berlin.htw.boundary.dto;

import jakarta.validation.constraints.*;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */

public class Item {

	@Size(max = 255, message = "Artikelname darf nicht länger als 255 Zeichen sein")
	private String productName;

	@Pattern(regexp = "\\d-\\d-\\d-\\d-\\d-\\d", message = "Artikelnummer muss im Format '1-2-3-4-5-6' sein")
	private String productId;

	@Max(value = 10, message = "Der Inhalt des Warenkorbs darf nicht mehr als 10 Artikel überschreiten")
	@Min(value = 1, message = "Der Inhalt des Warenkorbs darf nicht weniger als 1 Artikel betragen")
	private Integer count;

	@DecimalMin(value = "10.00", message = "Preis muss mindestens 10 Euro sein")
	@DecimalMax(value = "100.00", message = "Preis darf höchstens 100 Euro sein")
	private Float price;

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(final Integer count) {
		this.count = count;
	}

	public Float getPrice() {
		return price;
	}

	public void setPrice(Float price) {
		this.price = price;
	}

}
