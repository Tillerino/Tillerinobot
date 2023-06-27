{ pkgs ? (import <nixpkgs> {}).pkgsCross.x86_64-unknown-linux-musl }:
pkgs.pkgsStatic.callPackage ({ mkShell, jdk17, maven, rustc, cargo, just }: mkShell {
  buildInputs = [
    jdk17
    (maven.override { jdk = jdk17; })
    rustc
    cargo
    
    just
  ];
})
