import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.slf4j.LoggerFactory
import ui.app

val logger: org.slf4j.Logger = LoggerFactory.getLogger("Bot")

fun main() = application {
    setupLogging()
    Window(onCloseRequest = ::exitApplication) {
        app()
    }
}
