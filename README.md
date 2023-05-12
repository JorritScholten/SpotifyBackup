# Spotify Backup

Simple application to save a Spotify users' list of liked songs and
playlists as an offline database. Rewrite of an older project written in
Python. Later goals include an (optional) ui, visualisation of the
database and automated backups to an online repository.

## Tasks

- [ ] Structure project according to industry norms.
- [ ] Good documentation and a user guide.
- [ ] Implement a robust commandline interface in the style of the Python argparse package.
    - [x] Create a boolean flag argument that's true if present
    - [x] Create an integer argument
        - [x] Create a subtype with bounds checking
    - [x] Create a string argument
        - [x] Create a filepath/uri argument with validity checking
    - [ ] Add baked in support for help argument
        - [x] Parametric help printing per argument.
        - [ ] Parametric usage printing in CmdParser
        - [x] Expand on CmdParser constructor for baked in program description
    - [x] Add optional default value to value arguments: make it so
      that non-mandatory arguments are optional and vice-versa
        - [x] Split tests up because CmdParserTest.java is getting rather large
  - [ ] Make optional arguments flag-like by adding CmdParser.isPresent(name)
  - [ ] Implement positional arguments
  - [ ] Write javadoc comments for each Argument constructor
    - [ ] Add pretty printing (look in to jANSI library?)
- [ ] Write unit tests.
- [ ] Create Spotify API wrapper.
- [ ] Design a sensible SQL database that minimises data duplication.
