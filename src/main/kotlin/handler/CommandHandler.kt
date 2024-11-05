package handler

import com.github.twitch4j.chat.TwitchChat
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import dev.kord.core.Kord
import kotlin.time.Duration

data class Command(
    val names: List<String>,
    val handler: suspend CommandHandlerScope.(arguments: List<String>) -> Unit,
    val description: String
)

data class CommandHandlerScope(
    val discordClient: Kord,
    val chat: TwitchChat,
    val messageEvent: ChannelMessageEvent,
    // TODO
    //val remindHandler: RemindHandler,
    //val runNamesRedeemHandler: RunNamesRedeemHandler,
    var addedUserCooldown: Duration = Duration.ZERO,
    var addedCommandCooldown: Duration = Duration.ZERO
)

val commands = listOf<Command>(

)