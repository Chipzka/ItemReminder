package com.itemReminder;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.config.ConfigManager;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.Arrays;
import java.util.List;

@Slf4j
@PluginDescriptor(
        name = "Item Reminder"
)
public class ExamplePlugin extends Plugin {
    //Check where the player is and if they have the item equipped
    private static final int[] REQUIRED_ITEM_ID = {1351, 22951}; // Replace with the ID of the required item (Boots of stone 23037 and Brimstone boots 22951)
    private static final List<WorldPoint> REQUIRED_AREA = Arrays.asList(
            new WorldPoint(3208, 3220, 2),
            new WorldPoint(3209, 3220, 2),
            new WorldPoint(3208, 3221, 2),
            new WorldPoint(3209, 3221, 2));
    @Inject
    private Client client;

    @Inject
    private EventBus eventBus;

    @Inject
    private ClientThread clientThread;

    @Inject
    private OverlayManager overlayManager;

    private boolean alertSent = false;

    @Inject
    private ExampleConfig config;

    @Override
    protected void startUp() throws Exception {
        log.info("Plugin started!");
    }
    @Override
    protected void shutDown() throws Exception {
        log.info("Plugin stopped!");
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        // Check if the player is in any of the required areas
        if (isPlayerInRequiredArea()) {
            // Check if any of the required items are equipped
            if (!isAnyItemEquipped(REQUIRED_ITEM_ID)) {
                // If alert hasn't been sent already, send the alert
                if (!alertSent) {
                    sendAlert("Item Missing!", "You need to equip one of the required items for this area.");
                    alertSent = true; // Set alert flag to true so it doesn't trigger again
                }
            } else {
                // Reset alert state if the required item is equipped
                if (alertSent) {
                    alertSent = false; // Reset the alert state when the player equips the item
                }
            }
        } else {
            // Reset the alert state if the player leaves the required area
            if (alertSent) {
                alertSent = false; // Reset alert state when the player leaves the required area
            }
        }
    }

    // Check if the player is in any of the required areas
    private boolean isPlayerInRequiredArea() {
        WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
        // Check if the player's location is one of the required areas
        for (WorldPoint area : REQUIRED_AREA) {
            if (playerLocation.equals(area)) {
                return true; // Player is in a required area
            }
        }
        return false; // Player is not in any of the required areas
    }

    // Check if the required item is equipped
    private boolean isAnyItemEquipped(int[] itemIds) {
        ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
        if (equipment == null) {
            return false;
        }

        // Iterate through the equipment items and check if any of the item IDs match
        for (Item item : equipment.getItems()) {
            if (item != null) {
                for (int itemId : itemIds) {
                    if (item.getId() == itemId) {
                        return true; // Return true if any of the required items are equipped
                    }
                }
            }
        }
        return false; // Return false if none of the required items are equipped
    }

    // Function to send an alert (chat message, sound, etc.)
    private void sendAlert(String title, String message) {
        // Display a message in the game chat
        client.addChatMessage(net.runelite.api.ChatMessageType.GAMEMESSAGE, "", title + ": " + message, null);
        // Optionally, play a sound when the item is missing
        // client.playSoundEffect(SoundEffectID.ALERT);
    }


    @Provides
    ExampleConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ExampleConfig.class);
    }
}
