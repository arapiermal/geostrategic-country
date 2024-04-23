package com.erimali.cntrygame;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

public class ZoomableScrollPaneSimple extends ScrollPane {
    private double scaleValue = 1; //was 0.7
    private double zoomIntensity = 0.02;
    private double zoomFactor = 1.25;
    private Group zoomNode;

    public ZoomableScrollPaneSimple(Group zoomNode) {
        super();
        this.zoomNode = zoomNode;
        setContent(zoomNode);
        setPannable(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.NEVER);
        setFitToHeight(true);
        setFitToWidth(true);
        updateScale();
    }


    private void updateScale() {

        // amount of scrolling in each direction in zoomNode coordinate units
        Point2D scrollOffset = figureScrollOffset();
        zoomNode.setScaleX(scaleValue);
        zoomNode.setScaleY(scaleValue);
        // move viewport so that old center remains in the center after the scaling
        repositionScroller(scrollOffset);
    }

    public void zoomIn() {
        scaleValue = scaleValue * zoomFactor;
        updateScale();
        this.layout();
    }

    public void zoomOut() {
        scaleValue = scaleValue / zoomFactor;
        updateScale();
        this.layout();
    }


    private Point2D figureScrollOffset() {
        double extraWidth = zoomNode.getLayoutBounds().getWidth() - this.getViewportBounds().getWidth();
        double hScrollProportion = (this.getHvalue() - this.getHmin()) / (this.getHmax() - this.getHmin());
        double scrollXOffset = hScrollProportion * Math.max(0, extraWidth);
        double extraHeight = zoomNode.getLayoutBounds().getHeight() - this.getViewportBounds().getHeight();
        double vScrollProportion = (this.getVvalue() - this.getVmin()) / (this.getVmax() - this.getVmin());
        double scrollYOffset = vScrollProportion * Math.max(0, extraHeight);
        return new Point2D(scrollXOffset, scrollYOffset);
    }

    private void repositionScroller(Point2D scrollOffset) {
        double scrollXOffset = scrollOffset.getX();
        double scrollYOffset = scrollOffset.getY();
        double extraWidth = zoomNode.getLayoutBounds().getWidth() - this.getViewportBounds().getWidth();
        if (extraWidth > 0) {
            double halfWidth = this.getViewportBounds().getWidth() / 2 ;
            double newScrollXOffset = (zoomFactor - 1) *  halfWidth + zoomFactor * scrollXOffset;
            this.setHvalue(this.getHmin() + newScrollXOffset * (this.getHmax() - this.getHmin()) / extraWidth);
        } else {
            this.setHvalue(this.getHmin());
        }
        double extraHeight = zoomNode.getLayoutBounds().getHeight() - this.getViewportBounds().getHeight();
        if (extraHeight > 0) {
            double halfHeight = this.getViewportBounds().getHeight() / 2 ;
            double newScrollYOffset = (zoomFactor - 1) * halfHeight + zoomFactor * scrollYOffset;
            this.setVvalue(this.getVmin() + newScrollYOffset * (this.getVmax() - this.getVmin()) / extraHeight);
        } else {
            this.setHvalue(this.getHmin());
        }
    }

}