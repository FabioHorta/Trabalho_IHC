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

public class RegisterController {

    // Elementos da interface gráfica ligados ao FXML
    @FXML private TextField nameField;          // Campo para o nome de utilizador
    @FXML private TextField nifField;           // Campo para o NIF (número fiscal)
    @FXML private PasswordField passwordField;  // Campo para palavra-passe
    @FXML private Button registerButton;        // Botão para submeter o registo

    // Caminho do ficheiro onde os utilizadores são guardados
    private final String USERS_FILE = "utilizadores.txt";

    // Ação executada ao clicar no botão "Registar"
    @FXML
    private void handleRegister(ActionEvent event) {
        // Recolhe os dados dos campos
        String nome = nameField.getText().trim();
        String nif = nifField.getText().trim();
        String pass = passwordField.getText().trim();

        // Verifica se algum campo está em branco
        if (nome.isEmpty() || nif.isEmpty() || pass.isEmpty()) {
            mostrarAlerta("Campos em branco", "Preencha todos os campos.");
            return;
        }

        // Validação do NIF — deve conter exatamente 9 dígitos
        if (!nif.matches("\\d{9}")) {
            mostrarAlerta("NIF inválido", "O NIF deve ter exatamente 9 dígitos.");
            return;
        }

        // Verifica se o nome de utilizador já existe
        if (existeUtilizador(nome)) {
            mostrarAlerta("Erro", "Nome de utilizador já existente.");
            return;
        }

        // Se tudo estiver correto, escreve o novo utilizador no ficheiro
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE, true))) {
            writer.write(nome + ";" + nif + ";" + pass); // Formato: nome;nif;password
            writer.newLine(); // Nova linha no ficheiro
            mostrarAlerta("Conta criada", "Conta registada com sucesso.");
            limparCampos(); // Limpa os campos da interface após registo
        } catch (IOException e) {
            mostrarAlerta("Erro", "Não foi possível guardar o utilizador.");
            e.printStackTrace(); // Mostra erro técnico no terminal
        }
    }

    // Verifica se já existe um utilizador com o nome fornecido
    private boolean existeUtilizador(String nome) {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] partes = linha.split(";");
                if (partes.length >= 1 && partes[0].equals(nome)) {
                    return true; // Nome já existente
                }
            }
        } catch (IOException e) {
            // Se o ficheiro ainda não existir, considera que não há utilizadores
        }
        return false; // Nome não encontrado
    }

    // Ação executada ao clicar no link/botão para voltar ao login
    @FXML
    private void goToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml")); // Carrega a tela de login
            Parent root = loader.load();
            Stage stage = (Stage) registerButton.getScene().getWindow(); // Obtém a janela atual
            stage.setScene(new Scene(root)); // Substitui a cena atual
            stage.show();
        } catch (IOException e) {
            mostrarAlerta("Erro", "Não foi possível voltar ao login.");
        }
    }

    // Exibe um alerta de informação com título e mensagem
    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null); // Sem cabeçalho
        alert.setContentText(mensagem);
        alert.showAndWait(); // Espera que o utilizador feche o alerta
    }

    // Limpa todos os campos de texto
    private void limparCampos() {
        nameField.clear();
        nifField.clear();
        passwordField.clear();
    }
}
