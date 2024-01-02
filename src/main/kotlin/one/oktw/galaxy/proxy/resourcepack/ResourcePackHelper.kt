package one.oktw.galaxy.proxy.resourcepack

import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.resource.ResourcePackRequest
import one.oktw.galaxy.proxy.Main.Companion.main

class ResourcePackHelper {
    companion object {
        fun trySendResourcePack(player: Player, galaxy: String) {
            val resourcePack = main.config.galaxiesResourcePack[galaxy] ?: return
            val request = ResourcePackRequest.resourcePackRequest()
                .packs(resourcePack.packInfo)
                .required(true)
                .build()
            player.sendResourcePacks(request)
        }

        fun tryRemoveResourcePack(player: Player, galaxy: String) {
            val resourcePack = main.config.galaxiesResourcePack[galaxy] ?: return
            player.removeResourcePacks(resourcePack.uuid)
        }
    }
}
