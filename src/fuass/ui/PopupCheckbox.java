package fuass.ui;

import javax.swing.*;
import java.awt.*;
import java.util.function.Supplier;

/**
 * A checkbox that shows a popup menu on right-click and highlights itself on
 * hover, exactly like the result rows built with {@link PopupLabel}.
 * Left-click keeps the default behaviour (toggles the selection).
 * Used by the Advanced Search checklist so that candidates can be
 * inspected without accidentally changing the selection.
 *
 * <p>The popup is built on the first right-click, see {@link PopupHover}.
 */
public class PopupCheckbox extends JCheckBox {

    private final Font baseFont;

    public PopupCheckbox(String text, Supplier<JPopupMenu> popupSupplier) {
        super(text);
        // read through a local so the subclass field is assigned before any
        // overridable call escapes the constructor
        Font base = getFont();
        this.baseFont = base;

        PopupHover.install(this, popupSupplier);
    }

    /**
     * Highlights a checkbox that belongs to the default (pareto) selection.
     * Passing false restores the normal appearance.
     */
    public void setDefaultStyle(boolean isDefault) {
        setFont(isDefault
                ? baseFont.deriveFont(Font.BOLD, baseFont.getSize2D() + 1f)
                : baseFont);
    }
}
