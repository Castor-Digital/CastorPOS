package com.castorpos;

public class WaitListEntry {
    private String partyName;
    private int partySize;

    public WaitListEntry(String partyName, int partySize) {
        this.partyName = partyName;
        this.partySize = partySize;
    }

    public String getPartyName() {
        return partyName;
    }

    public int getPartySize() {
        return partySize;
    }
}

