package de.leibnizfmp.maporganelle;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;

public class PreviewLabeledSpinner implements Serializable {
    /**
     * creates a labeled spinner
     *
     * @param label name
     * @param model for which spinner
     * @param unit  label after spinner
     * @return box with labeled spinner
     */
    static Box addLabeledSpinnerUnit(String label,
                                     SpinnerModel model,
                                     String unit) {

        Box spinnerLabelBox = new Box(BoxLayout.X_AXIS);
        JLabel l1 = new JLabel(label);
        l1.setPreferredSize(new Dimension(150, l1.getMinimumSize().height));
        spinnerLabelBox.add(l1);

        JSpinner spinner = new JSpinner(model);
        l1.setLabelFor(spinner);
        spinnerLabelBox.add(spinner);
        spinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, spinner.getMinimumSize().height));

        JLabel l2 = new JLabel(unit);
        l2.setPreferredSize(new Dimension(30, l2.getMinimumSize().height));
        spinnerLabelBox.add(l2);

        return spinnerLabelBox;
    }
}