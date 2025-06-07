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

public class FaturaController {

    // Elementos da interface gráfica (ligados ao FXML)
    @FXML private TextField txtNumeroFatura;
    @FXML private TextField txtNome;
    @FXML private TextField txtContribuinte;
    @FXML private ChoiceBox<String> comboCategoria;
    @FXML private DatePicker dateCompra;
    @FXML private CheckBox checkAdicionarFicheiro;
    @FXML private Button btnEscolherAnexo;
    @FXML private Label labelFicheiro;

    // Labels de erro para validação
    @FXML private Label erroNumeroFatura, erroNome, erroCategoria, erroData, erroFicheiro;

    // Guarda o ficheiro selecionado pelo utilizador
    private File ficheiroSelecionado;

    // Método executado ao iniciar a interface (FXML)
    @FXML
    public void initialize() {
        // Adiciona as opções à ChoiceBox de categoria
        comboCategoria.getItems().addAll("Alimentação", "Tecnologia", "Habitação", "Viaturas","Outros");

        // Preenche automaticamente o campo de contribuinte com o utilizador atual
        txtContribuinte.setText(LoginController.nifAtual);

        // Desativa o botão de anexar ficheiro por padrão
        btnEscolherAnexo.setDisable(true);
    }

    // Habilita ou desabilita o botão de escolher ficheiro, conforme o checkbox
    @FXML
    public void ativarEscolhaFicheiro() {
        boolean ativo = checkAdicionarFicheiro.isSelected();
        btnEscolherAnexo.setDisable(!ativo);
        if (!ativo) {
            ficheiroSelecionado = null;
            labelFicheiro.setText("Nenhum ficheiro selecionado");
        }
    }

    // Abre uma janela para o utilizador escolher um ficheiro
    @FXML
    public void escolherFicheiro() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Ficheiro da Fatura");
        ficheiroSelecionado = fileChooser.showOpenDialog(null); // Mostra diálogo de seleção
        if (ficheiroSelecionado != null) {
            labelFicheiro.setText(ficheiroSelecionado.getName()); // Mostra nome do ficheiro na interface
        }
    }

    // Valida os dados do formulário e guarda a fatura
    @FXML
    public void guardarFatura() {
        limparErros(); // Remove mensagens de erro anteriores

        boolean valido = true;
        String numero = txtNumeroFatura.getText().trim();
        String nome = txtNome.getText().trim();
        String categoria = comboCategoria.getValue();
        LocalDate data = dateCompra.getValue();

        // Validação dos campos obrigatórios
        if (numero.isEmpty()) {
            mostrarErro(txtNumeroFatura, erroNumeroFatura, "Insira o número da fatura");
            valido = false;
        }
        if (nome.isEmpty()) {
            mostrarErro(txtNome, erroNome, "Insira o nome da fatura");
            valido = false;
        }
        if (categoria == null) {
            mostrarErro(comboCategoria, erroCategoria, "Selecione uma categoria");
            valido = false;
        }
        if (data == null) {
            mostrarErro(dateCompra, erroData, "Selecione uma data");
            valido = false;
        }
        if (checkAdicionarFicheiro.isSelected() && ficheiroSelecionado == null) {
            erroFicheiro.setText("Selecione o ficheiro.");
            erroFicheiro.setVisible(true);
            valido = false;
        }

        if (!valido) return; // Interrompe se houver erros

        // Verifica se a fatura já existe (por número ou nome)
        boolean duplicado = false;
        try (BufferedReader br = new BufferedReader(new FileReader("data/financas.txt"))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] partes = linha.split(";");
                if (partes.length >= 3) {
                    String numeroExistente = partes[1];
                    String nomeExistente = partes[2];
                    if (numeroExistente.equalsIgnoreCase(numero) || nomeExistente.equalsIgnoreCase(nome)) {
                        duplicado = true;
                        break;
                    }
                }
            }
        } catch (IOException e) {
        }

        // Se for duplicado, pergunta ao utilizador se quer continuar
        if (duplicado) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Duplicado");
            confirm.setHeaderText("Já existe uma fatura com este número ou nome");
            confirm.setContentText("Deseja mesmo adicionar outra?");

            ButtonType sim = new ButtonType("Sim");
            ButtonType nao = new ButtonType("Não", ButtonBar.ButtonData.CANCEL_CLOSE);
            confirm.getButtonTypes().setAll(sim, nao);

            confirm.showAndWait().ifPresent(resposta -> {
                if (resposta == sim) {
                    guardarFaturaFinal(numero, nome, categoria, data);
                }
            });
        } else {
            guardarFaturaFinal(numero, nome, categoria, data);
        }
    }

    // Método que realmente grava os dados da fatura no ficheiro
    private void guardarFaturaFinal(String numero, String nome, String categoria, LocalDate data) {
        try {
            // Cria pastas necessárias se não existirem
            Files.createDirectories(Paths.get("faturas"));
            Files.createDirectories(Paths.get("data"));

            // Define o caminho de destino do anexo
            Path destino = ficheiroSelecionado != null
                    ? Paths.get("faturas").resolve(ficheiroSelecionado.getName())
                    : Paths.get("N/A");

            // Copia o ficheiro para a pasta de faturas
            if (ficheiroSelecionado != null) {
                Files.copy(ficheiroSelecionado.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
            }

            // Gera um novo ID
            int id = gerarId("data/financas.txt");

            // Cria a linha de dados para salvar
            String linha = id + ";" + numero + ";" + nome + ";" + categoria + ";" + data + ";" + destino + ";" + LoginController.nifAtual;

            // Escreve no ficheiro de finanças
            BufferedWriter writer = new BufferedWriter(new FileWriter("data/financas.txt", true));
            writer.write(linha);
            writer.newLine();
            writer.close();

            mostrarAlerta("Sucesso", "Fatura guardada com sucesso!");
            limparCampos(); // Limpa a interface

        } catch (IOException e) {
            mostrarAlerta("Erro", "Erro ao guardar a fatura.");
        }
    }

    // Gera um ID incremental com base no último ID presente no ficheiro
    private int gerarId(String caminho) throws IOException {
        File file = new File(caminho);
        if (!file.exists()) return 1;
        List<String> linhas = Files.readAllLines(file.toPath());
        if (linhas.isEmpty()) return 1;
        return Integer.parseInt(linhas.get(linhas.size() - 1).split(";")[0]) + 1;
    }

    // Exibe erro visual num campo
    private void mostrarErro(Control campo, Label labelErro, String mensagem) {
        campo.setStyle("-fx-border-color: red;");
        labelErro.setText(mensagem);
        labelErro.setVisible(true);
        campo.requestFocus();
    }

    // Limpa os estilos e mensagens de erro dos campos
    private void limparErros() {
        txtNumeroFatura.setStyle(null);
        txtNome.setStyle(null);
        comboCategoria.setStyle(null);
        dateCompra.setStyle(null);

        erroNumeroFatura.setVisible(false);
        erroNome.setVisible(false);
        erroCategoria.setVisible(false);
        erroData.setVisible(false);
        erroFicheiro.setVisible(false);
    }

    // Limpa os campos da interface para novo registo
    private void limparCampos() {
        txtNumeroFatura.clear();
        txtNome.clear();
        comboCategoria.setValue(null);
        dateCompra.setValue(null);
        ficheiroSelecionado = null;
        labelFicheiro.setText("Nenhum ficheiro selecionado");
        checkAdicionarFicheiro.setSelected(false);
        btnEscolherAnexo.setDisable(true);
    }

    // Ação para voltar ao menu principal
    @FXML
    private void voltarMenu(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("menu.fxml"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    // Mostra um alerta simples com título e mensagem
    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
    
    // Abre a janela de registo de garantia e preenche os dados automaticamente
    @FXML
    private void abrirGarantiaComDados(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("garantia.fxml"));
            Parent root = loader.load();

            GarantiaController controller = loader.getController();

            // Passa o nome e a data para o formulário de garantia
            controller.preencherCampos(txtNome.getText(), dateCompra.getValue());
            controller.setFecharAutomaticamente(true);  // Garante que a nova janela fecha sozinha após guardar

            Stage stage = new Stage();
            stage.setTitle("Registar Garantia");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            mostrarAlerta("Erro", "Não foi possível abrir o registo de garantia.");
        }
    }
}
