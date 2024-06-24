package es.skepz.magik.races

import es.skepz.magik.CustomItem
import es.skepz.magik.Magik
import es.skepz.magik.skepzlib.colorize
import es.skepz.magik.skepzlib.playSound
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class Elf(magik: Magik) : Race(magik) {

    private val bow = CustomItem(magik, Material.BOW, 1, "&2Longbow",
        listOf("&aBuilt with precision to do maximum damage"),
        "elven_bow", true,
        mutableMapOf(Pair(Enchantment.POWER, 5),
            Pair(Enchantment.INFINITY, 1),
            Pair(Enchantment.PUNCH, 4)))

    override fun update(player: Player) {
        if (player.isSneaking && (player as LivingEntity).isOnGround) {
            player.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, 2, 0, false, false))
            player.addPotionEffect(PotionEffect(PotionEffectType.WEAKNESS, 2, 2, false, false))
        }

        if (player.location.block.lightLevel <= 10) {
            player.addPotionEffect(PotionEffect(PotionEffectType.WEAKNESS, 2, 1, false, false))
        } else {
            player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 2, 0, false, false))
            player.addPotionEffect(PotionEffect(PotionEffectType.JUMP_BOOST, 2, 0, false, false))
        }
    }

    override fun guiDisplayItem(): ItemStack {
        val item = ItemStack(Material.BOW, 1)

        item.itemMeta = item.itemMeta.also {
            it.isUnbreakable = true
            it.displayName(colorize("&2&lElf"))
            it.lore(listOf(
                colorize("&7Great for players who prefer ranged PVP"),
                colorize("&7- &aLongbow: Shoots more powerful arrows"),
                colorize("&7- &aFaster than most"),
                colorize("&7- &aInvisible when sneaking on the ground"),
                colorize("&7- &aJumps higher"),
                colorize("&7- &cScared of the dark (weaker and no boosts)"),
            ))
        }

        return item
    }

    private fun Player.isElf(): Boolean {
        return getRace(magik, this) is Elf
    }

    override fun name(): String {
        return "Elf"
    }

    override fun set(player: Player) {
        val inv = player.inventory
        inv.addItem(bow.generate())
        if (!inv.contains(Material.ARROW)) {
            inv.addItem(ItemStack(Material.ARROW, 1))
        }
    }

    override fun remove(player: Player) {
        val inv = player.inventory
        inv.contents.forEach { item ->
            if (item == null) return@forEach
            if (bow.check(item)) {
                inv.remove(item)
            }
        }
    }

    @EventHandler
    fun itemDrop(event: PlayerDropItemEvent) {
        val player = event.player.takeIf { it.isElf() }
            ?: return

        if (bow.check(event.itemDrop.itemStack)) {
            event.isCancelled = true
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
        }
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val player = (event.whoClicked as? Player)?.takeIf { it.isElf() }
            ?: return

        val item = event.currentItem
            ?: return

        if (bow.check(item) && event.inventory.type != InventoryType.CRAFTING) {
            event.isCancelled = true
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
        }
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        if (!event.player.isElf()) return
        val drops = event.drops
        val remove = mutableListOf<ItemStack>()
        drops.forEach { item ->
            if (item == null) return@forEach
            if (bow.check(item)) {
                remove.add(item)
            }
        }
        remove.forEach { item ->
            drops.remove(item)
        }
    }

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) {
        val player = event.player.takeIf { it.isElf() }
            ?: return

        setRace(magik, player, this, false)
    }
}