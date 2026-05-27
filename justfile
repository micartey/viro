
update-deps:
    $(nix build .#viro.mitmCache.updateScript --print-out-paths)