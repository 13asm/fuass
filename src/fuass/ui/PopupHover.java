package fuass.ui;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Supplier;

/**
 * Hover highlight plus right-click popup shared by the result rows
 * ({@link CombinationPanel}) and the Advanced Search checklist
 * ({@link AdvancedSearch}), so both places behave exactly the same.
 *
 * <p>The row tints itself with a light version of the list selection color
 * while the mouse is over it, and keeps the highlight while its popup is
 * open. The popup is built lazily on the first right-click: a full result set
 * builds hundreds of rows, so creating every popup up front would allocate
 * thousands of Swing components on the event thread for nothing.
 *
 * <p>Pass a {@code null} supplier to install the hover without a popup.
 */
final class PopupHover extends MouseAdapter {

    /**
     * Tint of the list selection color, never the selection color itself: on
     * Windows the raw selection blue is the same accent blue used for the
     * check mark of a selected checkbox, so a hovered row used to look like a
     * checked one. Mixing it with white keeps the hue but drops it far below
     * the check mark in luminance, and lets the row keep its normal text color.
     */
    private static final Color HOVER_BACKGROUND = hoverBackground();

    private final JComponent row;
    private final Supplier<JPopupMenu> popupSupplier;

    private JPopupMenu popup;

    private PopupHover(JComponent row, Supplier<JPopupMenu> popupSupplier) {
        this.row = row;
        this.popupSupplier = popupSupplier;
        // JLabel is opaque by default, JCheckBox may not be with the system look and feel
        row.setOpaque(true);
        row.addMouseListener(this);
    }

    static void install(JComponent row, Supplier<JPopupMenu> popupSupplier) {
        new PopupHover(row, popupSupplier);
    }

    private static Color hoverBackground() {
        Color selection = UIManager.getColor("List.selectionBackground");
        if (selection == null) return new Color(0xB2D6F2);
        float tint = 0.30f;
        return new Color(
                Math.round(selection.getRed()   * tint + Color.WHITE.getRed()   * (1 - tint)),
                Math.round(selection.getGreen() * tint + Color.WHITE.getGreen() * (1 - tint)),
                Math.round(selection.getBlue()  * tint + Color.WHITE.getBlue()  * (1 - tint)));
    }

    // ---hover---

    @Override public void mouseEntered(MouseEvent e) {
        if (!popupVisible()) setHighlight(true);
    }

    @Override public void mouseExited(MouseEvent e) {
        if (!popupVisible()) setHighlight(false);
    }

    // ---popup---

    @Override public void mousePressed(MouseEvent e) {
        if (popupSupplier != null && SwingUtilities.isRightMouseButton(e)) {
            popup().show(row, e.getX() - 2, e.getY() - 2);
        }
    }

    private boolean popupVisible() {
        return popup != null && popup.isVisible();
    }

    /** Builds the popup on first use, wiring the highlight listener at the same time. */
    private JPopupMenu popup() {
        if (popup == null) {
            popup = popupSupplier.get();
            popup.addPopupMenuListener(new PopupMenuListener() {
                @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    setHighlight(true);
                }
                @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    setHighlight(mouseInsideRow());
                }
                @Override public void popupMenuCanceled(PopupMenuEvent e) {
                    setHighlight(mouseInsideRow());
                }
            });
        }
        return popup;
    }

    /**
     * Whether the pointer sits over this row, even when the popup is drawn on
     * top of it, so closing the popup does not drop the highlight while the
     * mouse is still there.
     */
    private boolean mouseInsideRow() {
        try {
            Point p = MouseInfo.getPointerInfo().getLocation();
            SwingUtilities.convertPointFromScreen(p, row);
            return p.x >= 0 && p.x < row.getWidth() && p.y >= 0 && p.y < row.getHeight();
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private void setHighlight(boolean highlighted) {
        // background only: the text keeps its normal color
        row.setBackground(highlighted ? HOVER_BACKGROUND : Color.WHITE);
    }
}
