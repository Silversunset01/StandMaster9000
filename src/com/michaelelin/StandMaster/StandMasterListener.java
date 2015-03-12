package com.michaelelin.StandMaster;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.PlayerInteractEvent;

import com.michaelelin.StandMaster.StandMasterPlugin.Placement;
import com.michaelelin.StandMaster.data.DataModifier;
import com.michaelelin.StandMaster.data.StandMasterData;

public class StandMasterListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK
                && event.getMaterial() == Material.ARMOR_STAND) {
            StandMasterPlugin.getInstance().placements.add(new Placement(
                    event.getClickedBlock().getRelative(event.getBlockFace())
                            .getLocation().add(0.5, 0, 0.5),
                    event.getPlayer(),
                    event.getClickedBlock().getWorld().getFullTime()));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getEntityType() == EntityType.ARMOR_STAND
                && event.getSpawnReason() == SpawnReason.DEFAULT) {
            List<Placement> placements = StandMasterPlugin.getInstance().placements;
            for (int i = placements.size() - 1; i >= 0; i--) {
                Placement p = placements.get(i);
                Location loc = event.getLocation();
                if (p.location.getWorld() == loc.getWorld()
                        && p.location.getBlockX() == loc.getBlockX()
                        && p.location.getBlockY() == loc.getBlockY()
                        && p.location.getBlockZ() == loc.getBlockZ()
                        && p.time == loc.getWorld().getFullTime()) {
                    List<DataModifier<? extends Entity, ? extends StandMasterData>.Executable> mods =
                            StandMasterPlugin.getInstance().getModifierList(p.player);
                    for (DataModifier<? extends Entity, ? extends StandMasterData>.Executable mod
                            : mods) {
                        mod.modify(event.getEntity());
                    }
                    placements.remove(i);
                    mods.clear();
                } else if (p.time < event.getLocation().getWorld().getFullTime()) {
                    placements.remove(i);
                }
            }
        }
    }

}