package com.erimali.cntrygame;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class ZoomableScrollPane extends ScrollPane {
    private double scaleValue = 1; //was 0.7
    private double zoomIntensity = 0.02;
    private Group target;
    private Node zoomNode;

    //SVGPath[] inside Group inside Group inside VBox inside ScrollPane...(inside GameStage...)
    public ZoomableScrollPane(Group target) {
        super();
        this.target = target;
        this.zoomNode = new Group(target);
        setContent(outerNode(zoomNode));

        setPannable(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.NEVER);
        setFitToHeight(true); //center
        setFitToWidth(true); //center

        updateScale();
    }

    private StackPane outerNode(Node node) {
        //Node outerNode = centeredNode(node);
        StackPane box = new StackPane(node);
        //box.setAlignment(Pos.CENTER); //default is center, leftup default if creating line on top (?)
        box.setBackground(new Background(new BackgroundFill(Paint.valueOf("#C2DFFF"), null, null)));

        box.setOnScroll(e -> {
            e.consume();
            onScroll(e.getTextDeltaY(), new Point2D(e.getX(), e.getY()));
        });
        return box;
    }

    /*
        private Node centeredNode(Node node) {
            VBox vBox = new VBox(node);
            vBox.setAlignment(Pos.CENTER);
            vBox.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, null, null)));
            return vBox;
        }
    */
    private void updateScale() {
        target.setScaleX(scaleValue);
        target.setScaleY(scaleValue);
    }

    private void onScroll(double wheelDelta, Point2D mousePoint) {
        double zoomFactor = Math.exp(wheelDelta * zoomIntensity);

        Bounds innerBounds = zoomNode.getLayoutBounds();
        Bounds viewportBounds = getViewportBounds();

        // calculate pixel offsets from [0, 1] range
        double valX = this.getHvalue() * (innerBounds.getWidth() - viewportBounds.getWidth());
        double valY = this.getVvalue() * (innerBounds.getHeight() - viewportBounds.getHeight());

        scaleValue = scaleValue * zoomFactor;
        updateScale();
        this.layout(); // refresh ScrollPane scroll positions & target bounds

        // convert target coordinates to zoomTarget coordinates
        Point2D posInZoomTarget = target.parentToLocal(zoomNode.parentToLocal(mousePoint));

        // calculate adjustment of scroll position (pixels)
        Point2D adjustment = target.getLocalToParentTransform().deltaTransform(posInZoomTarget.multiply(zoomFactor - 1));

        // convert back to [0, 1] range
        // (too large/small values are automatically corrected by ScrollPane)
        Bounds updatedInnerBounds = zoomNode.getBoundsInLocal();
        this.setHvalue((valX + adjustment.getX()) / (updatedInnerBounds.getWidth() - viewportBounds.getWidth()));
        this.setVvalue((valY + adjustment.getY()) / (updatedInnerBounds.getHeight() - viewportBounds.getHeight()));
    }

    //FAIL
    public void zoomIn() {
        double viewportCenterX = getViewportBounds().getWidth() / 2.0;
        double viewportCenterY = getViewportBounds().getHeight() / 2.0;
        onScroll(25, new Point2D(viewportCenterX, viewportCenterY));

    }

    public void zoomOut() {
        double viewportCenterX = getViewportBounds().getWidth() / 2.0;
        double viewportCenterY = getViewportBounds().getHeight() / 2.0;
        onScroll(-25, new Point2D(viewportCenterX, viewportCenterY));
    }

}