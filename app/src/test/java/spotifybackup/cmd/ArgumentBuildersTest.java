package spotifybackup.cmd;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import spotifybackup.cmd.exception.IllegalArgumentNameException;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ArgumentBuildersTest {
    static private ScanResult scanResult;

    @BeforeAll
    static void bypassEncapsulationAndScanCmdPackage() {
        ClassGraph.CIRCUMVENT_ENCAPSULATION = ClassGraph.CircumventEncapsulationMethod.JVM_DRIVER;
        scanResult = new ClassGraph()
//                .verbose()
                .enableAllInfo()
                .acceptPackages("spotifybackup.cmd")
                .scan();
    }

    /**
     * This test ensures that super.validate() is called all along the inheritance chain by triggering an exception in
     * Argument$Builder.validate() givenMalformedName_whenBuildingArguments_thenThrowException
     */
    @Test
    void ensureEachArgumentImplementationChainsValidateCorrectly() {
        var arguments = scanResult
                .getSubclasses(Argument.Builder.class)
                .exclude(scanResult
                        .getSubclasses(Argument.Builder.class)
                        .filter(ClassInfo::isAbstract)
                )
                .loadClasses();
        for (var argument : arguments) {
            assertThrows(IllegalArgumentNameException.class, () -> {
                Argument.Builder<?> test = (Argument.Builder<?>) argument.getConstructors()[0].newInstance();
                test.name("").build();
            }, argument.getName() + " does not properly call super.validate().");
        }
    }
}
