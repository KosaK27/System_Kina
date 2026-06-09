package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import model.Session;
import util.SceneManager;

public class MainController {
    @FXML private TabPane tabPane;
    @FXML private Tab moviesTab;
    @FXML private Tab screeningsTab;
    @FXML private Tab reservationsTab;
    @FXML private Tab allReservationsTab;
    @FXML private Tab usersTab;
    @FXML private Tab sellTab;
    @FXML private Tab hallsTab;
    @FXML private Label userInfoLabel;

    private Tab adminPanelTab;

    private ScreeningController screeningController;
    private ReservationController reservationController;
    private EmployeeReservationController allReservationsController;
    private UsersController usersController;
    private SellController sellController;
    private AdminPanelController adminPanelController;

    private static MainController instance;

    public static MainController getInstance() { return instance; }

    @FXML
    public void initialize() {
        instance = this;

        userInfoLabel.setText(
                Session.getLoggedUser().getName() + " · " + Session.getLoggedUser().getRole());

        boolean canManage = Session.canManage();
        boolean isAdmin = Session.isAdmin();
        boolean isClient = !canManage;

        if (isClient) {
            tabPane.getTabs().removeAll(allReservationsTab, usersTab, sellTab, hallsTab);
        } else {
            tabPane.getTabs().remove(reservationsTab);
            if (!isAdmin) {
                tabPane.getTabs().remove(hallsTab);
            }
        }

        loadTab(moviesTab, "/view/MovieView.fxml");
        screeningController = loadTab(screeningsTab, "/view/ScreeningView.fxml");

        if (isClient) {
            reservationController = loadTab(reservationsTab, "/view/ReservationView.fxml");
        }
        if (canManage) {
            allReservationsController = loadTab(allReservationsTab, "/view/AllReservationsView.fxml");
            usersController = loadTab(usersTab, "/view/UsersView.fxml");
            sellController = loadTab(sellTab, "/view/SellView.fxml");
        }
        if (isAdmin) {
            loadTab(hallsTab, "/view/HallView.fxml");

            adminPanelTab = new Tab("Panel Administratora");
            adminPanelTab.setClosable(false);
            adminPanelController = loadTab(adminPanelTab, "/view/AdminPanelView.fxml");
            tabPane.getTabs().add(adminPanelTab);
        }

        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == null) return;
            if (newTab == allReservationsTab && allReservationsController != null) {
                allReservationsController.loadAll();
            } else if (newTab == reservationsTab && reservationController != null) {
                reservationController.loadReservations();
            } else if (newTab == screeningsTab && screeningController != null) {
                screeningController.loadAll();
            } else if (newTab == usersTab && usersController != null) {
                adminPanelController.loadData();
            } else if (newTab == sellTab && sellController != null) {
                sellController.loadData();
            } else if (newTab == adminPanelTab && adminPanelController != null) {
                adminPanelController.loadData();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private <T> T loadTab(Tab tab, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            tab.setContent(loader.load());
            return loader.getController();
        } catch (Exception e) {
            tab.setContent(new Label("Błąd: " + e.getMessage()));
            return null;
        }
    }

    public void switchToScreenings(String movieTitle) {
        tabPane.getSelectionModel().select(screeningsTab);
        if (screeningController != null) screeningController.filterByMovie(movieTitle);
    }

    public void switchToMyReservations() {
        if (reservationController != null) {
            tabPane.getSelectionModel().select(reservationsTab);
            reservationController.loadReservations();
        }
    }

    public void switchToAllReservationsFilteredByUser(String userName) {
        if (allReservationsController != null) {
            tabPane.getSelectionModel().select(allReservationsTab);
            allReservationsController.filterByUser(userName);
        }
    }

    @FXML
    public void logout() {
        instance = null;
        Session.logout();
        SceneManager.getInstance().switchTo("/view/LoginView.fxml", "System Kina");
    }
}