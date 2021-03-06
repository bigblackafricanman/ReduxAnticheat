package redux.anticheat.check.packets.movement;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;

import redux.anticheat.Main;
import redux.anticheat.check.Category;
import redux.anticheat.check.PacketCheck;
import redux.anticheat.player.PlayerData;
import redux.anticheat.utils.ReflectionUtils;

public class Glide extends PacketCheck {

	public Glide() {
		super("Glide", 10, null, false, true, Category.MOVEMENT,
				new PacketType[] { PacketType.Play.Client.POSITION }, true, 65);
	}

	@Override
	public void listen(PacketEvent e) {
		final Player p = e.getPlayer();
		final PlayerData pd = Main.getInstance().getPlayerManager().getPlayer(p.getUniqueId());

		if (pd.teleportTicks > 0 || pd.vehicleTicks > 0 || pd.flyTicks > 0 || p.isFlying() || pd.velocTicks > 0) {
			return;
		}

		if (locUtils.canClimb(pd.getLastLocation())
				|| locUtils.canClimb(pd.getNextLocation()) || Main.getInstance().getLocUtils()
						.isCollidedWithWeirdBlock(pd.getLastLocation(), pd.getNextLocation())) {
			return;
		}

		if (pd.fallingTicks > 0 && pd.isFalling && !pd.wasFalling && !pd.isRising && !ReflectionUtils.getOnGround(p)
				&& !locUtils.isOnSolidGround(pd.getLastLocation())
				&& !locUtils.isOnSolidGround(pd.getNextLocation()) && p.getFallDistance() > 1) {
			double expected = (-0.0078);
			expected = (pd.fallingTicks > 3 ? -((expected * 1 + (pd.fallingTicks * 0.21)) / 10) : expected);

			if (expected < -1) {
				expected = -1;
			}

			expected -= (pd.offGroundTicks * 0.00078);
			expected -= (Math.abs(pd.getVelocity()) * 0.1);

			if (!near(pd.getDeltaY(), expected) && pd.getDeltaY() > expected && isValid(pd.getDeltaY())) {
				flag(pd, pd.getDeltaY() + " > " + expected + " | fallTicks: " + pd.fallingTicks + ", onGround: "
						+ ReflectionUtils.getOnGround(p) + " & "
						+ locUtils.isOnSolidGround(pd.getNextLocation()) + ", veloc: "
						+ pd.getVelocity());
				pd.fallingTicks = 0;
			}
			
			//p.sendMessage("speed: " + pd.getDeltaY() + "/" + expected);

		}
	}

	private boolean near(double deltaY, double predicted) {
		return Math.abs(deltaY - predicted) < 0.005;
	}

	private boolean isValid(double d) {
		if (near(d, -0.07840000152587834)) {
			return false;
		}

		return true;
	}

}
