package ru.javaprojects.archivist.departments;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.archivist.MatcherFactory;
import ru.javaprojects.archivist.common.model.Person;

import static ru.javaprojects.archivist.CommonTestData.ID;
import static ru.javaprojects.archivist.CommonTestData.NAME_PARAM;

public class DepartmentTestData {
    public static final MatcherFactory.Matcher<Department> DEPARTMENT_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(Department.class);

    public static final long DEPARTMENT1_ID = 100009;
    public static final long DEPARTMENT2_ID = 100010;
    public static final long DEPARTMENT3_ID = 100011;
    public static final long DEPARTMENT4_ID = 100012;
    public static final long DEPARTMENT5_ID = 100013;
    public static final String DEPARTMENT1_NAME = "KTK-40";
    public static final String DEPARTMENT2_NAME = "NIO-6";
    public static final String DEPARTMENT5_NAME = "DEP-33";

    public static final String DEPARTMENTS_ATTRIBUTE = "departments";
    public static final String DEPARTMENT_ATTRIBUTE = "department";

    public static final String BOSS_LAST_NAME_PARAM = "boss.lastName";
    public static final String BOSS_FIRST_NAME_PARAM = "boss.firstName";
    public static final String BOSS_MIDDLE_NAME_PARAM = "boss.middleName";
    public static final String BOSS_PHONE_PARAM = "boss.phone";
    public static final String BOSS_EMAIL_PARAM = "boss.email";


    public static final Department department1 = new Department(DEPARTMENT1_ID, DEPARTMENT1_NAME,
            new Person("Sokolov", "Alexandr", "Ivanovich", "1-32-98", "a.sokolov@npo.lan"));
    public static final Department department2 = new Department(DEPARTMENT2_ID, DEPARTMENT2_NAME,
            new Person("Ivanov", "Petr", "Alexandrovich", "1-34-63", "p.ivanov@npo.lan"));
    public static final Department department3 = new Department(DEPARTMENT3_ID, "NIO-8",
            new Person("Kozlov", "Ivan", "Ivanovich", "1-44-12", "i.kozlov@npo.lan"));
    public static final Department department4 = new Department(DEPARTMENT4_ID, "DEP-25",
            new Person("Sidorov", "Alexandr", "Petrovich", "1-36-78", "a.sidorov@npo.lan"));
    public static final Department department5 = new Department(DEPARTMENT5_ID, "DEP-33",
            new Person("Petrov", "Vladimir", "Ivanovich", "1-45-12", "v.petrov@npo.lan"));

    public static Department getNew() {
        return new Department(null, "newName", new Person("newLastName", "newFirstName", "newMiddleName", "newPhone", "new@gmail.com"));
    }

    public static MultiValueMap<String, String> getNewParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        Department newDepartment = getNew();
        params.add(NAME_PARAM, newDepartment.getName());
        params.add(BOSS_LAST_NAME_PARAM, newDepartment.getBoss().getLastName());
        params.add(BOSS_FIRST_NAME_PARAM, newDepartment.getBoss().getFirstName());
        params.add(BOSS_MIDDLE_NAME_PARAM, newDepartment.getBoss().getMiddleName());
        params.add(BOSS_PHONE_PARAM, newDepartment.getBoss().getPhone());
        params.add(BOSS_EMAIL_PARAM, newDepartment.getBoss().getEmail());
        return params;
    }

    public static MultiValueMap<String, String> getNewInvalidParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(NAME_PARAM, "A");
        params.add(BOSS_LAST_NAME_PARAM, "B");
        params.add(BOSS_FIRST_NAME_PARAM, "");
        params.add(BOSS_MIDDLE_NAME_PARAM, "A");
        params.add(BOSS_PHONE_PARAM, "<p>dsfsdf<p>");
        return params;
    }

    public static Department getUpdated() {
        return new Department(DEPARTMENT1_ID, "updatedName",
                new Person("updatedLastName", "updatedFirstName", "updatedMiddleName", "updatedPhone", "updated@gmail.com"));
    }

    public static MultiValueMap<String, String> getUpdatedParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        Department updatedDepartment = getUpdated();
        params.add(ID, DEPARTMENT1_ID + "");
        params.add(NAME_PARAM, updatedDepartment.getName());
        params.add(BOSS_LAST_NAME_PARAM, updatedDepartment.getBoss().getLastName());
        params.add(BOSS_FIRST_NAME_PARAM, updatedDepartment.getBoss().getFirstName());
        params.add(BOSS_MIDDLE_NAME_PARAM, updatedDepartment.getBoss().getMiddleName());
        params.add(BOSS_PHONE_PARAM, updatedDepartment.getBoss().getPhone());
        params.add(BOSS_EMAIL_PARAM, updatedDepartment.getBoss().getEmail());
        return params;
    }

    public static MultiValueMap<String, String> getUpdatedInvalidParams() {
        MultiValueMap<String, String> params = getNewInvalidParams();
        params.add(ID, DEPARTMENT1_ID + "");
        return params;
    }
}
