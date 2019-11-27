package main;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import domain.UserVO;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import views.LoginController;
import views.MainController;
import views.MasterController;
import views.RegisterController;
import views.TodoController;

public class MainApp extends Application{
	public static MainApp app;
	
	private StackPane mainPage = null;
	
	private Map<String, MasterController> controllerMap = new HashMap<>();

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("calendar");
		app = this; //싱글톤으로 만들어준다.
		try {
			FXMLLoader mainLoader = new FXMLLoader();
			mainLoader.setLocation(getClass().getResource("/views/MainLayout.fxml"));
			mainPage = mainLoader.load();
			
			MainController mc = mainLoader.getController();
			mc.setRoot(mainPage);
			controllerMap.put("main", mc);
			
			FXMLLoader loginLoader = new FXMLLoader();
			loginLoader.setLocation(getClass().getResource("/views/LoginLayout.fxml"));
			AnchorPane loginPage = loginLoader.load();
			
			LoginController lc = loginLoader.getController();
			lc.setRoot(loginPage);
			controllerMap.put("login", lc);
			
			//회원가입 페이지 로드
			FXMLLoader registerLoader = new FXMLLoader();
			registerLoader.setLocation(getClass().getResource("/views/RegisterLayout.fxml"));
			AnchorPane registerPage = registerLoader.load();
			
			RegisterController rc = registerLoader.getController();
			rc.setRoot(registerPage);
			controllerMap.put("register", rc);
			
			//Todo 팝업 페이지로등
			FXMLLoader todoLoader = new FXMLLoader();
			todoLoader.setLocation(getClass().getResource("/views/TodoLayout.fxml"));
			AnchorPane todoPage = todoLoader.load();
			TodoController tc = todoLoader.getController();
			tc.setRoot(todoPage);
			controllerMap.put("todo", tc);
			
			Scene scene = new Scene(mainPage);
			mainPage.getChildren().add(loginPage);
			
			scene.getStylesheets().add(getClass().getResource("app.css").toExternalForm());
			
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("앱 로딩중 오류 발생");
		}

	}
	
	public void loadTodoData(LocalDate date) {
		TodoController tc = (TodoController) controllerMap.get("todo");
		tc.setDate(date);
	}
	
	public void setLoginInfo(UserVO vo) {
		MainController mc = (MainController) controllerMap.get("main");
		mc.setLoginInfo(vo);
	}
	
	public void loadPage(String name) {
		MasterController c = controllerMap.get(name);
		c.init();
		Pane pane = c.getRoot();
		mainPage.getChildren().add(pane);
		
		pane.setTranslateX(-800);
		pane.setOpacity(0);
		
		Timeline timeline = new Timeline();
		KeyValue toRight = new KeyValue(pane.translateXProperty(), 0);
		KeyValue fadeIn = new KeyValue(pane.opacityProperty(), 1);
		
		KeyFrame frame = new KeyFrame(Duration.millis(500), toRight, fadeIn);
		
		timeline.getKeyFrames().add(frame);
		timeline.play();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	public void slideOut(Pane pane) {
		Timeline timeline = new Timeline();
		KeyValue toRight = new KeyValue(pane.translateXProperty(), 800);
		KeyValue fadeOut = new KeyValue(pane.opacityProperty(), 0);
		
		KeyFrame frame = new KeyFrame(Duration.millis(500), (e) -> {
			mainPage.getChildren().remove(pane);
		}, toRight, fadeOut);
		
		timeline.getKeyFrames().add(frame);
		timeline.play();
	}
	
	public void setFocus(LocalDate date) {
		MainController mc = (MainController)controllerMap.get("main");
		mc.setClickData(date);
	}
	
	public UserVO getLoginUser() {
		MainController mc = (MainController) controllerMap.get("main");
		return mc.getUser();
	}
}






