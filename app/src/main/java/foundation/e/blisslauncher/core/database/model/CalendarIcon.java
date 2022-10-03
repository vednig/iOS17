package foundation.e.blisslauncher.core.database.model;

import android.widget.TextView;

/** Created by falcon on 17/3/18. */
public class CalendarIcon {
    public TextView monthTextView;
    public TextView dayTextView;

    public CalendarIcon(TextView monthTextView, TextView dayTextView) {
        this.monthTextView = monthTextView;
        this.dayTextView = dayTextView;
    }
}
