package fuass.ui;

import javax.swing.*;
import java.awt.*;
import java.util.function.Supplier;

/**
 * A label that shows a popup menu on right-click and highlights itself on hover.
 * Left-click has no effect, it is only used to inspect the item.
 *
 * <p>The popup is built on the first right-click, see {@link PopupCheckbox}.
 * The hover and popup behaviour itself lives in {@link PopupHover}, shared
 * with the Advanced Search checklist.
 */
public class PopupLabel extends JLabel {

    public PopupLabel(String text, int fontStyle, Supplier<JPopupMenu> popupSupplier) {
        super(text);

        setFont(getFont().deriveFont(fontStyle));
        setOpaque(true);
        setBackground(Color.WHITE);

        PopupHover.install(this, popupSupplier);
    }
}
