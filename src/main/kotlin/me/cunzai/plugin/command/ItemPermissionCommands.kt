package me.cunzai.plugin.command

import me.cunzai.plugin.config.ConfigLoader
import org.bukkit.command.CommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.subCommand

@CommandHeader(name = "itemPermission", permission = "admin")
object ItemPermissionCommands {

    @CommandBody
    val reload = subCommand {
        execute<CommandSender> { sender, context, argument ->
            ConfigLoader.config.reload()
            ConfigLoader.i()

            sender.sendMessage("ok")
        }
    }

}