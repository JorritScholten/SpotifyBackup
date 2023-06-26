package spotifybackup.app;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import spotifybackup.cmd.CmdParser;
import spotifybackup.cmd.argument.integer.MandatoryIntArgument;

import java.util.logging.Level;
import java.util.logging.LogManager;

public class App {
    public static void main(String[] args) {
//        addition(args);
        System.out.println("starting main");
        LogManager.getLogManager().getLogger("").setLevel(Level.WARNING);
        try (EntityManagerFactory emf = Persistence.createEntityManagerFactory("testdb")) {
            System.out.println("genres in table: " + emf.createEntityManager().createNamedQuery("Genre.countBy").getFirstResult());
        }
    }

    public static void addition(String[] args) {
        var argParser = new CmdParser.Builder()
                .argument(new MandatoryIntArgument.Builder()
                        .name("value1")
                        .description("First value.")
                        .shortName('a')
                        .build())
                .argument(new MandatoryIntArgument.Builder()
                        .name("value2")
                        .description("Second value.")
                        .shortName('b')
                        .build())
                .description("Program description")
                .programName("Add.jar")
                .epilogue("Help footer")
                .addHelp()
                .build();
        try {
            argParser.parseArguments(args);
            if (argParser.isPresent("help")) {
                System.out.println(argParser.getHelp());
                System.exit(1);
            } else {
                int value1 = (int) argParser.getValue("value1");
                int value2 = (int) argParser.getValue("value2");
                System.out.printf("%d + %d = %d\n", value1, value2, value1 + value2);
                System.exit(0);
            }
        } catch (Exception e) {
            System.out.println("Error with input: " + e.getMessage());
            System.out.println(argParser.getHelp());
            System.exit(-1);
        }
    }
}
