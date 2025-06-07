package trabalho_final;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.io.IOException;

public class MenuController {

    @FXML
    private void abrirFaturas(ActionEvent event) throws IOException {
        carregarPagina("fatura.fxml", event);
    }

    @FXML
    private void abrirGarantias(ActionEvent event) throws IOException {
        carregarPagina("garantia.fxml", event);
    }

    @FXML
    private void abrirAlertas(ActionEvent event) throws IOException {
        carregarPagina("alerta.fxml", event);
    }

    @FXML
    private void abrirConsulta(ActionEvent event) throws IOException {
        carregarPagina("consulta.fxml", event);
    }

    @FXML
    private void terminarSessao(ActionEvent event) throws IOException {
        carregarPagina("login.fxml", event);
    }

    private void carregarPagina(String fxml, ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxml));
        Stage stage = (Stage)((javafx.scene.Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}
