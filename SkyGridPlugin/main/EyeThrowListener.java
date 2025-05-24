package main;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.Color;

/**
 * Custom Eye‐of‐Ender behavior:
 * 1) Arc toward nearest portal center  
 * 2) Hover & mirrored spin for 5s  
 * 3) Final pop + return Eye  
 * If no portal found, explode immediately.
 */
public class EyeThrowListener implements Listener {
	private final SkyGridPlugin plugin;
	private final Set<UUID> locked = new HashSet<>();

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
		
		if (!player.getWorld().getName().startsWith(WorldManager.PREFIX)) {
            return;
        }
		
		UUID uid = player.getUniqueId();
		if (locked.contains(uid)) return;

		// pick correct slot
		ItemStack stack = (hand == EquipmentSlot.HAND)
				? player.getInventory().getItemInMainHand()
						: player.getInventory().getItemInOffHand();
		if (stack.getType() != Material.ENDER_EYE) return;

		// consume + lock
		e.setCancelled(true);
		stack.setAmount(stack.getAmount() - 1);
		if (hand == EquipmentSlot.HAND) player.getInventory().setItemInMainHand(stack);
		else player.getInventory().setItemInOffHand(stack);
		locked.add(uid);

		// compute launch position (in front of hand)
		Location eyeLoc = player.getEyeLocation();
		Vector dir = eyeLoc.getDirection().normalize();
		Location launchPos = eyeLoc.clone().add(dir.clone().multiply(0.5)).subtract(0, 0.5, 0);
		Vector startVec = launchPos.toVector();

		// play launch sound
		player.getWorld().playSound(launchPos, Sound.ENTITY_ENDER_EYE_LAUNCH, 1f, 1f);

		// spawn the thrown eye entity once
		Item thrown = player.getWorld()
				.dropItem(launchPos, new ItemStack(Material.ENDER_EYE));
		thrown.setGravity(false);
		thrown.setPickupDelay(Integer.MAX_VALUE);
		// make invulnerable so lava/fire can't destroy it
		thrown.setInvulnerable(true);
		thrown.setFireTicks(0);

		// particle palettes
		DustOptions trailL = new DustOptions(Color.fromRGB(180,255,200), 1f);
		DustOptions trailD = new DustOptions(Color.fromRGB( 80,150, 90), 0.8f);
		DustOptions spinD  = new DustOptions(Color.fromRGB(100,255,150), 1.2f);
		DustOptions popL   = new DustOptions(Color.fromRGB(200,160,255), 1.5f);
		DustOptions popD   = new DustOptions(Color.fromRGB(100,  40,200), 1.2f);

		// lookup nearest portal
		Location nearest = plugin.getPortalManager().getNearest(player.getLocation());
		if (nearest == null) {
			// no portal → immediate explosion at launchPos
			player.getWorld().playSound(launchPos, Sound.ENTITY_ENDER_EYE_DEATH, 1f, 1f);
			player.getWorld().spawnParticle(Particle.EXPLOSION, launchPos, 1);
			player.getWorld().spawnParticle(Particle.DUST, launchPos, 8, 0,0,0, popL);
			player.getWorld().spawnParticle(Particle.DUST, launchPos, 8, 0,0,0, popD);
			player.getWorld().dropItem(launchPos, new ItemStack(Material.ENDER_EYE));
			thrown.remove();
			locked.remove(uid);
			return;
		}

		// center the portal block: +0.5 x/z, +1 y, +0.5 for height
		Location target = nearest.clone().add(0.5, 1.5, 0.5);

		// cap travel at 16 blocks
		Vector toTarget = target.toVector().subtract(startVec);
		if (toTarget.length() > 16) {
			toTarget = toTarget.normalize().multiply(16);
		}
		Location landing = launchPos.clone().add(toTarget);
		Vector endVec = landing.toVector();

		// Bezier control point (midpoint + up)
		Vector mid = startVec.clone().add(endVec).multiply(0.5);
		Vector ctrl = mid.clone().add(new Vector(0, 8, 0));

		// Phase 1: arc-flight (~6.5s)
		new BukkitRunnable() {
			final int TOTAL = 130;
			int tick = 0;
			Vector prev = startVec.clone();

			@Override
			public void run() {
				if (++tick > TOTAL) {
					cancel();
					startHover(thrown, landing, player, spinD, popL, popD, uid);
					return;
				}
				double t = tick / (double) TOTAL;
				double s = t*t*(3 - 2*t); // smoothstep

				Vector a = startVec.clone().multiply((1-s)*(1-s));
				Vector b = ctrl.clone().multiply(2*(1-s)*s);
				Vector c = endVec.clone().multiply(s*s);
				Vector pos = a.add(b).add(c);
				Location loc = pos.toLocation(player.getWorld());

				// reset fire each tick just in case
				thrown.setFireTicks(0);

				thrown.setVelocity(pos.clone().subtract(prev));
				prev = pos;

				if (tick % 5 == 0) {
					player.getWorld().spawnParticle(Particle.DUST, loc, 1, 0,0,0, trailL);
					player.getWorld().spawnParticle(Particle.DUST, loc, 1, 0,0,0, trailD);
					double ang = tick * 0.3;
					for (int i = 0; i < 2; i++) {
						double phi = ang + i * Math.PI;
						double dx = 0.2*Math.cos(phi), dz = 0.2*Math.sin(phi);
						player.getWorld().spawnParticle(Particle.SMALL_FLAME,
								loc.clone().add(dx, 0, dz), 1, 0,0,0, 0);
					}
				}
			}
		}.runTaskTimer(plugin, 1, 1);
	}

	/**
	 * Phase 2: hover & mirrored spin (5 s), then pop & return.
	 */
	private void startHover(Item eyeEntity,
			Location base,
			Player player,
			DustOptions spinDust,
			DustOptions popLight,
			DustOptions popDark,
			UUID uid) {
		new BukkitRunnable() {
			final int DURATION = 100;
			final double BOB_FREQ = Math.PI / 10, BOB_AMP = 0.25;
			final double SP0 = 0.15, SP1 = 0.9;
			int ht = 0;
			double angle = 0;

			@Override
			public void run() {
				if (++ht > DURATION) {
					// final pop
					player.getWorld().playSound(base, Sound.ENTITY_ENDER_EYE_DEATH, 1f, 1f);
					player.getWorld().spawnParticle(Particle.EXPLOSION, base, 1);
					player.getWorld().spawnParticle(Particle.DUST, base, 8, 0,0,0, popLight);
					player.getWorld().spawnParticle(Particle.DUST, base, 8, 0,0,0, popDark);
					player.getWorld().dropItem(base, new ItemStack(Material.ENDER_EYE));
					eyeEntity.remove();
					locked.remove(uid);
					cancel();
					return;
				}

				// steady bob
				double dy = Math.sin(ht * BOB_FREQ) * BOB_AMP;
				Location loc = base.clone().add(0, dy, 0);
				eyeEntity.teleport(loc);

				// mirrored spin particles every 10 ticks
				if (ht % 10 == 0) {
					double progress = ht / (double) DURATION;
					angle += SP0 + (SP1 - SP0) * progress;
					double dx = 0.4 * Math.cos(angle), dz = 0.4 * Math.sin(angle);

					// side A
					player.getWorld().spawnParticle(Particle.DUST,
							loc.clone().add(dx, 0, dz), 1, 0,0,0, spinDust);
					player.getWorld().spawnParticle(Particle.SMALL_FLAME,
							loc.clone().add(dx, 0, dz), 1, 0,0,0, 0);

					// mirrored side B
					player.getWorld().spawnParticle(Particle.DUST,
							loc.clone().add(-dx, 0, -dz), 1, 0,0,0, spinDust);
					player.getWorld().spawnParticle(Particle.SMALL_FLAME,
							loc.clone().add(-dx, 0, -dz), 1, 0,0,0, 0);
				}
			}
		}.runTaskTimer(plugin, 1, 1);
	}
}
