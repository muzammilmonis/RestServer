# RestServer 1.21.11 — Core Alpha

RestServer is a source-derived Minecraft server fork. This repository keeps the upstream
Paper/Leaf compatibility namespaces required by existing plugins, while the built server
identifies itself as RestServer and includes RestServer-owned core bootstrap behavior.

## Implemented in this alpha

- `RestServer.jar` source-build workflow (no nested Paper/Leaf runtime jar wrapper).
- Minecraft engine fixed to the real 1.21.11 engine in this build.
- `server.properties` gets `version=1.21.11` if missing.
- `version=` is authoritative: any other value stops before Minecraft bootstrap/world loading.
- RestServer build manifest/brand, console app name, `/version` provider, watchdog/crash wording.
- Startup terminal clear when an interactive ANSI-capable terminal is detected.
- Conservative clean-console filter that never hides WARN/ERROR/FATAL events.
- Paper/Spigot plugin compatibility and Leaf compatibility packages are intentionally retained.

## Build on Windows

Open PowerShell in the repository and run:

```powershell
Set-ExecutionPolicy -Scope Process Bypass
.\build-restserver.ps1
```

The final file is copied to the repository root as `RestServer.jar`.

## Run

```text
java -Xms4G -Xmx4G -jar RestServer.jar nogui
```

## Engine-selection rule

This alpha contains one real engine: Minecraft 1.21.11. Therefore:

```properties
version=1.21.11
```

boots normally, while an unsupported value such as `version=1.21.10` exits before worlds load.
Future multi-version support must add a real matching engine module; RestServer does not spoof
protocol/world/server versions.

## Upstream attribution

RestServer is source-derived from open-source upstream projects including Leaf and Paper.
Their applicable licenses, notices, patches, compatibility packages, and source attribution
remain in the repository/distribution as required. Runtime branding is a separate concern from
license attribution.
