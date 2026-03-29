package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Auction - represents a timed auction for one item.
 */
public class Auction {
    private int id;
    private int itemId;
    private Timestamp startTime;
    private Timestamp endTime;
    private String status;         // scheduled | active | ended | cancelled
    private Integer winnerId;      // null until seller selects winner
    private String winnerName;     // joined convenience field
    private BigDecimal winningBid;
    private Timestamp createdAt;

    // ── Convenience fields joined from related tables ─────────────────────
    private Item item;             // the associated item (populated by DAO)
    private BigDecimal currentHighestBid;   // highest bid so far
    private int totalBids;                  // count of bids

    public Auction() {}

    // ── Status helpers ────────────────────────────────────────────────────────

    /** Returns true if the auction is currently accepting bids. */
    public boolean isActive() {
        long now = System.currentTimeMillis();
        return "active".equals(status)
            && startTime != null && endTime != null
            && now >= startTime.getTime()
            && now <= endTime.getTime();
    }

    public boolean isScheduled() { return "scheduled".equals(status); }
    public boolean isEnded()     { return "ended".equals(status); }

    /** Minimum bid a buyer must place (start_price OR current highest + 1 cent). */
    public BigDecimal getMinimumNextBid() {
        if (currentHighestBid != null) {
            return currentHighestBid.add(new BigDecimal("0.01"));
        }
        return item != null ? item.getStartPrice() : BigDecimal.ZERO;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int getId()                              { return id; }
    public void setId(int id)                       { this.id = id; }

    public int getItemId()                          { return itemId; }
    public void setItemId(int itemId)               { this.itemId = itemId; }

    public Timestamp getStartTime()                 { return startTime; }
    public void setStartTime(Timestamp startTime)   { this.startTime = startTime; }

    public Timestamp getEndTime()                   { return endTime; }
    public void setEndTime(Timestamp endTime)       { this.endTime = endTime; }

    public String getStatus()                       { return status; }
    public void setStatus(String status)            { this.status = status; }

    public Integer getWinnerId()                    { return winnerId; }
    public void setWinnerId(Integer winnerId)       { this.winnerId = winnerId; }

    public String getWinnerName()                   { return winnerName; }
    public void setWinnerName(String winnerName)    { this.winnerName = winnerName; }

    public BigDecimal getWinningBid()               { return winningBid; }
    public void setWinningBid(BigDecimal wb)        { this.winningBid = wb; }

    public Timestamp getCreatedAt()                 { return createdAt; }
    public void setCreatedAt(Timestamp createdAt)   { this.createdAt = createdAt; }

    public Item getItem()                           { return item; }
    public void setItem(Item item)                  { this.item = item; }

    public BigDecimal getCurrentHighestBid()        { return currentHighestBid; }
    public void setCurrentHighestBid(BigDecimal b)  { this.currentHighestBid = b; }

    public int getTotalBids()                       { return totalBids; }
    public void setTotalBids(int totalBids)         { this.totalBids = totalBids; }

    @Override
    public String toString() {
        return "Auction{id=" + id + ", itemId=" + itemId + ", status='" + status + "'}";
    }
}
