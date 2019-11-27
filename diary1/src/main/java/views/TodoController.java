package views;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

import domain.TodoVO;
import domain.UserVO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import main.MainApp;
import util.JDBCUtil;
import util.Util;

public class TodoController extends MasterController {
	@FXML
	private TextField txtTitle;
	@FXML
	private TextArea txtContent;
	@FXML
	private ListView<TodoVO> todoList;
	@FXML
	private TextField todoTitle;
	@FXML
	private TextArea todoContent;

	private LocalDate date;

	private ObservableList<TodoVO> list; // 옵저베이블 리스트

	@Override
	public void init() {
		txtTitle.setText("");
		txtContent.setText("");
		todoTitle.setText("");
		todoContent.setText("");
	}

	@FXML
	public void initialize() {
		list = FXCollections.observableArrayList();
		todoList.setItems(list);
	}

	public void setDate(LocalDate date) {
		this.date = date;
		UserVO user = MainApp.app.getLoginUser();

		Connection con = JDBCUtil.getConnection();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		list.clear(); // 기존에 있던 일정들을 모두 비워준다.
		String sql = "SELECT * FROM diary_todos WHERE `date` = ? AND owner = ?";

		try {
			pstmt = con.prepareStatement(sql);
			pstmt.setDate(1, Date.valueOf(date));
			pstmt.setString(2, user.getId());
			rs = pstmt.executeQuery();

			while (rs.next()) {
				TodoVO todo = new TodoVO();
				todo.setId(rs.getInt("id"));
				todo.setTitle(rs.getString("title"));
				todo.setContent(rs.getString("content"));
				todo.setDate(rs.getDate("date").toLocalDate()); // sql date를 LocalDate로 변환시
				todo.setOwner(rs.getString("owner"));
				list.add(todo);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Util.showAlert("에러", "데이터베이스 접속중 오류 발생", AlertType.ERROR);
		} finally {
			JDBCUtil.close(rs);
			JDBCUtil.close(pstmt);
			JDBCUtil.close(con);
		}
	}

	// 일정 신규 등록 매서드
	public void register() {
		String title = txtTitle.getText().trim();
		String content = txtContent.getText().trim();
		UserVO user = MainApp.app.getLoginUser();

		if (title.isEmpty() || content.isEmpty()) {
			Util.showAlert("필수항목 비어있음", "제목이나 내용은 비어있을 수 없습니다", AlertType.INFORMATION);
			return;
		}

		Connection con = JDBCUtil.getConnection();
		PreparedStatement pstmt = null;
		String sql = "INSERT INTO diary_todos(`title`, `content`, `date`, `owner`) " + " VALUES(?, ?, ?, ?)";
		try {
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, title);
			pstmt.setString(2, content);
			pstmt.setDate(3, Date.valueOf(date)); // LocalDate객체를 SQL 삽입시 valueOf를 사용
			pstmt.setString(4, user.getId());

			int result = pstmt.executeUpdate();
			if (result != 1) {
				Util.showAlert("에러", "데이터베이스에 정상적으로 입력되지 않았습니다.", AlertType.INFORMATION);
				return;
			}
			Util.showAlert("성공", "데이터베이스에 정상적으로 입력되었습니다.", AlertType.INFORMATION);

			MainApp.app.slideOut(getRoot());

		} catch (Exception e) {
			e.printStackTrace();
			Util.showAlert("에러", "데이터베이스 연결중 오류 발생", AlertType.ERROR);
		} finally {
			JDBCUtil.close(pstmt);
			JDBCUtil.close(con);
		}
	}

	// 일정 수정 매서드
	public void update() {
		String title = todoTitle.getText();
		String content = todoContent.getText();
		UserVO user = MainApp.app.getLoginUser();

		if (title.isEmpty() || content.isEmpty() ) {
			Util.showAlert("필수항목 비어있음", "필수 입력항목이 비어있습니다.", AlertType.INFORMATION);
			return;
		}
		int idx = todoList.getSelectionModel().getSelectedIndex();
		if (idx < 0) {
			Util.showAlert("알림", "선택된 일정이 없습니다.", AlertType.INFORMATION);
			return;
		}
		
		TodoVO todo = list.get(idx);

		Connection con = JDBCUtil.getConnection();
		PreparedStatement pstmt = null;
		String sql = "UPDATE diary_todos SET title = ?, content = ? WHERE id =?";
		
		try {
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, title);
			pstmt.setString(2, content);
			pstmt.setInt(3, todo.getId());
			int result = pstmt.executeUpdate();
			
			if(result != 1) {
				Util.showAlert("에러", "데이터베이스 처리중 오류 발생", AlertType.ERROR);
				return;
			}
			Util.showAlert("성공", "수정이 성공적으로 이루어졌습니다", AlertType.INFORMATION);
			MainApp.app.slideOut(getRoot());
		} catch (Exception e) {
		e.printStackTrace();
		Util.showAlert("에러", "데이터베이스 처리중 오류 발생", AlertType.ERROR);
		} finally {
			JDBCUtil.close(pstmt);
			JDBCUtil.close(con);
		}
		
		
	}

	// 일정창 닫기
	public void close() {
		MainApp.app.slideOut(getRoot());
	}

	// 일정 제목 클릭시 내용이 출력되도록 하는 매서드
	public void loadTodo() {
		int idx = todoList.getSelectionModel().getSelectedIndex();
		if (idx < 0) {
			return;
		}

		TodoVO vo = list.get(idx);
		todoTitle.setText(vo.getTitle());
		todoContent.setText(vo.getContent());
	}
}
