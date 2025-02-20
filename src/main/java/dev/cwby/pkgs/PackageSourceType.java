package dev.cwby.pkgs;

public enum PackageSourceType {

    GENERIC("GENERIC"),
    NPM("NPM"),
    DEB("DEB"),
    RPM("RPM"),
    COMMAND("COMMAND");

    private final String type;

    PackageSourceType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

}

