package controllers;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AdminDonationsCalendarController {

    @FXML
    private TextField searchField;
    @FXML
    private DatePicker dateFilter;
    @FXML
    private Label totalLabel;
    @FXML
    private StackPane calendarContainer;

    private final DonationController donationController = new DonationController();
    private Calendar donationCalendar;
    private CalendarView calendarView;
    private List<DonationController.CalendarEventDto> sourceEvents;

    @FXML
    public void initialize() {
        setupCalendarFx();

        loadData();

        searchField.textProperty().addListener((obs, oldValue, newValue) -> refreshCalendarEntries());
        dateFilter.valueProperty().addListener((obs, oldValue, newValue) -> refreshCalendarEntries());
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        dateFilter.setValue(null);
        refreshCalendarEntries();
    }

    private void loadData() {
        sourceEvents = donationController.getCalendarRecuperations();
        refreshCalendarEntries();
    }

    private void setupCalendarFx() {
        donationCalendar = new Calendar("Recuperations");
        donationCalendar.setStyle(Calendar.Style.STYLE2);

        CalendarSource source = new CalendarSource("Donations");
        source.getCalendars().add(donationCalendar);

        calendarView = new CalendarView();
        calendarView.getCalendarSources().add(source);
        calendarView.setShowAddCalendarButton(false);
        calendarView.setShowPrintButton(false);
        calendarView.setShowPageToolBarControls(true);
        calendarView.showMonthPage();
        // CalendarFX provides built-in event details popover on click.

        calendarContainer.getChildren().setAll(calendarView);
    }

    private void refreshCalendarEntries() {
        donationCalendar.clear();

        if (sourceEvents == null) {
            totalLabel.setText("0");
            return;
        }

        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        LocalDate selectedDate = dateFilter.getValue();
        int visibleCount = 0;

        for (DonationController.CalendarEventDto event : sourceEvents) {
            boolean matchesKeyword = keyword.isEmpty()
                    || safe(event.getTitre()).toLowerCase().contains(keyword)
                    || safe(event.getStatut()).toLowerCase().contains(keyword);

            LocalDateTime start;
            try {
                start = LocalDateTime.parse(event.getDateRecuperation());
            } catch (Exception e) {
                continue;
            }

            boolean matchesDate = selectedDate == null || start.toLocalDate().equals(selectedDate);
            if (!matchesKeyword || !matchesDate) {
                continue;
            }

            Entry<DonationController.CalendarEventDto> entry = new Entry<>(event.getTitre());
            entry.setInterval(start, start.plusHours(1));
            entry.setLocation("Donation #" + event.getId());
            entry.setUserObject(event);
            donationCalendar.addEntry(entry);
            visibleCount++;
        }

        totalLabel.setText(String.valueOf(visibleCount));
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
