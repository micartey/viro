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
Most parts are covered by the build-in event system of Spring Boot, some other parts are covered by [jairo](https://github.com/micartey/jairo).
jairo is being used to only execute events for the current selected brush.
It provides a possibility to add a filter before invoking a method, which checks if the brush is the selected brush.
The code can be found [here](https://github.com/micartey/viro/blob/1a8fa0810a2b03d8fedd1727def66c3e9a417cc9/src/main/java/me/micartey/viro/input/MouseObserver.java#L49-L62).

![](images/event_bus.png)

<div align="center">
  <i>(Yes this image has been drawn with viro - I just suck at drawing with a mouse)</i>
</div>

The graphic above shows how the mouse (some mouse event) is adding sth to the event bus which gets picked up by the Brush.
If the mouse event is e.g. a release event, the Bursh will push the created shape to the event bus which will then gets picked up by the Canvis and get rendered.
I firmly believe that event-based architectures are superior and easier to maintain.
After 3. years I continued development and getting back into the project was easy due to the architecture my younger me (thankfully) created.

## Getting Started

<details>
<summary>Setup viro for development</summary>

1. Create a `.sdk` folder
2. Download a [javafx-sdk](https://gluonhq.com/products/javafx/) and extract it into the `.sdk` folder.
   The resulting structure should be: `.sdk/<your-fx-sdk>/lib`
3. Edit the Run configuration in your idea and add the following JVM flag
```
--module-path ./.sdk/<your-fx-sdk>/lib --add-modules javafx.controls,javafx.fxml,javafx.graphics
```

</details>

Go to the [releases](https://github.com/micartey/viro/releases) and download the newest version of viro. 
This can either be a commit or the latest stable version.
As both should work you can choose for yourself.

You also need to have a Java version newer or equal to Java 17.