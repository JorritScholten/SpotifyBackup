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
    - [ ] Remove Thread.sleep() call to make everything asynchronous
    - [ ] Handle authorisation redirects as HTTPS request instead of HTTP
  - [ ] Store artist GET request to db
  - [ ] Write unit test using mocking (leaning towards EasyMock)
  - [ ] Store track info to db
  - [ ] Store playlist to db
- [ ] Design a sensible SQL database that minimises data duplication.

## Documentation

[*See wiki page on github*](https://github.com/JorritScholten/SpotifyBackup/wiki)
