package com.poultry.platform.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "category_attribute_defs",
        uniqueConstraints = @UniqueConstraint(columnNames = {"category_id", "field_key"}))
public class CategoryAttributeDef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "field_key", nullable = false, length = 60)
    private String fieldKey;

    @Column(nullable = false, length = 80)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttributeDataType dataType;

    @Column(nullable = false)
    private boolean required;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<String> enumOptions = new ArrayList<>();

    @Column(length = 120)
    private String placeholder;

    @Column(nullable = false)
    private int sortOrder;

    @Column(nullable = false)
    private boolean matchable;

    @Column(nullable = false)
    private boolean showInList = true;

    @Column(nullable = false)
    private boolean showInNotify = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public String getFieldKey() { return fieldKey; }
    public void setFieldKey(String fieldKey) { this.fieldKey = fieldKey; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public AttributeDataType getDataType() { return dataType; }
    public void setDataType(AttributeDataType dataType) { this.dataType = dataType; }
    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }
    public List<String> getEnumOptions() { return enumOptions; }
    public void setEnumOptions(List<String> enumOptions) { this.enumOptions = enumOptions; }
    public String getPlaceholder() { return placeholder; }
    public void setPlaceholder(String placeholder) { this.placeholder = placeholder; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
    public boolean isMatchable() { return matchable; }
    public void setMatchable(boolean matchable) { this.matchable = matchable; }
    public boolean isShowInList() { return showInList; }
    public void setShowInList(boolean showInList) { this.showInList = showInList; }
    public boolean isShowInNotify() { return showInNotify; }
    public void setShowInNotify(boolean showInNotify) { this.showInNotify = showInNotify; }
}
