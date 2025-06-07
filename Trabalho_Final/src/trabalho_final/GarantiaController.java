package trabalho_final;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.List;

public class GarantiaController {

    // Ligações com os elementos da interface FXML
    @FXML private TextField txtNomeProduto;
    @FXML private DatePicker dateCompra;
    @FXML private TextField txtDuracaoGarantia;
    @FXML private CheckBox checkAdicionarFicheiro;
    @FXML private Button btnEscolherAnexo;
    @FXML private Label labelFicheiro;
    @FXML private Label erroNomeProduto, erroData, erroDuracao, erroFicheiro;

    // Variável para guardar o ficheiro selecionado
    private File ficheiroSelecionado;
    private boolean fecharAutomaticamente = false;

    // Setter para definir se a janela deve fechar automaticamente após salvar
    public void setFecharAutomaticamente(boolean fechar) {
        this.fecharAutomaticamente = fechar;
    }

    // Método chamado automaticamente ao iniciar a interface
    @FXML
    public void initialize() {
        btnEscolherAnexo.setDisable(true); // Botão de escolher ficheiro desativado inicialmente
    }

    // Ativa ou desativa o botão de escolher ficheiro com base no CheckBox
    @FXML
    public void ativarEscolhaFicheiro() {
        boolean ativo = checkAdicionarFicheiro.isSelected();
        btnEscolherAnexo.setDisable(!ativo);
        if (!ativo) {
            ficheiroSelecionado = null;
            labelFicheiro.setText("Nenhum ficheiro selecionado");
        }
    }

    // Abre um FileChooser para selecionar o ficheiro
    @FXML
    public void escolherFicheiro() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Ficheiro da Garantia");
        ficheiroSelecionado = fileChooser.showOpenDialog(null);
        if (ficheiroSelecionado != null) {
            labelFicheiro.setText(ficheiroSelecionado.getName());
        }
    }

    // Método principal para guardar uma garantia
    @FXML
    public void guardarGarantia() {
        limparErros(); // Limpa mensagens de erro anteriores

        // Recolhe os dados inseridos pelo utilizador
        String nome = txtNomeProduto.getText().trim();
        LocalDate data = dateCompra.getValue();
        String duracao = txtDuracaoGarantia.getText().trim();
        boolean valido = true;

        // Validações dos campos obrigatórios
        if (nome.isEmpty()) {
            mostrarErro(txtNomeProduto, erroNomeProduto, "Insira o nome do produto");
            valido = false;
        }
        if (data == null) {
            mostrarErro(dateCompra, erroData, "Selecione uma data");
            valido = false;
        }
        if (duracao.isEmpty() || !duracao.matches("\\d+")) { // Regex que aceita apenas números
            mostrarErro(txtDuracaoGarantia, erroDuracao, "Duração inválida");
            valido = false;
        }
        if (checkAdicionarFicheiro.isSelected() && ficheiroSelecionado == null) {
            erroFicheiro.setText("Selecione o ficheiro.");
            erroFicheiro.setVisible(true);
            valido = false;
        }

        if (!valido) return; // Se houver erro, interrompe

        // Verifica se existe uma fatura com o mesmo nome mas em data diferente
        boolean faturaMesmoNome = false;
        boolean mesmaData = false;

        try (BufferedReader br = new BufferedReader(new FileReader("data/financas.txt"))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] partes = linha.split(";");
                if (partes.length >= 5) {
                    String nomeFatura = partes[2];
                    String dataFatura = partes[4];
                    if (nomeFatura.equalsIgnoreCase(nome)) {
                        faturaMesmoNome = true;
                        if (dataFatura.equals(data.toString())) {
                            mesmaData = true;
                        }
                    }
                }
            }
        } catch (IOException e) {
            // Se falhar leitura, ignora
        }

        // Se nome já existe, mas a data for diferente, perguntar ao utilizador
        if (faturaMesmoNome && !mesmaData) {
            Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
            alerta.setTitle("Fatura com Nome Igual");
            alerta.setHeaderText("Já existe uma fatura com este nome mas noutra data.");
            alerta.setContentText("Quer criar uma garantia sem fatura associada?");

            ButtonType sim = new ButtonType("Sim");
            ButtonType nao = new ButtonType("Não", ButtonBar.ButtonData.CANCEL_CLOSE);
            alerta.getButtonTypes().setAll(sim, nao);

            alerta.showAndWait().ifPresent(resposta -> {
                if (resposta == sim) {
                    guardarGarantiaDiretamente(nome, data, duracao);
                } else {
                    fecharJanela();
                }
            });
        } else {
            guardarGarantiaDiretamente(nome, data, duracao);
        }
    }

    // Guarda a garantia num ficheiro e copia o anexo se existir
    private void guardarGarantiaDiretamente(String nome, LocalDate data, String duracao) {
        try {
            // Cria as pastas necessárias se ainda não existirem
            Files.createDirectories(Paths.get("garantias"));
            Files.createDirectories(Paths.get("data"));

            // Define o caminho de destino para o anexo
            Path destino = ficheiroSelecionado != null
                    ? Paths.get("garantias").resolve(ficheiroSelecionado.getName())
                    : Paths.get("N/A");

            // Copia o ficheiro para a pasta "garantias"
            if (ficheiroSelecionado != null) {
                Files.copy(ficheiroSelecionado.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
            }

            // Gera um ID novo baseado no último ID no ficheiro
            int id = gerarId("data/garantias.txt");

            // Linha de dados a ser gravada no ficheiro
            String linha = id + ";" + nome + ";" + data + ";" + duracao + ";" + destino + ";" + LoginController.nifAtual;

            // Escreve no ficheiro de garantias
            BufferedWriter writer = new BufferedWriter(new FileWriter("data/garantias.txt", true));
            writer.write(linha);
            writer.newLine();
            writer.close();

            mostrarAlerta("Sucesso", "Garantia guardada com sucesso!");
            limparCampos(); // Limpa os campos da interface

            if (fecharAutomaticamente) {
                fecharJanela(); // Fecha janela, se for o caso
            }

        } catch (IOException e) {
            mostrarAlerta("Erro", "Erro ao guardar a garantia.");
        }
    }

    // Fecha a janela atual
    private void fecharJanela() {
        Stage stage = (Stage) txtNomeProduto.getScene().getWindow();
        stage.close();
    }

    // Exibe uma mensagem de erro num campo
    private void mostrarErro(Control campo, Label labelErro, String mensagem) {
        campo.setStyle("-fx-border-color: red;");
        labelErro.setText(mensagem);
        labelErro.setVisible(true);
        campo.requestFocus(); // Foca no campo com erro
    }

    // Limpa os estilos de erro dos campos
    private void limparErros() {
        txtNomeProduto.setStyle(null);
        txtDuracaoGarantia.setStyle(null);
        dateCompra.setStyle(null);

        erroNomeProduto.setVisible(false);
        erroData.setVisible(false);
        erroDuracao.setVisible(false);
        erroFicheiro.setVisible(false);
    }

    // Limpa todos os campos da interface
    private void limparCampos() {
        txtNomeProduto.clear();
        txtDuracaoGarantia.clear();
        dateCompra.setValue(null);
        ficheiroSelecionado = null;
        labelFicheiro.setText("Nenhum ficheiro selecionado");
        checkAdicionarFicheiro.setSelected(false);
        btnEscolherAnexo.setDisable(true);
    }

    // Gera um novo ID incremental com base no último ID existente
    private int gerarId(String caminho) throws IOException {
        File file = new File(caminho);
        if (!file.exists()) return 1;
        List<String> linhas = Files.readAllLines(file.toPath());
        if (linhas.isEmpty()) return 1;
        return Integer.parseInt(linhas.get(linhas.size() - 1).split(";")[0]) + 1;
    }

    // Volta ao menu principal
    @FXML
    private void voltarMenu(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("menu.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    // Preenche os campos nome e data da compra (usado ao vir de outro formulário)
    public void preencherCampos(String nomeProduto, LocalDate dataCompraValor) {
        txtNomeProduto.setText(nomeProduto);
        dateCompra.setValue(dataCompraValor);
    }
    
    // Mostra um alerta informativo
    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
