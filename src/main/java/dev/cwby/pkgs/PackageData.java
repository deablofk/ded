package dev.cwby.pkgs;

import java.util.List;

public class PackageData {

    public String name;
    public String description;
    public String homepage;
    public List<String> licenses;
    public List<String> languages;
    public List<PackageCategory> categories;
    public Source source;
    public boolean isInstalled;
    public Trigger trigger;

    public static class Source {
        public PackageSourceType type;
        public String packageOrUrl;
        public String executable;
    }

    public static class Trigger {
        public List<String> filetypes;
        public List<String> projectRoot;
    }
}