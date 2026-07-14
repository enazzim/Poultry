package com.poultry.platform.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * 플랫폼의 1급 주체(농가·파트너·운영).
 * EggFactory/MES는 필수 부모가 아니라, FARM 조직에 대한 선택적 연동(capability)이다.
 */
@Entity
@Table(name = "organizations")
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole orgRole;

    @Column(length = 20)
    private String regionCode;

    @Column(length = 40)
    private String phone;

    /**
     * 외부 MES(EggFactory 등) 매핑용 농가 코드. FARM만 사용하며 nullable.
     * null이면 수동(MANUAL) 공고만 가능하고 MES webhook 대상이 아니다.
     */
    @Column(length = 60, unique = true)
    private String farmCode;

    @Column(nullable = false)
    private boolean approved = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    /** MES 옵트인 여부 — farmCode 등록 시에만 true */
    public boolean isMesLinked() {
        return farmCode != null && !farmCode.isBlank();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public UserRole getOrgRole() { return orgRole; }
    public void setOrgRole(UserRole orgRole) { this.orgRole = orgRole; }
    public String getRegionCode() { return regionCode; }
    public void setRegionCode(String regionCode) { this.regionCode = regionCode; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getFarmCode() { return farmCode; }
    public void setFarmCode(String farmCode) { this.farmCode = farmCode; }
    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }
    public Instant getCreatedAt() { return createdAt; }
}
