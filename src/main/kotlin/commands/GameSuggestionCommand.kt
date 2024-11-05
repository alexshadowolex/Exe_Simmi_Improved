package commands

import DiscordMessageContent
import config.DiscordBotConfig
import config.TwitchBotConfig
import handler.Command
import logger
import sendMessageToDiscordBot
import sendMessageToTwitchChatAndLogIt
import kotlin.time.Duration.Companion.seconds

val gameSuggestionCommand: Command = Command(
    names = listOf("gs", "gamesuggestion"),
    description = "Automatically posts the given message in the game suggestion channel on Discord.",
    handler = { arguments ->
        val message = arguments.joinToString(" ")
        if (message.trim().isEmpty()) {
            sendMessageToTwitchChatAndLogIt(chat, "No input has been provided ${TwitchBotConfig.rejectEmote}")
            addedUserCooldown = 5.seconds
            return@Command
        }

        val currentMessageContent = DiscordMessageContent(
            message = DiscordMessageContent.Message.FromText(message),
            title = "Suggestion for ",
            user = messageEvent.user.name,
            channelId = DiscordBotConfig.gameChannelId
        )

        val channel = sendMessageToDiscordBot(currentMessageContent)
        sendMessageToTwitchChatAndLogIt(chat, "Message sent in #${channel.name} ${TwitchBotConfig.confirmEmote}")

        addedUserCooldown = TwitchBotConfig.userCooldown
        addedCommandCooldown = TwitchBotConfig.commandCooldown
    }
)