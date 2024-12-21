package handler

import backgroundCoroutineScope
import com.github.twitch4j.chat.TwitchChat
import config.TwitchBotConfig
import json
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import logger
import sendMessageToTwitchChatAndLogIt
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


class RemindHandler(private val chat: TwitchChat) {
    private val reminderFile = File("data\\saveData\\reminders.json")

    private var reminders = setOf<Reminder>()
        private set(value) {
            field = value
            reminderFile.writeText(json.encodeToString(field))
        }

    init {
        reminders = if (!reminderFile.exists()) {
            reminderFile.createNewFile()
            logger.info("Reminder file created.")
            mutableSetOf()
        } else {
            try {
                json.decodeFromString<Set<Reminder>>(reminderFile.readText())
                    .toMutableSet().also { currentRemindersData ->
                    logger.info(
                        "Existing reminder file found! Values: ${currentRemindersData.joinToString(" | ")}"
                    )
                }
            } catch (e: Exception) {
                logger.warn("Error while reading reminder file. Initializing empty set", e)
                mutableSetOf()
            }
        }

        startReminderCheck()
    }

    fun addToReminders(intervalTime: Duration, userName: String, remindMessage: String) {
        reminders += Reminder(
            remindMessage.ifEmpty {
                "I don't know. Something I guess? $userName did not say for what ${TwitchBotConfig.remindEmoteFail}"
            },
            Clock.System.now() + intervalTime
        )
    }

    private fun startReminderCheck() {
        backgroundCoroutineScope.launch {
            while (true) {
                reminders
                    .filter { it.timestampDue <= Clock.System.now() }
                    .forEach {
                        sendMessageToTwitchChatAndLogIt(
                            chat,
                            "${TwitchBotConfig.channel} ${TwitchBotConfig.remindEmote} " +
                                    "Time's up ${TwitchBotConfig.remindEmote} " +
                                    "Reminder for: ${it.message}"
                        )
                        reminders -= it
                    }

                delay(1.seconds)
            }
        }
    }
}


@Serializable
private data class Reminder (
    val message: String,
    val timestampDue: Instant
)