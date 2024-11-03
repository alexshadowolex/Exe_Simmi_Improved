package config

import addQuotationMarks
import getPropertyValue
import logger
import showErrorMessageWindow
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.system.exitProcess

object GoogleSpreadSheetConfig {
    private val googleSpreadSheetConfigFile = File("data\\properties\\googleSpreadSheetConfig.properties")
    private val properties = Properties().apply {
        if(!googleSpreadSheetConfigFile.exists()) {
            logger.error(
                "Error while reading property file ${googleSpreadSheetConfigFile.path} in GoogleSpreadSheet init: " +
                        "File does not exist!"
            )
            showErrorMessageWindow(
                title = "Error while reading properties file",
                message = "Property file ${googleSpreadSheetConfigFile.path.addQuotationMarks()} does not exist!"
            )

            exitProcess(-1)
        }
        load(googleSpreadSheetConfigFile.inputStream())
    }


    var spreadSheetId: String = getPropertyValue(
        properties, "spreadSheetId", googleSpreadSheetConfigFile.path
    )
        set(value) {
            field = value
            properties.setProperty("spreadSheetId", value)
            savePropertiesToFile()
        }

    var sheetName: String = getPropertyValue(
        properties, "sheetName", googleSpreadSheetConfigFile.path
    )
        set(value) {
            field = value
            properties.setProperty("sheetName", value)
            savePropertiesToFile()
        }

    var sheetId = getPropertyValue(
            properties, "sheetId", googleSpreadSheetConfigFile.path
    ).toInt()
        set(value) {
            field = value
            properties.setProperty("sheetId", value.toString())
            savePropertiesToFile()
        }

    var firstDataCell: String = getPropertyValue(
            properties, "firstDataCell", googleSpreadSheetConfigFile.path
    )
        set(value) {
            field = value
            properties.setProperty("firstDataCell", value)
            savePropertiesToFile()
        }

    var lastDataCell: String = getPropertyValue(
            properties, "lastDataCell", googleSpreadSheetConfigFile.path
    )
        set(value) {
            field = value
            properties.setProperty("lastDataCell", value)
            savePropertiesToFile()
        }


    private fun savePropertiesToFile() {
        properties.store(FileOutputStream(googleSpreadSheetConfigFile.path), null)
    }
}