# Spotify Backup

Simple application to save a Spotify users' list of liked songs and
playlists as an offline database. Rewrite of an older project written in
Python. Later goals include an (optional) ui, visualisation of the
database and automated backups to an online repository.

## Tasks

- [ ] Implement a robust commandline interface.
    - [x] specify which accounts are targeted for backups
        - [x] add flag to `Config.UserInfo` to mark backups
        - [x] specify accounts using a REPL or bounded list from `listAccountsInConfig()`
        - [x] add flag to suppress all backups (to simplify development and testing)
    - [x] request values for config file when creating a new config
        - [x] create cmd line argument to set config values directly
        - [x] specify values in terminal using prompt upon file creation
    - [ ] Look into a better system to define/edit the config file (maybe using [Lanterna](https://github.com/mabe02/lanterna))
    - [ ] Look into using an existing CLI options library that has more features than `spotifybackup.cmd.CmdParser`
      - [ ] Look into [PicoCLI](https://picocli.info/)
      - [ ] Look into [JCommander](https://jcommander.org/)
      - [ ] Look into [Apache Commons CLI](https://commons.apache.org/proper/commons-cli/index.html)
- [x] Make use of Spotify API
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
        - [ ] Add option to limit playlist backups if they are attached to account currently being backed up
    - [x] Store Liked Songs to db
    - [x] Store Liked Albums to db
    - [x] Store followed artists to db
    - [x] Request detailed information for various SpotifyObjects
    - [ ] Handle cloning/duplicating one account to another
        - [ ] handle selecting target and source account in the commandline
            - [x] list accounts in the database (source accounts)
            - [x] list accounts in the config file (target accounts)
            - [x] handle removing cloning targets
            - [x] handle adding cloning targets
            - [ ] handle broken objects in users field in config file in commandline
        - [ ] create methods for transferring various account details
            - [x] Liked Songs to a playlist
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
