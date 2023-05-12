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
    - [x] Add baked in support for help argument
    - [x] Add optional default value to value arguments
        - [x] Split tests up because CmdParserTest.java is getting rather large
    - [x] Make optional arguments flag-like by adding CmdParser.isPresent(name)
    - [ ] Format code, write documentation and ensure compliance with style
        - [ ] Write javadoc comments for each Argument constructor:
            - FlagArgument
            - MandatoryStringArgument
            - DefaultStringArgument
            - MandatoryFilePathArgument
            - DefaultFilePathArgument
        - [ ] Write usage documentation in a markdown document
        - [ ] Add pretty printing (look in to jANSI library?)
        - [ ] De-clutter and split functions to reduce length:
            - CmdParser.identifyValueArgumentByShortName()
            - CmdParser.parser()
        - [ ] Create or import word wrapping function for getHelp() printing
    - [ ] Implement positional arguments (at a later point in time)
- [ ] Write unit tests.
- [ ] Create Spotify API wrapper.
- [ ] Design a sensible SQL database that minimises data duplication.
