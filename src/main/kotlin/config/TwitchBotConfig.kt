package config

import addQuotationMarks
import getPropertyValue
import joinToLowercasePropertiesString
import logger
import showErrorMessageWindow
import toIntPropertiesString
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

object TwitchBotConfig {
    private val twitchBotConfigFile = File("data\\properties\\twitchBotConfig.properties")
    private val properties = Properties().apply {
        if(!twitchBotConfigFile.exists()) {
            logger.error(
                "Error while reading property file ${twitchBotConfigFile.path} in TwitchBotConfig init: " +
                        "File does not exist!"
            )
            showErrorMessageWindow(
                title = "Error while reading properties file",
                message = "Property file ${twitchBotConfigFile.path.addQuotationMarks()} does not exist!"
            )

            exitProcess(-1)
        }
        load(twitchBotConfigFile.inputStream())
    }


    var channel: String = getPropertyValue(
        properties, "channel", twitchBotConfigFile.path
    )
        set(value) {
            field = value
            properties.setProperty("channel", value)
            savePropertiesToFile()
        }

    var commandPrefix: String = getPropertyValue(
            properties, "commandPrefix", twitchBotConfigFile.path
    )
        set(value) {
            field = value
            properties.setProperty("commandPrefix", value)
            savePropertiesToFile()
        }

    var userCooldown = try {
        getPropertyValue(
            properties, "userCooldownSeconds", twitchBotConfigFile.path
        ).toInt().seconds
    } catch (e: NumberFormatException) {
        val defaultValue = 0.seconds
        logger.warn(
            "Invalid number found while parsing property userCooldownSeconds, setting to $defaultValue"
        )
        defaultValue
    }
        set(value) {
            field = value
            properties.setProperty("userCooldownSeconds", value.toIntPropertiesString(DurationUnit.SECONDS))
            savePropertiesToFile()
        }

    var commandCooldown = try {
        getPropertyValue(
            properties, "commandCooldownSeconds", twitchBotConfigFile.path
        ).toInt().seconds
    } catch (e: NumberFormatException) {
        val defaultValue = 0.seconds
        logger.warn(
            "Invalid number found while parsing property commandCooldownSeconds, setting to $defaultValue"
        )
        defaultValue
    }
        set(value) {
            field = value
            properties.setProperty("commandCooldownSeconds", value.toIntPropertiesString(DurationUnit.SECONDS))
            savePropertiesToFile()
        }

    var leaveEmote: String = getPropertyValue(
            properties, "leaveEmote", twitchBotConfigFile.path
    )
        set(value) {
            field = value
            properties.setProperty("leaveEmote", value)
            savePropertiesToFile()
        }

    var arriveEmote: String = getPropertyValue(
            properties, "arriveEmote", twitchBotConfigFile.path
    )
        set(value) {
            field = value
            properties.setProperty("arriveEmote", value)
            savePropertiesToFile()
        }

    var confirmEmote: String = getPropertyValue(
            properties, "confirmEmote", twitchBotConfigFile.path
    )
        set(value) {
            field = value
            properties.setProperty("confirmEmote", value)
            savePropertiesToFile()
        }

    var rejectEmote: String = getPropertyValue(
            properties, "rejectEmote", twitchBotConfigFile.path
    )
        set(value) {
            field = value
            properties.setProperty("rejectEmote", value)
            savePropertiesToFile()
        }

    var explanationEmote: String = getPropertyValue(
            properties, "explanationEmote", twitchBotConfigFile.path
    )
        set(value) {
            field = value
            properties.setProperty("explanationEmote", value)
            savePropertiesToFile()
        }

    var allowedDomains: List<String> = getPropertyValue(
            properties, "allowedDomains", twitchBotConfigFile.path
    ).split(",").filter { it.isNotEmpty() }
        set(value) {
            field = value
            properties.setProperty("allowedDomains", value.joinToLowercasePropertiesString(","))
            savePropertiesToFile()
        }

    var blacklistedUsers: List<String> = getPropertyValue(
            properties, "blacklistedUsers", twitchBotConfigFile.path
    ).split(",").filter { it.isNotEmpty() }
        set(value) {
            field = value
            properties.setProperty("blacklistedUsers", value.joinToLowercasePropertiesString(","))
            savePropertiesToFile()
        }

    var blacklistEmote: String = getPropertyValue(
            properties, "blacklistEmote", twitchBotConfigFile.path
    )
        set(value) {
            field = value
            properties.setProperty("blacklistEmote", value)
            savePropertiesToFile()
        }

    var remindCommandUsers: List<String> = getPropertyValue(
            properties, "remindCommandUsers", twitchBotConfigFile.path
    ).split(",").filter { it.isNotEmpty() }
        set(value) {
            field = value
            properties.setProperty("remindCommandUsers", value.joinToLowercasePropertiesString(","))
            savePropertiesToFile()
        }

    var remindEmote: String = getPropertyValue(
            properties, "remindEmote", twitchBotConfigFile.path
    )
        set(value) {
            field = value
            properties.setProperty("remindEmote", value)
            savePropertiesToFile()
        }

    var remindEmoteFail: String = getPropertyValue(
            properties, "remindEmoteFail", twitchBotConfigFile.path
    )
        set(value) {
            field = value
            properties.setProperty("remindEmoteFail", value)
            savePropertiesToFile()
        }

    var runNameRedeemId: String = getPropertyValue(
            properties, "runNameRedeemId", twitchBotConfigFile.path
    )
        set(value) {
            field = value
            properties.setProperty("runNameRedeemId", value)
            savePropertiesToFile()
        }

    var amountDisplayedRunnerNames = try {
        getPropertyValue(
            properties, "amountDisplayedRunnerNames", twitchBotConfigFile.path
        ).toInt()
    } catch (e: NumberFormatException) {
        val defaultValue = 3
        logger.warn(
            "Invalid number found while parsing property amountDisplayedRunnerNames, setting to $defaultValue"
        )
        defaultValue
    }
        set(value) {
            field = value
            properties.setProperty("amountDisplayedRunnerNames", value.toString())
            savePropertiesToFile()
        }

    var currentRunnerNamePreText: String = getPropertyValue(
            properties, "currentRunnerNamePreText", twitchBotConfigFile.path
    )
        set(value) {
            field = value
            properties.setProperty("currentRunnerNamePreText", value)
            savePropertiesToFile()
        }

    var currentRunnerNamePostText: String = getPropertyValue(
            properties, "currentRunnerNamePostText", twitchBotConfigFile.path
    )
        set(value) {
            field = value
            properties.setProperty("currentRunnerNamePostText", value)
            savePropertiesToFile()
        }

    var hitCounterLocation: String = getPropertyValue(
            properties, "hitCounterLocation", twitchBotConfigFile.path
    )
        set(value) {
            field = value
            properties.setProperty("hitCounterLocation", value)
            savePropertiesToFile()
        }

    var runnersListIndexEmote: String = getPropertyValue(
            properties, "runnersListIndexEmote", twitchBotConfigFile.path
    )
        set(value) {
            field = value
            properties.setProperty("runnersListIndexEmote", value)
            savePropertiesToFile()
        }


    private fun savePropertiesToFile() {
        properties.store(FileOutputStream(twitchBotConfigFile.path), null)
    }
}