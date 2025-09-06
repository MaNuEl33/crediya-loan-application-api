package co.com.crediya.r2dbc;

import co.com.crediya.model.manualvalidationapplicationreport.ManualValidationApplicationReport;
import co.com.crediya.model.manualvalidationapplicationreport.ManualValidationApplicationReportElement;
import co.com.crediya.model.manualvalidationapplicationreport.ManualValidationApplicationReportFilter;
import co.com.crediya.model.manualvalidationapplicationreport.gateways.ManualValidationApplicationReportRepository;
import co.com.crediya.r2dbc.utils.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageImpl;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

@Repository
@Log4j2
@RequiredArgsConstructor
public class ManualValidationApplicationReportRepositoryAdapter
        implements ManualValidationApplicationReportRepository {

    private final DatabaseClient databaseClient;

    @Override
    public Mono<ManualValidationApplicationReport> getManualValidationApplicationReport(
            ManualValidationApplicationReportFilter filter, int pageNumber, int pageSize) {
        log.info("Retrieving the manual validation application report from the database.");

        final var monoCount = this.buildCountSql(filter, pageNumber, pageSize)
                .fetch().one()
                .map(this::toTotalRows)
                .subscribeOn(Schedulers.parallel());

        final var monoList = this.buildListSql(filter, pageNumber, pageSize)
                .fetch().all()
                .map(this::toManualValidationApplicationReportElement)
                .collectList()
                .subscribeOn(Schedulers.parallel());

        return Mono.zip(monoCount, monoList)
                .map(t -> PaginationUtils.buildPageImpl(t.getT2(), pageNumber, pageSize, t.getT1()))
                .map(this::toManualValidationApplicationReport)
                .doOnSuccess(r -> log.info("Report retrieved successfully from the database."))
                .doOnError(err -> log.error("Error retrieving the manual validation application report from the database.", err));
    }

    private DatabaseClient.GenericExecuteSpec buildCountSql(
            ManualValidationApplicationReportFilter filter, int pageNumber, int pageSize) {

        var sql = this.databaseClient.sql("SELECT * FROM fn_reporte_solicitudes_validacion_manual_conteo" +
                "(:term, :email, :loanTypeId, :applicationStateId, :pageNumber, :pageSize) AS total");

        return this.bindParameters(filter, pageNumber, pageSize, sql);
    }

    private DatabaseClient.GenericExecuteSpec buildListSql(
            ManualValidationApplicationReportFilter filter, int pageNumber, int pageSize) {

        var sql = this.databaseClient.sql("SELECT * FROM fn_reporte_solicitudes_validacion_manual_listado" +
                "(:term, :email, :loanTypeId, :applicationStateId, :pageNumber, :pageSize)");

        return this.bindParameters(filter, pageNumber, pageSize, sql);
    }

    private DatabaseClient.GenericExecuteSpec bindParameters(
            ManualValidationApplicationReportFilter filter, int pageNumber, int pageSize,
            DatabaseClient.GenericExecuteSpec sql) {

        if (Objects.nonNull(filter)) {
            sql = this.addTermFilter(sql, filter.getTerm());
            sql = this.addEmailFilter(sql, filter.getEmail());
            sql = this.addLoanTypeIdFilter(sql, filter.getLoanTypeId());
            sql = this.addApplicationStateIdFilter(sql, filter.getApplicationStateId());
        }

        sql = this.addPagination(sql, pageNumber, pageSize);

        return sql;
    }

    private Integer toTotalRows(Map<String, Object> row) {
        return (Integer) row.get("total");
    }

    private ManualValidationApplicationReportElement toManualValidationApplicationReportElement(Map<String, Object> row) {
        return ManualValidationApplicationReportElement.builder()
                .applicationId((Long) row.get("id_solicitud"))
                .amount((BigDecimal) row.get("monto"))
                .term((Integer) row.get("plazo"))
                .email((String) row.get("email"))
                .loanType((String) row.get("tipo_prestamo"))
                .interestRate((BigDecimal) row.get("tasa_interes"))
                .applicationState((String) row.get("estado"))
                .build();
    }

    private ManualValidationApplicationReport toManualValidationApplicationReport(
            PageImpl<ManualValidationApplicationReportElement> pageImpl) {

        return ManualValidationApplicationReport.builder()
                .applications(pageImpl.getContent())
                .totalApplications(pageImpl.getTotalElements())
                .pageNumber(pageImpl.getPageable().getPageNumber() + 1)
                .pageSize(pageImpl.getPageable().getPageSize())
                .totalPages(pageImpl.getTotalPages())
                .isFirstPage(pageImpl.isFirst())
                .isLastPage(pageImpl.isLast())
                .isEmpty(pageImpl.isEmpty())
                .build();
    }

    private DatabaseClient.GenericExecuteSpec addTermFilter(DatabaseClient.GenericExecuteSpec sql, Integer term) {
        final var parameterName = "term";

        if (Objects.isNull(term)) {
            return sql.bindNull(parameterName, Integer.class);
        }

        return sql.bind(parameterName, term);
    }

    private DatabaseClient.GenericExecuteSpec addEmailFilter(DatabaseClient.GenericExecuteSpec sql, String email) {
        final var parameterName = "email";

        if (Objects.isNull(email) || email.trim().isEmpty()) {
            return sql.bindNull(parameterName, String.class);
        }

        return  sql.bind(parameterName, email);
    }

    private DatabaseClient.GenericExecuteSpec addLoanTypeIdFilter(
            DatabaseClient.GenericExecuteSpec sql, Long loanTypeId) {
        final var parameterName = "loanTypeId";

        if (Objects.isNull(loanTypeId)) {
            return sql.bindNull(parameterName, Long.class);
        }

        return sql.bind(parameterName, loanTypeId);
    }

    private DatabaseClient.GenericExecuteSpec addApplicationStateIdFilter(
            DatabaseClient.GenericExecuteSpec sql, Long applicationStateId) {
        final var parameterName = "applicationStateId";

        if (Objects.isNull(applicationStateId)) {
            return sql.bindNull(parameterName, Long.class);
        }

        return sql.bind(parameterName, applicationStateId);
    }

    private  DatabaseClient.GenericExecuteSpec addPagination(
            DatabaseClient.GenericExecuteSpec sql, int pageNumber, int pageSize) {
        return sql.bind("pageNumber", pageNumber).bind("pageSize", pageSize);
    }
}
