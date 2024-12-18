package com.backend.allreva.artist.query.application;

import com.backend.allreva.artist.command.domain.ArtistRepository;
import com.backend.allreva.artist.command.domain.Artist;
import com.backend.allreva.artist.exception.ArtistNotFoundException;
import com.backend.allreva.artist.exception.ArtistSearchNoContentException;
import com.backend.allreva.artist.query.application.response.SpotifyArtistWrapper;
import com.backend.allreva.artist.query.application.response.SpotifySearchResponse;
import com.backend.allreva.common.feign.SpotifyAccountClient;
import com.backend.allreva.common.feign.SpotifyClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArtistQueryService {
    private final SpotifyClient spotifyClient;

    private final SpotifyAccountClient spotifyAccountClient;

    private final ArtistRepository artistRepository;

    @Value("${spotify.client.id}")
    private String clientId;

    @Value("${spotify.client.secret}")
    private String clientSecret;

    public List<SpotifySearchResponse> searchArtist(final String query) {
        String token = "Bearer " + getAccessToken();
        SpotifyArtistWrapper response = spotifyClient.searchArtists(token, query);

        List<SpotifySearchResponse> artists = response.artists().items();

        if(artists.isEmpty()) throw new ArtistSearchNoContentException();

        return artists;
    }

    public Artist getArtistById(String id) {
        return artistRepository.findById(id).orElseThrow(ArtistNotFoundException::new);
    }

    private String getAccessToken() {
        String credentials = clientId + ":" + clientSecret;
        String encodedCredentials = Base64.getEncoder()
                .encodeToString(credentials.getBytes());

        return spotifyAccountClient.getAccessToken(
                "Basic " + encodedCredentials,
                "client_credentials"
        ).access_token();
    }
}
