package ru.javaprojects.archivist.companies.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.javaprojects.archivist.common.util.validation.NoHtml;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Address {

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 32)
    @Column(name = "country", nullable = false)
    private String country;

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 32)
    @Column(name = "city", nullable = false)
    private String city;

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 64)
    @Column(name = "street", nullable = false)
    private String street;

    @NotBlank
    @NoHtml
    @Size(max = 32)
    @Column(name = "house", nullable = false)
    private String house;

    @NotBlank
    @NoHtml
    @Size(min = 6, max = 6)
    @Digits(integer = 6, fraction = 0)
    @Column(name = "zipcode", nullable = false)
    private String zipcode;

    public String getFormattedAddress() {
        return String.join(", ", country, zipcode, city, street, house);
    }
}
