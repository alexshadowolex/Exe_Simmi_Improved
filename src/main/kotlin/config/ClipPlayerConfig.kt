package config

import addQuotationMarks
import getPropertyValue
import joinToLowercasePropertiesString
import logger
import showErrorMessageWindow
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.system.exitProcess

object ClipPlayerConfig {
    private val clipPlayerConfigFile = File("data\\properties\\clipPlayer.properties")
    private val properties = Properties().apply {
        if(!clipPlayerConfigFile.exists()) {
            logger.error(
                "Error while reading property file ${clipPlayerConfigFile.path} in ClipPlayerConfig init: " +
                        "File does not exist!"
            )
            showErrorMessageWindow(
                title = "Error while reading properties file",
                message = "Property file ${clipPlayerConfigFile.path.addQuotationMarks()} does not exist!"
            )

            exitProcess(-1)
        }
        load(clipPlayerConfigFile.inputStream())
    }

    var clipLocation: String = getPropertyValue(
        properties, "clipLocation", clipPlayerConfigFile.path
    )
        set(value) {
            field = value
            properties.setProperty("clipLocation", value)
            savePropertiesToFile()
        }

    var allowedVideoFiles: List<String> = getPropertyValue(
        properties, "allowedVideoFiles", clipPlayerConfigFile.path
    ).lowercase(Locale.getDefault()).split(",").filter { it.isNotEmpty() }
        set(value) {
            field = value
            properties.setProperty(
                "allowedVideoFiles", value.joinToLowercasePropertiesString(",")
            )
            savePropertiesToFile()
        }

    var port = try {
        getPropertyValue(
            properties, "port", clipPlayerConfigFile.path
        ).toInt()
    } catch (e: NumberFormatException) {
        val defaultValue = 12345
        logger.warn(
            "Invalid number found while parsing property port, setting to $defaultValue"
        )
        defaultValue
    }
    set(value) {
        field = value
        properties.setProperty("port", value.toString())
        savePropertiesToFile()
    }


    private fun savePropertiesToFile() {
        properties.store(FileOutputStream(clipPlayerConfigFile.path), null)
    }
}