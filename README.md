# Spotify Backup

Simple application to save a Spotify users' list of liked songs and
playlists as an offline database. Rewrite of an older project written in
Python. Later goals include an (optional) ui, visualisation of the
database and automated backups to an online repository.

## Tasks

- [x] Improve visual appearance of help text
    - [x] Modify tests to reflect new help appearance
- [ ] Implement optional arguments (optionally present, value mandatory)
    - [ ] untangle mandatory presence from having a value in parser
    - create optional arguments for existing types
        - [ ] OptionalStringArgument
        - [ ] OptionalFilePathArgument
        - [ ] OptionalEnumArgument
        - [ ] OptionalIntArgument
        - [ ] OptionalBoundedIntArgument
