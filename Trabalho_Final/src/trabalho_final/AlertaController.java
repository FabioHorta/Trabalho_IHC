package trabalho_final;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public class AlertaController {

    // ListView para exibir os alertas de garantias próximas do final
    @FXML private ListView<String> listViewAlertas;

    // Método chamado automaticamente ao abrir a interface
    @FXML
    public void initialize() {
        carregarAlertas(); // Carrega alertas dos ficheiros
    }

    // Lê o ficheiro de garantias e identifica quais estão perto do fim
    private void carregarAlertas() {
        listViewAlertas.getItems().clear(); // Limpa a lista antes de carregar novamente

        try (BufferedReader br = new BufferedReader(new FileReader("data/garantias.txt"))) {
            String linha;

            // Lê o ficheiro linha a linha
            while ((linha = br.readLine()) != null) {
                String[] partes = linha.split(";");

                // Valida se a linha tem campos suficientes
                if (partes.length >= 5) {
                    String nomeProduto = partes[1];          // Nome do produto
                    String dataCompra = partes[2];           // Data da compra
                    String duracaoGarantia = partes[3];      // Duração em anos

                    try {
                        // Converte data de compra para LocalDate
                        LocalDate dataCompraLocal = LocalDate.parse(dataCompra);

                        // Soma a duração da garantia (em anos) à data de compra
                        LocalDate dataExpiracao = dataCompraLocal.plusYears(Long.parseLong(duracaoGarantia));

                        // Calcula dias restantes até a garantia expirar
                        long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), dataExpiracao);

                        // Se faltam até 30 dias, exibe alerta
                        if (diasRestantes <= 30 && diasRestantes > 0) {
                            listViewAlertas.getItems().add("Produto: " + nomeProduto + " - Expira em " + diasRestantes + " dias.");
                        } else if (diasRestantes <= 0) {
                            listViewAlertas.getItems().add("Produto: " + nomeProduto + " - Garantia Expirada.");
                        }

                    } catch (DateTimeParseException e) {
                        // Caso a data esteja com formato incorreto
                        listViewAlertas.getItems().add("Produto: " + nomeProduto + " - Data inválida!");
                    }
                }
            }
        } catch (IOException e) {
            // Caso ocorra erro ao ler o ficheiro
            e.printStackTrace();
        }
    }

    // Remove alerta selecionado da lista (apenas visual, não apaga no ficheiro)
    @FXML
    private void removerAlerta() {
        String alertaSelecionado = listViewAlertas.getSelectionModel().getSelectedItem();

        if (alertaSelecionado != null) {
            listViewAlertas.getItems().remove(alertaSelecionado); // Remove da interface
            mostrarAlerta("Sucesso", "Alerta removido.");          // Exibe confirmação
        } else {
            mostrarAlerta("Erro", "Selecione um alerta para remover.");
        }
    }

    // Volta à tela principal do menu
    @FXML
    private void voltarMenu(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("menu.fxml"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    // Método auxiliar para exibir uma mensagem ao utilizador
    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);       // Título da caixa de diálogo
        alert.setHeaderText(null);    // Sem cabeçalho
        alert.setContentText(mensagem);
        alert.showAndWait();          // Espera até o utilizador fechar
    }
}
