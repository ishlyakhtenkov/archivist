package ru.javaprojects.archivist.departments;

import lombok.experimental.UtilityClass;
import ru.javaprojects.archivist.departments.model.Department;
import ru.javaprojects.archivist.departments.model.Employee;
import ru.javaprojects.archivist.departments.to.DepartmentCreateTo;
import ru.javaprojects.archivist.departments.to.DepartmentUpdateTo;

@UtilityClass
public class DepartmentUtil {
    public static Department createNewDepartmentFromTo(DepartmentCreateTo departmentCreateTo) {
        return new Department(null, departmentCreateTo.getName());
    }

    public static Employee createNewEmployeeFromTo(DepartmentCreateTo departmentCreateTo, Department department) {
        return new Employee(null, departmentCreateTo.getBossLastName(),
                departmentCreateTo.getBossFirstName(), departmentCreateTo.getBossMiddleName(),
                departmentCreateTo.getBossPhone(), departmentCreateTo.getBossEmail(), department);
    }

    public static DepartmentUpdateTo asUpdateTo(Department department) {
        return new DepartmentUpdateTo(department.getId(), department.getName(), department.getBoss());
    }

    public static Department updateFromTo(Department department, DepartmentUpdateTo departmentUpdateTo) {
        department.setName(departmentUpdateTo.getName());
        department.setBoss(departmentUpdateTo.getBoss());
        return department;
    }
}
