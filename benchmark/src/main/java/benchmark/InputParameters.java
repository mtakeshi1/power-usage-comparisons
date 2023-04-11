package benchmark;

import java.text.NumberFormat;
import java.util.Locale;

public interface InputParameters {

    default NumberFormat numberFormat() {
        var nf = NumberFormat.getInstance(Locale.ROOT);
        nf.setGroupingUsed(false);
        nf.setMaximumFractionDigits(2);
        return nf;
    }

    String[] headers();

    String[] toCSV();

}
