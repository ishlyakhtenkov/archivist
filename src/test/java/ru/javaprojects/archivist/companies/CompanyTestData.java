package ru.javaprojects.archivist.companies;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.archivist.MatcherFactory;
import ru.javaprojects.archivist.companies.model.Address;
import ru.javaprojects.archivist.companies.model.Company;
import ru.javaprojects.archivist.companies.model.ContactPerson;
import ru.javaprojects.archivist.companies.model.Contacts;

import java.util.List;

import static ru.javaprojects.archivist.CommonTestData.ID;
import static ru.javaprojects.archivist.CommonTestData.NAME;

public class CompanyTestData {
    public static final MatcherFactory.Matcher<Company> COMPANY_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(Company.class);

    public static final long COMPANY1_ID = 100006;
    public static final long COMPANY2_ID = 100007;
    public static final long COMPANY3_ID = 100008;
    public static final String COMPANY1_NAME = "PAO \"TTK\"";
    public static final String COMPANY2_NAME = "AO \"Super Systems\"";

    public static final String COMPANIES_ATTRIBUTE = "companies";
    public static final String COMPANY_ATTRIBUTE = "company";

    public static final String COUNTRY = "address.country";
    public static final String ZIPCODE = "address.zipcode";
    public static final String CITY = "address.city";
    public static final String STREET = "address.street";
    public static final String HOUSE = "address.house";
    public static final String PHONE = "contacts.phone";
    public static final String FAX = "contacts.fax";
    public static final String EMAIL = "contacts.email";
    public static final String CONTACT_PERSON_POSITION = "contactPersons[0].position";
    public static final String CONTACT_PERSON_LAST_NAME = "contactPersons[0].lastName";
    public static final String CONTACT_PERSON_FIRST_NAME = "contactPersons[0].firstName";
    public static final String CONTACT_PERSON_MIDDLE_NAME = "contactPersons[0].middleName";
    public static final String CONTACT_PERSON_PHONE = "contactPersons[0].phone";

    public static final Company company1 = new Company(COMPANY1_ID, COMPANY1_NAME,
            new Address("Russia", "Moscow", "Aviamotornaya", "32", "121748"),
            new Contacts("8(495)745-13-22", "8(495)745-13-21", "ttk@mail.com"),
            List.of(new ContactPerson("Director", "Ivanov", "Pavel", "Ivanovich", "8(495)741-25-17", null)));

    public static final Company company2 = new Company(COMPANY2_ID, COMPANY2_NAME,
            new Address("Russia", "St Petersburg", "Nevsky avenue", "42 b2", "134896"),
            new Contacts("8(498)332-11-45", null, "supsystems@yandex.ru"),
            List.of(new ContactPerson("Chief engineer", "Petrov", "Ivan", "Alexandrovich", "8(745)111-25-89", null),
                    new ContactPerson("Secretary", "Belkina", "Anna", "Ivanovna", "8(745)111-25-89", null)));

    public static final Company company3 = new Company(COMPANY3_ID, "OOO \"Custom Solutions\"",
            new Address("Russia", "Tver", "Kominterna", "20", "114785"),
            new Contacts("8(564)662-28-15", null, null),
            List.of());

    public static Company getNew() {
        return new Company(null, "newName",
                new Address("newCountry", "newCity", "newStreet", "newHouse", "999999"),
                new Contacts("newPhone", "newFax", "new@gmail.com"),
                List.of(new ContactPerson("newContactPersonPosition", "newContactPersonLastName", "newContactPersonFirstName",
                        "newContactPersonMiddleName", "newContactPersonPhone", null)));
    }

    public static Company getUpdated() {
        return new Company(COMPANY1_ID, "updatedName",
                new Address("updatedCountry", "updatedCity", "updatedStreet", "updatedHouse", "888888"),
                new Contacts("updatedPhone", "updatedFax", "updated@gmail.com"),
                List.of(new ContactPerson("updatedContactPersonPosition", "updatedContactPersonLastName", "updatedContactPersonFirstName",
                        "updatedContactPersonMiddleName", "updatedContactPersonPhone", null)));
    }

    public static MultiValueMap<String, String> getNewParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        Company newCompany = getNew();
        params.add(NAME, newCompany.getName());
        params.add(COUNTRY, newCompany.getAddress().getCountry());
        params.add(ZIPCODE, newCompany.getAddress().getZipcode());
        params.add(CITY, newCompany.getAddress().getCity());
        params.add(STREET, newCompany.getAddress().getStreet());
        params.add(HOUSE, newCompany.getAddress().getHouse());
        params.add(PHONE, newCompany.getContacts().getPhone());
        params.add(FAX, newCompany.getContacts().getFax());
        params.add(EMAIL, newCompany.getContacts().getEmail());
        params.add(CONTACT_PERSON_POSITION, newCompany.getContactPersons().get(0).getPosition());
        params.add(CONTACT_PERSON_LAST_NAME, newCompany.getContactPersons().get(0).getLastName());
        params.add(CONTACT_PERSON_FIRST_NAME, newCompany.getContactPersons().get(0).getFirstName());
        params.add(CONTACT_PERSON_MIDDLE_NAME, newCompany.getContactPersons().get(0).getMiddleName());
        params.add(CONTACT_PERSON_PHONE, newCompany.getContactPersons().get(0).getPhone());
        return params;
    }

    public static MultiValueMap<String, String> getNewInvalidParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(NAME, "A");
        params.add(COUNTRY, "");
        params.add(ZIPCODE, "someZipcode");
        params.add(CITY, "C");
        params.add(STREET, "");
        params.add(HOUSE, "");
        params.add(CONTACT_PERSON_POSITION, "P");
        params.add(CONTACT_PERSON_LAST_NAME, "");
        params.add(CONTACT_PERSON_FIRST_NAME, "A");
        params.add(CONTACT_PERSON_MIDDLE_NAME, "");
        return params;
    }

    public static MultiValueMap<String, String> getUpdatedParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        Company updatedCompany = getUpdated();
        params.add(ID, String.valueOf(COMPANY1_ID));
        params.add(NAME, updatedCompany.getName());
        params.add(COUNTRY, updatedCompany.getAddress().getCountry());
        params.add(ZIPCODE, updatedCompany.getAddress().getZipcode());
        params.add(CITY, updatedCompany.getAddress().getCity());
        params.add(STREET, updatedCompany.getAddress().getStreet());
        params.add(HOUSE, updatedCompany.getAddress().getHouse());
        params.add(PHONE, updatedCompany.getContacts().getPhone());
        params.add(FAX, updatedCompany.getContacts().getFax());
        params.add(EMAIL, updatedCompany.getContacts().getEmail());
        params.add(CONTACT_PERSON_POSITION, updatedCompany.getContactPersons().get(0).getPosition());
        params.add(CONTACT_PERSON_LAST_NAME, updatedCompany.getContactPersons().get(0).getLastName());
        params.add(CONTACT_PERSON_FIRST_NAME, updatedCompany.getContactPersons().get(0).getFirstName());
        params.add(CONTACT_PERSON_MIDDLE_NAME, updatedCompany.getContactPersons().get(0).getMiddleName());
        params.add(CONTACT_PERSON_PHONE, updatedCompany.getContactPersons().get(0).getPhone());
        return params;
    }

    public static MultiValueMap<String, String> getUpdatedInvalidParams() {
        MultiValueMap<String, String> params = getNewInvalidParams();
        params.add(ID, String.valueOf(COMPANY1_ID));
        return params;
    }
}
