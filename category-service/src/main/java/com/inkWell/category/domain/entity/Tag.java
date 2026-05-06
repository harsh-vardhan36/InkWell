package com.inkWell.category.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tags")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    private int usageCount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public int getUsageCount() { return usageCount; }
    public void setUsageCount(int usageCount) { this.usageCount = usageCount; }

    public static TagBuilder builder() {
        return new TagBuilder();
    }

    public static class TagBuilder {
        private Tag tag = new Tag();
        public TagBuilder id(Long id) { tag.id = id; return this; }
        public TagBuilder name(String name) { tag.name = name; return this; }
        public TagBuilder slug(String slug) { tag.slug = slug; return this; }
        public TagBuilder usageCount(int usageCount) { tag.usageCount = usageCount; return this; }
        public Tag build() { return tag; }
    }
}
