package main;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Custom Eye-of-Ender behavior.
 */
public class EyeThrowListener implements Listener {
	private final SkyGridPlugin plugin;
	private final Set<UUID> locked = ConcurrentHashMap.newKeySet();

	public EyeThrowListener(SkyGridPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		EquipmentSlot hand = e.getHand();
		if (hand != EquipmentSlot.HAND && hand != EquipmentSlot.OFF_HAND) return;
		Action act = e.getAction();
		if (act != Action.RIGHT_CLICK_AIR && act != Action.RIGHT_CLICK_BLOCK) return;

		Player player = e.getPlayer();
		if (!WorldManager.isCustomWorld(player)) {
			return;
		}

		UUID uid = player.getUniqueId();
		if (!locked.add(uid)) return;

		ItemStack stack = hand == EquipmentSlot.HAND
				? player.getInventory().getItemInMainHand()
				: player.getInventory().getItemInOffHand();
		if (stack.getType() != Material.ENDER_EYE) {
			locked.remove(uid);
			return;
		}

		e.setCancelled(true);
		stack.setAmount(stack.getAmount() - 1);
		if (hand == EquipmentSlot.HAND) {
			player.getInventory().setItemInMainHand(stack);
		} else {
			player.getInventory().setItemInOffHand(stack);
		}

		Location eyeLoc = player.getEyeLocation();
		Vector dir = eyeLoc.getDirection().normalize();
		Location launchPos = eyeLoc.clone().add(dir.clone().multiply(0.5)).subtract(0, 0.5, 0);
		Vector startVec = launchPos.toVector();

		player.getWorld().playSound(launchPos, Sound.ENTITY_ENDER_EYE_LAUNCH, 1f, 1f);

		Item thrown = player.getWorld().dropItem(launchPos, new ItemStack(Material.ENDER_EYE));
		thrown.setGravity(false);
		thrown.setPickupDelay(Integer.MAX_VALUE);
		thrown.setInvulnerable(true);
		thrown.setFireTicks(0);

		DustOptions trailLight = new DustOptions(Color.fromRGB(180, 255, 200), 1f);
		DustOptions trailDark = new DustOptions(Color.fromRGB(80, 150, 90), 0.8f);
		DustOptions spinDust = new DustOptions(Color.fromRGB(100, 255, 150), 1.2f);
		DustOptions popLight = new DustOptions(Color.fromRGB(200, 160, 255), 1.5f);
		DustOptions popDark = new DustOptions(Color.fromRGB(100, 40, 200), 1.2f);

		Location nearest = plugin.getPortalManager().getNearest(player.getLocation());
		if (nearest == null) {
			popAndReturnEye(player, launchPos, popLight, popDark);
			thrown.remove();
			locked.remove(uid);
			return;
		}

		Location target = nearest.clone().add(0.5, 1.5, 0.5);
		Vector toTarget = target.toVector().subtract(startVec);
		if (toTarget.length() > 16) {
			toTarget = toTarget.normalize().multiply(16);
		}
		Location landing = launchPos.clone().add(toTarget);
		Vector endVec = landing.toVector();
		Vector mid = startVec.clone().add(endVec).multiply(0.5);
		Vector ctrl = mid.clone().add(new Vector(0, 8, 0));

		startFlight(thrown, player, startVec, endVec, ctrl, landing, trailLight, trailDark,
				spinDust, popLight, popDark, uid);
	}

	private void startFlight(Item thrown, Player player, Vector startVec, Vector endVec, Vector ctrl,
			Location landing, DustOptions trailLight, DustOptions trailDark, DustOptions spinDust,
			DustOptions popLight, DustOptions popDark, UUID uid) {
		final int totalTicks = 130;
		final int[] tick = {0};
		final Vector[] previous = {startVec.clone()};

		SkyGridScheduler.runEntityTimer(thrown, plugin, scheduled -> {
			if (++tick[0] > totalTicks) {
				scheduled.cancel();
				startHover(thrown, landing, player, spinDust, popLight, popDark, uid);
				return;
			}

			double t = tick[0] / (double) totalTicks;
			double s = t * t * (3 - 2 * t);
			Vector pos = startVec.clone().multiply((1 - s) * (1 - s))
					.add(ctrl.clone().multiply(2 * (1 - s) * s))
					.add(endVec.clone().multiply(s * s));
			Location loc = pos.toLocation(player.getWorld());

			thrown.setFireTicks(0);
			thrown.setVelocity(pos.clone().subtract(previous[0]));
			previous[0] = pos;

			if (tick[0] % 5 == 0) {
				player.getWorld().spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, trailLight);
				player.getWorld().spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, trailDark);
				double angle = tick[0] * 0.3;
				for (int i = 0; i < 2; i++) {
					double phi = angle + i * Math.PI;
					double dx = 0.2 * Math.cos(phi);
					double dz = 0.2 * Math.sin(phi);
					player.getWorld().spawnParticle(Particle.SMALL_FLAME,
							loc.clone().add(dx, 0, dz), 1, 0, 0, 0, 0);
				}
			}
		}, 1L, 1L);
	}

	private void startHover(Item eyeEntity, Location base, Player player, DustOptions spinDust,
			DustOptions popLight, DustOptions popDark, UUID uid) {
		final int duration = 100;
		final double bobFreq = Math.PI / 10;
		final double bobAmp = 0.25;
		final double startSpin = 0.15;
		final double endSpin = 0.9;
		final int[] hoverTick = {0};
		final double[] angle = {0};

		SkyGridScheduler.runEntityTimer(eyeEntity, plugin, scheduled -> {
			if (++hoverTick[0] > duration) {
				popAndReturnEye(player, base, popLight, popDark);
				eyeEntity.remove();
				locked.remove(uid);
				scheduled.cancel();
				return;
			}

			double dy = Math.sin(hoverTick[0] * bobFreq) * bobAmp;
			Location loc = base.clone().add(0, dy, 0);
			eyeEntity.teleportAsync(loc);

			if (hoverTick[0] % 10 == 0) {
				double progress = hoverTick[0] / (double) duration;
				angle[0] += startSpin + (endSpin - startSpin) * progress;
				double dx = 0.4 * Math.cos(angle[0]);
				double dz = 0.4 * Math.sin(angle[0]);

				player.getWorld().spawnParticle(Particle.DUST,
						loc.clone().add(dx, 0, dz), 1, 0, 0, 0, spinDust);
				player.getWorld().spawnParticle(Particle.SMALL_FLAME,
						loc.clone().add(dx, 0, dz), 1, 0, 0, 0, 0);
				player.getWorld().spawnParticle(Particle.DUST,
						loc.clone().add(-dx, 0, -dz), 1, 0, 0, 0, spinDust);
				player.getWorld().spawnParticle(Particle.SMALL_FLAME,
						loc.clone().add(-dx, 0, -dz), 1, 0, 0, 0, 0);
			}
		}, 1L, 1L);
	}

	private void popAndReturnEye(Player player, Location loc, DustOptions popLight, DustOptions popDark) {
		player.getWorld().playSound(loc, Sound.ENTITY_ENDER_EYE_DEATH, 1f, 1f);
		player.getWorld().spawnParticle(Particle.EXPLOSION, loc, 1);
		player.getWorld().spawnParticle(Particle.DUST, loc, 8, 0, 0, 0, popLight);
		player.getWorld().spawnParticle(Particle.DUST, loc, 8, 0, 0, 0, popDark);
		player.getWorld().dropItem(loc, new ItemStack(Material.ENDER_EYE));
	}
}
