package dev.cwby.pkgs;

import java.util.List;

public class PackageData {

    public String name;
    public String description;
    public String homepage;
    public List<String> licenses;
    public List<String> languages;
    public List<String> categories;
    public Source source;
    public boolean isInstalled;

    public static class Source {
        public String type;
        public String packageOrUrl;
        public String executable;
    }
}