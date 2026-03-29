package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Item - an item listed by a seller.
 */
public class Item {
    private int id;
    private int sellerId;
    private String sellerName;    // joined from users table (convenience field)
    private String name;
    private String description;
    private BigDecimal startPrice;
    private String imagePath;
    private Timestamp createdAt;

    public Item() {}

    // ── Getters & Setters ────────────────────────────────────────────────────

    public int getId()                         { return id; }
    public void setId(int id)                  { this.id = id; }

    public int getSellerId()                   { return sellerId; }
    public void setSellerId(int sellerId)      { this.sellerId = sellerId; }

    public String getSellerName()              { return sellerName; }
    public void setSellerName(String n)        { this.sellerName = n; }

    public String getName()                    { return name; }
    public void setName(String name)           { this.name = name; }

    public String getDescription()             { return description; }
    public void setDescription(String desc)    { this.description = desc; }

    public BigDecimal getStartPrice()          { return startPrice; }
    public void setStartPrice(BigDecimal p)    { this.startPrice = p; }

    public String getImagePath()               { return imagePath; }
    public void setImagePath(String path)      { this.imagePath = path; }

    public Timestamp getCreatedAt()            { return createdAt; }
    public void setCreatedAt(Timestamp t)      { this.createdAt = t; }

    @Override
    public String toString() {
        return "Item{id=" + id + ", name='" + name + "', startPrice=" + startPrice + "}";
    }
}
