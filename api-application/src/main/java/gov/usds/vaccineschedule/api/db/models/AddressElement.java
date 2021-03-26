package gov.usds.vaccineschedule.api.db.models;

import org.hibernate.annotations.Type;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.StringType;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by nickrobison on 3/26/21
 */
@Embeddable
public class AddressElement {

    @Type(type = "list-array")
    @Column
    private List<String> street = new ArrayList<>();

    @Column(nullable = false)
    private String city;
    @Column(nullable = false)
    private String state;
    @Column(nullable = false)
    private String postalCode;
    private String district;

    protected AddressElement() {
        /* for hibernate */
    }

    private AddressElement(List<String> street, String city, String state, String postalCode, String district) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.district = district;
    }

    public List<String> getStreet() {
        return Collections.unmodifiableList(street);
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String county) {
        this.district = county;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddressElement address = (AddressElement) o;
        return street.equals(address.street) && city.equals(address.city) && state.equals(address.state) && postalCode.equals(address.postalCode) && district.equals(address.district);
    }

    @Override
    public int hashCode() {
        return Objects.hash(street, city, state, postalCode, district);
    }

    public org.hl7.fhir.r4.model.Address toFHIR() {
        final Address address = new Address();
        address.setLine(this.getStreet().stream().map(StringType::new).collect(Collectors.toList()));
        address.setCity(this.city);
        address.setState(this.state);
        address.setPostalCode(this.postalCode);
        address.setDistrict(this.district);

        return address;
    }

    public static AddressElement fromFHIR(Address resource) {
        return new AddressElement(
                resource.getLine().stream().map(PrimitiveType::getValue).collect(Collectors.toList()),
                resource.getCity(),
                resource.getState(),
                resource.getPostalCode(),
                resource.getDistrict());
    }
}
