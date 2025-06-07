package trabalho_final;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.event.ActionEvent;
import javafx.scene.Node;

import java.io.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class ConsultaController {

    // Elementos da interface FXML (tabela e colunas)
    @FXML private TableView<Fatura> tabelaFaturas;
    @FXML private TableColumn<Fatura, String> colNumero;
    @FXML private TableColumn<Fatura, String> colNome;
    @FXML private TableColumn<Fatura, String> colCategoria;
    @FXML private TableColumn<Fatura, String> colData;
    @FXML private TableColumn<Fatura, String> colFicheiro;
    @FXML private TableColumn<Fatura, String> colGarantia;

    // Filtros
    @FXML private ComboBox<String> comboCategoria;
    @FXML private DatePicker dateFiltro;

    // Lista principal com todos os dados carregados
    private ObservableList<Fatura> listaOriginal = FXCollections.observableArrayList();

    // Método chamado automaticamente ao carregar a interface
    @FXML
    public void initialize() {
        // Liga cada coluna ao respetivo atributo da classe Fatura
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colData.setCellValueFactory(new PropertyValueFactory<>("data"));
        colFicheiro.setCellValueFactory(new PropertyValueFactory<>("ficheiro"));
        colGarantia.setCellValueFactory(new PropertyValueFactory<>("garantia"));

        // Preenche o ComboBox com categorias possíveis
        comboCategoria.getItems().addAll("Alimentação", "Tecnologia", "Habitação", "Viaturas","Outros", "Garantia");

        // Carrega os dados do ficheiro
        carregarFaturas();
    }

    // Lê os ficheiros e carrega as faturas e garantias do utilizador
    private void carregarFaturas() {
        listaOriginal.clear();

        Map<String, List<String[]>> garantiasMap = new HashMap<>();
        Set<String> nomesComFatura = new HashSet<>();

        // 1. Ler garantias do ficheiro e guardar por nome do produto
        try (BufferedReader brG = new BufferedReader(new FileReader("data/garantias.txt"))) {
            String linha;
            while ((linha = brG.readLine()) != null) {
                String[] partes = linha.split(";");
                // Verifica se a linha tem o número de campos esperado
                if (partes.length >= 6) {
                    String nomeProduto = partes[1];
                    String nif = partes[5];
                    // Ignora garantias de outros utilizadores
                    if (!nif.equals(LoginController.nifAtual)) continue;
                    garantiasMap.computeIfAbsent(nomeProduto, k -> new ArrayList<>()).add(partes);
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao ler garantias.txt");
        }

        // 2. Ler faturas do ficheiro e adicionar à lista se forem do utilizador atual
        try (BufferedReader br = new BufferedReader(new FileReader("data/financas.txt"))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] partes = linha.split(";");
                if (partes.length >= 7) {
                    String numero = partes[1];
                    String nome = partes[2];
                    String categoria = partes[3];
                    String data = partes[4];
                    String ficheiro = partes[5];
                    String nif = partes[6];

                    // Ignora faturas de outros utilizadores
                    if (!nif.equals(LoginController.nifAtual)) continue;

                    nomesComFatura.add(nome + ";" + data); // guarda combinação única

                    // Inicializa garantia como não especificada
                    String garantia = "Sem Especificação";

                    // Se existir garantia com o mesmo nome, escolhe a mais próxima da data
                    if (garantiasMap.containsKey(nome)) {
                        LocalDate dataFatura = LocalDate.parse(data);
                        String[] maisProxima = null;
                        long menorDiferenca = Long.MAX_VALUE;

                        for (String[] g : garantiasMap.get(nome)) {
                            try {
                                LocalDate dataGarantia = LocalDate.parse(g[2]);
                                long diff = Math.abs(ChronoUnit.DAYS.between(dataFatura, dataGarantia));
                                if (diff < menorDiferenca) {
                                    menorDiferenca = diff;
                                    maisProxima = g;
                                }
                            } catch (Exception ignored) {}
                        }

                        if (maisProxima != null) {
                            garantia = calcularGarantia(maisProxima[2], maisProxima[3]);
                        }
                    }

                    // Adiciona a fatura à lista
                    listaOriginal.add(new Fatura(numero, nome, categoria, data, ficheiro, garantia));
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao ler financas.txt");
        }

        // 3. Garantias que não têm fatura correspondente (nome + data)
        for (Map.Entry<String, List<String[]>> entry : garantiasMap.entrySet()) {
            String nome = entry.getKey();
            for (String[] dados : entry.getValue()) {
                String data = dados[2];
                String chave = nome + ";" + data;
                if (!nomesComFatura.contains(chave)) {
                    String validade = calcularGarantia(data, dados[3]);
                    listaOriginal.add(new Fatura("N/A", nome, "Garantia", data, dados[4], validade));
                }
            }
        }

        tabelaFaturas.setItems(listaOriginal); // Atualiza a tabela com todos os dados
    }

    // Calcula o estado da garantia (válida, expirada, etc.)
    private String calcularGarantia(String dataCompraStr, String anos) {
        try {
            LocalDate dataCompra = LocalDate.parse(dataCompraStr);
            LocalDate expira = dataCompra.plusYears(Long.parseLong(anos));
            long dias = ChronoUnit.DAYS.between(LocalDate.now(), expira);

            if (dias < 0) return "Expirada";
            else if (dias <= 30) return "Expira em " + dias + " dias";
            else return "Válida até " + expira.toString();

        } catch (Exception e) {
            return "Desconhecida";
        }
    }

    // Aplica filtros selecionados pelo utilizador
    @FXML
    private void filtrarFaturas() {
        ObservableList<Fatura> filtradas = FXCollections.observableArrayList();

        String categoriaSelecionada = comboCategoria.getValue();
        LocalDate dataSelecionada = dateFiltro.getValue();

        for (Fatura f : listaOriginal) {
            boolean corresponde = true;

            if (categoriaSelecionada != null && !f.getCategoria().equalsIgnoreCase(categoriaSelecionada)) {
                corresponde = false;
            }

            if (dataSelecionada != null && !f.getData().equals(dataSelecionada.toString())) {
                corresponde = false;
            }

            if (corresponde) {
                filtradas.add(f);
            }
        }

        tabelaFaturas.setItems(filtradas); // Atualiza a tabela com os resultados filtrados
    }

    // Voltar ao menu principal
    @FXML
    private void voltarParaMenu(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("menu.fxml"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}
