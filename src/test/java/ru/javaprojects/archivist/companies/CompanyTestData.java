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

    public static final String NAME = "name";
    public static final String COUNTRY_PARAM = "address.country";
    public static final String ZIPCODE_PARAM = "address.zipcode";
    public static final String CITY_PARAM = "address.city";
    public static final String STREET_PARAM = "address.street";
    public static final String HOUSE_PARAM = "address.house";
    public static final String PHONE_PARAM = "contacts.phone";
    public static final String FAX_PARAM = "contacts.fax";
    public static final String EMAIL_PARAM = "contacts.email";
    public static final String CONTACT_PERSON_POSITION_PARAM = "contactPersons[0].position";
    public static final String CONTACT_PERSON_LAST_NAME_PARAM = "contactPersons[0].lastName";
    public static final String CONTACT_PERSON_FIRST_NAME_PARAM = "contactPersons[0].firstName";
    public static final String CONTACT_PERSON_MIDDLE_NAME_PARAM = "contactPersons[0].middleName";
    public static final String CONTACT_PERSON_PHONE_PARAM = "contactPersons[0].phone";

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
        params.add(COUNTRY_PARAM, newCompany.getAddress().getCountry());
        params.add(ZIPCODE_PARAM, newCompany.getAddress().getZipcode());
        params.add(CITY_PARAM, newCompany.getAddress().getCity());
        params.add(STREET_PARAM, newCompany.getAddress().getStreet());
        params.add(HOUSE_PARAM, newCompany.getAddress().getHouse());
        params.add(PHONE_PARAM, newCompany.getContacts().getPhone());
        params.add(FAX_PARAM, newCompany.getContacts().getFax());
        params.add(EMAIL_PARAM, newCompany.getContacts().getEmail());
        params.add(CONTACT_PERSON_POSITION_PARAM, newCompany.getContactPersons().get(0).getPosition());
        params.add(CONTACT_PERSON_LAST_NAME_PARAM, newCompany.getContactPersons().get(0).getLastName());
        params.add(CONTACT_PERSON_FIRST_NAME_PARAM, newCompany.getContactPersons().get(0).getFirstName());
        params.add(CONTACT_PERSON_MIDDLE_NAME_PARAM, newCompany.getContactPersons().get(0).getMiddleName());
        params.add(CONTACT_PERSON_PHONE_PARAM, newCompany.getContactPersons().get(0).getPhone());
        return params;
    }

    public static MultiValueMap<String, String> getNewInvalidParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(NAME, "A");
        params.add(COUNTRY_PARAM, "");
        params.add(ZIPCODE_PARAM, "someZipcode");
        params.add(CITY_PARAM, "C");
        params.add(STREET_PARAM, "");
        params.add(HOUSE_PARAM, "");
        params.add(CONTACT_PERSON_POSITION_PARAM, "P");
        params.add(CONTACT_PERSON_LAST_NAME_PARAM, "");
        params.add(CONTACT_PERSON_FIRST_NAME_PARAM, "A");
        params.add(CONTACT_PERSON_MIDDLE_NAME_PARAM, "");
        return params;
    }

    public static MultiValueMap<String, String> getUpdatedParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        Company updatedCompany = getUpdated();
        params.add(ID, COMPANY1_ID + "");
        params.add(NAME, updatedCompany.getName());
        params.add(COUNTRY_PARAM, updatedCompany.getAddress().getCountry());
        params.add(ZIPCODE_PARAM, updatedCompany.getAddress().getZipcode());
        params.add(CITY_PARAM, updatedCompany.getAddress().getCity());
        params.add(STREET_PARAM, updatedCompany.getAddress().getStreet());
        params.add(HOUSE_PARAM, updatedCompany.getAddress().getHouse());
        params.add(PHONE_PARAM, updatedCompany.getContacts().getPhone());
        params.add(FAX_PARAM, updatedCompany.getContacts().getFax());
        params.add(EMAIL_PARAM, updatedCompany.getContacts().getEmail());
        params.add(CONTACT_PERSON_POSITION_PARAM, updatedCompany.getContactPersons().get(0).getPosition());
        params.add(CONTACT_PERSON_LAST_NAME_PARAM, updatedCompany.getContactPersons().get(0).getLastName());
        params.add(CONTACT_PERSON_FIRST_NAME_PARAM, updatedCompany.getContactPersons().get(0).getFirstName());
        params.add(CONTACT_PERSON_MIDDLE_NAME_PARAM, updatedCompany.getContactPersons().get(0).getMiddleName());
        params.add(CONTACT_PERSON_PHONE_PARAM, updatedCompany.getContactPersons().get(0).getPhone());
        return params;
    }

    public static MultiValueMap<String, String> getUpdatedInvalidParams() {
        MultiValueMap<String, String> params = getNewInvalidParams();
        params.add(ID, COMPANY1_ID + "");
        return params;
    }
}
