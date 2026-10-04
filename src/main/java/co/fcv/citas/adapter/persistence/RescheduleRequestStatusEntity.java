package co.fcv.citas.adapter.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "reschedule_request_statuses")
public class RescheduleRequestStatusEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 50)
    private String name;

    public RescheduleRequestStatusEntity() {}

    public RescheduleRequestStatusEntity(Short id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    public Short getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
}
