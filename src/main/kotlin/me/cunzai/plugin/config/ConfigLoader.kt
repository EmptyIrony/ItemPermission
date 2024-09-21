package me.cunzai.plugin.config

import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.platform.util.hasLore
import taboolib.platform.util.hasName
import java.util.ArrayList

object ConfigLoader {

    @Config
    lateinit var config: Configuration

    val items = ArrayList<ItemConfig>()

    @Awake(LifeCycle.ENABLE)
    fun i() {

        items.clear()

        val section = config.getConfigurationSection("tools")!!
        section.getKeys(false).mapNotNull { section.getConfigurationSection(it) }
            .forEach { itemSection ->
                val conditionSection = itemSection.getConfigurationSection("conditions")

                val conditions = ArrayList<Condition>()
                for (key in conditionSection!!.getKeys(false)) {
                    conditions += when(key.lowercase()) {
                        "lore" -> {
                            LoreCondition(conditionSection.getString(key)!!)
                        }
                        "name" -> {
                            NameCondition(conditionSection.getString(key)!!)
                        }
                        else -> continue
                    }
                }
                items += ItemConfig(
                    conditions,
                    itemSection.getString("permission")!!,
                    itemSection.getStringList("usedSlots").map {
                        UseSlot.valueOf(it.uppercase())
                    }
                )
            }
    }

    class ItemConfig(
        val conditions: List<Condition>,
        val permission: String,
        val equipSlot: List<UseSlot>
    )

    interface Condition {
        fun check(item: ItemStack): Boolean
    }

    class NameCondition(val name: String) : Condition {
        override fun check(item: ItemStack): Boolean {
            return item.hasName(name)
        }
    }

    class LoreCondition(val lore: String): Condition {
        override fun check(item: ItemStack): Boolean {
            return item.hasLore(lore)
        }
    }

    enum class UseSlot {
        HAND, OFF_HAND, EQUIPPED
    }

}