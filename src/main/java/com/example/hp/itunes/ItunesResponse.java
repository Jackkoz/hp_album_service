package com.example.hp.itunes;

import java.util.List;

public record ItunesResponse(List<ItunesResult> results) {
    public record ItunesResult(String collectionName, String artworkUrl100) { }
}
