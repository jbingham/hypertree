package com.sugen.gui.plot;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.Serializable;

/**
 * LabelRenderer for hyperbolic plots.
 * <p>
 * In a hyperbolic view, points further from the origin typically have
 * smaller labels. So this renderer takes into account each point's
 * distance from the origin and adjusts the font size accordingly.
 *
 * <p>As implemented, the HyperbolicLabelRenderer in fact contains
 * multiple DefaultLabelRenderers, one for each font size. This
 * significantly improves performance, because it avoids costly calls
 * related to constantly adjusting the font.
 *
 * @author Jonathan Bingham
 */
public class HyperbolicLabelRenderer
    extends Component implements LabelRenderer, Serializable {
    /** @serial	 */
    protected DefaultLabelRenderer rendererLarge;
    /** @serial	 */
    protected DefaultLabelRenderer rendererMedium;
    /** @serial	 */
    protected DefaultLabelRenderer rendererSmall;

    public HyperbolicLabelRenderer() {
        rendererLarge = new DefaultLabelRenderer();
        rendererMedium = new DefaultLabelRenderer();
        rendererSmall = new DefaultLabelRenderer();
        setFont(getFont());
    }

    /**
     * Avoid memory reallocation when computing distance from origin.
     * @serial
     */
    protected Point2D tmp = new Point2D.Double();

    public Component getRendererComponent(Component view,
                                          Object dataObject, int key,
                                          boolean isSelected, boolean hasFocus,
                                          boolean isAdjusting,
                                          boolean isVisible, Color color) {
        //Find distance from the origin
        double unitRadius = 0;
        if(key >= 0) {
            HyperbolicView hpv = (HyperbolicView)view;
            Point2D p = hpv.linearTransform.get(key, tmp);
            unitRadius = (hpv.origin.distance(p.getX(), p.getY())
                          / hpv.radius);
        }

        //Use the renderer with font size suitable for the
        //distance from the origin
        DefaultLabelRenderer renderer = rendererSmall;
        if(key >= 0 && dataObject != null && !"".equals(dataObject.toString())) {
            //if in focus or selected, always render at the largest size
            if(hasFocus || isSelected || unitRadius < 0.5)
                renderer = rendererLarge;
            else if(unitRadius < 0.7)
                renderer = rendererMedium;
            else if(unitRadius < 0.8)
                renderer = rendererSmall;
            else {
                renderer = rendererSmall;
                renderer.setText(null);
            }
        }
        return renderer.getRendererComponent(view,
                                             dataObject, key, isSelected,
                                             hasFocus, isAdjusting,
                                             isVisible, color);
    }

    public void setFont(Font font) {
        rendererLarge.setFont(font);
        rendererMedium.setFont(font.deriveFont((float)(font.getSize() - 2)));
        rendererSmall.setFont(font.deriveFont((float)(font.getSize() - 4)));
    }

    public Font getFont() {
        return rendererLarge.getFont();
    }
}
