package spotifybackup.app;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class AppTest {
    @Test
    void ensure_app_arguments_defined_correctly() {
        assertDoesNotThrow(App::getConfig);
    }

    @Test
    void view_app_arguments_help_text() {
        assertDoesNotThrow(() -> System.out.println(App.argParser.getHelp(120)));
    }
}
