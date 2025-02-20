package dev.cwby.pkgs;

public enum PackageCategory {
    LSP("LSP"),
    FORMATTER("FORMATTER");

    private final String category;

    PackageCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }
}
