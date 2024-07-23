package spotifybackup.app;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import java.io.IOException;
import java.util.Collections;

public class ConfigUI {
    private final TerminalScreen screen;
    private final MultiWindowTextGUI gui;
    private final BasicWindow window;
    private final String title = "SpotifyBackup App Configuration";
    private boolean finishedConfig = false;

    public ConfigUI() throws IOException {
        var termFactory = new DefaultTerminalFactory();
        termFactory.setInitialTerminalSize(new TerminalSize(120, 40));
        termFactory.setTerminalEmulatorTitle(title);
        screen = termFactory.createScreen();
        screen.refresh(Screen.RefreshType.DELTA);
        screen.startScreen();
        gui = new MultiWindowTextGUI(new SameTextGUIThread.Factory(), screen);
        screen.getTerminal().addResizeListener((terminal, newSize) -> {
            try {
                gui.updateScreen();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        window = new BasicWindow(title);
        window.setHints(Collections.singleton(Window.Hint.FULL_SCREEN));
        initializeComponents();
        gui.addWindow(window);
    }

    public void run() throws IOException {
        var thread = gui.getGUIThread();
        gui.updateScreen();
        do {
            if (gui.handleInput(screen.readInput())) gui.updateScreen();
        } while (!finishedConfig);
            if (gui.handleInput(screen.readInput())) {
                gui.updateScreen();
                thread.processEventsAndUpdate();
            }
        screen.stopScreen(true);
    }

    private void initializeComponents() {
        var layout = new GridLayout(1);
        layout.setVerticalSpacing(0);
        var panel = new Panel(layout);


        var outputHeading = new Label("Terminal output options");
        outputHeading.addStyle(SGR.BOLD);
        panel.addComponent(outputHeading);

        var printLibraryDuration = new CheckBox("Print total library duration");
        printLibraryDuration.setChecked(false); // TODO: store and retrieve this value from Config
        printLibraryDuration.addListener(new CheckBoxListener("printLibraryDuration"));
        panel.addComponent(printLibraryDuration);

        panel.addComponent(new EmptySpace());

        var finishedConfigButton = new Button("Done?", () -> finishedConfig = true);
        panel.addComponent(finishedConfigButton);


        window.setComponent(panel);
    }

    private static class CheckBoxListener implements CheckBox.Listener {
        private final String optionName;

        CheckBoxListener(String toggleableConfigOption) {
            optionName = toggleableConfigOption;
            TerminalInteraction.println("created checkbox listener for: " + optionName);
        }

        @Override
        public void onStatusChanged(boolean checked) {
            TerminalInteraction.println(optionName + " set to: " + checked);
        }
    }
}
