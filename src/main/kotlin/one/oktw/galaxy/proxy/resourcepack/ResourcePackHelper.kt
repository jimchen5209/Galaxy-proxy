package one.oktw.galaxy.proxy.resourcepack

import com.velocitypowered.api.proxy.Player
import one.oktw.galaxy.proxy.Main.Companion.main

class ResourcePackHelper {
    companion object{
        fun trySendResourcePack(player: Player, galaxy: String){
            val resourcePack = main.config.galaxiesResourcePack[galaxy] ?: return
            player.sendResourcePacks(resourcePack)
        }

        fun tryRemoveResourcePack(player: Player, galaxy: String) {
            val resourcePack = main.config.galaxiesResourcePack[galaxy] ?: return
            player.removeResourcePacks(resourcePack)
        }
    }
}
