package fun.restserver.version;

import com.destroystokyo.paper.util.VersionFetcher;
import io.papermc.paper.ServerBuildInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/** RestServer /version provider. No upstream promotional/update network request is made. */
public final class RestServerVersionFetcher implements VersionFetcher {
    private static final long CACHE_TIME = 60_000L;

    @Override
    public long getCacheTime() {
        return CACHE_TIME;
    }

    @Override
    public Component getVersionMessage() {
        final ServerBuildInfo info = ServerBuildInfo.buildInfo();
        return Component.text("RestServer ", NamedTextColor.AQUA)
            .append(Component.text(info.minecraftVersionId(), NamedTextColor.WHITE))
            .append(Component.text(" • engine " + System.getProperty("restserver.engine.version", info.minecraftVersionId()), NamedTextColor.GRAY));
    }
}
