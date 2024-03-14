package spotifybackup.storage;

import lombok.NonNull;
import se.michaelthelin.spotify.model_objects.specification.Image;

import java.util.SortedMap;
import java.util.TreeMap;

public enum ImageSelection {
    ALL,
    ONLY_LARGEST,
    ONLY_SMALLEST,
    NONE;

    private static SortedMap<Integer, Image> sortBySize(@NonNull Image[] images) {
        SortedMap<Integer, Image> map = new TreeMap<>();
        for (var image : images) {
            if (image.getHeight() == null || image.getWidth() == null ||
                    image.getHeight() <= 0 || image.getWidth() <= 0) continue;
            map.put(image.getWidth() * image.getHeight(), image);
        }
        return map;
    }

    /** @return null if images is null or no image has a valid size, otherwise smallest image in array. */
    static Image findSmallest(Image[] images) {
        if (images == null) return null;
        return sortBySize(images).pollFirstEntry().getValue();
    }

    /** @return null if images is null or no image has a valid size, otherwise smallest image in array. */
    static Image findLargest(Image[] images) {
        if (images == null) return null;
        return sortBySize(images).pollLastEntry().getValue();
    }
}
