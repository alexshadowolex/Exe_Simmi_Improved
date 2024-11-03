package config

import addQuotationMarks
import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import getPropertyValue
import logger
import showErrorMessageWindow
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.system.exitProcess
import kotlin.text.HexFormat

object DiscordBotConfig {
    private val discordBotConfigFile = File("data\\properties\\discordBotConfig.properties")
    private val properties = Properties().apply {
        if(!discordBotConfigFile.exists()) {
            logger.error(
                "Error while reading property file ${discordBotConfigFile.path} in DiscordBotConfig init: " +
                        "File does not exist!"
            )
            showErrorMessageWindow(
                title = "Error while reading properties file",
                message = "Property file ${discordBotConfigFile.path.addQuotationMarks()} does not exist!"
            )

            exitProcess(-1)
        }
        load(discordBotConfigFile.inputStream())
    }

    val discordToken = File("data\\tokens\\discordToken.txt").readText()

    var feedbackChannelId = try {
        Snowflake(
            getPropertyValue(
                properties, "feedbackChannelId", discordBotConfigFile.path
            ).toLong()
        )
    } catch (e: NumberFormatException) {
        logger.error(
            "Invalid number found while parsing property feedbackChannelId"
        )
        exitProcess(-1)
    }
    set(value) {
        field = value
        properties.setProperty("feedbackChannelId", value.toString())
        savePropertiesToFile()
    }

    var gameChannelId = try {
        Snowflake(
            getPropertyValue(
                properties, "gameChannelId", discordBotConfigFile.path
            ).toLong()
        )
    } catch (e: NumberFormatException) {
        logger.error(
            "Invalid number found while parsing property gameChannelId"
        )
        exitProcess(-1)
    }
    set(value) {
        field = value
        properties.setProperty("gameChannelId", value.toString())
        savePropertiesToFile()
    }

    var clipChannelId = try {
        Snowflake(
            getPropertyValue(
                properties, "clipChannelId", discordBotConfigFile.path
            ).toLong()
        )
    } catch (e: NumberFormatException) {
        logger.error(
            "Invalid number found while parsing property clipChannelId"
        )
        exitProcess(-1)
    }
    set(value) {
        field = value
        properties.setProperty("clipChannelId", value.toString())
        savePropertiesToFile()
    }

    @OptIn(ExperimentalStdlibApi::class)
    var embedAccentColor = try {
        Color(
            getPropertyValue(
                properties, "embedAccentColor", discordBotConfigFile.path
            ).toInt(radix = 16)
        )
    } catch (e: NumberFormatException) {
        logger.error(
            "Invalid number found while parsing property embedAccentColor"
        )
        exitProcess(-1)
    }
    set(value) {
        field = value
        properties.setProperty("embedAccentColor", value.rgb.toHexString(HexFormat.UpperCase).drop(2))
        savePropertiesToFile()
    }

    var endedRunChannelId = try {
        Snowflake(
            getPropertyValue(
                properties, "endedRunChannelId", discordBotConfigFile.path
            ).toLong()
        )
    } catch (e: NumberFormatException) {
        logger.error(
            "Invalid number found while parsing property endedRunChannelId"
        )
        exitProcess(-1)
    }
    set(value) {
        field = value
        properties.setProperty("endedRunChannelId", value.toString())
        savePropertiesToFile()
    }


    private fun savePropertiesToFile() {
        properties.store(FileOutputStream(discordBotConfigFile.path), null)
    }
}