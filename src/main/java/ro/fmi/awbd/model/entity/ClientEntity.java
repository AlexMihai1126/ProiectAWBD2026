package ro.fmi.awbd.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "client")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String name;

    @Email
    @Size(max = 200)
    @Column(length = 200)
    private String email;

    @Size(max = 30)
    @Column(length = 30)
    private String phone;

    @Size(max = 1000)
    @Column(length = 1000)
    private String notes;
}

