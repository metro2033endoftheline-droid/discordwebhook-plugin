package com.discordlogs.webhook;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DiscordWebhookPlugin extends JavaPlugin {

    // ============================================================
    //  Mets ton URL de webhook Discord ici (entre les guillemets)
    // ============================================================
    private static final String WEBHOOK_URL =
            "https://discord.com/api/webhooks/1521915130395496552/Fd1KkNBWoW8TWUbWXnqsJX-UG6cKTMyegLzUQxiWBNXJNWtkCNBMvciQl5P16MtWCXJY";

    @Override
    public void onEnable() {
        getLogger().info("DiscordWebhookPlugin activé, pret a envoyer des messages sur Discord !");
    }

    @Override
    public void onDisable() {
        getLogger().info("DiscordWebhookPlugin desactive.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("dwebhook")) {
            return false;
        }

        if (args.length == 0) {
            sender.sendMessage("Usage: /dwebhook <message>");
            return true;
        }

        // Recolle tous les arguments en un seul message
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(" ");
            sb.append(args[i]);
        }
        final String message = sb.toString();

        // Envoi en asynchrone pour ne jamais lag le serveur
        getServer().getScheduler().runTaskAsynchronously(this, () -> sendToDiscord(message));

        return true;
    }

    private void sendToDiscord(String message) {
        try {
            String jsonEscaped = message
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "");

            String json = "{\"content\": \"" + jsonEscaped + "\"}";

            URL url = new URL(WEBHOOK_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            try (OutputStream out = conn.getOutputStream()) {
                out.write(json.getBytes(StandardCharsets.UTF_8));
                out.flush();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode >= 300) {
                getLogger().warning("Discord webhook a repondu avec le code " + responseCode);
            }

            conn.disconnect();
        } catch (Exception e) {
            getLogger().warning("Erreur lors de l'envoi du webhook Discord : " + e.getMessage());
        }
    }
}
