package trabalho_final;

public class Fatura {
    private String nome;
    private String categoria;
    private String data;
    private String ficheiro;
    private String garantia;
    private String numero;


    // Construtor com garantia
    public Fatura(String numero, String nome, String categoria, String data, String ficheiro, String garantia) {
        this.numero = numero;
        this.nome = nome;
        this.categoria = categoria;
        this.data = data;
        this.ficheiro = ficheiro;
        this.garantia = garantia;
    }



    // Getters obrigatórios para funcionar com a TableView
    public String getNome() {
        return nome;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getData() {
        return data;
    }

    public String getFicheiro() {
        return ficheiro;
    }

    public String getGarantia() {
        return garantia;
    }
    
    public String getNumero() {
    return numero;
}
}
