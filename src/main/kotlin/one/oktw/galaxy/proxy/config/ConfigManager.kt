package one.oktw.galaxy.proxy.config

import com.google.gson.Gson
import net.kyori.adventure.resource.ResourcePackInfo
import one.oktw.galaxy.proxy.Main.Companion.main
import one.oktw.galaxy.proxy.config.model.GalaxySpec
import one.oktw.galaxy.proxy.config.model.ProxyConfig
import one.oktw.galaxy.proxy.config.model.RedisConfig
import java.io.InputStream
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class ConfigManager(private val basePath: Path = Paths.get("config")) {
    private val gson = Gson()

    lateinit var proxyConfig: ProxyConfig
        private set
    lateinit var redisConfig: RedisConfig
        private set
    val galaxies = HashMap<String, GalaxySpec>()
    val galaxiesResourcePack = ConcurrentHashMap<String, ResourcePackInfo>()

    init {
        readConfig()
        readGalaxies(FileSystems.newFileSystem(this::class.java.getResource("/config")!!.toURI(), emptyMap<String, Any>()).getPath("/config/galaxies"))
        readGalaxies(basePath.resolve("galaxies"))
    }

    fun reloadAll() {
        reload()
        reloadGalaxies()
    }

    fun reload() {
        readConfig()
    }

    fun reloadGalaxies() {
        readGalaxies(basePath.resolve("galaxies"))
    }

    private fun readConfig() {
        proxyConfig = fallbackToResource("proxy.json").reader().use { gson.fromJson(it, ProxyConfig::class.java) }
        redisConfig = fallbackToResource("redis.json").reader().use { gson.fromJson(it, RedisConfig::class.java) }
    }

    private fun readGalaxies(path: Path) {
        Files.newDirectoryStream(path).use {
            it.forEach { file ->
                if (Files.isDirectory(file) || !Files.isReadable(file)) return@forEach

                Files.newBufferedReader(file).use { json ->
                    val galaxyName = file.fileName.toString().substringBeforeLast(".")
                    galaxies[galaxyName] = gson.fromJson(json, GalaxySpec::class.java)
                    try {
                        main.logger.info("Loading Resource Pack of $galaxyName")
                        val packInfo = galaxies[galaxyName]?.let { spec ->
                            if (spec.ResourcePack.isNotBlank()) {
                                return@let ResourcePackInfo.resourcePackInfo()
                                    .id(UUID.nameUUIDFromBytes(spec.ResourcePack.toByteArray(StandardCharsets.UTF_8))) // From Minecraft
                                    .uri(URI(spec.ResourcePack))
                                    .computeHashAndBuild()
                                    .get()
                            }
                            return@let null
                        }
                        main.logger.info("Resource Pack: \n\tID=${galaxiesResourcePack[galaxyName]?.id()}\n\t${galaxiesResourcePack[galaxyName]?.hash()}")
                        galaxiesResourcePack[galaxyName] = packInfo ?: return
                    } catch (e: Exception) {
                        main.logger.error("Resource pack load failed!", e)
                    }
                }
            }
        }
    }

    private fun fallbackToResource(name: String): InputStream {
        val file = basePath.resolve(name)

        return if (Files.isReadable(file)) {
            Files.newInputStream(file)
        } else {
            this::class.java.getResourceAsStream("/config/$name")!!
        }
    }
}
