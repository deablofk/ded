package dev.cwby.pkgs;

import com.moandjiezana.toml.Toml;
import dev.cwby.pkgs.sources.GenericSourceInstaller;
import dev.cwby.pkgs.sources.ISourceInstaller;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PackageManager {
    public static final String PACKAGES_DIR = "config/packages/";
    public static final String INTERNALS_DIR = "config/internals/";
    private static final Map<String, ISourceInstaller> sources = new HashMap<>();
    private static final Map<String, PackageData> packages = new HashMap<>();
    private static final Toml toml = new Toml();

    static {
        sources.put("generic", new GenericSourceInstaller());
    }

    public static void initializePackages() {
        File packagesDirectory = new File(PACKAGES_DIR);
        if (!packagesDirectory.exists()) {
            return;
        }

        for (File pkgToml : packagesDirectory.listFiles()) {
            PackageData packageData = toml.read(pkgToml).to(PackageData.class);
            var internalFile = new File(INTERNALS_DIR + packageData.name);
            if (internalFile.exists()) {
                packageData.isInstalled = true;
            }
            packages.put(packageData.name, packageData);
        }
    }

    public static Collection<PackageData> getPackages() {
        return packages.values();
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
        }
    }

}