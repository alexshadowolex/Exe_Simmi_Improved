import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ui.app

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        app()
    }
}
