package redux.anticheat.check.packets.movement;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;

import redux.anticheat.Main;
import redux.anticheat.check.Category;
import redux.anticheat.check.PacketCheck;
import redux.anticheat.player.PlayerData;

public class FastClimb extends PacketCheck {

	public FastClimb() {
		super("FastClimb", 10, null, false, true, Category.MOVEMENT,
				new PacketType[] { PacketType.Play.Client.POSITION }, true, 70);
		setDescription("Checks if a player is climbing on a vine or ladder faster than normal.");
	}

	@Override
	public void listen(PacketEvent e) {
		final Player p = e.getPlayer();
		final PlayerData pd = Main.getInstance().getPlayerManager().getPlayer(p.getUniqueId());

		if (pd.flyTicks > 0 || p.isFlying() || pd.vehicleTicks > 0) {
			return;
		}

		if (Main.getInstance().getLocUtils().canClimb(pd.getLastLocation())
				&& Main.getInstance().getLocUtils().canClimb(pd.getNextLocation())) {
			if (pd.isRising) {
				pd.ticksOnClimbable++;
				if (pd.ticksOnClimbable > 7) {
					double limit = 0.28;

					limit += Math.abs(pd.getVelocity()) * 0.12;
					for (final PotionEffect pe : p.getActivePotionEffects()) {
						if (pe.getType().equals(PotionEffectType.JUMP)) {
							limit *= 1 + (pe.getAmplifier() * 0.2);
						}
					}
					if (pd.getDeltaY() > limit) {
						flag(pd, pd.getDeltaY() + " > " + limit);
					}
				}
			} else {
				pd.ticksOnClimbable = 0;
			}
		} else {
			pd.ticksOnClimbable = 0;
		}
	}

}
