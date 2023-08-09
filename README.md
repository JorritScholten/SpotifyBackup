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
- [ ] Create Spotify API wrapper.
- [ ] Design a sensible SQL database that minimises data duplication.
    - [x] Implement working Hibernate connection to embedded database.
    - [ ] Implement complete storage class system to store all track information from Spotify about Playlists and User's
      Liked Songs.
        - [x] genres
        - [x] spotify image
        - [x] artists
        - [x] spotify ID
        - [ ] playlist information (we'll treat Liked Songs as a playlist)
        - [ ] tracks in a playlist
        - [ ] users
        - [ ] tracks
    - [ ] Implement system to automatically handle creation and validation of database
    - [ ] Implement necessary DAO methods to enable fluent storing and use of data provided by Spotify API
        - [x] Create repository for Genre
        - [x] Create repository for Artist
            - Need to implement method to handle SimplifiedArtist objects, will require integration with Spotify API.
        - [ ] Create repository for Album
        - [x] Create repository for SpotifyID
        - [x] Create repository for SpotifyImage

## Documentation

[*See wiki page on github*](https://github.com/JorritScholten/SpotifyBackup/wiki)
