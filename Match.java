package com.lostfound.models;

import java.sql.Timestamp;

/**
 * Match.java
 * ------------------------------------------------
 * Represents a row in the 'matches' table.
 * Stores pairs of Lost + Found items that the
 * smart matching algorithm identified as related.
 * ------------------------------------------------
 */
public class Match {

    private int       matchId;
    private int       lostItemId;
    private int       foundItemId;

    // Extra fields populated via JOIN for display in UI
    private String    lostItemTitle;
    private String    lostItemRefCode;
    private String    foundItemTitle;
    private String    foundItemRefCode;
    private String    lostItemLocation;
    private String    foundItemLocation;

    private Timestamp matchDate;

    // ── Constructors ──────────────────────────────────────────
    public Match() {}

    public Match(int lostItemId, int foundItemId) {
        this.lostItemId  = lostItemId;
        this.foundItemId = foundItemId;
    }

    // ── Getters ───────────────────────────────────────────────
    public int       getMatchId()           { return matchId;           }
    public int       getLostItemId()        { return lostItemId;        }
    public int       getFoundItemId()       { return foundItemId;       }
    public String    getLostItemTitle()     { return lostItemTitle;     }
    public String    getLostItemRefCode()   { return lostItemRefCode;   }
    public String    getFoundItemTitle()    { return foundItemTitle;    }
    public String    getFoundItemRefCode()  { return foundItemRefCode;  }
    public String    getLostItemLocation()  { return lostItemLocation;  }
    public String    getFoundItemLocation() { return foundItemLocation; }
    public Timestamp getMatchDate()         { return matchDate;         }

    // ── Setters ───────────────────────────────────────────────
    public void setMatchId(int matchId)                      { this.matchId           = matchId;           }
    public void setLostItemId(int lostItemId)                { this.lostItemId        = lostItemId;        }
    public void setFoundItemId(int foundItemId)              { this.foundItemId       = foundItemId;       }
    public void setLostItemTitle(String lostItemTitle)       { this.lostItemTitle     = lostItemTitle;     }
    public void setLostItemRefCode(String lostItemRefCode)   { this.lostItemRefCode   = lostItemRefCode;   }
    public void setFoundItemTitle(String foundItemTitle)     { this.foundItemTitle    = foundItemTitle;    }
    public void setFoundItemRefCode(String foundItemRefCode) { this.foundItemRefCode  = foundItemRefCode;  }
    public void setLostItemLocation(String lostItemLocation) { this.lostItemLocation  = lostItemLocation;  }
    public void setFoundItemLocation(String v)               { this.foundItemLocation = v;                }
    public void setMatchDate(Timestamp matchDate)            { this.matchDate         = matchDate;         }

    @Override
    public String toString() {
        return "Match{" +
               "matchId="     + matchId     +
               ", lostItemId="+ lostItemId  +
               ", foundItemId="+ foundItemId +
               '}';
    }
}