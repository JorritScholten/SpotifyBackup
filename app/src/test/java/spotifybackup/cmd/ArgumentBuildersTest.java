package spotifybackup.cmd;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.TestInstantiationException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import spotifybackup.cmd.argument.FlagArgument;
import spotifybackup.cmd.exception.IllegalArgumentDescriptionException;
import spotifybackup.cmd.exception.IllegalArgumentNameException;
import spotifybackup.cmd.exception.IllegalArgumentShortnameException;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "EnableCmdParserTests", matches = "true")
class ArgumentBuildersTest {
    static private ScanResult scanResult;
    static private ClassInfoList abstractBuilders, implementedBuilders;
    static private final Supplier<ClassInfoList> allBuilders = () -> abstractBuilders.union(implementedBuilders);

    @BeforeAll
    static void bypass_encapsulation_and_scan_cmd_package() {
        ClassGraph.CIRCUMVENT_ENCAPSULATION = ClassGraph.CircumventEncapsulationMethod.JVM_DRIVER;
        scanResult = new ClassGraph()
//                .verbose()
                .enableAllInfo()
                .acceptPackages("spotifybackup.cmd")
                .scan();
        var subclasses = scanResult.getSubclasses(Argument.Builder.class);
        if (subclasses.isEmpty()) {
            throw new TestInstantiationException("Cannot find any subclasses for: " + Argument.Builder.class.getName());
        }
        abstractBuilders = subclasses.filter(ClassInfo::isAbstract);
        implementedBuilders = subclasses.exclude(abstractBuilders);
    }

    /**
     * This test ensures that super.validate() is called all along the inheritance chain by triggering an exception in
     * Argument$Builder.validate(). This test was implemented to ease future development.
     * givenMalformedName_whenBuildingArguments_thenThrowException
     * @implNote Actual functionality can be one-lined with (but didn't for clarity's sake):
     * ((Argument.Builder<?>)argumentBuilder.getConstructors()[0].newInstance()).name("").build();
     */
    @Test
    void ensure_each_builder_implementation_chains_validate_correctly()
            throws InvocationTargetException, InstantiationException, IllegalAccessException {
        for (var argumentBuilder : implementedBuilders.loadClasses()) {
            Argument.Builder<?, ?> builder = (Argument.Builder<?, ?>) argumentBuilder.getConstructors()[0].newInstance();
            builder.name("");
            assertThrows(IllegalArgumentNameException.class, builder::build,
                    argumentBuilder.getName() + " does not properly call super.validate() in build step.");
        }
    }

    /**
     * This test ensures that all fields in an Argument Builder implementation have the correct access, final fields can
     * be public. This test was implemented to ease future development.
     */
    @Test
    void ensure_builder_fields_are_private() {
        assertDoesNotThrow(() -> {
            for (var argumentBuilder : allBuilders.get().filter(c -> (!c.getDeclaredFieldInfo().isEmpty()))) {
                for (var field : argumentBuilder.getDeclaredFieldInfo()) {
                    if (!field.isFinal() && !field.isPrivate()) {
                        throw new RuntimeException("All non-final Builder fields should be private: " +
                                argumentBuilder.getName() + ":" + field.getName());
                    }
                }
            }
        });
    }

    /**
     * This test ensures that all Argument Builders with fields have a .validate() method with the correct access. This
     * test was implemented to ease future development.
     */
    @Test
    void ensure_builders_with_fields_have_validate() {
        assertDoesNotThrow(() -> {
            for (var argumentBuilder : allBuilders.get().filter(c -> (!c.getDeclaredFieldInfo().isEmpty()))) {
                var methods = argumentBuilder.getDeclaredMethodInfo("validate");
                if (methods.isEmpty()) {
                    throw new RuntimeException("Missing validate() method in builder that has fields, location: "
                            + argumentBuilder.getName());
                }
                for (var method : methods) {
                    if (method.getTypeSignature() != null) {
                        throw new RuntimeException("validate() in " + argumentBuilder.getName() +
                                " should always return void.");
                    }
                    if (!method.isProtected()) {
                        throw new RuntimeException("validate() in " + argumentBuilder.getName() + " should be protected.");
                    }
                }
            }
        });
    }

    @Test
    void argument_builder_builds_successfully() {
        // Arrange
        var flagArgumentBuilder = new FlagArgument.Builder();
        final String name = "flag", description = "flag argument";
        final char shortName = 'f';

        // Act
        flagArgumentBuilder
                .name(name)
                .description(description)
                .shortName(shortName);

        // Assert
        assertDoesNotThrow(() -> {
            final var flagArgument = flagArgumentBuilder.build();
            assertEquals(name, flagArgument.name);
            assertEquals(description, flagArgument.description);
            assertEquals(shortName, flagArgument.shortName);
        });
    }

    /**
     * This test ensures that Argument$Builder.validate() rejects when name is null. FlagArgument is used because it is
     * the simplest implemented Argument: it has no value or other validation steps beyond Argument itself.
     */
    @Test
    void argument_builder_validates_name_not_null() {
        // Arrange
        var flagArgumentBuilder = new FlagArgument.Builder()
                .description("a description")
                .shortName('f');

        // Act
        // flagArgumentBuilder.name()

        // Assert
        assertThrows(IllegalArgumentNameException.class, flagArgumentBuilder::build);
    }

    /**
     * This test ensures that Argument$Builder.validate() rejects when name is an empty string. FlagArgument is used
     * because it is the simplest implemented Argument: it has no value or other validation steps beyond Argument
     * itself.
     */
    @Test
    void argument_builder_validates_name_not_empty() {
        // Arrange
        var flagArgumentBuilder = new FlagArgument.Builder()
                .description("a description")
                .shortName('f');

        // Act
        flagArgumentBuilder.name("");

        // Assert
        assertThrows(IllegalArgumentNameException.class, flagArgumentBuilder::build);
    }

    /**
     * This test ensures that Argument$Builder.validate() rejects when name is an empty string. FlagArgument is used
     * because it is the simplest implemented Argument: it has no value or other validation steps beyond Argument
     * itself.
     */
    @Test
    void argument_builder_validates_description_not_null() {
        // Arrange
        var flagArgumentBuilder = new FlagArgument.Builder()
                .shortName('f')
                .name("flag");

        // Act
        // flagArgumentBuilder.description()

        // Assert
        assertThrows(IllegalArgumentDescriptionException.class, flagArgumentBuilder::build);
    }

    /**
     * This test ensures that Argument$Builder.validate() rejects when shortname is not in the alphabet. FlagArgument is
     * used because it is the simplest implemented Argument: it has no value or other validation steps beyond Argument
     * itself.
     */
    @ParameterizedTest
    @ValueSource(chars = {'1', '$', ' ', '-', 'ö'})
    void argument_builder_validates_shortname(char shortName) {
        // Arrange
        var flagArgumentBuilder = new FlagArgument.Builder()
                .description("flag argument")
                .name("flag");

        // Act
        flagArgumentBuilder.shortName(shortName);

        // Assert
        assertThrows(IllegalArgumentShortnameException.class, flagArgumentBuilder::build);
    }

    /**
     * This test ensures that Argument$Builder.validate() validates name is recognised as
     * {@link CmdParser.ArgType#LONG_ARGUMENT}
     */
    @ParameterizedTest
    @ValueSource(strings = {"name", "snake_case", "camelCase", "PascalCase", "kebab-case"})
    void argument_builder_validates_valid_name(final String name) {
        // Arrange
        var flagArgumentBuilder = new FlagArgument.Builder().description("flag argument");

        // Act
        flagArgumentBuilder.name(name);

        // Assert
        assertDoesNotThrow(flagArgumentBuilder::build);
    }

    /** This test ensures that Argument$Builder.validate() rejects name values with illegal characters. */
    @ParameterizedTest
    @ValueSource(strings = {"flag argument", "flag$", "f", "flag.argument"})
    void argument_builder_rejects_invalid_name(final String name) {
        // Arrange
        var flagArgumentBuilder = new FlagArgument.Builder().description("flag argument");

        // Act
        flagArgumentBuilder.name(name);

        // Assert
        assertThrows(IllegalArgumentNameException.class, flagArgumentBuilder::build);
    }
}
