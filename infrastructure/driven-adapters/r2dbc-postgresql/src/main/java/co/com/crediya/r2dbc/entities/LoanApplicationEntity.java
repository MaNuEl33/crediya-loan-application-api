package co.com.crediya.r2dbc.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("solicitud")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class LoanApplicationEntity {
    @Id
    @Column("id_solicitud")
    private Long id;

    @Column("monto")
    private BigDecimal amount;

    @Column("plazo")
    private Integer term;

    private String email;

    @Column("id_estado")
    private Long stateId;

    @Column("id_tipo_prestamo")
    private Long loanTypeId;
}
