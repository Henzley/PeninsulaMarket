package za.ac.cput.marketplace.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "listings")
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    private BigDecimal price;

    private String location;

    private String contactInfo;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    @ElementCollection
    private java.util.List<String> imageUrls = new java.util.ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;

    public enum Status {
        PENDING, APPROVED, REJECTED
    }

    // Getters and setters
    public java.util.List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(java.util.List<String> imageUrls) { this.imageUrls = imageUrls; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }
}