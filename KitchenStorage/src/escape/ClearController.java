package escape;

import javafx.application.Platform;
import javafx.fxml.FXML;

public class ClearController {

    @FXML
    private void onExit() {
        Platform.exit();
    }
}
