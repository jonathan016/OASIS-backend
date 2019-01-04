package com.oasis.service.implementation.employees;

import com.oasis.model.exception.BadRequestException;
import com.oasis.model.exception.DataNotFoundException;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.SupervisionModel;
import com.oasis.repository.EmployeeRepository;
import com.oasis.repository.SupervisionRepository;
import com.oasis.service.api.employees.EmployeeDetailServiceApi;
import com.oasis.service.api.employees.EmployeeListServiceApi;
import com.oasis.model.constant.service_constant.PageSizeConstant;
import com.oasis.model.constant.service_constant.ServiceConstant;
import com.oasis.service.tool.util.Regex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.oasis.model.constant.exception_constant.ErrorCodeAndMessage.DATA_NOT_FOUND;
import static com.oasis.model.constant.exception_constant.ErrorCodeAndMessage.INCORRECT_PARAMETER;

@Service
@Transactional
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class EmployeeListServiceImpl
        implements EmployeeListServiceApi {

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private SupervisionRepository supervisionRepository;

    @Autowired
    private EmployeeDetailServiceApi employeeDetailServiceApi;

    @Override
    @Cacheable(value = "employeesListData",
               unless = "#result.size() == 0")
    public Map< String, List< ? > > getEmployeesListData(
            final String username, final String query, final int page, final String sort
    )
            throws
            BadRequestException,
            DataNotFoundException {

        Map< String, List< ? > > employeesListData = new HashMap<>();

        final List< EmployeeModel > employees = getEmployeesList(username, query, page, sort);
        final List< EmployeeModel > supervisors = getSupervisorsList(employees);
        final List< String > employeePhotos = getEmployeesPhotos(employees);

        employeesListData.put("employees", employees);
        employeesListData.put("supervisors", supervisors);
        employeesListData.put("employeePhotos", employeePhotos);

        return employeesListData;
    }

    @Override
    public List< EmployeeModel > getEmployeesList(
            final String username, final String query, final int page, String sort
    )
            throws
            BadRequestException,
            DataNotFoundException {

        final boolean emptyQueryGiven = ( query != null && query.isEmpty() );
        final boolean emptySortGiven = ( sort != null && sort.isEmpty() );

        if (emptyQueryGiven || emptySortGiven) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        } else {
            final boolean useParameterSort = ( sort != null );

            if (useParameterSort) {
                final boolean properSortFormatGiven = sort.matches(Regex.REGEX_EMPLOYEE_SORT);

                if (!properSortFormatGiven) {
                    throw new BadRequestException(INCORRECT_PARAMETER);
                }
            } else {
                sort = "A";
            }

            final Set< EmployeeModel > employees;
            final long employeesCount = getEmployeesCount(username, query);
            final long availablePages = (long) Math.ceil(
                    (double) employeesCount / PageSizeConstant.EMPLOYEES_LIST_PAGE_SIZE);

            final boolean noEmployees = ( employeesCount == 0 );
            final boolean pageIndexOutOfBounds = ( ( page < 1 ) || ( page > availablePages ) );

            if (noEmployees || pageIndexOutOfBounds) {
                throw new DataNotFoundException(DATA_NOT_FOUND);
            } else {
                final boolean viewAllEmployees = ( query == null );

                if (viewAllEmployees) {
                    employees = new LinkedHashSet<>(getSortedEmployees(username, page, sort));
                } else {
                    employees = new LinkedHashSet<>(getSortedEmployeesFromQuery(page, query, sort));
                }

                // Removes top administrator
                employees.removeIf(employeeModel -> (employeeModel.getUsername().equals("admin")));

                return new ArrayList<>(employees);
            }
        }
    }

    @Override
    public long getEmployeesCount(
            final String username, final String query
    ) {

        final boolean viewAllEmployees = ( query == null );

        if (viewAllEmployees) {
            return employeeRepository.countAllByDeletedIsFalseAndUsernameIsNot(username);
        } else {
            return employeeRepository
                    .countAllByDeletedIsFalseAndUsernameContainsIgnoreCaseOrDeletedIsFalseAndNameContainsIgnoreCase(
                            query, query);
        }
    }

    private List< EmployeeModel > getSupervisorsList(
            final List< EmployeeModel > employees
    ) {

        List< EmployeeModel > supervisors = new ArrayList<>();

        for (final EmployeeModel employee : employees) {

            final SupervisionModel supervision = supervisionRepository
                    .findByDeletedIsFalseAndEmployeeUsernameEquals(employee.getUsername());

            final boolean supervisionForCurrentEmployeeExists = ( supervision != null );

            if (supervisionForCurrentEmployeeExists) {
                final EmployeeModel supervisor = employeeRepository
                        .findByDeletedIsFalseAndUsernameEquals(supervision.getSupervisorUsername());

                supervisors.add(supervisor);
            } else {    // For top administrator, who does not have any supervisor at all
                supervisors.add(null);
            }
        }

        return supervisors;
    }

    private List< String > getEmployeesPhotos(
            final List< EmployeeModel > employees
    ) {

        List< String > photos = new ArrayList<>();

        for (final EmployeeModel employee : employees) {
            photos.add(employeeDetailServiceApi.getEmployeeDetailPhoto(employee.getUsername(), employee.getPhoto()));
        }

        return photos;
    }

    private Set< EmployeeModel > getSortedEmployees(
            final String username, final int page, final String sort
    ) {

        Set< EmployeeModel > sortedEmployees = new LinkedHashSet<>();

        final int zeroBasedIndexPage = page - 1;
        final Pageable pageable = PageRequest.of(zeroBasedIndexPage, PageSizeConstant.EMPLOYEES_LIST_PAGE_SIZE);

        if (sort.equals(ServiceConstant.ASCENDING)) {
            sortedEmployees
                    .addAll(employeeRepository.findAllByDeletedIsFalseAndUsernameIsNotOrderByNameAsc(username, pageable)
                                              .getContent());
        } else {
            sortedEmployees.addAll(employeeRepository
                                           .findAllByDeletedIsFalseAndUsernameIsNotOrderByNameDesc(username, pageable)
                                           .getContent());
        }

        return sortedEmployees;
    }

    private Set< EmployeeModel > getSortedEmployeesFromQuery(
            final int page, final String query, final String sort
    ) {

        Set< EmployeeModel > sortedEmployees = new LinkedHashSet<>();

        final int zeroBasedIndexPage = page - 1;
        final Pageable pageable = PageRequest.of(zeroBasedIndexPage, PageSizeConstant.EMPLOYEES_LIST_PAGE_SIZE);

        if (sort.equals(ServiceConstant.ASCENDING)) {
            sortedEmployees.addAll(employeeRepository
                                           .findAllByDeletedIsFalseAndUsernameContainsIgnoreCaseOrDeletedIsFalseAndNameContainsIgnoreCaseOrderByNameAsc(
                                                   query, query, pageable).getContent());
        } else {
            sortedEmployees.addAll(employeeRepository
                                           .findAllByDeletedIsFalseAndUsernameContainsIgnoreCaseOrDeletedIsFalseAndNameContainsIgnoreCaseOrderByNameDesc(
                                                   query, query, pageable).getContent());
        }

        return sortedEmployees;
    }

}
