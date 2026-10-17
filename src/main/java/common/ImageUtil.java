package common;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.swing.ImageIcon;

/**
 * Utility for loading, scaling, and persisting package/destination images.
 */
public class ImageUtil {

    public static final String DATA_PACKAGES_DIR = "data/images/packages/";
    public static final String DATA_DESTINATIONS_DIR = "data/images/destinations/";

    public static final String DEFAULT_PACKAGE_PLACEHOLDER = "/images/packages/placeholder.png";
    public static final String DEFAULT_DESTINATION_PLACEHOLDER = "/images/destinations/placeholder.png";

    /**
     * Loads an image icon from external data/ directory, resources/ classpath, or absolute path,
     * scaled smoothly to target dimensions.
     */
    public static ImageIcon loadScaledImage(String imagePath, int targetWidth, int targetHeight, String placeholderPath) {
        ImageIcon rawIcon = null;

        if (imagePath != null && !imagePath.trim().isEmpty()) {
            String trimmed = imagePath.trim();

            // 1. Try file in data/ directory
            File dataFile = new File(trimmed);
            if (dataFile.exists()) {
                rawIcon = new ImageIcon(dataFile.getAbsolutePath());
            } else {
                File pkgFile = new File(DATA_PACKAGES_DIR + trimmed);
                if (pkgFile.exists()) {
                    rawIcon = new ImageIcon(pkgFile.getAbsolutePath());
                } else {
                    File destFile = new File(DATA_DESTINATIONS_DIR + trimmed);
                    if (destFile.exists()) {
                        rawIcon = new ImageIcon(destFile.getAbsolutePath());
                    }
                }
            }

            // 2. Try classpath resource
            if (rawIcon == null || rawIcon.getIconWidth() <= 0) {
                String resPath = trimmed.startsWith("/") ? trimmed : "/" + trimmed;
                URL url = ImageUtil.class.getResource(resPath);
                if (url != null) {
                    rawIcon = new ImageIcon(url);
                } else {
                    URL pkgUrl = ImageUtil.class.getResource("/images/packages/" + trimmed);
                    if (pkgUrl != null) {
                        rawIcon = new ImageIcon(pkgUrl);
                    }
                }
            }
        }

        // 3. Fallback to placeholder if still null
        if (rawIcon == null || rawIcon.getIconWidth() <= 0) {
            String fallback = placeholderPath != null ? placeholderPath : DEFAULT_PACKAGE_PLACEHOLDER;
            URL url = ImageUtil.class.getResource(fallback);
            if (url != null) {
                rawIcon = new ImageIcon(url);
            }
        }

        if (rawIcon == null || rawIcon.getIconWidth() <= 0) {
            return null;
        }

        Image scaled = rawIcon.getImage().getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    /**
     * Copies a selected file into target directory and returns the saved filename.
     */
    public static String saveUploadedImage(File sourceFile, String targetDirectory, String prefix) throws IOException {
        File dir = new File(targetDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String originalName = sourceFile.getName();
        String ext = "";
        int dot = originalName.lastIndexOf('.');
        if (dot >= 0) {
            ext = originalName.substring(dot).toLowerCase();
        } else {
            ext = ".png";
        }

        String safeFileName = prefix + "_" + System.currentTimeMillis() + ext;
        File targetFile = new File(dir, safeFileName);
        Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        return safeFileName;
    }
}
