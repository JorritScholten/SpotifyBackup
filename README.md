# Spotify Backup

Simple application to save a Spotify users' list of liked songs and
playlists as an offline database. Rewrite of an older project written in
Python. Later goals include an (optional) ui, visualisation of the
database and automated backups to an online repository.

## Tasks

- [x] Implement a robust commandline interface.
- [ ] Make use of Spotify API
  wrapper. [Using this library](https://github.com/spotify-web-api-java/spotify-web-api-java/)
    - [x] Make successful GET request to Spotify API
        - [x] Host temporary REST service
        - [x] Handle OAuth 2.0 redirects
        - [x] Change authentication to use PKCE as well so that the client doesn't need an API secret
        - [x] Remove Thread.sleep() call to make authorization request asynchronous
        - [x] Handle token refresh when current token expires
        - [ ] Handle the various API response error codes
        - [ ] Handle OAuth redirect resulting in permission denied
    - [x] Store artist GET request to db
    - [ ] Write unit test using mocking (leaning towards EasyMock)
    - [x] Store track info to db
    - [x] Store playlist to db
    - [x] Store Liked Songs to db
    - [x] Store Liked Albums to db
    - [x] Store followed artists to db
    - [x] Request detailed information for various SpotifyObjects
    - [ ] Handle cloning/duplicating one account to another
        - [ ] handle selecting target and source account in the commandline
            - [ ] list accounts in the database (source accounts)
            - [ ] list accounts in the config (target accounts)
        - [ ] create methods for transferring various account details
            - [ ] Liked Songs
            - [ ] created playlists (and their contents)
            - [ ] playlist subscriptions
            - [ ] liked albums
            - [ ] followed artists
    - [ ] create custom exceptions to replace generic RuntimeException
- [x] Design a sensible SQL database that minimises data duplication.
    - [ ] handle updating SpotifyUser info when detailed info changes
    - [ ] handle updating SpotifyUser info from simplified/public to detailed
    - [ ] create custom exceptions to replace generic RuntimeException
