package model;

import java.sql.Timestamp;

/**
 * Notification - per-user message generated when a seller picks a winner.
 * type: "win" → "You won the bid!"   "loss" → "You did not win the bid"
 */
public class Notification {
    private int id;
    private int userId;
    private int auctionId;
    private String type;        // "win" | "loss"
    private String message;
    private boolean read;
    private Timestamp createdAt;

    private String auctionItemName; // joined convenience field

    public Notification() {}

    public int getId()                        { return id; }
    public void setId(int id)                 { this.id = id; }

    public int getUserId()                    { return userId; }
    public void setUserId(int userId)         { this.userId = userId; }

    public int getAuctionId()                 { return auctionId; }
    public void setAuctionId(int auctionId)   { this.auctionId = auctionId; }

    public String getType()                   { return type; }
    public void setType(String type)          { this.type = type; }

    public String getMessage()                { return message; }
    public void setMessage(String message)    { this.message = message; }

    public boolean isRead()                   { return read; }
    public void setRead(boolean read)         { this.read = read; }

    public Timestamp getCreatedAt()           { return createdAt; }
    public void setCreatedAt(Timestamp t)     { this.createdAt = t; }

    public String getAuctionItemName()        { return auctionItemName; }
    public void setAuctionItemName(String n)  { this.auctionItemName = n; }

    public boolean isWin()  { return "win".equals(type);  }
    public boolean isLoss() { return "loss".equals(type); }
}
