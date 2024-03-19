# viro

<div align="center">
  <a href="https://www.oracle.com/java/" target="_blank">
    <img
      src="https://img.shields.io/badge/Written%20in-java-%23EF4041?style=for-the-badge"
      height="30"
    />
  </a>
  <a href="https://github.com/micartey/viro/actions/workflows/maven-publish.yml" target="_blank">
    <img
      src="https://img.shields.io/badge/actions-build-%27a147?style=for-the-badge"
      height="30"
    />
  </a>
</div>

<br />

<p align="center">
  <a href="#-introduction">Introduction</a> •
  <a href="#-installation">Installation</a> •
  <a href="https://github.com/micartey/viro/issues">Troubleshooting</a>
</p>

## 📚 Introduction

viro is a java overlay doodle application meant to quickly draw and highlight things in screen sharings or recordings.

![](images/preview.gif)

### Setup

1. Create a `.sdk` folder
2. Download a [javafx-sdk](https://gluonhq.com/products/javafx/) and extract it into the `.sdk` folder. 
The resulting structure should be: `.sdk/<your-fx-sdk>/lib`
3. Edit the Run configuration in your idea and add the following JVM flag
```
--module-path ./.sdk/<your-fx-sdk>/lib --add-modules javafx.controls,javafx.fxml,javafx.graphics
```