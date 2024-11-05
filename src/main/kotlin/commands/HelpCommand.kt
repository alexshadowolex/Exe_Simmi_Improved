package commands

import config.TwitchBotConfig
import handler.Command
import handler.commands
import sendMessageToTwitchChatAndLogIt

val helpCommand: Command = Command(
    names = listOf("help"),
    description = "Displays all available commands. " +
            "If a valid command is given as argument, the description of said command will be displayed instead.",
    handler = { arguments ->
        var message =
            """ 
                Available commands: 
                ${commands.joinToString("; ") { command -> 
                    command.names.joinToString("|") { "${TwitchBotConfig.commandPrefix}${it}" } 
                }}.
            """.trimIndent()

        val command = commands.find { arguments.firstOrNull()?.lowercase() in it.names }
        if(command != null) {
            message =
                """
                    Command ${arguments.first()}: 
                    ${command.description}
                """.trimIndent()
        }

        sendMessageToTwitchChatAndLogIt(chat, message)
    }
)