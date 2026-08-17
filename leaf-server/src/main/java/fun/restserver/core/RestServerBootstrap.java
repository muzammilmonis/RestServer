package fun.restserver.core;

import fun.restserver.console.RestServerConsoleFilter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Properties;
import joptsimple.OptionSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

/**
 * RestServer bootstrap logic that must run before Minecraft bootstrap/world loading.
 *
 * <p>The version property is authoritative. This build embeds the 1.21.11 engine;
 * requesting any other engine version fails before Minecraft registries or worlds load.</p>
 */
public final class RestServerBootstrap {
    public static final String BRAND = "RestServer";
    public static final String ENGINE_VERSION = "1.21.11";
    public static final int PROTOCOL_VERSION = 774;
    public static final int WORLD_VERSION = 4671;

    private static final long BOOT_NANOS = System.nanoTime();

    private RestServerBootstrap() {
    }

    public static void earlyInit(final OptionSet options) {
        clearConsole();
        installConsoleFilter();

        final Path propertiesPath = resolvePropertiesPath(options);
        final String requestedVersion;
        try {
            requestedVersion = ensureVersionProperty(propertiesPath);
        } catch (final IOException exception) {
            fatal("Could not read/write " + propertiesPath + ": " + exception.getMessage());
            return;
        }

        if (!ENGINE_VERSION.equals(requestedVersion)) {
            fatal("server.properties requests Minecraft " + requestedVersion
                + ", but this RestServer build contains engine " + ENGINE_VERSION + ".\n"
                + "RestServer will not fake the version or open the world with a different engine."
            );
            return;
        }

        System.setProperty("restserver.engine.version", ENGINE_VERSION);
        System.setProperty("restserver.protocol.version", Integer.toString(PROTOCOL_VERSION));
        System.setProperty("restserver.world.version", Integer.toString(WORLD_VERSION));

        System.out.println("\u001B[38;5;45mRestServer\u001B[0m  \u001B[90m•\u001B[0m  Minecraft " + ENGINE_VERSION
            + "  \u001B[90m•\u001B[0m  Java " + Runtime.version().feature());
    }

    public static long bootNanos() {
        return BOOT_NANOS;
    }

    private static Path resolvePropertiesPath(final OptionSet options) {
        try {
            final Object configured = options.valueOf("config");
            if (configured instanceof File file) {
                return file.toPath();
            }
            if (configured instanceof Path path) {
                return path;
            }
            if (configured != null) {
                return Path.of(configured.toString());
            }
        } catch (final RuntimeException ignored) {
            // CraftBukkit always defines --config, but keep a safe vanilla fallback.
        }
        return Path.of("server.properties");
    }

    private static String ensureVersionProperty(final Path path) throws IOException {
        final Properties properties = new Properties();
        if (Files.isRegularFile(path)) {
            try (InputStream input = Files.newInputStream(path)) {
                properties.load(input);
            }
        }

        final String existing = properties.getProperty("version");
        if (existing != null && !existing.isBlank()) {
            return existing.trim();
        }

        final Path parent = path.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        // If server.properties already exists, append only our key so comments/order survive.
        if (Files.exists(path) && Files.size(path) > 0L) {
            final String prefix;
            final byte[] bytes = Files.readAllBytes(path);
            if (bytes.length > 0 && bytes[bytes.length - 1] != '\n' && bytes[bytes.length - 1] != '\r') {
                prefix = System.lineSeparator();
            } else {
                prefix = "";
            }
            Files.writeString(path, prefix + "version=" + ENGINE_VERSION + System.lineSeparator(),
                StandardOpenOption.APPEND);
        } else {
            properties.setProperty("version", ENGINE_VERSION);
            try (OutputStream output = Files.newOutputStream(path,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
                properties.store(output, "RestServer engine selection");
            }
        }

        return ENGINE_VERSION;
    }

    private static void installConsoleFilter() {
        final String mode = System.getProperty("restserver.console.mode", "clean").toLowerCase(Locale.ROOT);
        if (!"clean".equals(mode)) {
            return;
        }
        try {
            final LoggerContext context = (LoggerContext) LogManager.getContext(false);
            context.getConfiguration().addFilter(RestServerConsoleFilter.INSTANCE);
            context.updateLoggers();
        } catch (final RuntimeException ignored) {
            // Console filtering is cosmetic; never make server boot depend on it.
        }
    }

    private static void clearConsole() {
        if (Boolean.getBoolean("restserver.console.noClear")) {
            return;
        }

        final String term = System.getenv("TERM");
        final boolean interactive = System.console() != null
            || (term != null && !term.equalsIgnoreCase("dumb"))
            || System.getenv("WT_SESSION") != null
            || System.getenv("ANSICON") != null
            || System.getenv("PTERODACTYL_SERVER_UUID") != null;

        if (interactive) {
            System.out.print("\u001B[H\u001B[2J\u001B[3J");
            System.out.flush();
        }
    }

    private static void fatal(final String message) {
        System.err.println("[RestServer] FATAL: " + message);
        System.err.flush();
        System.exit(2);
    }
}
