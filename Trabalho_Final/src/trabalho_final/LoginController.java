package trabalho_final;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;

public class LoginController {

    // Variável pública estática para guardar o NIF do utilizador logado
    public static String nifAtual; 

    // Campos da interface (ligados ao FXML)
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Hyperlink registerLink;

    // Caminho do ficheiro onde os utilizadores estão guardados
    private final String USERS_FILE = "utilizadores.txt";

    // Método chamado quando o botão "Login" é pressionado
    @FXML
    private void handleLogin(ActionEvent event) {
        // Recolhe os dados inseridos nos campos de texto
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // Valida se algum campo está vazio
        if (username.isEmpty() || password.isEmpty()) {
            mostrarAlerta("Campos em branco", "Por favor preencha todos os campos.");
            return;
        }

        boolean encontrado = false; // Marca se o utilizador foi encontrado

        // Lê o ficheiro linha a linha para verificar credenciais
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] partes = linha.split(";");
                if (partes.length >= 3) {
                    String nomeFicheiro = partes[0]; // nome de utilizador
                    String nif = partes[1];          // NIF do utilizador
                    String passe = partes[2];        // palavra-passe

                    // Verifica se o nome e a palavra-passe correspondem
                    if (nomeFicheiro.equals(username) && passe.equals(password)) {
                        encontrado = true;
                        LoginController.nifAtual = nif; // Guarda o NIF do utilizador autenticado
                        break;
                    }
                }
            }
        } catch (IOException e) {
            // Caso haja erro ao ler o ficheiro
            mostrarAlerta("Erro", "Não foi possível ler o ficheiro de utilizadores.");
        }

        // Se o utilizador foi encontrado, carrega a interface do menu
        if (encontrado) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("menu.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) loginButton.getScene().getWindow(); // Obtém a janela atual
                stage.setScene(new Scene(root)); // Define a nova cena
                stage.show();
            } catch (IOException e) {
                mostrarAlerta("Erro", "Não foi possível carregar o menu principal.");
            }
        } else {
            // Caso as credenciais estejam erradas
            mostrarAlerta("Credenciais inválidas", "Utilizador ou palavra-passe incorretos.");
        }
    }

    // Método chamado ao clicar no link "Registar"
    @FXML
    private void handleRegister(ActionEvent event) {
        try {
            // Carrega a interface de registo
            FXMLLoader loader = new FXMLLoader(getClass().getResource("register.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) registerLink.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            mostrarAlerta("Erro", "Não foi possível carregar a view de registo.");
        }
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(AlertType.INFORMATION); // Tipo de alerta informativo
        alert.setTitle(titulo);
        alert.setHeaderText(null); // Sem cabeçalho
        alert.setContentText(mensagem);
        alert.showAndWait(); // Mostra e espera que o utilizador feche
    }
}
