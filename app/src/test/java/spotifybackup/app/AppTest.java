package spotifybackup.app;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class AppTest {
    @Test
    void ensure_app_arguments_defined_correctly() {
        assertDoesNotThrow(App::getConfig);
    }
}
