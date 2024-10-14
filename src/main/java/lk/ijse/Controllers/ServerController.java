package lk.ijse.Controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerController {

    @FXML private TextField textField;
    @FXML private Button sendBtn;
    @FXML private Button addImageBtn;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox messageContainer;

    private Stage primaryStage;
    private ServerSocket serverSocket;
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    public void initialize() {
        new Thread(this::startServer).start();
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    private void startServer() {
        try {
            serverSocket = new ServerSocket(3000);
            Platform.runLater(() -> appendMessage("Server Started. Waiting for client...", null));

            socket = serverSocket.accept();
            Platform.runLater(() -> appendMessage("Client Connected.", null));

            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());

            new Thread(this::receiveMessages).start();

        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(() -> appendMessage("Failed to start server.", null));
        }
    }

    private void receiveMessages() {
        try {
            while (true) {
                int messageType = dataInputStream.readInt();
                if (messageType == 0) { // Text message
                    String clientMessage = dataInputStream.readUTF();
                    Platform.runLater(() -> appendMessage("Client: " + clientMessage, null));
                } else if (messageType == 1) { // Image message
                    int imageSize = dataInputStream.readInt();
                    byte[] imageData = new byte[imageSize];
                    dataInputStream.readFully(imageData);

                    Image image = new Image(new ByteArrayInputStream(imageData));
                    Platform.runLater(() -> appendMessage("Client: Sent an image", image));
                }
            }
        } catch (IOException e) {
            Platform.runLater(() -> appendMessage("Client disconnected.", null));
        } finally {
            closeConnection();
        }
    }

    @FXML
    public void textFieldOnAction(ActionEvent actionEvent) {
        sendMessage();
    }

    @FXML
    public void sendBtnOnAction(ActionEvent actionEvent) {
        sendMessage();
    }

    @FXML
    public void addImageBtnOnAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null) {
            try {
                Image image = new Image(new FileInputStream(selectedFile));
                sendImage(image, selectedFile.getPath());
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
                appendMessage("Failed to load image.", null);
            }
        }
    }

    private void sendMessage() {
        String message = textField.getText().trim();
        if (!message.isEmpty()) {
            try {
                dataOutputStream.writeInt(0);
                dataOutputStream.writeUTF(message);
                dataOutputStream.flush();
                appendMessage("Server: " + message, null);
                textField.clear();
            } catch (IOException e) {
                e.printStackTrace();
                appendMessage("Failed to send message.", null);
            }
        }
    }

    private void sendImage(Image image, String imagePath) {
        try {
            File file = new File(imagePath);
            byte[] imageData = new byte[(int) file.length()];
            FileInputStream fis = new FileInputStream(file);
            fis.read(imageData);
            fis.close();

            dataOutputStream.writeInt(1); // 1 for image message
            dataOutputStream.writeInt(imageData.length);
            dataOutputStream.write(imageData);
            dataOutputStream.flush();

            appendMessage("Server: Image sent", image);
        } catch (IOException e) {
            e.printStackTrace();
            appendMessage("Failed to send image.", null);
        }
    }

    private void appendMessage(String message, Image image) {
        Text text = new Text(message + "\n");
        messageContainer.getChildren().add(text);

        if (image != null) {
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(100);
            imageView.setPreserveRatio(true);
            messageContainer.getChildren().add(imageView);
        }

        // Scroll to the bottom
        scrollPane.setVvalue(1.0);
    }

    private void closeConnection() {
        try {
            if (dataInputStream != null) dataInputStream.close();
            if (dataOutputStream != null) dataOutputStream.close();
            if (socket != null) socket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}