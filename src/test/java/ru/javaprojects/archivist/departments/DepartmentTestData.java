package ru.javaprojects.archivist.departments;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.archivist.MatcherFactory;
import ru.javaprojects.archivist.common.model.Person;
import ru.javaprojects.archivist.departments.model.Department;
import ru.javaprojects.archivist.departments.model.Employee;

import static ru.javaprojects.archivist.CommonTestData.ID;
import static ru.javaprojects.archivist.CommonTestData.NAME;

public class DepartmentTestData {
    public static final MatcherFactory.Matcher<Department> DEPARTMENT_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(Department.class);

    public static final MatcherFactory.Matcher<Employee> EMPLOYEE_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(Employee.class);

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
    public static final String EMPLOYEE_ATTRIBUTE = "employee";

    public static final String BOSS_LAST_NAME = "boss.lastName";
    public static final String BOSS_FIRST_NAME = "boss.firstName";
    public static final String BOSS_MIDDLE_NAME = "boss.middleName";
    public static final String BOSS_PHONE = "boss.phone";
    public static final String BOSS_EMAIL = "boss.email";

    public static final String LAST_NAME = "lastName";
    public static final String FIRST_NAME = "firstName";
    public static final String MIDDLE_NAME = "middleName";
    public static final String PHONE = "phone";
    public static final String EMAIL = "email";
    public static final String FIRED = "fired";

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

    public static final long DEP2_EMPLOYEE1_ID = 100065L;
    public static final long DEP2_EMPLOYEE2_ID = 100066L;

    public static final Employee dep2Employee1 = new Employee(DEP2_EMPLOYEE1_ID, "Kasparov", "Iosif", "Matveevich", "1-22-44", "i.kasparov@npo.lan");
    public static final Employee dep2Employee2 = new Employee(DEP2_EMPLOYEE2_ID, "Sidelnikov", "Vasiliy", "Kuzmich", "1-37-88", "v.sidelnikov@npo.lan");

    static {
        department2.addEmployee(dep2Employee1);
        department2.addEmployee(dep2Employee2);
    }

    public static Department getNewDepartment() {
        return new Department(null, "newName", new Person("newLastName", "newFirstName", "newMiddleName", "newPhone", "new@gmail.com"));
    }

    public static MultiValueMap<String, String> getNewDepartmentParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        Department newDepartment = getNewDepartment();
        params.add(NAME, newDepartment.getName());
        params.add(BOSS_LAST_NAME, newDepartment.getBoss().getLastName());
        params.add(BOSS_FIRST_NAME, newDepartment.getBoss().getFirstName());
        params.add(BOSS_MIDDLE_NAME, newDepartment.getBoss().getMiddleName());
        params.add(BOSS_PHONE, newDepartment.getBoss().getPhone());
        params.add(BOSS_EMAIL, newDepartment.getBoss().getEmail());
        return params;
    }

    public static MultiValueMap<String, String> getNewDepartmentInvalidParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(NAME, "A");
        params.add(BOSS_LAST_NAME, "B");
        params.add(BOSS_FIRST_NAME, "");
        params.add(BOSS_MIDDLE_NAME, "A");
        params.add(BOSS_PHONE, "<p>dsfsdf<p>");
        return params;
    }

    public static Department getUpdatedDepartment() {
        return new Department(DEPARTMENT1_ID, "updatedName",
                new Person("updatedLastName", "updatedFirstName", "updatedMiddleName", "updatedPhone", "updated@gmail.com"));
    }

    public static MultiValueMap<String, String> getUpdatedDepartmentParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        Department updatedDepartment = getUpdatedDepartment();
        params.add(ID, String.valueOf(DEPARTMENT1_ID));
        params.add(NAME, updatedDepartment.getName());
        params.add(BOSS_LAST_NAME, updatedDepartment.getBoss().getLastName());
        params.add(BOSS_FIRST_NAME, updatedDepartment.getBoss().getFirstName());
        params.add(BOSS_MIDDLE_NAME, updatedDepartment.getBoss().getMiddleName());
        params.add(BOSS_PHONE, updatedDepartment.getBoss().getPhone());
        params.add(BOSS_EMAIL, updatedDepartment.getBoss().getEmail());
        return params;
    }

    public static MultiValueMap<String, String> getUpdatedDepartmentInvalidParams() {
        MultiValueMap<String, String> params = getNewDepartmentInvalidParams();
        params.add(ID, String.valueOf(DEPARTMENT1_ID));
        return params;
    }

    public static Employee getNewEmployee() {
        return new Employee(null, "newEmpLastName", "newEmpFirstName", "newEmpMiddleName", "newPhoneNum",
                "newEmail@gmail.com", department2);
    }

    public static MultiValueMap<String, String> getNewEmployeeParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        Employee newEmployee = getNewEmployee();
        params.add(LAST_NAME, newEmployee.getLastName());
        params.add(FIRST_NAME, newEmployee.getFirstName());
        params.add(MIDDLE_NAME, newEmployee.getMiddleName());
        params.add(PHONE, newEmployee.getPhone());
        params.add(EMAIL, newEmployee.getEmail());
        params.add(DEPARTMENT_ATTRIBUTE, String.valueOf(DEPARTMENT2_ID));
        return params;
    }

    public static MultiValueMap<String, String> getNewEmployeeInvalidParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(LAST_NAME, "A");
        params.add(FIRST_NAME, "");
        params.add(MIDDLE_NAME, "A");
        params.add(PHONE, "");
        params.add(EMAIL, "dfvfgdgdf");
        params.add(DEPARTMENT_ATTRIBUTE, String.valueOf(DEPARTMENT2_ID));
        return params;
    }

    public static Employee getUpdatedEmployee() {
        return new Employee(DEP2_EMPLOYEE1_ID, "updatedEmpLastName", "updatedEmpFirstName", "updatedEmpMiddleName",
                "updatedPhoneNum", "updatedEmail@gmail.com", department1);
    }

    public static MultiValueMap<String, String> getUpdatedEmployeeParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        Employee updatedEmployee = getUpdatedEmployee();
        params.add(ID, String.valueOf(DEP2_EMPLOYEE1_ID));
        params.add(LAST_NAME, updatedEmployee.getLastName());
        params.add(FIRST_NAME, updatedEmployee.getFirstName());
        params.add(MIDDLE_NAME, updatedEmployee.getMiddleName());
        params.add(PHONE, updatedEmployee.getPhone());
        params.add(EMAIL, updatedEmployee.getEmail());
        params.add(DEPARTMENT_ATTRIBUTE, String.valueOf(DEPARTMENT1_ID));
        return params;
    }

    public static MultiValueMap<String, String> getUpdatedEmployeeInvalidParams() {
        MultiValueMap<String, String> params = getNewEmployeeInvalidParams();
        params.add(ID, String.valueOf(DEP2_EMPLOYEE1_ID));
        return params;
    }
}
