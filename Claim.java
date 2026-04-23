package com.lostfound.models;

import java.sql.Timestamp;

/**
 * Claim.java
 * ------------------------------------------------
 * Represents a row in the 'claims' table.
 * A user submits a claim on a found item.
 * Admin then approves or rejects it.
 *
 * status lifecycle: PENDING → APPROVED | REJECTED
 * ------------------------------------------------
 */
public class Claim {

    private int       claimId;
    private int       itemId;
    private String    itemTitle;       // joined from items table
    private String    itemRefCode;     // joined from items table
    private int       claimedBy;      // user_id of claimant
    private String    claimedByName;  // joined from users table
    private String    proofDescription;
    private String    status;          // "PENDING","APPROVED","REJECTED"
    private Timestamp claimedAt;

    // ── Constructors ──────────────────────────────────────────
    public Claim() {}

    public Claim(int itemId, int claimedBy, String proofDescription) {
        this.itemId           = itemId;
        this.claimedBy        = claimedBy;
        this.proofDescription = proofDescription;
        this.status           = "PENDING"; // always starts as PENDING
    }

    // ── Getters ───────────────────────────────────────────────
    public int       getClaimId()          { return claimId;          }
    public int       getItemId()           { return itemId;           }
    public String    getItemTitle()        { return itemTitle;        }
    public String    getItemRefCode()      { return itemRefCode;      }
    public int       getClaimedBy()        { return claimedBy;        }
    public String    getClaimedByName()    { return claimedByName;    }
    public String    getProofDescription() { return proofDescription; }
    public String    getStatus()           { return status;           }
    public Timestamp getClaimedAt()        { return claimedAt;        }

    // ── Setters ───────────────────────────────────────────────
    public void setClaimId(int claimId)                     { this.claimId          = claimId;          }
    public void setItemId(int itemId)                       { this.itemId           = itemId;           }
    public void setItemTitle(String itemTitle)              { this.itemTitle        = itemTitle;        }
    public void setItemRefCode(String itemRefCode)          { this.itemRefCode      = itemRefCode;      }
    public void setClaimedBy(int claimedBy)                 { this.claimedBy        = claimedBy;        }
    public void setClaimedByName(String claimedByName)      { this.claimedByName    = claimedByName;    }
    public void setProofDescription(String proofDescription){ this.proofDescription = proofDescription; }
    public void setStatus(String status)                    { this.status           = status;           }
    public void setClaimedAt(Timestamp claimedAt)           { this.claimedAt        = claimedAt;        }

    // ── Helper methods ────────────────────────────────────────
    public boolean isPending()  { return "PENDING".equals(status);  }
    public boolean isApproved() { return "APPROVED".equals(status); }
    public boolean isRejected() { return "REJECTED".equals(status); }

    @Override
    public String toString() {
        return "Claim{" +
               "claimId=" + claimId  +
               ", itemId=" + itemId  +
               ", claimedBy=" + claimedBy +
               ", status='" + status + '\'' +
               '}';
    }
}