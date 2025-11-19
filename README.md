# viro

<div align="center">
  <a href="https://www.oracle.com/java/" target="_blank">
    <img
      src="https://img.shields.io/badge/Written%20in-java-%23EF4041?style=for-the-badge"
      height="30"
    />
  </a>
  <a href="https://github.com/micartey/viro/actions/workflows/maven-build-and-release.yml" target="_blank">
    <img
      src="https://img.shields.io/badge/actions-build-%27a147?style=for-the-badge"
      height="30"
    />
  </a>
</div>

<br />

<p align="center">
  <a href="#-introduction">Introduction</a> •
  <a href="#-getting-started">Getting Started</a> •
  <a href="https://github.com/micartey/viro/issues">Troubleshooting</a>
</p>

## 📚 Introduction

viro is a java overlay doodle application meant to quickly draw and highlight things in screen sharings or recordings.
It is implemented in JavaFx and builds on the Spring Boot framework.

![](images/preview.png)

### Architecture

Due to scalability and ease of development, an event-based architecture is being used.
Most parts are covered by the build-in event system of Spring Boot, some other parts are covered
by [jation](https://github.com/micartey/jation).
jation is being used to only execute events for the current selected brush.
It provides a possibility to add a filter before invoking a method, which checks if the brush is the selected brush.
The code can be
found [here](https://github.com/micartey/viro/blob/1a8fa0810a2b03d8fedd1727def66c3e9a417cc9/src/main/java/me/micartey/viro/input/MouseObserver.java#L49-L62).

### Development

*(Writing this for future me in order to not spend an entire evening to get this up and running)*

```bash
nix develop
```

You can also enable javaFx globally.
This also provides a runtime that can actually execute the artifact.

```nix
programs.java = {
    enable = true;
    package = (pkgs.jdk17.override { enableJavaFX = true; });
};
```

## Getting Started

Go to the [releases](https://github.com/micartey/viro/releases) and download the newest version of viro.
Or use nix (recommended):

```bash
nix run github:micartey/viro
```

### Requirements

- Java 17+
- Depending on your system: bundled javafx (e.g. on Nix)

### Hyprland

For Hyprland I recommend to set the following window rules for a proper experience.

```nix
wayland.windowManager.hyprland.settings.windowrulev2 = [
    "float,class:(.*)viro(.*)$"
    "bordersize 0, class:(.*)viro(.*)$"
    "noblur, title:^(Radial-Menu)$"
    "noshadow, title:^(Radial-Menu)$"
];
```

### MCP

viro also has an `SSE`-MCP-Endpoint for Large-Language-Modules (LLMs) to interface and draw shapes on their own.
For gemini-cli, you'll need to add the following:

```json
{
    "mcpServers": {
        "viro": {
            "url": "http://localhost:8099/mcp/sse"
        }
    }
}
```

*(Please keep in mind that the syntax for this depends on the tool you use)*

#### TODO:

- [ ] Return shape id on `drawShape`
- [ ] Offer tool to erase based on id
- [ ] Offer tool to import local images

### Shortcuts

There are some shortcuts you can use.
They are currently not configurable, but will be in the feature.
At the moment, the following shortcuts are pre-defined and static:

<br />

| Shortcut            | Name         | Description                                  |
|---------------------|--------------|----------------------------------------------|
| CTRL + Z            | Undo         | Remove shape                                 |
| CTRL + Y            | Redo         | Recreate removed shape                       |
| CTRL + SHIFT + I    | Import Image | Import a png / jpg into the scene            |
| CTRL + SHIFT + ENTF | Clear        | Remove all shapes and reset background color |
