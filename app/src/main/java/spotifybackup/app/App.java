package spotifybackup.app;

import spotifybackup.cmd.Argument;
import spotifybackup.cmd.CmdParser;
import spotifybackup.cmd.FlagArgument;

public class App {
    public static void main(String[] args) {
        System.out.println("Hello SpotifyBackup!");
        CmdParser argParser = new CmdParser(new Argument[]{
                new FlagArgument("help", "Print program help.", 'h')
        });
//        argParser.parseArguments(args);
    }
}
