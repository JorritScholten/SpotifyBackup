package spotifybackup.storage;

import se.michaelthelin.spotify.model_objects.specification.Image;

public enum ImageSelection {
    ALL,
    ONLY_LARGEST,
    ONLY_SMALLEST,
    NONE;

    /** @return null if images is null, otherwise smallest image in array. */
    static Image findSmallest(Image[] images) {
        if (images == null) return null;
        int smallestSize = Integer.MAX_VALUE;
        Image smallestImage = null;
        for (var image : images) {
            if (image.getHeight() == null || image.getWidth() == null ||
                    image.getHeight() <= 0 || image.getWidth() <= 0) continue;
            if (image.getHeight() * image.getWidth() < smallestSize) {
                smallestSize = image.getHeight() * image.getWidth();
                smallestImage = image;
            }
        }
        return smallestImage;
    }

    /** @return null if images is null, otherwise smallest image in array. */
    static Image findLargest(Image[] images) {
        if (images == null) return null;
        int largestSize = Integer.MIN_VALUE;
        Image largestImage = null;
        for (var image : images) {
            if (image.getHeight() == null || image.getWidth() == null ||
                    image.getHeight() <= 0 || image.getWidth() <= 0) continue;
            if (image.getHeight() * image.getWidth() > largestSize) {
                largestSize = image.getHeight() * image.getWidth();
                largestImage = image;
            }
        }
        return largestImage;
    }
}
