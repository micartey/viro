{
  description = "Viro Java doodle application";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    utils.url = "github:numtide/flake-utils";
  };

  outputs =
    {
      nixpkgs,
      utils,
      ...
    }:
    utils.lib.eachDefaultSystem (
      system:
      let
        pkgs = import nixpkgs {
          inherit system;

          config = {
            allowUnfree = true;
            permittedInsecurePackages = [ "gradle-7.6.6" ];
          };
        };

        javaFxJdk = pkgs.jdk17.override { enableJavaFX = true; };

        mkViro = pkgs.stdenv.mkDerivation (finalAttrs: {
          pname = "viro";
          version = "1.0";

          src = ./.;

          nativeBuildInputs = [ javaFxJdk pkgs.gradle_8 ];
          buildInputs = [ javaFxJdk ];

          # Replaces the need for disabling the sandbox
          mitmCache = pkgs.gradle_8.fetchDeps {
            pkg = finalAttrs.finalPackage;
            data = ./deps.json;
          };

          gradleBuildTask = "bootJar";
          gradleFlags = [ "-x" "test" ];

          preBuild = ''
            export JAVA_HOME=${javaFxJdk}
            export JAVAFX_JMODS=${javaFxJdk}/lib/openjdk/jmods
          '';

          installPhase = ''
            mkdir -p $out/share/java
            cp build/libs/viro-1.0.jar $out/share/java/viro.jar

            mkdir -p $out/bin
            cat > $out/bin/viro <<EOF
            #!/bin/sh

            if command -v hyprctl > /dev/null; then
                hyprctl keyword windowrulev2 "float,class:(.*)viro(.*)$" > /dev/null 2>&1
                hyprctl keyword windowrulev2 "bordersize 0, class:(.*)viro(.*)$" > /dev/null 2>&1
                hyprctl keyword windowrulev2 "noblur, title:^(Radial-Menu)$" > /dev/null 2>&1
                hyprctl keyword windowrulev2 "noshadow, title:^(Radial-Menu)$" > /dev/null 2>&1
            fi

            exec ${javaFxJdk}/bin/java --module-path ${javaFxJdk}/lib/openjdk/jmods --add-modules javafx.controls,javafx.fxml -Xmx5G -jar $out/share/java/viro.jar
            EOF
            chmod +x $out/bin/viro
          '';

          meta.mainProgram = "viro";
        });

      in
      {
        packages = {
          default = mkViro;
          viro = mkViro;
        };

        apps = rec {
          default = {
            type = "app";
            program = "${mkViro}/bin/viro";
          };
          viro = default;
        };

        devShells.default = pkgs.mkShell {
          buildInputs = [ javaFxJdk pkgs.lefthook ];

          JAVA_HOME = "${javaFxJdk}";
          JAVAFX_JMODS = "${javaFxJdk}/lib/openjdk/jmods";
        };
      }
    );
}