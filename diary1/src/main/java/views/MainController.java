package views;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import domain.UserVO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import main.MainApp;
import util.JDBCUtil;
import util.Util;

public class MainController extends MasterController{
	@FXML
	private Button btnPrev;
	@FXML
	private Button btnNext;
	@FXML
	private Label lblDate;
	@FXML
	private Label lblDay;
	@FXML
	private Label loginInfo;
	@FXML
	private GridPane gridCalendar;
	
	private UserVO user;
	
	private List<DayController> dayList;
	
	private Map<String, String> dayOfWeek = new HashMap<>();
	
	private YearMonth currentYM; //현재 년도와 월을 저장하는 변수
	
	public UserVO getUser() {
		return user;
	}
	
	public void setLoginInfo(UserVO vo) {
		this.user = vo;
		loginInfo.setText(vo.getName() + "[" + vo.getId() + "]");
		loadMonthData(YearMonth.now());
		setToday(LocalDate.now());
	}
	
	public void logout() {
		user = null;
		MainApp.app.loadPage("login");
	}
	
	public void prevMonth() {
		loadMonthData(currentYM.minusMonths(1));
		LocalDate firstDay = LocalDate.of(currentYM.getYear(), 
											currentYM.getMonthValue(), 1);
		setToday(firstDay);
		
		for(DayController day : dayList) {
			if(day.getDate().equals(firstDay)) {
				day.setFocus();
				break;
			}
		}
	}
	
	public void nextMonth() {
		loadMonthData(currentYM.plusMonths(1));
		LocalDate firstDay = LocalDate.of(currentYM.getYear(), 
											currentYM.getMonthValue(), 1);
		setToday(firstDay);
		
		for(DayController day : dayList) {
			if(day.getDate().equals(firstDay)) {
				day.setFocus();
				break;
			}
		}
	}
	
	@Override
	public void init() {
		// TODO Auto-generated method stub
	}
	
	@FXML
	private void initialize() {
		dayList = new ArrayList<>();
		
		for(int i = 0; i < 5; i++) {
			for(int j = 0; j < 7; j++) {
				FXMLLoader loader = new FXMLLoader();
				loader.setLocation(getClass().getResource("/views/DayLayout.fxml"));
				try {
					AnchorPane ap = loader.load();
					gridCalendar.add(ap, j, i);
					DayController dc = loader.getController();
					dc.setRoot(ap);
					dayList.add(dc);
				} catch (Exception e) {
					e.printStackTrace();
					System.out.printf("j : %d, i : %d 번째 그리는 중 오류 발생\n", j, i);
					Util.showAlert("에러", "달력 초기화중 오류 발생", AlertType.ERROR);
				}
			}
		}
		
		String[] engDay = {"SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"};
		String[] korDay = {"일요일", "월요일", "화요일", "수요일", "목요일", "금요일", "토요일"};
		
		for(int i = 0; i < engDay.length; i++) {
			dayOfWeek.put(engDay[i], korDay[i]);
		}
		
	}
	
	public void setToday(LocalDate date) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy.MM.dd");
		lblDate.setText(date.format(dtf));
		lblDay.setText(dayOfWeek.get(date.getDayOfWeek().toString()));
	}
	
	public void loadMonthData(YearMonth ym) {
		//해당 년월의 1일 날짜를 만들어서 가져온다.
		LocalDate calendarDate = LocalDate.of(ym.getYear(), ym.getMonthValue(), 1);
		while(!calendarDate.getDayOfWeek().toString().equals("SUNDAY")) {
			//일요일이 아닐때까지 하루씩 빼나아간다.
			calendarDate = calendarDate.minusDays(1); //하루씩 감소
		}
		//여기까지 오면 해당 주간의 첫째날로 설정되게 된다. 여기서부터 캘린더를 그린다.
		
		LocalDate first = LocalDate.of(ym.getYear(), ym.getMonthValue(), 1);
		LocalDate last = first.plusMonths(1).withDayOfMonth(1).minusDays(1); 
		
		Connection con = JDBCUtil.getConnection();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "SELECT date, COUNT(*) AS cnt FROM diary_todos"
					+ " WHERE owner = ? AND date BETWEEN ? AND ? "
					+ " GROUP BY date";
		
		try {
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, user.getId());
			pstmt.setDate(2, Date.valueOf(first));
			pstmt.setDate(3, Date.valueOf(last));
			rs = pstmt.executeQuery();
			
			Map<LocalDate, Integer> cntMap = new HashMap<>();

			while(rs.next()) {
				LocalDate key = rs.getDate("date").toLocalDate();
				Integer value = rs.getInt("cnt");
				cntMap.put(key, value);
			}
			
			for(DayController day : dayList) {
				day.setDayLabel(calendarDate); //현재 날짜 셋팅하고
				if(cntMap.containsKey(calendarDate)) {
					day.setCountLabel(cntMap.get(calendarDate));
				}else {
					day.setCountLabel(0); //값이 없다면 0으로 셋팅
				}
				day.outFocus();
				calendarDate = calendarDate.plusDays(1); //하루 더해준다.
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			Util.showAlert("에러", "데이터베이스 연결 중 오류 발생", AlertType.ERROR);
		} finally {
			JDBCUtil.close(rs);
			JDBCUtil.close(pstmt);
			JDBCUtil.close(con);
		}
		
		
		
		currentYM = ym;
	}
	
	public void setClickData(LocalDate date) {
		setToday(date);
		//오늘날짜 셋팅후에 모든 날짜에서 active를 제거한다.
		for(DayController dc : dayList) {
			dc.outFocus();
		}
	}
}







