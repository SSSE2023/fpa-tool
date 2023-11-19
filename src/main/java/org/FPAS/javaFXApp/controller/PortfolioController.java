package org.FPAS.javaFXApp.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import lombok.NoArgsConstructor;
import org.FPAS.javaFXApp.SharedData;
import org.FPAS.springApp.Repository.*;
import org.FPAS.springApp.model.*;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.*;

import static org.FPAS.javaFXApp.FXMLHandler.changeScene;

@Controller
@NoArgsConstructor
public class PortfolioController implements Initializable {

    @FXML
    private Button transactionButton;
    @FXML
    private Button investmentsButton;
    @FXML
    private LineChart<String, Number> lineChart;
    @FXML
    private BarChart<String, Number> barChart;
    @FXML
    private Label usernameField;
    @FXML
    private Button sign_out;
    @FXML
    private Label riskRatingLabel;
    @FXML
    private Label portfolioLabel;
    public static PortfolioRepository portfolioRepository;
    public static BenchmarkRepository benchmarkRepository;
    public static ClientRepository clientRepository;
    public static InvestmentsRepository investmentsRepository;
    public static PerformanceMetricsRepository performanceMetricsRepository;

    @Autowired
    public PortfolioController( ClientRepository clientRepository, PortfolioRepository portfolioRepository, BenchmarkRepository benchmarkRepository, InvestmentsRepository investmentsRepository,PerformanceMetricsRepository performanceMetricsRepository) {
        PortfolioController.clientRepository = clientRepository;
        PortfolioController.benchmarkRepository = benchmarkRepository;
        PortfolioController.portfolioRepository = portfolioRepository;
        PortfolioController.investmentsRepository = investmentsRepository;
        PortfolioController.performanceMetricsRepository = performanceMetricsRepository;
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        transactionButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                changeScene(event, "TransactionRecordingView.fxml", null, null, TransactionRecordingController.class);
            }
        });

        sign_out.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                changeScene(event, "AuthenticationView2.fxml", null, null, authController.class);
            }
        });

        investmentsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                changeScene(event, "InvestmentEntryView.fxml", null, null, InvestmentEntryController.class);
            }
        });

        String riskRatingSentence = "The portfolio's risk rating has been assessed and currently stands at %d.";
        riskRatingLabel.setText(String.format(riskRatingSentence, calculateRiskRating()));
        String portfolioValueSentence = "Total Portfolio Value: $%.2f";
        portfolioLabel.setText(String.format(portfolioValueSentence, totalPortfolioValue()));

        loadLineChartData();
       loadBarChartData();
        Optional<Client> userOptional = clientRepository.findByUsernameAndPassword(SharedData.getUsername(), SharedData.getPassword());
        userOptional.ifPresent(client -> {
            usernameField.setText(client.getName());
        });
    }


    private void loadLineChartData() {
        Optional<Client> client = clientRepository.findByUsernameAndPassword(SharedData.getUsername(), SharedData.getPassword());
        List<PerformanceMetrics> performanceMetricsList = performanceMetricsRepository.findByClient(client.get());
        List<Benchmark> benchmark = benchmarkRepository.findAll();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        XYChart.Series<String, Number> series2 = new XYChart.Series<>();

        series.setName("Client Performance");
        series2.setName("Benchmark");

        for (PerformanceMetrics data : performanceMetricsList) {
            series.getData().add(new XYChart.Data<>("2020", data.getReturn_2020()));
            series.getData().add(new XYChart.Data<>("2021", data.getReturn_2021()));
            series.getData().add(new XYChart.Data<>("2022", data.getReturn_2022()));
            series.getData().add(new XYChart.Data<>("2023", data.getReturn_2023()));
        }

        for (Benchmark data : benchmark) {
            series2.getData().add(new XYChart.Data<>(Integer.toString(data.getAnnum()), data.getBenchmarkReturn()));
        }

        lineChart.setLegendVisible(true);
        lineChart.getData().addAll(series, series2);
    }


    private void loadBarChartData () {
        Optional<Client> client = clientRepository.findByUsernameAndPassword(SharedData.getUsername(),SharedData.getPassword());
        List<Portfolio> portfolioDataList = portfolioRepository.findByClient(client.get());

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        for (Portfolio data : portfolioDataList) {
            series.getData().add(new XYChart.Data<>(data.getSymbol(), data.getPurchasePrice() * data.getQuantity()));
        }
        barChart.getData().clear();
        barChart.getData().add(series);
        barChart.setLegendVisible(false);
    }

    private int calculateRiskRating(){
        Optional<Client> client = clientRepository.findByUsernameAndPassword(SharedData.getUsername(),SharedData.getPassword());
        List<Portfolio> portfolioDataList = portfolioRepository.findByClient(client.get());
        int riskRating = 0;
        for (Portfolio data : portfolioDataList) {
            String symbol = data.getSymbol();
            List<Investments> investmentsDataList = investmentsRepository.findBySymbol(symbol);
            for (Investments data2 : investmentsDataList) {
                riskRating+=data2.getRisk_rating();
            }
        }
        return riskRating;
    }
    private double totalPortfolioValue(){
        Optional<Client> client = clientRepository.findByUsernameAndPassword(SharedData.getUsername(),SharedData.getPassword());
        List<Portfolio> portfolioDataList = portfolioRepository.findByClient(client.get());
        double totalPortfolioValue  = 0;
        for (Portfolio data : portfolioDataList) {
            totalPortfolioValue = data.getPurchasePrice() * data.getQuantity();
        }
        return totalPortfolioValue;
    }

}

