package de.leibnizfmp.maporganelle;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;
import java.text.DecimalFormat;

public class PreviewLabeledSpinner5Digit implements Serializable {
    /**
     * creates a 5 digit spinner
     *
     * @param label name
     * @param model for spinner
     * @param unit  label after the spinner box
     * @return box with labeled spinner with 5 digits
     */
    static Box addLabeledSpinner5Digit(String label,
                                       SpinnerModel model,
                                       String unit) {

        Box spinnerLabelBox = new Box(BoxLayout.X_AXIS);
        JLabel l1 = new JLabel(label);
        l1.setPreferredSize(new Dimension(150, l1.getMinimumSize().height));
        spinnerLabelBox.add(l1);

        JSpinner spinner = new JSpinner(model);
        JSpinner.NumberEditor editor = (JSpinner.NumberEditor) spinner.getEditor();
        DecimalFormat format = editor.getFormat();
        format.setMinimumFractionDigits(4);
        l1.setLabelFor(spinner);
        spinnerLabelBox.add(spinner);
        spinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, spinner.getMinimumSize().height));

        JLabel l2 = new JLabel(unit);
        l2.setPreferredSize(new Dimension(30, l2.getMinimumSize().height));
        spinnerLabelBox.add(l2);

        return spinnerLabelBox;
    }
}