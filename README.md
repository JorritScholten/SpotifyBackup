# Spotify Backup

Simple application to save a Spotify users' list of liked songs and
playlists as an offline database. Rewrite of an older project written in
Python. Later goals include an (optional) ui, visualisation of the
database and automated backups to an online repository.

## Tasks

- Structure project according to industry norms.
- Good documentation and a user guide.
- Write unit tests.
- [x] Implement a robust commandline interface in the style of the Python argparse package.
- [ ] Make use of Spotify API wrapper. [Using this library](
  https://github.com/spotify-web-api-java/spotify-web-api-java/)
    - [x] Make successful GET request to Spotify API
        - [x] Host temporary REST service
        - [x] Handle OAuth 2.0 redirects
        - [x] Change authentication to use PKCE as well so that the client doesn't need an API secret
        - [x] Remove Thread.sleep() call to make authorization request asynchronous
        - [x] Handle token refresh when current token expires
        - [ ] Handle the various API response error codes
    - [x] Store artist GET request to db
    - [ ] Write unit test using mocking (leaning towards EasyMock)
    - [ ] Store track info to db
    - [ ] Store playlist to db
    - [ ] Store Liked Songs to db
- [x] Design a sensible SQL database that minimises data duplication.
    - [x] Implement working Hibernate connection to embedded database.
    - [x] Implement complete storage class system to store all track information from Spotify about Playlists and Users
      Liked/Saved Songs.
        - [x] genres
        - [x] spotify image
        - [x] artists
        - [x] spotify ID
        - [x] playlist information
        - [x] "Liked Songs"/"Saved Tracks"
        - [x] tracks in a playlist
        - [x] users
        - [x] tracks
        - [x] albums
    - [x] Implement system to automatically handle creation and validation of database
    - [x] Implement necessary DAO methods to enable fluent storing and use of data provided by Spotify API
        - [x] Create repository for SpotifyGenre
        - [x] Create repository for SpotifyArtist
        - [x] Create repository for SpotifyAlbum
        - [x] Create repository for SpotifyID
        - [x] Create repository for SpotifyImage
        - [x] Create repository for SpotifyTrack
        - [x] Create repository for SpotifyUser
            - [ ] handle updating SpotifyUser info when detailed info changes
            - [ ] handle updating SpotifyUser info from simplified/public to detailed
        - [x] Create repository for SpotifyPlaylist
            - [x] Create repository for SpotifyPlaylistItem
        - [x] Create repository for Users Liked/Saved Songs
            - [x] handle tracks being removed
            - [x] handle removed track being re-added
            - [x] create method to get list of track ids for a specific user
            - [x] create method to get most recently added track, this is so that repeated runs won't need to get a
              user's entire library
    - [x] Convert all HQL queries to JPA CriteriaQuery's

[//]: # (## Documentation)

[//]: # ([*See wiki page on github*]&#40;https://github.com/JorritScholten/SpotifyBackup/wiki&#41;)
