# Spotify Backup

Simple application to save a Spotify users' list of liked songs and
playlists as an offline database. Rewrite of an older project written in
Python. Later goals include an (optional) ui, visualisation of the
database and automated backups to an online repository.

## Tasks

- Structure project according to industry norms.
- Good documentation and a user guide.
- Write unit tests.
- [ ] Implement a robust commandline interface in the style of the Python argparse package.
    - [ ] Format code, write documentation and ensure compliance with style
        - [ ] Write usage documentation in a markdown document
        - [ ] Split CmdParser class into CmdParser and ParsedArguments to eliminate
          `CmdParser.argumentsParsed` and clean up code structure
        - [x] Use `Optional<Argument>` as return in the identifyArgument functions
        - [x] Deduplicate contents of `CmdParser.generateUsage()`
        - [ ] Change code structure so that `CmdParser.printHelp()` can be invoked from the command
          line without generating an exception
        - [ ] Clean up unit tests according to best practices
        - [x] Exchange public CmdParser telescoping constructors to using a builder pattern as outlined in
          [this article](
          https://blogs.oracle.com/javamagazine/post/exploring-joshua-blochs-builder-design-pattern-in-java)
        - [ ] Exchange various telescoping Arguments constructors to using builder patterns as with CmdParser
        - [x] Remove MessageUtils from codebase
        - [x] In CmdParser constructor exchange `Set<String> argumentNames` and `Set<Character>
          argumentShortNames` for a `.stream()` call
    - [ ] Add pretty printing (at a later point in time, look in to jANSI library?)
    - [ ] Implement positional arguments (at a later point in time)
- [ ] Create Spotify API wrapper.
- [ ] Design a sensible SQL database that minimises data duplication.

## Documentation

[*See wiki page on GitHub*](https://github.com/JorritScholten/SpotifyBackup/wiki)
