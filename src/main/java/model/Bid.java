package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Bid - a single bid placed by a buyer on an auction.
 */
public class Bid {
    private int id;
    private int auctionId;
    private int bidderId;
    private String bidderName;     // joined convenience field
    private BigDecimal bidAmount;
    private Timestamp bidTime;

    public Bid() {}

    public Bid(int auctionId, int bidderId, BigDecimal bidAmount) {
        this.auctionId = auctionId;
        this.bidderId  = bidderId;
        this.bidAmount = bidAmount;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }

    public int getAuctionId()                   { return auctionId; }
    public void setAuctionId(int auctionId)     { this.auctionId = auctionId; }

    public int getBidderId()                    { return bidderId; }
    public void setBidderId(int bidderId)       { this.bidderId = bidderId; }

    public String getBidderName()               { return bidderName; }
    public void setBidderName(String name)      { this.bidderName = name; }

    public BigDecimal getBidAmount()            { return bidAmount; }
    public void setBidAmount(BigDecimal amt)    { this.bidAmount = amt; }

    public Timestamp getBidTime()               { return bidTime; }
    public void setBidTime(Timestamp bidTime)   { this.bidTime = bidTime; }

    @Override
    public String toString() {
        return "Bid{id=" + id + ", auctionId=" + auctionId + ", amount=" + bidAmount + "}";
    }
}
