package es.skepz.magik.races

import es.skepz.magik.Magik
import es.skepz.magik.tuodlib.colorize
import es.skepz.magik.tuodlib.displayParticles
import es.skepz.magik.tuodlib.playSound
import es.skepz.magik.tuodlib.sendMessage
import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

class Goblin(magik: Magik) : Race(magik) {

    private val swordKey = NamespacedKey(magik, "goblin_knife")
    private val swordName = colorize("&eDagger")

    private val cooldownMap = mutableMapOf<Player, Int>()
    private val defaultCooldown = 10

    override fun cooldownUpdate() {
        cooldownMap.forEach { (plr, seconds) ->
            if (seconds == 0) {
                cooldownMap.remove(plr)
                val stick = findSword(plr) ?: return@forEach
                updateData(plr, stick)
                return@forEach
            }
            cooldownMap[plr] = seconds - 1
            val stick = findSword(plr) ?: return@forEach
            updateData(plr, stick)
        }
    }

    override fun update(player: Player) {
        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 2, 1, false, false))
    }

    override fun guiDisplayItem(): ItemStack {
        val item = ItemStack(Material.IRON_SWORD, 1)

        item.itemMeta = item.itemMeta.also {
            it.isUnbreakable = true
            it.displayName(Component.text(colorize("&c&lGoblin")))
            it.lore(listOf(
                Component.text(colorize("&7Great for players who love to move quick and make fast attacks")),
                Component.text(colorize("&7- &aDagger: attack fast and efficiently")),
                Component.text(colorize("&7- &aAmong the quickest around")),
                Component.text(colorize("&7- &aImmune to poison")),
                Component.text(colorize("&7- &cCan only wear leather armor")),
                Component.text(colorize("&7- &c6 max hearts"))))
        }

        return item
    }

    private fun generateSword(): ItemStack {
        val item = ItemStack(Material.IRON_SWORD)
        item.itemMeta = item.itemMeta.also {
            it.displayName(Component.text(swordName))
            it.lore(listOf(
                Component.text(colorize("&aQuick and efficient strikes")),
                Component.text(colorize("&8[&6Right Click&8] &7to dash."))
            ))
            it.isUnbreakable = true
            it.persistentDataContainer.set(swordKey, PersistentDataType.DOUBLE, Math.PI)
            it.addEnchant(Enchantment.DAMAGE_ALL, 5, true)
            it.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED,
                AttributeModifier(UUID.randomUUID(), "goblin_modifier", 0.8, AttributeModifier.Operation.ADD_NUMBER))
            it.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        }
        return item
    }

    private fun findSword(player: Player) : ItemStack? {
        player.inventory.contents.forEach {
            if (it == null) return@forEach
            if (checkSword(it)) {
                return it
            }
        }
        return null
    }

    private fun checkSword(item: ItemStack): Boolean {
        if (!item.hasItemMeta()) return false
        return item.itemMeta.persistentDataContainer.has(swordKey, PersistentDataType.DOUBLE)
    }

    private fun updateData(player: Player, sword: ItemStack) {
        sword.itemMeta = sword.itemMeta.also {
            if (cooldownMap.containsKey(player)) {
                it.displayName(Component.text(colorize("$swordName &8[&c${cooldownMap[player] ?: 0}&8]")))
            } else {
                it.displayName(Component.text(colorize(swordName)))
            }
        }
    }

    private fun Player.isGoblin(): Boolean {
        return getRace(magik, this) is Goblin
    }

    override fun name(): String {
        return "Goblin"
    }

    override fun set(player: Player) {
        player.maxHealth = 12.0
        val inventory = player.inventory
        inventory.addItem(generateSword())

        // handle armor
        val helm = inventory.helmet
        val ches = inventory.chestplate
        val legg = inventory.leggings
        val boot = inventory.boots

        if (helm != null && isHeavyArmor(helm)) {
            inventory.addItem(helm)
            inventory.helmet = ItemStack(Material.AIR)
        }
        if (ches != null && isHeavyArmor(ches)) {
            inventory.addItem(ches)
            inventory.chestplate = ItemStack(Material.AIR)
        }
        if (legg != null && isHeavyArmor(legg)) {
            inventory.addItem(legg)
            inventory.leggings = ItemStack(Material.AIR)
        }
        if (boot != null && isHeavyArmor(boot)) {
            inventory.addItem(boot)
            inventory.boots = ItemStack(Material.AIR)
        }
    }

    override fun remove(player: Player) {
        val inv = player.inventory
        inv.contents.forEach { item ->
            if (item == null) return@forEach
            if (checkSword(item)) {
                inv.remove(item)
            }
        }
    }

    private fun isHeavyArmor(item: ItemStack): Boolean {
        return item.type == Material.CHAINMAIL_BOOTS || item.type == Material.IRON_BOOTS ||
                item.type == Material.DIAMOND_BOOTS || item.type == Material.GOLDEN_BOOTS ||
                item.type == Material.NETHERITE_BOOTS || item.type == Material.CHAINMAIL_LEGGINGS ||
                item.type == Material.IRON_LEGGINGS || item.type == Material.DIAMOND_LEGGINGS ||
                item.type == Material.GOLDEN_LEGGINGS || item.type == Material.NETHERITE_LEGGINGS ||
                item.type == Material.CHAINMAIL_CHESTPLATE|| item.type == Material.IRON_CHESTPLATE ||
                item.type == Material.DIAMOND_CHESTPLATE || item.type == Material.GOLDEN_CHESTPLATE ||
                item.type == Material.NETHERITE_CHESTPLATE || item.type == Material.CHAINMAIL_HELMET ||
                item.type == Material.IRON_HELMET || item.type == Material.DIAMOND_HELMET ||
                item.type == Material.GOLDEN_HELMET || item.type == Material.NETHERITE_HELMET
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val item = event.item
            ?: return

        val player = event.player
        if (!player.isGoblin()) {
            return
        }

        // check if the item is armor
        if (isHeavyArmor(item) && (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK)) {
            event.isCancelled = true
            sendMessage(player, "&cGoblins can only wear leather armor!")
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
            return
        }

        if (checkSword(item) && event.action == Action.RIGHT_CLICK_AIR) {
            val cooldown = cooldownMap[player]
            if (cooldown != null) {
                sendMessage(player, "&cYou can't use this item for another $cooldown seconds!")
                playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
                return
            }

            val direction = player.location.direction

            val multiplier = if (player.isOnGround) 5 else 1

            direction.x *= multiplier
            direction.z *= multiplier
            direction.y *= 0.0

            player.velocity = direction
            cooldownMap[player] = defaultCooldown
            updateData(player, item)
        }

    }

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        val player = (event.entity as? Player)?.takeIf { it.isGoblin() }
            ?: return

        if (event.cause == EntityDamageEvent.DamageCause.POISON) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun itemDrop(event: PlayerDropItemEvent) {
        val player = event.player.takeIf { it.isGoblin() }
            ?: return

        if (checkSword(event.itemDrop.itemStack)) {
            event.isCancelled = true
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
        }
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val player = (event.whoClicked as? Player)?.takeIf { it.isGoblin() }
            ?: return

        val item = event.currentItem
            ?: return

        if (checkSword(item) && (event.action == InventoryAction.MOVE_TO_OTHER_INVENTORY || event.inventory.type == InventoryType.ANVIL)) {
            event.isCancelled = true
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
        }

        if (event.click.isShiftClick && event.inventory.type == InventoryType.CRAFTING && isHeavyArmor(item)) {
            event.isCancelled = true
            sendMessage(player, "&cGoblins can only wear leather armor!")
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
            return
        }

        if (event.slotType != InventoryType.SlotType.ARMOR) {
            return
        }
        val cursor = event.cursor
        if (cursor != null && isHeavyArmor(cursor)) {
            event.isCancelled = true
            sendMessage(player, "&cAvians can only wear leather or chainmail armor!")
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
            return
        }
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        if (!event.player.isGoblin()) return
        val drops = event.drops
        val remove = mutableListOf<ItemStack>()
        drops.forEach { item ->
            if (item == null) return@forEach
            if (checkSword(item)) {
                remove.add(item)
            }
        }
        remove.forEach { item ->
            drops.remove(item)
        }
    }

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) {
        val player = event.player.takeIf { it.isGoblin() }
            ?: return

        setRace(magik, player, this, false)
    }
}