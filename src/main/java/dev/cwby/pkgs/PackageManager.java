package dev.cwby.pkgs;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import dev.cwby.pkgs.sources.GenericSourceInstaller;
import dev.cwby.pkgs.sources.ISourceInstaller;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PackageManager {
    public static final String PACKAGES_DIR = "config/packages/";
    public static final String INTERNALS_DIR = "config/internals/";
    private static final Map<PackageSourceType, ISourceInstaller> sources = new HashMap<>();
    private static final Map<String, PackageData> packages = new HashMap<>();
    private static Set<String> installedPackages = readLocalRegistry();

    static {
        sources.put(PackageSourceType.GENERIC, new GenericSourceInstaller());
        PackageManager.initializeAvailablePackages();
    }

    private static PackageData parseFilePackage(File file) {
        var toml = new Toml().read(file);
        var packageData = toml.to(PackageData.class);
        packageData.source.type = PackageSourceType.valueOf(toml.getString("source.type").toUpperCase());
        List<String> categories = toml.getList("categories");
        packageData.categories = categories.stream().map(x -> PackageCategory.valueOf(x.toUpperCase())).toList();
        if (installedPackages.contains(packageData.name)) {
            packageData.isInstalled = true;
        }
        packages.put(packageData.name, packageData);
        return packageData;
    }

    public static void initializeAvailablePackages() {
        File[] files = new File(PACKAGES_DIR).listFiles();
        if (files == null) {
            return;
        }
        Arrays.stream(files).forEach(PackageManager::parseFilePackage);
    }

    public static Collection<PackageData> getPackages() {
        return packages.values();
    }

    public static List<PackageData> filterCategory(PackageCategory category) {
        return packages.values().stream().filter(packageData -> installedPackages.contains(packageData.name) && packageData.categories.contains(category)).toList();
    }

    public static void installPackage(String name) {
        PackageData packageData = packages.get(name);
        if (packageData == null || packageData.isInstalled) {
            System.out.println("Package " + name + " not found or already installed");
            return;
        }

        if (sources.containsKey(packageData.source.type)) {
            ISourceInstaller installer = sources.get(packageData.source.type);
            installer.install(packageData);
            packageData.isInstalled = true;
            savePackages();
        }
    }

    private static void savePackages() {
        Map<String, Object> mapToml = new HashMap<>();
        List<String> installedPackages = new ArrayList<>();
        for (PackageData packageData : packages.values()) {
            if (packageData.isInstalled) {
                installedPackages.add(packageData.name);
            }
        }
        mapToml.put("installed", installedPackages);

        var tomlWriter = new TomlWriter();
        var file = new File("config/registry.toml");
        try {
            tomlWriter.write(mapToml, file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Set<String> readLocalRegistry() {
        Toml toml = new Toml().read(new File("config/registry.toml"));
        List<String> t = toml.getList("installed");
        return new HashSet<>(t);
    }

}