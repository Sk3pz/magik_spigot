package es.skepz.magik.races

import es.skepz.magik.Magik
import es.skepz.magik.tuodlib.*
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Arrow
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.RayTraceResult
import java.util.*

class Enderian(magik: Magik) : Race(magik) {

    private val swordKey = NamespacedKey(magik, "enderian_sword")
    private val swordName = colorize("&5Sword of the End")

    private val cooldownMap = mutableMapOf<Player, Int>()
    private val defaultCooldown = 6

    private val maxTeleportRange = 80.0

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
        if (player.location.world.environment == World.Environment.THE_END) {
            player.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 2, 0, false, false))
            player.resetMaxHealth()
        } else {
            player.maxHealth = 18.0
        }
    }

    override fun guiDisplayItem(): ItemStack {
        val item = ItemStack(Material.ENDER_EYE, 1)

        item.itemMeta = item.itemMeta.also {
            it.isUnbreakable = true
            it.displayName(Component.text(colorize("&5&lEnderian")))
            it.lore(listOf(
                Component.text(colorize("&7The power of the end in your fingertips")),
                Component.text(colorize("&7- &aSword of the End: Teleport where you look")),
                Component.text(colorize("&7- &aSneak for silk touch")),
                Component.text(colorize("&7- &aImmune to arrows")),
                Component.text(colorize("&7- &aRegenerate health in the end")),
                Component.text(colorize("&7- &cLess health outside the end")),
                Component.text(colorize("&7- &cCan't use elytra")),
                Component.text(colorize("&7- &cCan't use shields"))))
        }

        return item
    }

    private fun generateSword(): ItemStack {
        val item = ItemStack(Material.DIAMOND_SWORD)
        item.itemMeta = item.itemMeta.also {
            it.displayName(Component.text(swordName))
            it.lore(listOf(
                Component.text(colorize("&aThe power of the end in your fingertips")),
                Component.text(colorize("&8[&6Right Click&8] &7to teleport."))
            ))
            it.isUnbreakable = true
            it.persistentDataContainer.set(swordKey, PersistentDataType.DOUBLE, Math.PI)
            it.addEnchant(Enchantment.DAMAGE_ALL, 5, true)
            it.addEnchant(Enchantment.KNOCKBACK, 2, true)
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

    private fun Player.isEnderian(): Boolean {
        return getRace(magik, this) is Enderian
    }

    override fun name(): String {
        return "Enderian"
    }

    override fun set(player: Player) {
        val inv = player.inventory
        inv.addItem(generateSword())
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

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val item = event.item
            ?: return

        val player = event.player
        if (!player.isEnderian()) {
            return
        }

        if (item.type == Material.ELYTRA && (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK)) {
            event.isCancelled = true
            sendMessage(player, "&cYou cant wear an elytra as an Enderian!")
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
            return
        }

        if (item.type == Material.SHIELD) {
            event.isCancelled = true
            sendMessage(player, "&cYou cant use a shield as an Enderian!")
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
        }

        if (checkSword(item) && (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK)) {
            val cooldown = cooldownMap[player]
            if (cooldown != null) {
                sendMessage(player, "&cYou can't use this item for another $cooldown seconds!")
                playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
                return
            }

            val eyeLoc = player.eyeLocation

            val rs = player.world.rayTraceBlocks(eyeLoc, eyeLoc.direction, maxTeleportRange)
            if (rs == null) {
                sendMessage(player, "&cThat block is out of range!")
                return
            }
            val block = rs.hitBlock
            val blockFace = rs.hitBlockFace
            if (block == null || blockFace == null) {
                sendMessage(player, "&cThat block is out of range!")
                return
            }

            var targetLoc = block.getRelative(blockFace).location
//            if (targetLoc.block.type != Material.AIR) {
//                targetLoc = block.getRelative(blockFace).location
//            } else {
//                targetLoc.add(0.0, 1.0, 0.0)
//            }
            targetLoc.pitch = eyeLoc.pitch
            targetLoc.yaw = eyeLoc.yaw
            player.teleport(targetLoc)

            // to implement cooldown, uncomment this
//            if (player.uniqueId != UUID.fromString("32e85b31-fdb2-4199-9a88-4478f465ed4e")) {
//                cooldownMap[player] = defaultCooldown
//                updateData(player, item)
//            }
        }
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as Player
        if (!player.isEnderian()) {
            return
        }

        if (event.slotType != InventoryType.SlotType.ARMOR) {
            return
        }

        val cursor = event.cursor
        if (cursor != null && cursor.type == Material.ELYTRA) {
            event.isCancelled = true
            sendMessage(player, "&cYou cant wear an elytra as an Enderian!")
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
            return
        }
    }

    @EventHandler
    fun onBreak(event: BlockBreakEvent) {
        val player = event.player
        if (!player.isEnderian()) {
            return
        }

        if (!player.isSneaking) return

        var type = Material.BEDROCK
        val hand = player.inventory.itemInMainHand
        if (hand.type != Material.AIR) {
            type = hand.type
        }

        val fakeItem = ItemStack(type, 1)
        fakeItem.itemMeta = fakeItem.itemMeta.also {
            it.addEnchant(Enchantment.SILK_TOUCH, 1, false)
        }

        val block = event.block
        event.isDropItems = false
        val drops = block.getDrops(fakeItem)
        drops.forEach {
            dropItem(block.location.add(0.5,0.5,0.5), it)
        }
    }

    @EventHandler
    fun onDamage(event: EntityDamageByEntityEvent) {
        (event.entity as? Player)?.takeIf { it.isEnderian() }
            ?: return
        if (event.damager !is Arrow) return

        event.isCancelled = true
    }

    @EventHandler
    fun itemDrop(event: PlayerDropItemEvent) {
        val player = event.player.takeIf { it.isEnderian() }
            ?: return

        if (checkSword(event.itemDrop.itemStack)) {
            event.isCancelled = true
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
        }
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val player = (event.whoClicked as? Player)?.takeIf { it.isEnderian() }
            ?: return

        val item = event.currentItem
            ?: return

        if (checkSword(item) && (event.action == InventoryAction.MOVE_TO_OTHER_INVENTORY || event.inventory.type == InventoryType.ANVIL)) {
            event.isCancelled = true
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
        }
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        if (!event.player.isEnderian()) return
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
        val player = event.player.takeIf { it.isEnderian() }
            ?: return

        setRace(magik, player, this, false)
    }
}