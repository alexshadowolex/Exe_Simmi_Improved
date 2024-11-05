import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import config.DiscordBotConfig
import config.TwitchBotConfig
import dev.kord.core.Kord
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import ui.app
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess

val logger: org.slf4j.Logger = LoggerFactory.getLogger("Bot")
val backgroundCoroutineScope = CoroutineScope(Dispatchers.IO)
val json = Json {
    prettyPrint = true
}

suspend fun main() = try {
    val discordClient = Kord(DiscordBotConfig.discordToken)
    CoroutineScope(discordClient.coroutineContext).launch {
        discordClient.login {
            @OptIn(PrivilegedIntent::class)
            intents += Intent.MessageContent
        }
    }
    logger.info("Discord client started.")

    val twitchClient = setupTwitchBot(discordClient, backgroundCoroutineScope)

    hostClipPlayerServer()
    logger.info("WebSocket hosted.")

    application {
        DisposableEffect(Unit) {
            onDispose {
                sendMessageToTwitchChatAndLogIt(twitchClient!!.chat,"Bot shutting down ${TwitchBotConfig.leaveEmote}")
                logger.info("App shutting down...")
            }
        }

        Window(
            state = WindowState(size = DpSize(500.dp, 250.dp)),
            title = "Exe.Simmi.Improved",
            onCloseRequest = ::exitApplication,
            icon = painterResource("icon.ico"),
            resizable = false
        ) {
            app()
        }

        // TODO: New version check
    }
} catch (e: Throwable) {
    showErrorMessageWindow(
        message =   e.message + "\n" +
                StringWriter().also { e.printStackTrace(PrintWriter(it)) },
        title = "Error while executing app"
    )
    logger.error("Error while executing program.", e)
    exitProcess(-1)
}
