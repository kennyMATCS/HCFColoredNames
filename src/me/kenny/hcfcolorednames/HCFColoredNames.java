package me.kenny.hcfcolorednames;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;
import org.zencode.mango.Mango;

import me.qiooip.notorious.Notorious;
import net.md_5.bungee.api.ChatColor;

public class HCFColoredNames extends JavaPlugin implements Listener {
	private Notorious notorious;
	private Mango mango;
	private Map<Player, Integer> playerIds = new HashMap<Player, Integer>();
	private String ENEMY_COLOR, MEMBER_COLOR, ARCHER_TAG_COLOR, ALLY_COLOR;
	
	@Override
	public void onEnable() {
		this.notorious = (Notorious) this.getServer().getPluginManager().getPlugin("Notorious");
		this.mango = (Mango) this.getServer().getPluginManager().getPlugin("Mango");
		
		this.getServer().getPluginManager().registerEvents(this, this);
		
		File file = new File(getDataFolder(), "config.yml");
		if (!file.exists()) {
			this.getConfig().options().copyDefaults(true);
			this.saveConfig();
		}
		
		this.ENEMY_COLOR = this.getConfig().getString("enemy-color");
		this.ALLY_COLOR = this.getConfig().getString("ally-color");
		this.MEMBER_COLOR = this.getConfig().getString("member-color");
		this.ARCHER_TAG_COLOR = this.getConfig().getString("archer-tag-color");
		
		update();
	}
	
	@Override
	public void onDisable() {
		
	}
	
	@SuppressWarnings("deprecation")
	public void update() {
		Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, new BukkitRunnable() {
			@Override
			public void run() {
				for (int i = 0; i < Bukkit.getOnlinePlayers().length; i++) {
					Player player = Bukkit.getOnlinePlayers()[i];
					int playerId = getPlayerID(player);
					
					Team members = notorious.getScoreboardDataHandler().getScoreboardFor(player).getOrCreateTeam("members", playerId);
					
					members.setPrefix(ChatColor.translateAlternateColorCodes('&', MEMBER_COLOR));
						
					Team enemies = notorious.getScoreboardDataHandler().getScoreboardFor(player).getOrCreateTeam("enemies", playerId);
					enemies.setPrefix(ChatColor.translateAlternateColorCodes('&', ENEMY_COLOR));
						
					Team allies = notorious.getScoreboardDataHandler().getScoreboardFor(player).getOrCreateTeam("allies", playerId);
					allies.setPrefix(ChatColor.translateAlternateColorCodes('&', ALLY_COLOR));
						
					Team archerTag = notorious.getScoreboardDataHandler().getScoreboardFor(player).getOrCreateTeam("archerTag", playerId);
					archerTag.setPrefix(ChatColor.translateAlternateColorCodes('&', ARCHER_TAG_COLOR));
					
					for (Player teamPlayer : Bukkit.getOnlinePlayers()) {
						if (mango.getFactionManager().getFaction(teamPlayer) != null) {
							if (notorious.getArcherTagTimeHandler().isActive(teamPlayer)) {
								remove(teamPlayer, archerTag);
								archerTag.addPlayer(teamPlayer);
								continue;
							} else if (mango.getFactionManager().getFaction(teamPlayer) == mango.getFactionManager().getFaction(player)) {
								remove(teamPlayer, members);
								members.addPlayer(teamPlayer);
								continue;
							} else if (mango.getFactionManager().getFaction(teamPlayer).getAllies().contains(mango.getFactionManager().getFaction(player))) {
								remove(teamPlayer, allies);
								allies.addPlayer(teamPlayer);
								continue;
							} 
						} 

						remove(teamPlayer, enemies);
						enemies.addPlayer(teamPlayer);
					}
				}
			}	
		}, 2L, 2L);
	}

	protected int getPlayerID(Player player) {
		if (!playerIds.containsKey(player)) {
			if (playerIds.isEmpty()) {
				playerIds.put(player, 1);
				return playerIds.get(player);
			}
			
			int max = max(playerIds);
			// adds a player id equal to the current highest value plus one
			playerIds.put(player, max + 1);
		}
		
		return playerIds.get(player);
	}
	
	// gets the max value of a map
	protected <K, V extends Comparable<V>> V max(Map<K, V> map) {
		Map.Entry<K, V> maxEntry = null;
		for (Map.Entry<K, V> entry : map.entrySet()) {
			if (maxEntry == null || entry.getValue()
				.compareTo(maxEntry.getValue()) > 0) {
					maxEntry = entry;
				}
		}
		return maxEntry.getValue();
	}
	
	protected void remove(Player player, Team team) {
		if (team.getPlayers().contains((OfflinePlayer) player)) {
			team.removePlayer(player);
		}
	}
}
