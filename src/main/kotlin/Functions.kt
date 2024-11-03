import com.github.twitch4j.TwitchClient
import com.github.twitch4j.chat.TwitchChat
import config.TwitchBotConfig
import dev.kord.core.Kord
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import java.io.FileOutputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Paths
import java.time.format.DateTimeFormatterBuilder
import java.util.*
import javax.swing.JOptionPane
import kotlin.system.exitProcess

// Setup Twitch Bot
/**
 * Sets up the connection to twitch
 * @return {TwitchClient} the TwitchClient-class
 */
suspend fun setupTwitchBot(discordClient: Kord, backgroundCoroutineScope: CoroutineScope): TwitchClient? {
    // TODO
    return null
}


// Setup local clip server
/**
 * Hosts the local clip player server.
 */
fun hostClipPlayerServer() {
    // TODO
}


// Twitch Bot Functions

/**
 * Helper function that sends a message to twitch chat and logs it
 * @param chat {TitchChat} the twitch chat
 * @param message {String} content of the message
 */
fun sendMessageToTwitchChatAndLogIt(chat: TwitchChat, message: String) {
    chat.sendMessage(TwitchBotConfig.channel, message)
    logger.info("Sent Twitch chat message: $message")
}

// Logging
private const val LOG_DIRECTORY = "logs"
/**
 * Sets up the logging process with {MultiOutputStream} to both console and log file
 */
fun setupLogging() {
    Files.createDirectories(Paths.get(LOG_DIRECTORY))

    val logFileName = DateTimeFormatterBuilder()
        .appendInstant(0)
        .toFormatter()
        .format(Clock.System.now().toJavaInstant())
        .replace(':', '-')

    val logFile = Paths.get(LOG_DIRECTORY, "${logFileName}.log").toFile().also {
        if (!it.exists()) {
            it.createNewFile()
        }
    }

    System.setOut(PrintStream(MultiOutputStream(System.out, FileOutputStream(logFile))))

    logger.info("Log file ${logFile.name.addQuotationMarks()} has been created.")
}


// General functions
/**
 * Gets the value of the specified property out of the given properties-file. When an error occurres, the
 * function will display a descriptive error message windows and end the app.
 * @param properties {Properties} already initialized properties-class
 * @param propertyName {String} name of the property
 * @param propertiesFileRelativePath {String} the relative path of the properties file
 * @return {String} on success, the raw value of the property
 */
fun getPropertyValue(properties: Properties, propertyName: String, propertiesFileRelativePath: String): String {
    return try {
        properties.getProperty(propertyName)
    } catch (e: Exception) {
        logger.error("Exception occurred while reading property $propertyName in file $propertiesFileRelativePath: ", e)
        showErrorMessageWindow(
            message =   "Error while reading value of property ${propertyName.addQuotationMarks()} " +
                    "in file $propertiesFileRelativePath.",
            title = "Error while reading properties"
        )
        exitProcess(-1)
    }
}

/**
 * Displays an error message window as JOptionPane.
 * @param message {String} the message to display
 * @param title {String} the title to display
 */
fun showErrorMessageWindow(message: String, title: String) {
    JOptionPane.showMessageDialog(
        null,
        "$message\nCheck logs for more information",
        title,
        JOptionPane.ERROR_MESSAGE
    )
}