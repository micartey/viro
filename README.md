# viro

<div align="center">
  <a href="https://www.oracle.com/java/">
    <img
      src="https://img.shields.io/badge/Written%20in-java-%23EF4041?style=for-the-badge"
      height="30"
    />
  </a>
  <a href="https://clientastisch.github.io/viro/docs" target="_blank">
    <img
      src="https://img.shields.io/badge/javadoc-reference-5272B4.svg?style=for-the-badge"
      height="30"
    />
  </a>
</div>

## :books: Introduction

Viro is a Java doodle application. It is based on the [Spring](https://spring.io/) framework and uses JavaFX for rendering.

![](images/preview.gif)

---

## :mag_right: Compatibility

- [x] [Windows](https://www.microsoft.com/)
- [x] [Java 8](https://github.com/AdoptOpenJDK/openjdk-jdk8u)

## :ballot_box: Installation

To use viro, you must have Java installed or use the bundled zip available on the release tab.

In case you downloaded the source and want to compile it, make sure to run

```js
mvn package -DskipTests=true -f pom.xml
```

## :gear: Settings

Viro uses [PreferenceFx](https://github.com/dlsc-software-consulting-gmbh/PreferencesFX) to store, display and load settings. You Also have the possibility to set your own keybindings.

![](images/settings.png)

## :safety_pin: Default Settings

### :heavy_check_mark: Show

```
ALT + PAUSE
```

### :x: Hide

```
ESC
```