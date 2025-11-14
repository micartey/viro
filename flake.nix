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

        javaFx = pkgs.jdk17.override { enableJavaFX = true; };

        # Use buildMavenPackage
        mkViro = pkgs.maven.buildMavenPackage rec {
          pname = "viro";
          version = "0.1.0";

          src = ./.;

          nativeBuildInputs = [ javaFx ];
          buildInputs = [ javaFx ];

          mvnHash = "sha256-doVILnKgOqpIIxjOutfTcbUOV+YdqwTyzHmTgH9Uu5c=";
          doCheck = false;

          mvnFetchExtraArgs = {
            hash = pkgs.lib.fakeHash;
          };

          JAVA_HOME = "${javaFx}";

          installPhase = ''
            mkdir -p $out/share/java
            cp target/*.jar $out/share/java/viro.jar

            mkdir -p $out/bin
            cat > $out/bin/viro <<EOF
            #!/bin/sh
            exec ${javaFx}/bin/java -jar $out/share/java/viro.jar
            EOF
            chmod +x $out/bin/viro
          '';

          meta.mainProgram = "viro";
        };

      in
      {
        # nix build .#viro
        packages = {
          default = mkViro;
          viro = mkViro;
        };

        # nix run .#viro
        apps = rec {
          default = {
            type = "app";
            program = "${mkViro}/bin/viro";
          };
          viro = default;
        };

        devShells.default = pkgs.mkShell {
          buildInputs = with pkgs; [
            maven
            javaFx
            lefthook
          ];

          JAVA_HOME = "${javaFx}";
        };
      }
    );
}
