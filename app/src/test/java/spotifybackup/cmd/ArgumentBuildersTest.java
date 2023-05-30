package spotifybackup.cmd;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.TestInstantiationException;
import spotifybackup.cmd.exception.IllegalArgumentNameException;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ArgumentBuildersTest {
    static private ScanResult scanResult;
    static private ClassInfoList abstractBuilders, implementedBuilders;

    @BeforeAll
    static void bypassEncapsulationAndScanCmdPackage() {
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
     * @implNote Actual functionality can be one-lined with (but didn't for clarities sake):
     * ((Argument.Builder<?>)argumentBuilder.getConstructors()[0].newInstance()).name("").build();
     */
    @Test
    void ensureEachBuilderImplementationChainsValidateCorrectly() {
        for (var argumentBuilder : implementedBuilders.loadClasses()) {
            assertThrows(IllegalArgumentNameException.class, () -> {
                Argument.Builder<?> builder = (Argument.Builder<?>) argumentBuilder.getConstructors()[0].newInstance();
                builder.name("").build();
            }, argumentBuilder.getName() + " does not properly call super.validate().");
        }
    }
}
