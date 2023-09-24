package es.skepz.magik.races

import es.skepz.magik.CustomItem
import es.skepz.magik.Magik
import es.skepz.magik.tuodlib.colorize
import es.skepz.magik.tuodlib.playSound
import es.skepz.magik.tuodlib.sendMessage
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.entity.Tameable
import org.bukkit.entity.Wolf
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class Lycan(magik: Magik) : Race(magik) {

    private val claw = CustomItem(magik, Material.PRISMARINE_SHARD, 1, "&cLycan Claw",
        listOf("&cStronger at night...", "&8[&6Right Click&8] &7to tame wolves."),
        "lycan_claw", false,
        mapOf(Pair(Enchantment.DAMAGE_ALL, 5)))

    override fun comingSoon(): Boolean {
        return true
    }

    override fun update(player: Player) {
        val world = player.location.world
        val claw = claw.find(player.inventory)

        if (world.environment != World.Environment.NORMAL) {
            player.maxHealth = 12.0
            player.addPotionEffect(PotionEffect(PotionEffectType.WEAKNESS, 2, 0, false, false))
            if (claw != null) {
                claw.itemMeta = claw.itemMeta.also {
                    val sharpnessLVL = it.getEnchantLevel(Enchantment.DAMAGE_ALL)
                    if (sharpnessLVL != 1) {
                        it.addEnchant(Enchantment.DAMAGE_ALL, 1, false)
                        it.lore(
                            listOf(
                                Component.text(colorize("&cThere is no moon here. You feel weak.")),
                                Component.text(colorize("&8[&6Right Click&8] &7to tame wolves."))
                            )
                        )
                    }
                }
            }
            return
        }

        if (player.location.world.isDayTime) {
            // day
            if (claw != null) {
                claw.itemMeta = claw.itemMeta.also {
                    val sharpnessLVL = it.getEnchantLevel(Enchantment.DAMAGE_ALL)
                    if (sharpnessLVL != 3) {
                        it.addEnchant(Enchantment.DAMAGE_ALL, 3, false)
                        it.lore(
                            listOf(
                                Component.text(colorize("&cStronger at night...")),
                                Component.text(colorize("&8[&6Right Click&8] &7to tame wolves."))
                            )
                        )
                    }
                }
            }
        } else {
            // night
            player.maxHealth = 28.0
            player.addPotionEffect(PotionEffect(PotionEffectType.JUMP, 2, 1, false, false))
            player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 2, 0, false, false))
            player.addPotionEffect(PotionEffect(PotionEffectType.INCREASE_DAMAGE, 2, 0, false, false))
            player.addPotionEffect(PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 2, 0, false, false))
            if (claw != null) {
                claw.itemMeta = claw.itemMeta.also {
                    val sharpnessLVL = it.getEnchantLevel(Enchantment.DAMAGE_ALL)
                    if (sharpnessLVL != 10) {
                        it.addEnchant(Enchantment.DAMAGE_ALL, 10, true)
                        it.lore(
                            listOf(
                                Component.text(colorize("&cATTACK")),
                                Component.text(colorize("&8[&6Right Click&8] &7to tame wolves."))
                            )
                        )
                    }
                }
            }
        }
    }

    override fun guiDisplayItem(): ItemStack {
        val item = ItemStack(Material.BONE, 1)

        item.itemMeta = item.itemMeta.also {
            it.isUnbreakable = true
            it.displayName(Component.text(colorize("&4&lLycan")))
            it.lore(listOf(
                Component.text(colorize("&7All the benefits of a werewolf, without the wolf part")),
                Component.text(colorize("&7- &aWolf Claw: Attacks stronger at night")),
                Component.text(colorize("&7- &aExtremely strong at night")),
                Component.text(colorize("&7- &aTame wolves with no bones")),
                Component.text(colorize("&7- &cWeak during the day")),
                Component.text(colorize("&7- &cEven weaker in other dimensions (no moon)")),
            ))
        }

        return item
    }

    private fun Player.isLycan(): Boolean {
        return getRace(magik, this) is Lycan
    }

    override fun name(): String {
        return "Lycan"
    }

    override fun set(player: Player) {
        val inv = player.inventory
        inv.addItem(claw.generate())
        if (!inv.contains(Material.ARROW)) {
            inv.addItem(ItemStack(Material.ARROW, 1))
        }
    }

    override fun remove(player: Player) {
        val inv = player.inventory
        inv.contents.forEach { item ->
            if (item == null) return@forEach
            if (claw.check(item)) {
                inv.remove(item)
            }
        }
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEntityEvent) {
        val player = event.player.takeIf { it.isLycan() }
            ?: return
        if (!claw.check(player.activeItem)) return
        val wolf = event.rightClicked.takeIf { it is Wolf }
        val dog = wolf as Tameable
        if (dog.isTamed) return sendMessage(player, "&cThat dog is already tamed. No stealing pets.")
        dog.owner = player
    }

    @EventHandler
    fun itemDrop(event: PlayerDropItemEvent) {
        val player = event.player.takeIf { it.isLycan() }
            ?: return

        if (claw.check(event.itemDrop.itemStack)) {
            event.isCancelled = true
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
        }
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val player = (event.whoClicked as? Player)?.takeIf { it.isLycan() }
            ?: return

        val item = event.currentItem
            ?: return

        if (claw.check(item) && event.inventory.type != InventoryType.CRAFTING) {
            event.isCancelled = true
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
        }
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        if (!event.player.isLycan()) return
        val drops = event.drops
        val remove = mutableListOf<ItemStack>()
        drops.forEach { item ->
            if (item == null) return@forEach
            if (claw.check(item)) {
                remove.add(item)
            }
        }
        remove.forEach { item ->
            drops.remove(item)
        }
    }

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) {
        val player = event.player.takeIf { it.isLycan() }
            ?: return

        setRace(magik, player, this, false)
    }
}