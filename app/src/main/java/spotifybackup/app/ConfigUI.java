package spotifybackup.app;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.bundle.LanternaThemes;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.ansi.UnixTerminal;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;

public class ConfigUI {
    private final TerminalScreen screen;
    private final MultiWindowTextGUI gui;
    private final BasicWindow window;
    private final String title = "SpotifyBackup App Configuration";
    private final Table<String> configUsers = new Table<>("", "Account display name", "Spotify ID",
            "Has refresh token?", "Do backup?", "Cloning Target(s)");

    public ConfigUI() throws IOException {
        var termFactory = new DefaultTerminalFactory();
        termFactory.setInitialTerminalSize(new TerminalSize(120, 40));
        termFactory.setTerminalEmulatorTitle(title);
        screen = termFactory.createScreen();
        screen.refresh(Screen.RefreshType.DELTA);
        screen.startScreen();
        gui = new MultiWindowTextGUI(new SameTextGUIThread.Factory(), screen);
        gui.setEOFWhenNoWindows(false);
        gui.setTheme(LanternaThemes.getRegisteredTheme("blaster"));
        screen.getTerminal().addResizeListener((terminal, newSize) -> tryToUpdateGui());
        window = new BasicWindow(title);
        window.setHints(Collections.singleton(Window.Hint.FULL_SCREEN));
        initializeComponents();
        gui.addWindow(window);
    }

    private void tryToUpdateGui() {
        try {
            gui.updateScreen();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() throws IOException {
        var thread = gui.getGUIThread();
        gui.updateScreen();
        do {
            final var input = screen.readInput();
            if (gui.handleInput(input)) {
                gui.updateScreen();
                thread.processEventsAndUpdate();
            } else if (input.getKeyType().equals(KeyType.EOF)) {
                if (screen.getTerminal().getClass() != UnixTerminal.class) TerminalInteraction.println("window closed");
                break;
            } else {
                gui.updateScreen();
                if (screen.getTerminal().getClass() != UnixTerminal.class)
                    TerminalInteraction.println("unhandled input: " + input);
            }
        } while (!gui.getWindows().isEmpty());
        screen.stopScreen(true);
    }

    private void initializeComponents() {
        var panelLayout = new GridLayout(1);
        panelLayout.setVerticalSpacing(0);
        var panel = new Panel(panelLayout);
        final int windowWidth = screen.getTerminalSize().getColumns() -
                (window.getHints().contains(Window.Hint.NO_DECORATIONS) ? 0 : 2) -
                (panelLayout.getLeftMarginSize() + panelLayout.getRightMarginSize());
        final LayoutData grow = LinearLayout.createLayoutData(LinearLayout.Alignment.Beginning,
                LinearLayout.GrowPolicy.CanGrow);


        var outputHeading = new Label("Terminal output options");
        outputHeading.addStyle(SGR.BOLD);
        panel.addComponent(outputHeading);

        panel.addComponent(new EmptySpace());

        var printLibraryDuration = new CheckBox("Print total library duration");
        printLibraryDuration.setChecked(false); // TODO: store and retrieve this value from Config
        printLibraryDuration.addListener(new CheckBoxListener("printLibraryDuration"));
        panel.addComponent(printLibraryDuration);

        panel.addComponent(new EmptySpace());
        var testListener = new Button("listener test");
        testListener.addListener(new ButtonListener("testListener"));
        panel.addComponent(testListener);

        panel.addComponent(new EmptySpace());
        var beep = new Button("beep");
        beep.addListener(new ButtonListener("beeper", () -> {
            try {
                screen.getTerminal().bell();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
        panel.addComponent(beep);

        {
            panel.addComponent(new EmptySpace());
            ComboBox<String> themeSelect = new ComboBox<>(LanternaThemes.getRegisteredThemes());
            for (var themeName : LanternaThemes.getRegisteredThemes()) {
                if (gui.getTheme().equals(LanternaThemes.getRegisteredTheme(themeName))) {
                    themeSelect.setSelectedItem(themeName);
                    break;
                }
            }
            themeSelect.addListener((selectedIndex, previousSelection, changedByUserInteraction) -> {
                if (changedByUserInteraction && selectedIndex != previousSelection) {
                    gui.setTheme(LanternaThemes.getRegisteredTheme(themeSelect.getSelectedItem()));
                    tryToUpdateGui();
                }
            });
            var labeledComponent = new Panel(new LinearLayout(Direction.HORIZONTAL))
                    .setPreferredSize(new TerminalSize(windowWidth, 1))
                    .addComponent(new Label("UI theme"))
                    .addComponent(themeSelect);
            panel.addComponent(labeledComponent);
        }

        {
            panel.addComponent(new EmptySpace());
            var textBox = new TextBox("initial content", TextBox.Style.SINGLE_LINE)
                    //.setValidationPattern()
                    .setTextChangeListener((newText, changedByUserInteraction) -> {
                        if (changedByUserInteraction) {
                            if (screen.getTerminal().getClass() != UnixTerminal.class)
                                TerminalInteraction.println("newText: " + newText);
                        }
                    });
            var labeledText = new Panel(new LinearLayout(Direction.HORIZONTAL))
                    .setPreferredSize(new TerminalSize(windowWidth, 1))
                    .addComponent(new Label("property"))
                    .addComponent(textBox, grow);
            panel.addComponent(labeledText);
        }

        panel.addComponent(new EmptySpace());
        panel.addComponent(new Label("Users in config"));
        updateConfigUsersRows();
        configUsers.setCellSelection(false);
        configUsers.setSelectAction(this::tableSelected);
        panel.addComponent(configUsers);
        panel.addComponent(new Button("add new user", () -> {
            if (screen.getTerminal().getClass() != UnixTerminal.class)
                TerminalInteraction.println("adding new user");
        }));

        panel.addComponent(new EmptySpace());

        var finishedConfigButton = new Button("Done?", window::close); // action executed before listener
        panel.addComponent(finishedConfigButton);


        window.setComponent(panel);
    }

    private void updateConfigUsersRows() {
        configUsers.getTableModel().clear();
        int rowCount = 0;
        final int idColumnLength = configUsers.getTableModel().getColumnLabel(2).length();
        for (var user : App.config.getUsers()) {
            var spotifyId = user.getSpotifyId().orElse("<ID missing>");
            configUsers.getTableModel().addRow(
                    String.valueOf(rowCount++),
                    user.getDisplayName().orElse(""),
                    spotifyId.length() <= idColumnLength ? spotifyId : spotifyId.substring(0,
                            idColumnLength - 2) + "..",
                    user.getRefreshToken().orElse("").isBlank() ? "No" : "Yes",
                    user.getDoBackup() ? "Yes" : "No",
                    String.join(", ", user.getCloneTargets().stream().map(u -> u.getSpotifyId().get()).toList()));
        }
    }

    private void tableSelected() {
        if (screen.getTerminal().getClass() != UnixTerminal.class)
            TerminalInteraction.println("table selected, row: " + configUsers.getSelectedRow());
    }

    private class CheckBoxListener implements CheckBox.Listener {
        private final String optionName;
        private final Optional<Runnable> action;

        CheckBoxListener(String toggleableConfigOption) {
            this(toggleableConfigOption, null);
        }

        CheckBoxListener(String toggleableConfigOption, Runnable action) {
            optionName = toggleableConfigOption;
            this.action = Optional.ofNullable(action);
            if (screen.getTerminal().getClass() != UnixTerminal.class)
                TerminalInteraction.println("created checkbox listener for: " + optionName);
        }

        @Override
        public void onStatusChanged(boolean checked) {
            if (screen.getTerminal().getClass() != UnixTerminal.class)
                TerminalInteraction.println(optionName + " set to: " + checked);
            action.ifPresent(Runnable::run);
        }
    }

    private class ButtonListener implements Button.Listener {
        private final String buttonName;
        private final Optional<Runnable> action;

        ButtonListener(String buttonName) {
            this(buttonName, null);
        }

        ButtonListener(String buttonName, Runnable action) {
            this.buttonName = buttonName;
            this.action = Optional.ofNullable(action);
            if (screen.getTerminal().getClass() != UnixTerminal.class)
                TerminalInteraction.println("created button listener for: " + buttonName);
        }

        @Override
        public void onTriggered(Button button) {
            if (screen.getTerminal().getClass() != UnixTerminal.class)
                TerminalInteraction.println("button: " + buttonName + " triggered");
            action.ifPresent(Runnable::run);
        }
    }

    private class TextBoxListener implements TextBox.TextChangeListener {
        private final String optionName;
        private final Optional<Runnable> action;

        TextBoxListener(String optionName) {
            this(optionName, null);
        }

        TextBoxListener(String optionName, Runnable action) {
            this.optionName = optionName;
            this.action = Optional.ofNullable(action);
            if (screen.getTerminal().getClass() != UnixTerminal.class)
                TerminalInteraction.println("created textbox listener for: " + optionName);
        }

        @Override
        public void onTextChanged(String newText, boolean changedByUserInteraction) {
            if (changedByUserInteraction) {
                if (screen.getTerminal().getClass() != UnixTerminal.class)
                    TerminalInteraction.println("textbox " + optionName + " changed to: " + newText);
                action.ifPresent(Runnable::run);
            }
        }
    }

    private class ComboBoxListener implements ComboBox.Listener {
        private final String optionName;
        private final Optional<Consumer<Integer>> action;

        ComboBoxListener(String optionName) {
            this(optionName, null);
        }

        ComboBoxListener(String optionName, Consumer<Integer> action) {
            this.optionName = optionName;
            this.action = Optional.ofNullable(action);
            if (screen.getTerminal().getClass() != UnixTerminal.class)
                TerminalInteraction.println("created combo box listener for: " + optionName);
        }

        @Override
        public void onSelectionChanged(int selectedIndex, int previousSelection, boolean changedByUserInteraction) {
            if (changedByUserInteraction && selectedIndex != previousSelection) {
                if (screen.getTerminal().getClass() != UnixTerminal.class)
                    TerminalInteraction.println("combo box " + optionName + " changed to: " + selectedIndex);
                action.ifPresent(integerConsumer -> integerConsumer.accept(selectedIndex));
            }
        }
    }
}
