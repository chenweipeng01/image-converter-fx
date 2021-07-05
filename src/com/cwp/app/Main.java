package com.cwp.app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.sf.image4j.use.Image4j;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileSystemView;
import java.awt.MenuItem;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;

public class Main extends Application {
    private static final String APP_NAME = "image-converter-tool";

    private static final String ICON_PATH = "/images/icon2.png";

    private static final int FRAME_WIDTH = 290;

    private static final int FRAME_HEIGHT = 215;

    private static final String BACKGROUND_COLOR = "#3c3f41";

    private static final String CSS_FILE_NAME = "main.css";

    private static final double FRAME_OPACITY = 0.8;

    private static final int V_SPACING = 5;

    private static final Insets V_PADDING = new Insets(15, 20, 15, 20);

    private static final int H_SPACING = 5;

    private static final Insets H_PADDING = new Insets(5, 0, 0, 0);

    private Stage mainStage;

    private File currentInputFile;

    private Label fileNameLabel;

    private Button selectFileButton;

    private String filePath;

    private String fileName;

    private String inputFileSuffix;

    private ChoiceBox outputChoiceBox;

    private Button selectOutputDirButton;

    private String outputDir;

    private Label outputDirNameLabel;

    private String defaultOutputDir;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        initStage(primaryStage);

        primaryStage.setScene(getScene());

        primaryStage.show();
    }

    private void initStage(Stage primaryStage) throws IOException, AWTException {
        mainStage = primaryStage;
        defaultOutputDir = FileSystemView.getFileSystemView().getHomeDirectory().getCanonicalPath();
        // 无任务栏程序
        primaryStage.setAlwaysOnTop(false);
        primaryStage.initStyle(StageStyle.UTILITY);
        primaryStage.setTitle(APP_NAME);
        primaryStage.setOpacity(FRAME_OPACITY);
        primaryStage.setResizable(false);

        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream(ICON_PATH)));
        // 不真正退出
        Platform.setImplicitExit(false);

        SystemTray tray = SystemTray.getSystemTray();
        BufferedImage image = ImageIO.read(getClass().getResourceAsStream(ICON_PATH));
        TrayIcon trayIcon = new TrayIcon(image, APP_NAME);
        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip(APP_NAME);
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getButton() == MouseEvent.BUTTON1) {
                    Platform.runLater(() -> {
                        if (primaryStage.isIconified()) {
                            primaryStage.setIconified(false);
                        }
                        if (!primaryStage.isShowing()) {
                            primaryStage.show();
                        }
                        primaryStage.toFront();
                    });
                }
            }
        });
        MenuItem exitItem = new MenuItem("exit");
        exitItem.addActionListener(e -> {
            System.exit(0);
        });
        final PopupMenu popup = new PopupMenu();
        popup.add(exitItem);
        trayIcon.setPopupMenu(popup);
        tray.add(trayIcon);
    }

    private Scene getScene() {
        VBox vbox = new VBox(V_SPACING);
        vbox.setPadding(V_PADDING);
        Scene scene = new Scene(vbox, FRAME_WIDTH, FRAME_HEIGHT);
        scene.setFill(Paint.valueOf(BACKGROUND_COLOR));
        vbox.setBackground(Background.EMPTY);
        URL url_css = getClass().getResource(CSS_FILE_NAME);
        scene.getStylesheets().add(url_css.toExternalForm());

        vbox.getChildren().add(getFileSelecterHbox());
        vbox.getChildren().add(getFileNameHbox());
        vbox.getChildren().add(getOutputTypeHbox());
        vbox.getChildren().add(getOutputDirHbox());
        vbox.getChildren().add(getOutputDirNameHbox());
        vbox.getChildren().add(getGenerateHbox());
        return scene;
    }

    private HBox getCommonHBox() {
        HBox hBox = new HBox(H_SPACING);
        hBox.setPadding(H_PADDING);
        return hBox;
    }

    private Label getCommonLabel(String text, Node labelFor) {
        Label label = new Label();
        label.getStyleClass().add("common-label");
        if (null != labelFor) {
            label.setLabelFor(labelFor);
        }
        label.setText(text);
        return label;
    }

    private void showAlert(String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.titleProperty().set("提示");
        alert.headerTextProperty().set(text);
        alert.showAndWait();
    }

    private void setOutputTypeItems() {
        outputChoiceBox.getItems().clear();
        switch (inputFileSuffix) {
            case "jpg": {
                outputChoiceBox.getItems().add("ico");
                outputChoiceBox.getItems().add("png");
                outputChoiceBox.getItems().add("bmp");
                break;
            }
            case "png": {
                outputChoiceBox.getItems().add("ico");
                outputChoiceBox.getItems().add("jpg");
                outputChoiceBox.getItems().add("bmp");
                break;
            }
            case "ico": {
                outputChoiceBox.getItems().add("jpg");
                outputChoiceBox.getItems().add("png");
                outputChoiceBox.getItems().add("bmp");
                break;
            }
        }
    }

    private HBox getOutputDirHbox() {
        HBox hBox = getCommonHBox();

        selectOutputDirButton = getCommonButton("选择输出目录", 245.0);
        selectOutputDirButton.setOnAction(arg0 -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File outputDirFile = directoryChooser.showDialog(mainStage);
            if (outputDirFile != null && outputDirFile.exists()) {
                try {
                    outputDir = outputDirFile.getCanonicalPath();
                    outputDirNameLabel.setText(outputDir);
                    outputDirNameLabel.setTooltip(new Tooltip(outputDir));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        hBox.getChildren().add(selectOutputDirButton);
        return hBox;
    }

    private HBox getOutputTypeHbox() {
        HBox hBox = getCommonHBox();

        outputChoiceBox = new ChoiceBox();
        outputChoiceBox.setPrefWidth(170);
        outputChoiceBox.setTooltip(new Tooltip("输出文件后缀"));
        EventHandler<? super javafx.scene.input.MouseEvent> eventHandler = (event) -> {
            if (inputFileSuffix == null || inputFileSuffix.isEmpty()) {
                showAlert("请先选择文件");
            } else {
                setOutputTypeItems();
            }
        };
        outputChoiceBox.setOnMouseClicked(eventHandler);

        Label label = getCommonLabel("输出类型：", outputChoiceBox);
        hBox.getChildren().add(label);
        hBox.getChildren().add(outputChoiceBox);
        return hBox;
    }

    private HBox getFileSelecterHbox() {
        HBox hBox = getCommonHBox();

        selectFileButton = getCommonButton("选择本地图片", 245.0);
        selectFileButton.setOnAction(arg0 -> {
            FileChooser fileChooser1 = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Image files", "*.jpg", "*.png", "*.ico");
            fileChooser1.getExtensionFilters().add(extFilter);
            currentInputFile = fileChooser1.showOpenDialog(mainStage);
            if (currentInputFile != null && currentInputFile.exists()) {
                try {
                    filePath = currentInputFile.getCanonicalPath();
                    fileName = currentInputFile.getName();
                    inputFileSuffix = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase(Locale.ROOT);
                    setOutputTypeItems();
                    fileNameLabel.setText(fileName);
                    fileNameLabel.setTooltip(new Tooltip(filePath));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });

        hBox.getChildren().add(selectFileButton);
        return hBox;
    }

    private HBox getFileNameHbox() {
        HBox hBox = getCommonHBox();

        Label label = getCommonLabel("文件名：", selectFileButton);
        fileNameLabel = getCommonLabel("未选择", selectFileButton);
        hBox.getChildren().add(label);
        hBox.getChildren().add(fileNameLabel);
        return hBox;
    }

    private HBox getOutputDirNameHbox() {
        HBox hBox = getCommonHBox();

        Label label = getCommonLabel("输出目录：", selectOutputDirButton);
        outputDirNameLabel = getCommonLabel(defaultOutputDir, selectOutputDirButton);
        outputDirNameLabel.setTooltip(new Tooltip(defaultOutputDir));
        outputDirNameLabel.setMaxWidth(170);
        hBox.getChildren().add(label);
        hBox.getChildren().add(outputDirNameLabel);
        return hBox;
    }

    private HBox getGenerateHbox() {
        HBox hBox = getCommonHBox();
        Button generateBtn = getCommonButton("生成", 200.0);
        generateBtn.setPrefWidth(200);
        generateBtn.setId("generateBtn");
        generateBtn.setOnAction(event -> {
            if (currentInputFile == null) {
                showAlert("请选择一张本地图片");
                return;
            }
            if (!currentInputFile.exists()) {
                showAlert("本地图片不存在，请重新选择");
                return;
            }
            if (!(inputFileSuffix.equals("png") || inputFileSuffix.equals("jpg") || inputFileSuffix.equals("ico"))) {
                showAlert("本地图片格式不正确，请重新选择");
                return;
            }
            Object outputType = outputChoiceBox.getValue();
            if (outputType == null || ((String) outputType).isEmpty()) {
                showAlert("请选择输出图片格式");
                return;
            }
            String outputFinalDir = outputDir;
            if (outputFinalDir == null || outputFinalDir.isEmpty()) {
                outputFinalDir = defaultOutputDir;
            }
            String fileNameWithoutSuffix = fileName.substring(0, fileName.lastIndexOf("."));
            String outputPath = outputFinalDir + File.separator + fileNameWithoutSuffix + "." + outputType;
            boolean result = generate(filePath, outputPath);
            if (result) {
                showAlert("生成成功！");
                return;
            } else {
                showAlert("生成失败，请重试");
                return;
            }
        });

        Button openBtn = getCommonButton("打开", 40.0);
        hBox.getChildren().add(generateBtn);
        hBox.getChildren().add(openBtn);

        return hBox;
    }

    private Button getCommonButton(String text, Double width) {
        Button btn = new Button(text);
        btn.getStyleClass().add("common-btn");
        if (null != width) {
            btn.setPrefWidth(width.doubleValue());
        }
        return btn;
    }

    private boolean generate(String filePath, String outputPath) {
        return Image4j.Convert(filePath, outputPath);
    }
}
