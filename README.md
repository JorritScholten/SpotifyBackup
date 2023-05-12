# Spotify Backup

Simple application to save a Spotify users' list of liked songs and
playlists as an offline database. Rewrite of an older project written in
Python. Later goals include an (optional) ui, visualisation of the
database and automated backups to an online repository.

## Tasks

- [ ] Structure project according to industry norms.
- [ ] Good documentation and a user guide.
- [ ] Implement a robust commandline interface.
    - [x] Write unit tests.
    - [x] Trying out a Python argparse style interface.
        - [x] Create a boolean flag argument that's true if present
        - [x] Create an integer argument
            - [x] Create a subtype with bounds checking
        - [x] Create a string argument
            - [ ] Create a filepath/uri argument with validity checking
        - [x] Rewrite/improve argument lexer/parser: split lexing and parsing
            - [x] Create lexer
            - [x] Create parser
            - [x] Feed output of parser back into arguments list
        - [x] De-pythonise current CmdParser implementation
        - [ ] Add optional default value to value arguments: make it so
          that non-mandatory arguments are optional and vice-versa
- [ ] Write unit tests.
- [ ] Create Spotify API wrapper.
- [ ] Design a sensible SQL database that minimises data duplication.
