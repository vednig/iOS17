package foundation.e.blisslauncher.core.customviews;

import android.graphics.Rect;
import android.view.View;

/**
 * Allows the implementing {@link View} to not draw underneath system bars.
 * e.g., notification bar on top and home key area on the bottom.
 */
public interface Insettable {

    void setInsets(Rect insets);
}
