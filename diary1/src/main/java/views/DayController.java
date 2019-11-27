package views;

import java.time.LocalDate;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import main.MainApp;

public class DayController extends MasterController{
	@FXML
	private Label lblDay;
	@FXML
	private Label lblCount;
	
	private LocalDate date;
	
	private boolean isFocused = false;
	
	public void setDayLabel(LocalDate date) {
		this.date = date;
		lblDay.setText(String.valueOf(date.getDayOfMonth()));
	}
	
	public void setCountLabel(Integer cnt) {
		lblCount.setText(cnt.toString());
	}
	
	@Override
	public void init() {
		date = null;
	}
	
	public void setFocus() {
		
		if(isFocused) {
			MainApp.app.loadTodoData(date);
			MainApp.app.loadPage("todo");
			return;
		}
		
		MainApp.app.setFocus(date);
		isFocused = true;
		getRoot().getStyleClass().add("active");
	}
	
	public void outFocus() {
		isFocused = false;
		getRoot().getStyleClass().remove("active");
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}
	
}





