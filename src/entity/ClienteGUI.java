package entity;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

public class ClienteGUI extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private BufferDeClientes bufferDeClientes;
    private final int TAMANHO_BUFFER = 10000;
    private String arquivoSelecionado;
    private boolean arquivoCarregado = false; // Para verificar se o arquivo foi carregado
    private JTextField textField;

    public ClienteGUI() {
        setTitle("Gerenciamento de Clientes");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        bufferDeClientes = new BufferDeClientes();
        criarInterface();
    }


    private void carregarArquivo() {
        JFileChooser fileChooser = new JFileChooser();
        int retorno = fileChooser.showOpenDialog(this);
        if (retorno == JFileChooser.APPROVE_OPTION) {
            arquivoSelecionado = fileChooser.getSelectedFile().getAbsolutePath();
            bufferDeClientes.associaBuffer(new ArquivoCliente()); // Substitua por sua implementação
            bufferDeClientes.inicializaBuffer("leitura", arquivoSelecionado); // Passa o nome do arquivo aqui
            tableModel.setRowCount(0); // Limpa a tabela
            carregarMaisClientes(); // Carrega os primeiros clientes
            arquivoCarregado = true; // Marca que o arquivo foi carregado
        }
    }
    private void criarInterface() {
        JPanel panel = new JPanel(new BorderLayout());
        JButton btnCarregar = new JButton("Carregar Clientes");
        tableModel = new DefaultTableModel(new String[]{"#", "Nome", "Sobrenome", "Telefone", "Endereço", "Credit Score"}, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        textField = new JTextField();
        JButton btnText = new JButton("Filtrar Clientes");
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.add(textField, BorderLayout.CENTER);
        textPanel.add(btnText, BorderLayout.EAST);


        // Adiciona um listener ao JScrollPane para carregar mais clientes ao rolar
        scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if (!scrollPane.getVerticalScrollBar().getValueIsAdjusting()) {
                    // Verifica se estamos no final da tabela e se o arquivo foi carregado
                    if (arquivoCarregado &&
                        tableModel.getRowCount() >= TAMANHO_BUFFER &&
                        scrollPane.getVerticalScrollBar().getValue() + 
                        scrollPane.getVerticalScrollBar().getVisibleAmount() >= 
                        scrollPane.getVerticalScrollBar().getMaximum()) {
                        carregarMaisClientes();
                    }
                }
            }
        });

        btnCarregar.addActionListener(e -> carregarArquivo());
        btnText.addActionListener(e -> filtrarClientes());

        panel.add(btnCarregar, BorderLayout.NORTH);
        panel.add(textPanel, BorderLayout.SOUTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        add(panel);
    }

    private void carregarMaisClientes() {
        // Carrega apenas 10.000 registros de cada vez
        Cliente[] clientes = bufferDeClientes.proximosClientes(TAMANHO_BUFFER); // Chama o método com o tamanho do buffer
        if (clientes != null && clientes.length > 0) {
            for (Cliente cliente : clientes) {
                if (cliente != null) { // Verifica se o cliente não é nulo
                    tableModel.addRow(new Object[]{tableModel.getRowCount() + 1, cliente.getNome(), cliente.getSobrenome(), cliente.getTelefone(), cliente.getEndereco(), cliente.getCreditScore()});
                }
            }
        }
    }

    public void filtrarClientes() {
        if (arquivoSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Arquivo não selecionado.");
            return;
        }

        bufferDeClientes.inicializaBuffer("leitura", arquivoSelecionado); // Passa o nome do arquivo aqui
        tableModel.setRowCount(0); // Limpa a tabela

        String busca = textField.getText().trim().toLowerCase();
        if (busca.isEmpty()) {
            carregarMaisClientes(); // Carrega os primeiros clientes
            return;
        }
        Cliente[] clientes = bufferDeClientes.proximosClientes(TAMANHO_BUFFER); // Chama o método com o tamanho do buffer
        while (clientes != null && clientes.length > 0) {
            for (Cliente cliente : clientes) {
                if (cliente != null && busca.equals(cliente.getNome().toLowerCase())) {
                    tableModel.addRow(new Object[]{tableModel.getRowCount() + 1, cliente.getNome(), cliente.getSobrenome(), cliente.getTelefone(), cliente.getEndereco(), cliente.getCreditScore()});
                    return;
                }
            }
            clientes = bufferDeClientes.proximosClientes(TAMANHO_BUFFER);
        }
        JOptionPane.showMessageDialog(this, "Cliente não encontrado.");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClienteGUI gui = new ClienteGUI();
            gui.setVisible(true);
        });
    }
}
