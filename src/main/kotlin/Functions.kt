import com.github.philippheuer.credentialmanager.domain.OAuth2Credential
import com.github.twitch4j.TwitchClient
import com.github.twitch4j.TwitchClientBuilder
import com.github.twitch4j.chat.TwitchChat
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import com.github.twitch4j.common.enums.CommandPermission
import config.ClipPlayerConfig
import config.TwitchBotConfig
import dev.kord.core.Kord
import handler.Command
import handler.CommandHandlerScope
import handler.commands
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.partialcontent.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import ui.clipOverlayPage
import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Paths
import java.time.format.DateTimeFormatterBuilder
import java.util.*
import javax.swing.JOptionPane
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.seconds

// Setup Twitch Bot
/**
 * Sets up the connection to twitch
 * @return {TwitchClient} the TwitchClient-class
 */
fun setupTwitchBot(discordClient: Kord, backgroundCoroutineScope: CoroutineScope): TwitchClient {
    val oAuth2Credential = OAuth2Credential("twitch", TwitchBotConfig.chatAccountToken)

    val twitchClient = TwitchClientBuilder.builder()
        .withEnableHelix(true)
        .withEnableChat(true)
        .withEnablePubSub(true)
        .withChatAccount(oAuth2Credential)
        .build()

    val nextAllowedCommandUsageInstantPerUser = mutableMapOf<Pair<Command, /* user: */ String>, Instant>()
    val nextAllowedCommandUsageInstantPerCommand = mutableMapOf<Command, Instant>()
    val commandsInUsage = mutableSetOf<Command>()

    // TODO
    //val remindHandler = RemindHandler(chat = twitchClient.chat, reminderFile = File("data\\saveData\\reminders.json"), checkerScope = backgroundCoroutineScope)
    //val runNamesRedeemHandler = RunNamesRedeemHandler(runNamesFile = File("data\\saveData\\runNames.json"))
    //SpreadSheetHandler.instance.setupConnectionAndLoadData(runNamesRedeemHandler)

    twitchClient.chat.run {
        connect()
        joinChannel(TwitchBotConfig.channel)
        sendMessage(TwitchBotConfig.channel, "Bot running ${TwitchBotConfig.arriveEmote}")
    }

    val channelID = twitchClient.helix.getUsers(
        TwitchBotConfig.chatAccountToken, null, listOf(TwitchBotConfig.channel)
    ).execute().users.first().id
    twitchClient.pubSub.listenForChannelPointsRedemptionEvents(
        oAuth2Credential,
        channelID
    )

    twitchClient.pubSub.listenForChannelPointsRedemptionEvents(oAuth2Credential, channelID)

    twitchClient.eventManager.onEvent(ChannelMessageEvent::class.java) { messageEvent ->
        messageEvent.messageEvent.getTagValue("color").orElse(null)?.let {
            // TODO
            /*runNamesRedeemHandler.saveNameWithColor(
                name = messageEvent.user.name,
                color = it.removePrefix("#")
            )*/
        }

        val message = messageEvent.message
        if (!message.startsWith(TwitchBotConfig.commandPrefix)) {
            return@onEvent
        }

        val parts = message.substringAfter(TwitchBotConfig.commandPrefix).split(" ")
        val command = commands.find { parts.first().lowercase() in it.names } ?: return@onEvent

        // This feature has been built because of ShardZero abusing bot features.
        // The bot will not allow commands from blacklisted users
        if(
            messageEvent.user.name in TwitchBotConfig.blacklistedUsers ||
            messageEvent.user.id in TwitchBotConfig.blacklistedUsers
            ){

            twitchClient.chat.sendMessage(
                TwitchBotConfig.channel,
                "Imagine not being a blacklisted user. Couldn't be you " +
                        "${messageEvent.user.name} ${TwitchBotConfig.blacklistEmote}"
            )
            if(messageEvent.user.id !in TwitchBotConfig.blacklistedUsers) {
                logger.warn(
                    "Blacklisted user ${messageEvent.user.name} tried using a command. " +
                    "Please use following ID in the properties file instead of the name: ${messageEvent.user.id}"
                )
            }
            return@onEvent
        }

        if(commandsInUsage.contains(command)) {
            logger.info(
                "Command ${command.names.first()} is already in usage. " +
                "Aborting handler for user ${messageEvent.user.name}"
            )
            return@onEvent
        }

        commandsInUsage.add(command)

        logger.info(
            "User '${messageEvent.user.name}' tried using command '${command.names.first()}' " +
            "with arguments: ${parts.drop(1).joinToString()}"
        )

        val nextAllowedCommandUsageInstant = nextAllowedCommandUsageInstantPerUser
            .getOrPut(command to messageEvent.user.name) {
                Clock.System.now()
            }

        val nextAllowedGlobalCommandUsageInstant = nextAllowedCommandUsageInstantPerCommand
            .getOrPut(command) {
                Clock.System.now()
            }

        if (
            (Clock.System.now() - nextAllowedGlobalCommandUsageInstant).isNegative() &&
            CommandPermission.MODERATOR !in messageEvent.permissions
            ) {

            val secondsUntilTimeoutOver = (nextAllowedGlobalCommandUsageInstant - Clock.System.now())
                .inWholeSeconds.seconds

            twitchClient.chat.sendMessage(
                TwitchBotConfig.channel,
                "The command is still on cooldown. Please try again in $secondsUntilTimeoutOver."
            )
            logger.info("Unable to execute command due to ongoing command cooldown.")

            commandsInUsage.remove(command)
            return@onEvent
        }

        if (
            (Clock.System.now() - nextAllowedCommandUsageInstant).isNegative() &&
            CommandPermission.MODERATOR !in messageEvent.permissions
            ) {

            val secondsUntilTimeoutOver = (nextAllowedCommandUsageInstant - Clock.System.now()).inWholeSeconds.seconds

            twitchClient.chat.sendMessage(
                TwitchBotConfig.channel,
                "You are still on cooldown. Please try again in $secondsUntilTimeoutOver."
            )
            logger.info("Unable to execute command due to ongoing user cooldown.")

            commandsInUsage.remove(command)
            return@onEvent
        }

        val commandHandlerScope = CommandHandlerScope(
            discordClient = discordClient,
            chat = twitchClient.chat,
            messageEvent = messageEvent
            // TODO
            //remindHandler = remindHandler,
            //runNamesRedeemHandler = runNamesRedeemHandler
        )

        backgroundCoroutineScope.launch {
            logger.info("Starting command handler")
            command.handler(commandHandlerScope, parts.drop(1))

            val key = command to messageEvent.user.name
            nextAllowedCommandUsageInstantPerUser[key] = Clock.System.now() + commandHandlerScope.addedUserCooldown

            nextAllowedCommandUsageInstantPerCommand[command] = Clock.System.now() + commandHandlerScope.addedCommandCooldown

            commandsInUsage.remove(command)
        }
    }

    /*twitchClient.eventManager.onEvent(RewardRedeemedEvent::class.java) { redeemEvent ->
        val redeem = redeems.find { redeemEvent.redemption.reward.id in it.id || redeemEvent.redemption.reward.title in it.id }?.also {
            if (redeemEvent.redemption.reward.title in it.id) {
                logger.warn("Redeem ${redeemEvent.redemption.reward.title}. Please use following ID in the properties file instead of the name: ${redeemEvent.redemption.reward.id}")
            }
        } ?: return@onEvent

        val redeemHandlerScope = RedeemHandlerScope(
            chat = twitchClient.chat,
            redeemEvent = redeemEvent,
            runNamesRedeemHandler = runNamesRedeemHandler
        )

        backgroundCoroutineScope.launch {
            logger.info("Starting redeem handler")
            redeem.handler(redeemHandlerScope)
        }
    }*/

    logger.info("Twitch client started.")
    return twitchClient
}


// Setup local clip server
/**
 * Hosts the local clip player server.
 */
fun hostClipPlayerServer() {
    embeddedServer(CIO, port = ClipPlayerConfig.port) {
        install(WebSockets)
        install(PartialContent)
        install(AutoHeadResponse)

        routing {
            clipOverlayPage()

            webSocket("/socket") {
                val clipPlayerInstance = ClipPlayer.instance ?: run {
                    close(CloseReason(CloseReason.Codes.INTERNAL_ERROR, "Clip player not setup."))
                    logger.error("Clip player not setup.")
                    return@webSocket
                }

                logger.info("Clip player got new connection.")

                try {
                    for (frame in incoming) {
                        send(clipPlayerInstance.popNextRandomClip())
                    }
                } finally {
                    logger.info("User disconnected from clip player.")
                }
            }

            staticFiles("/video", File(ClipPlayerConfig.clipLocation))
        }
    }.start(wait = false)
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


/**
 * This function checks on bot start up if the save data folder exists and if not, it creates it.
 */
fun createSaveDataFolder() {
    val folder = File("data\\saveData")

    if(!folder.exists() || !folder.isDirectory) {
        folder.mkdirs()
        logger.info("Created folder ${folder.name}")
    }
}