<div align="center">

# SanityJS

KubeJS integration for Sanity: Descent Into Madness on Minecraft 1.20.1 Forge.
</div>

`SanityJS` exposes Sanity's runtime hooks to KubeJS and adds source-item, equipped-source, and indicator APIs so packs can script sanity behavior without maintaining a separate compatibility mod.

## What It Covers

- Server-side sanity change, tick, trigger, item-source, and equipped-source events
- Startup item registries for `sanity_source`, `sanity_helmet`, `sanity_chestplate`, `sanity_leggings`, and `sanity_boots`
- Helper registries for attaching sanity sources to existing vanilla or modded items
- Client-side sanity indicator render hooks
- Script bindings for `SanityHelper`, `SanityEventType`, `SanityConfig`, `SanityProcessor`, and source definition/builders

## Supported Stack

- Minecraft `1.20.1`
- Forge `47.4.10`
- KubeJS `2001.6.5-build.16`
- Rhino `2001.2.3-build.10`
- Architectury `9.2.14`
- Optional Curios `5.3.1+1.20.1`
- Optional ProbeJS runtime/docs support

## Documentation

The authored wiki pages live in [`docs`](./docs):

- [`docs/overview.mdx`](./docs/overview.mdx)
- [`docs/startupevents/registry.mdx`](./docs/startupevents/registry.mdx)
- [`docs/serverevents/normal.mdx`](./docs/serverevents/normal.mdx)
- [`docs/serverevents/sources.mdx`](./docs/serverevents/sources.mdx)
- [`docs/clientevents/indicator.mdx`](./docs/clientevents/indicator.mdx)

## Development Notes

- Public JavaScript-facing sanity values use the `0-100` scale. Raw `0-1` helpers still exist for advanced integrations and internal bridging.
- Registered item and equipped sources only consume cooldowns or durability when they actually move sanity.
- Equipped sources resolve vanilla armor and hand slots plus optional Curios slot ids.
- Source definitions can come from startup item builders or `SanityHelper.registerItemSource` and `SanityHelper.registerEquippedSource`.
