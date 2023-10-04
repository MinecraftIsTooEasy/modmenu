package com.terraformersmc.modmenu.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModrinthData;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Utils;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;

import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModrinthUtil {
	public static final Logger LOGGER = LogManager.getLogger("Mod Menu/Update Checker");

	private static final HttpClient client = HttpClientBuilder.create().build();

	private static boolean allowsUpdateChecks(Mod mod) {
		return mod.allowsUpdateChecks();
	}

	public static void checkForUpdates() {
		if (!ModMenuConfig.UPDATE_CHECKER.getValue()) {
			return;
		}

/*		CompletableFuture.runAsync(() -> {
			LOGGER.info("Checking mod updates...");

			Map<String, Set<Mod>> modHashes = new HashMap<>();
			new ArrayList<>(ModMenu.MODS.values()).stream().filter(ModrinthUtil::allowsUpdateChecks).forEach(mod -> {
				String modId = mod.getId();

				try {
					String hash = mod.getSha512Hash();

					if (hash != null) {
						LOGGER.debug("Hash for {} is {}", modId, hash);
						modHashes.putIfAbsent(hash, new HashSet<>());
						modHashes.get(hash).add(mod);
					}
				} catch (IOException e) {
					LOGGER.error("Error getting mod hash for mod {}: ", modId, e);
				}
			});

			String environment = ModMenu.devEnvironment ? "/development": "";
			String primaryLoader = ModMenu.runningQuilt ? "quilt" : "fabric";
			List<String> loaders = ModMenu.runningQuilt ? Arrays.asList("fabric", "quilt") : Arrays.asList("fabric");

			String mcVer = SharedConstants.getGameVersion().getName();
			String[] splitVersion = FabricLoader.getInstance().getModContainer(ModMenu.MOD_ID)
					.get().getMetadata().getVersion().getFriendlyString().split("\\+", 1); // Strip build metadata for privacy
			final String modMenuVersion = splitVersion.length > 1 ? splitVersion[1] : splitVersion[0];
			final String userAgent = "%s/%s (%s/%s%s)".formatted(ModMenu.GITHUB_REF, modMenuVersion, mcVer, primaryLoader, environment);
			String body = ModMenu.GSON_MINIFIED.toJson(new LatestVersionsFromHashesBody(modHashes.keySet(), loaders, mcVer));
			LOGGER.debug("User agent: " + userAgent);
			LOGGER.debug("Body: " + body);
			var latestVersionsRequest = HttpRequest.newBuilder()
					.POST(HttpRequest.BodyPublishers.ofString(body))
					.header("User-Agent", userAgent)
					.header("Content-Type", "application/json")
					.uri(URI.create("https://api.modrinth.com/v2/version_files/update"))
					.build();

			try {
				var latestVersionsResponse = client.send(latestVersionsRequest, HttpResponse.BodyHandlers.ofString());

				int status = latestVersionsResponse.statusCode();
				LOGGER.debug("Status: " + status);
				if (status == 410) {
					apiV2Deprecated = true;
					LOGGER.warn("Modrinth API v2 is deprecated, unable to check for mod updates.");
				} else if (status == 200) {
					JsonObject responseObject = JsonParser.parseString(latestVersionsResponse.body()).getAsJsonObject();
					LOGGER.debug(String.valueOf(responseObject));
					responseObject.asMap().forEach((lookupHash, versionJson) -> {
						var versionObj = versionJson.getAsJsonObject();
						var projectId = versionObj.get("project_id").getAsString();
						var versionNumber = versionObj.get("version_number").getAsString();
						var versionId = versionObj.get("id").getAsString();
						var primaryFile = versionObj.get("files").getAsJsonArray().asList().stream()
								.filter(file -> file.getAsJsonObject().get("primary").getAsBoolean()).findFirst();

						if (primaryFile.isEmpty()) {
							return;
						}

						var versionHash = primaryFile.get().getAsJsonObject().get("hashes").getAsJsonObject().get("sha512").getAsString();

						if (!Objects.equals(versionHash, lookupHash)) {
							// hashes different, there's an update.
							modHashes.get(lookupHash).forEach(mod -> {
								LOGGER.info("Update available for '{}@{}', (-> {})", mod.getId(), mod.getVersion(), versionNumber);
								mod.setModrinthData(new ModrinthData(projectId, versionId, versionNumber));
							});
						}
					});
				}
			} catch (IOException | InterruptedException e) {
				LOGGER.error("Error checking for updates: ", e);
			}
		});*/
	}

	public static class LatestVersionsFromHashesBody {
		public Collection<String> hashes;
		public String algorithm = "sha512";
		public Collection<String> loaders;
		@SerializedName("game_versions")
		public Collection<String> gameVersions;

		public LatestVersionsFromHashesBody(Collection<String> hashes, Collection<String> loaders, String mcVersion) {
			this.hashes = hashes;
			this.loaders = loaders;
			this.gameVersions = new HashSet<>();
			this.gameVersions.add(mcVersion);
		}
	}
}
