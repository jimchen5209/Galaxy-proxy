package one.oktw.galaxy.proxy.command

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import one.oktw.galaxy.proxy.Main.Companion.main
import one.oktw.galaxy.proxy.resourcepack.ResourcePackHelper

class Lobby : SimpleCommand {
    override fun execute(invocation: SimpleCommand.Invocation?) {
        val source = invocation?.source() ?: return
        if (source !is Player) return

        source.createConnectionRequest(main.lobby).fireAndForget()
        ResourcePackHelper.trySendResourcePack(source, "lobby")
    }
}
