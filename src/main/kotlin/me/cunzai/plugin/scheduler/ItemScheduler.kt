package me.cunzai.plugin.scheduler

import me.cunzai.plugin.config.ConfigLoader
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.model.user.User
import net.luckperms.api.node.types.PermissionNode
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.platform.Schedule
import taboolib.platform.util.isAir
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

object ItemScheduler {

    @Schedule(period = 10L, async = true)
    fun s() {
        for (player in Bukkit.getOnlinePlayers()) {
            player.check()
        }
    }

    private fun Player.check() {
        for (itemConfig in ConfigLoader.items) {
            try {
                check(itemConfig)
            } catch (e: IllegalStateException) {
                return
            }
        }
    }

    private fun Player.check(config: ConfigLoader.ItemConfig) {
        val test = config.equipSlot.any {
            val items = when (it) {
                ConfigLoader.UseSlot.EQUIPPED -> inventory.armorContents
                ConfigLoader.UseSlot.HAND -> arrayOf(inventory.itemInMainHand)
                ConfigLoader.UseSlot.OFF_HAND -> arrayOf(inventory.itemInOffHand)
            }

            for (item in items) {
                if (item.isAir()) continue
                for (condition in config.conditions) {
                    if (!condition.check(item)) return@any false
                }
                // 满足所有条件才醒
                return@any true
            }

            return@any false
        }

        if (!test) {
            if (hasPermission(config.permission)) {
                println("玩家 ${name} 下权限")
                val user = LuckPermsProvider.get().userManager.getUser(uniqueId) ?: throw IllegalStateException("player not loaded lp data")
                val data = user.data()
                data.toCollection().firstOrNull { it is PermissionNode && it.permission == config.permission }?.let {
                    data.remove(it)
                    data.add(it.toBuilder().value(false).build())
                }
            }
        } else {
            println("玩家 ${name} 上权限")
            if (!hasPermission(config.permission)) {
                val user = LuckPermsProvider.get().userManager.getUser(uniqueId) ?: throw IllegalStateException("player not loaded lp data")
                val data = user.data()
                data.toCollection().firstOrNull { it is PermissionNode && it.permission == config.permission }?.let {
                    data.remove(it)
                    data.add(it.toBuilder().value(true).build())
                }
            }
        }
    }


}