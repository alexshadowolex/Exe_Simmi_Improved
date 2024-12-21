package commands

import config.TwitchBotConfig
import handler.Command
import isUserInUsageList
import isUserTheBroadcaster
import logger
import sendMessageToTwitchChatAndLogIt
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private val remindCommandNames = listOf("remind", "r")

val remindCommand: Command = Command(
    names = remindCommandNames,
    description = "Saves a remind message for given duration. When the time is due, the bot will post the given " +
            "message or a default one, if there was no message given. " +
            "Usage: <interval time in minutes> <message>",
    handler = { arguments ->
        if (
            !isUserInUsageList(messageEvent.user, TwitchBotConfig.remindCommandUsers, "remind") &&
            !isUserTheBroadcaster(messageEvent.user.name)
            ) {

            logger.info("User ${messageEvent.user.name} is not eligible to use the remind command.")
            return@Command
        }

        if (arguments.isEmpty()) {
            sendMessageToTwitchChatAndLogIt(
                chat,
                "No arguments have been provided. Please provide at least the waiting " +
                        "duration in minutes ${TwitchBotConfig.explanationEmote}"
            )
            addedUserCooldown = 5.seconds
            return@Command
        }

        val intervalTime = arguments[0].toDoubleOrNull()

        if(intervalTime == null) {
            sendMessageToTwitchChatAndLogIt(chat,
                "You must give an interval time like this: " +
                        "\"${TwitchBotConfig.commandPrefix}${remindCommandNames.first()} <interval time> <message>\""
            )
            addedUserCooldown = 5.seconds
            return@Command
        }

        val intervalTimeDuration = intervalTime.minutes

        if (intervalTimeDuration.inWholeMilliseconds <= 0) {
            sendMessageToTwitchChatAndLogIt(chat, "Invalid interval time. It must be greater than zero!")
            addedUserCooldown = 5.seconds
            return@Command
        }

        addedUserCooldown = TwitchBotConfig.userCooldown
        addedCommandCooldown = TwitchBotConfig.commandCooldown

        remindHandler.addToReminders(
            intervalTimeDuration,
            messageEvent.user.name,
            arguments.drop(1).joinToString(" ")
        )

        sendMessageToTwitchChatAndLogIt(
            chat,
            "${TwitchBotConfig.explanationEmote} You will be reminded in $intervalTimeDuration"
        )
    }
)