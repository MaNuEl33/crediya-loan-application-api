package co.com.crediya.r2dbc.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("estado")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class LoanApplicationStateEntity {
    @Id
    @Column("id_estado")
    private Long id;

    @Column("codigo")
    private String code;

    @Column("nombre")
    private String name;

    @Column("descripcion")
    private String description;
}
